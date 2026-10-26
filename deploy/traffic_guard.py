#!/usr/bin/env python3
"""Traveller-only Nginx limits and a conservative server-egress cutoff."""
import argparse
import datetime as dt
import json
import os
from pathlib import Path
import re
import shutil
import subprocess
import sys
import tempfile

ROOT = Path('/var/lib/traveller-traffic')
SNIPPET = Path('/etc/nginx/snippets/traveller-traffic-guard.conf')
ZONES = Path('/etc/nginx/conf.d/traveller-traffic-zones.conf')
PROGRAM = Path('/usr/local/sbin/traveller-traffic-guard')
SERVICE = Path('/etc/systemd/system/traveller-traffic-guard.service')
TIMER = Path('/etc/systemd/system/traveller-traffic-guard.timer')
GIB = 1024 ** 3


def run(*args):
    return subprocess.run(args, check=True, capture_output=True, text=True).stdout


def atomic_json(path, value):
    temp = path.with_suffix('.tmp')
    temp.write_text(json.dumps(value, indent=2) + '\n')
    os.replace(temp, path)


def advance(state, tx, boot, month, budget):
    """Keep usage across reboots; reset the accounting period at UTC month-end."""
    if not state:
        state = {'month': month, 'bytes': 0, 'warned': False}
        delta = 0
    else:
        delta = tx - state['last_tx'] if state.get('boot') == boot and tx >= state['last_tx'] else tx
        if state['month'] != month:
            # Charge the boundary interval to the new month, conservatively.
            state = {'month': month, 'bytes': 0, 'warned': False}
    state = dict(state)
    state.update(bytes=state['bytes'] + delta, last_tx=tx, boot=boot)
    state['blocked'] = state['bytes'] >= budget
    warning = state['bytes'] >= budget * 0.8 and not state['warned']
    state['warned'] = state['warned'] or warning
    return state, warning


def server_span(text):
    # Mask quoted values and comments while preserving positions for safe braces.
    mask = re.sub(r'"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|#[^\n]*',
                  lambda m: ' ' * len(m.group()), text)
    found = []
    for match in re.finditer(r'\bserver\s*\{', mask):
        opening = mask.index('{', match.start())
        depth, end = 1, opening + 1
        while end < len(mask) and depth:
            depth += (mask[end] == '{') - (mask[end] == '}')
            end += 1
        if depth:
            raise ValueError('Unbalanced Nginx server block')
        body = text[opening + 1:end - 1]
        if (re.search(r'\bserver_name\s+[^;]*\btraveller\.doxylabs\.net(?=[\s;])', body)
                and re.search(r'\blisten\s+[^;]*\b443\b', body)
                and re.search(r'\bproxy_pass\s+http://127\.0\.0\.1:8082', body)):
            found.append((opening, end))
    return found


def proxy_locations(text):
    mask = re.sub(r'"(?:\\.|[^"\\])*"|\'(?:\\.|[^\'\\])*\'|#[^\n]*',
                  lambda m: ' ' * len(m.group()), text)
    found = []
    for match in re.finditer(r'\blocation\s+[^{};]*\{', mask):
        opening = mask.index('{', match.start())
        depth, end = 1, opening + 1
        while end < len(mask) and depth:
            depth += (mask[end] == '{') - (mask[end] == '}')
            end += 1
        if depth:
            raise ValueError('Unbalanced Nginx location block')
        if re.search(r'\bproxy_pass\s+http://127\.0\.0\.1:8082(?=[/;\s])', text[opening:end]):
            found.append((opening, end))
    return found


def install(budget_gib):
    if os.geteuid() != 0:
        raise SystemExit('Run as root in the DoxyLabs console.')
    if not 1 <= budget_gib <= 10000:
        raise SystemExit('Monthly GiB must be between 1 and 10000.')
    subprocess.run(['nginx', '-t'], check=True)
    dump = run('nginx', '-T')
    files = set(re.findall(r'^# configuration file (.+):$', dump, re.M))
    candidates = []
    sources = {}
    for name in files:
        path = Path(name).resolve()
        if path == SNIPPET or path == ZONES or not path.is_file():
            continue
        text = path.read_text()
        sources[path] = text
        for span in server_span(text):
            candidates.append((path, text, span))
    if len(candidates) != 1:
        raise SystemExit('Expected exactly one Traveller HTTPS proxy; no changes made. Found ' + str(len(candidates)))
    site, original, (opening, end) = candidates[0]
    marker = 'include ' + str(SNIPPET) + ';'
    changes = {}
    for path, text in sources.items():
        spans = []
        if path == site:
            spans.append((opening, end))
        for start, stop in proxy_locations(text):
            # The Traveller subdomain inherits the server-wide include already.
            if path == site and opening < start < end:
                continue
            spans.append((start, stop))
        changed = text
        for start, stop in sorted(spans, reverse=True):
            if marker not in text[start:stop]:
                changed = changed[:start + 1] + '\n    ' + marker + changed[start + 1:]
        if changed != text:
            changes[path] = changed
    route = json.loads(run('ip', '-j', 'route', 'get', '1.1.1.1'))
    interface = route[0]['dev']
    if not re.fullmatch(r'[A-Za-z0-9_.:-]+', interface):
        raise SystemExit('Unexpected network interface name')
    counter = Path('/sys/class/net') / interface / 'statistics/tx_bytes'
    if not counter.is_file():
        raise SystemExit('Cannot read outbound traffic counter')
    ROOT.mkdir(mode=0o755, parents=True, exist_ok=True)
    if not (ROOT / 'config.json').exists():
        atomic_json(ROOT / 'config.json', {'interface': interface, 'monthly_gib': budget_gib})
    backup = Path(tempfile.mkdtemp(prefix='traveller-traffic-', dir='/var/backups'))
    paths = list(dict.fromkeys(list(changes) + [SNIPPET, ZONES, PROGRAM, SERVICE, TIMER]))
    previous = {}
    for index, path in enumerate(paths):
        previous[path] = path.read_bytes() if path.exists() else None
        if previous[path] is not None:
            (backup / str(index)).write_bytes(previous[path])
    atomic_json(backup / 'manifest.json', {str(p): str(i) if previous[p] is not None else None for i, p in enumerate(paths)})
    was_active = subprocess.run(['systemctl', 'is-active', '--quiet', 'traveller-traffic-guard.timer']).returncode == 0
    try:
        SNIPPET.parent.mkdir(parents=True, exist_ok=True)
        ZONES.write_text('''limit_req_zone $binary_remote_addr zone=traveller_ip:2m rate=2r/s;
limit_req_zone $server_name zone=traveller_total:1m rate=30r/s;
limit_conn_zone $binary_remote_addr zone=traveller_conn_ip:2m;
limit_conn_zone $server_name zone=traveller_conn_total:1m;
''')
        SNIPPET.write_text('''# Applied only to Traveller servers and locations proxying its application.
limit_req zone=traveller_ip burst=20 nodelay;
limit_req zone=traveller_total burst=60 nodelay;
limit_req_status 429;
limit_req_log_level notice;
limit_conn traveller_conn_ip 8;
limit_conn traveller_conn_total 40;
limit_conn_status 429;
client_max_body_size 32k;
client_body_timeout 15s;
keepalive_timeout 15s;
send_timeout 20s;
add_header X-Traveller-Traffic-Guard active always;
if (-f /var/lib/traveller-traffic/blocked) {
    return 503 "Traveller is temporarily paused because its traffic budget was reached. Please try again next month.\\n";
}
''')
        for path, changed in changes.items():
            path.write_text(changed)
        PROGRAM.write_bytes(Path(__file__).read_bytes())
        PROGRAM.chmod(0o755)
        SERVICE.write_text('''[Unit]
Description=Traveller outbound traffic budget check
After=network-online.target
[Service]
Type=oneshot
ExecStart=/usr/bin/python3 /usr/local/sbin/traveller-traffic-guard tick
''')
        TIMER.write_text('''[Unit]
Description=Check Traveller traffic budget every 30 seconds
[Timer]
OnBootSec=30s
OnUnitActiveSec=30s
AccuracySec=5s
[Install]
WantedBy=timers.target
''')
        subprocess.run(['nginx', '-t'], check=True)
        subprocess.run(['systemctl', 'daemon-reload'], check=True)
        tick()
        subprocess.run(['systemctl', 'reload', 'nginx'], check=True)
        subprocess.run(['systemctl', 'enable', '--now', 'traveller-traffic-guard.timer'], check=True)
    except Exception:
        if not was_active:
            subprocess.run(['systemctl', 'disable', '--now', 'traveller-traffic-guard.timer'])
        for path, value in previous.items():
            if value is None:
                path.unlink(missing_ok=True)
            else:
                path.write_bytes(value)
        subprocess.run(['systemctl', 'daemon-reload'])
        subprocess.run(['nginx', '-t'], check=True)
        subprocess.run(['systemctl', 'reload', 'nginx'], check=True)
        raise
    print('Traffic protection installed. Nginx backup:', backup)
    status()


def tick():
    import fcntl
    ROOT.mkdir(parents=True, exist_ok=True)
    with (ROOT / 'lock').open('a') as lock:
        fcntl.flock(lock, fcntl.LOCK_EX)
        config = json.loads((ROOT / 'config.json').read_text())
        tx = int((Path('/sys/class/net') / config['interface'] / 'statistics/tx_bytes').read_text())
        boot = Path('/proc/sys/kernel/random/boot_id').read_text().strip()
        month = dt.datetime.now(dt.timezone.utc).strftime('%Y-%m')
        state_path = ROOT / 'usage.json'
        old = json.loads(state_path.read_text()) if state_path.exists() else None
        state, warning = advance(old, tx, boot, month, config['monthly_gib'] * GIB)
        # A failed state write leaves the previous ledger intact; do not silently reset it.
        atomic_json(state_path, state)
        if state['blocked']:
            (ROOT / 'blocked').touch(mode=0o644, exist_ok=True)
        else:
            (ROOT / 'blocked').unlink(missing_ok=True)
        if warning:
            print('WARNING: 80% of Traveller traffic budget used.', flush=True)
        if state['blocked'] and not (old or {}).get('blocked'):
            print('WARNING: Traveller paused after reaching its traffic budget.', flush=True)


def status():
    config = json.loads((ROOT / 'config.json').read_text())
    state = json.loads((ROOT / 'usage.json').read_text())
    print(json.dumps({'month_utc': state['month'], 'outbound_gib_since_tracking': round(state['bytes'] / GIB, 4),
                      'monthly_cutoff_gib': config['monthly_gib'], 'paused': state['blocked'],
                      'scope': 'whole-server outbound counter; only Traveller is paused'}, indent=2))


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('action', choices=['install', 'tick', 'status'])
    parser.add_argument('--monthly-gib', type=int, default=100)
    args = parser.parse_args()
    if args.action == 'install':
        install(args.monthly_gib)
    elif args.action == 'tick':
        tick()
    else:
        status()

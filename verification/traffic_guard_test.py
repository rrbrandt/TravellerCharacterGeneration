import importlib.util
from pathlib import Path
import unittest
from unittest import mock
import contextlib
import subprocess
import tempfile

spec = importlib.util.spec_from_file_location('guard', Path(__file__).parents[1] / 'deploy/traffic_guard.py')
guard = importlib.util.module_from_spec(spec)
spec.loader.exec_module(guard)


class TrafficGuardTests(unittest.TestCase):
    def test_initial_counter_is_baseline(self):
        state, warn = guard.advance(None, 90000, 'boot1', '2026-10', 1000)
        self.assertEqual(state['bytes'], 0)
        self.assertFalse(warn)

    def test_warning_then_cutoff(self):
        state, _ = guard.advance(None, 1000, 'boot1', '2026-10', 1000)
        state, warn = guard.advance(state, 1800, 'boot1', '2026-10', 1000)
        self.assertTrue(warn)
        self.assertFalse(state['blocked'])
        state, warn = guard.advance(state, 2000, 'boot1', '2026-10', 1000)
        self.assertFalse(warn)
        self.assertTrue(state['blocked'])

    def test_reboot_preserves_usage(self):
        state, _ = guard.advance(None, 1000, 'boot1', '2026-10', 1000)
        state, _ = guard.advance(state, 1600, 'boot1', '2026-10', 1000)
        state, _ = guard.advance(state, 450, 'boot2', '2026-10', 1000)
        self.assertEqual(state['bytes'], 1050)
        self.assertTrue(state['blocked'])

    def test_month_resets_and_reopens(self):
        state, _ = guard.advance(None, 1000, 'boot1', '2026-10', 1000)
        state, _ = guard.advance(state, 2100, 'boot1', '2026-10', 1000)
        state, _ = guard.advance(state, 2200, 'boot1', '2026-11', 1000)
        self.assertEqual(state['bytes'], 100)
        self.assertFalse(state['blocked'])
        self.assertFalse(state['warned'])

    def test_counter_reset_does_not_subtract_usage(self):
        state, _ = guard.advance(None, 1000, 'boot1', '2026-10', 1000)
        state, _ = guard.advance(state, 1500, 'boot1', '2026-10', 1000)
        state, _ = guard.advance(state, 100, 'boot1', '2026-10', 1000)
        self.assertEqual(state['bytes'], 600)

    def test_only_traveller_https_proxy_selected(self):
        text = '''server { server_name doxylabs.net; listen 443 ssl; location / { return 200; } }
server { server_name traveller.doxylabs.net; listen 80; return 301 https://$host$request_uri; }
server { server_name traveller.doxylabs.net; listen 443 ssl;
location / { proxy_pass http://127.0.0.1:8082; } }
'''
        spans = guard.server_span(text)
        self.assertEqual(len(spans), 1)
        self.assertIn('proxy_pass http://127.0.0.1:8082', text[spans[0][0]:spans[0][1]])

    def test_quoted_and_commented_braces(self):
        text = '''server { server_name traveller.doxylabs.net; listen 443 ssl;
# ignore } { here
location / { proxy_pass http://127.0.0.1:8082; add_header Example "}"; } }
'''
        self.assertEqual(len(guard.server_span(text)), 1)

    def test_ambiguous_config_is_detectable(self):
        text = 'server { server_name traveller.doxylabs.net; listen 443 ssl; proxy_pass http://127.0.0.1:8082; }'
        self.assertEqual(len(guard.server_span(text + text)), 2)

    def test_alternate_hostname_proxy_is_protected(self):
        text = '''server { server_name doxylabs.net; listen 443 ssl;
location /traveller/ { proxy_pass http://127.0.0.1:8082; }
location /hex-frontier/ { proxy_pass http://127.0.0.1:8083; } }'''
        spans = guard.proxy_locations(text)
        self.assertEqual(len(spans), 1)
        self.assertNotIn('8083', text[spans[0][0]:spans[0][1]])

    def check_install(self, fail_validation):
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            site = root / 'site.conf'
            original = 'server { server_name traveller.doxylabs.net; listen 443 ssl; location / { proxy_pass http://127.0.0.1:8082; } }'
            site.write_text(original)
            counter = root / 'net/eth0/statistics/tx_bytes'
            counter.parent.mkdir(parents=True)
            counter.write_text('1000')
            backup_root = root / 'backups'
            backup_root.mkdir()
            real_mkdtemp = tempfile.mkdtemp
            validations = []

            def command(args, **kwargs):
                if args[0] == 'nginx' and '-t' in args:
                    validations.append(args)
                    if fail_validation and len(validations) == 2:
                        raise subprocess.CalledProcessError(1, args)
                return subprocess.CompletedProcess(args, 3 if 'is-active' in args else 0)

            def inspect(*args):
                if args[0] == 'nginx':
                    return '# configuration file ' + str(site) + ':\n' + original
                return '[{"dev":"eth0"}]'

            def path(value):
                return root / 'net' if str(value) == '/sys/class/net' else Path(value)

            with contextlib.ExitStack() as stack:
                for name in ('ROOT', 'SNIPPET', 'ZONES', 'PROGRAM', 'SERVICE', 'TIMER'):
                    target = root / name.lower()
                    if name in ('SNIPPET', 'ZONES'):
                        target = root / 'nginx' / name.lower()
                        target.parent.mkdir(exist_ok=True)
                    stack.enter_context(mock.patch.object(guard, name, target))
                stack.enter_context(mock.patch.object(guard, 'Path', path))
                stack.enter_context(mock.patch.object(guard.os, 'geteuid', return_value=0, create=True))
                stack.enter_context(mock.patch.object(guard, 'run', side_effect=inspect))
                stack.enter_context(mock.patch.object(guard.subprocess, 'run', side_effect=command))
                stack.enter_context(mock.patch.object(guard.tempfile, 'mkdtemp', side_effect=lambda **kw: real_mkdtemp(prefix=kw['prefix'], dir=backup_root)))
                stack.enter_context(mock.patch.object(guard, 'tick'))
                stack.enter_context(mock.patch.object(guard, 'status'))
                if fail_validation:
                    with self.assertRaises(subprocess.CalledProcessError):
                        guard.install(100)
                    self.assertEqual(site.read_text(), original)
                    self.assertFalse(guard.SNIPPET.exists())
                    self.assertFalse(guard.PROGRAM.exists())
                else:
                    guard.install(100)
                    self.assertIn('include ' + str(guard.SNIPPET), site.read_text())
                    self.assertTrue(guard.PROGRAM.exists())
                    self.assertTrue(guard.TIMER.exists())
                self.assertTrue(list(backup_root.glob('*/manifest.json')))

    def test_install_transaction(self):
        self.check_install(False)

    def test_invalid_nginx_rolls_back(self):
        self.check_install(True)


if __name__ == '__main__':
    unittest.main()

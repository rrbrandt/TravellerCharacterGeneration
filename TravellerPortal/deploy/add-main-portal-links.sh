#!/bin/bash
set -euo pipefail
test ! -e /etc/nginx/snippets/traveller-main-portal-link.conf
backup=/var/backups/traveller-portal-links-$(date -u +%Y%m%dT%H%M%SZ)
mkdir -p "$backup"
cp -a /etc/nginx/sites-available/traveller "$backup/nginx-traveller"
install -m 644 /var/tmp/main-portal-link.conf /etc/nginx/snippets/traveller-main-portal-link.conf
python3 - <<'PY'
from pathlib import Path
p=Path('/etc/nginx/sites-available/traveller')
s=p.read_text()
assert 'traveller-main-portal-link.conf' not in s
for port in (8082,8084,8086):
    marker=f'        proxy_pass http://127.0.0.1:{port};'
    assert s.count(marker)==1
    s=s.replace(marker,marker+'\n        include /etc/nginx/snippets/traveller-main-portal-link.conf;')
p.write_text(s)
PY
if nginx -t && systemctl reload nginx; then
 echo "Portal return navigation enabled; backup=$backup"
else
 cp "$backup/nginx-traveller" /etc/nginx/sites-available/traveller
 nginx -t && systemctl reload nginx
 exit 1
fi

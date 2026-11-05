#!/bin/bash
set -euo pipefail
backup=/var/backups/traveller-accounts-routing-$(date -u +%Y%m%dT%H%M%SZ)
mkdir -p "$backup"
cp -a /etc/nginx/sites-available/traveller "$backup/nginx-traveller"
python3 - <<'PY'
from pathlib import Path
p=Path('/etc/nginx/sites-available/traveller')
s=p.read_text(); marker='    location / {'
assert s.count(marker)==1 and 'location /account/' not in s
p.write_text(s.replace(marker,'''    location = /account { return 301 /account/login; }
    location /account/ {
        client_max_body_size 8k;
        proxy_pass http://127.0.0.1:8085;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

'''+marker))
PY
if nginx -t; then systemctl reload nginx; else cp "$backup/nginx-traveller" /etc/nginx/sites-available/traveller; exit 1; fi
install -m 644 /var/tmp/traveller-account-portal.html /var/www/traveller-portal/index.html
echo "ACCOUNTS DEPLOYED; mail remains disabled; backup=$backup"

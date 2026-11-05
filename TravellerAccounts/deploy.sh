#!/bin/bash
set -euo pipefail
test ! -e /opt/traveller-accounts
! ss -ltn | grep -q ':8085 '
test -s /etc/traveller/database.json
backup=/var/backups/traveller-accounts-$(date -u +%Y%m%dT%H%M%SZ)
mkdir -p "$backup"
cp -a /etc/nginx/sites-available/traveller "$backup/nginx-traveller"
cp -a /var/www/traveller-portal/index.html "$backup/index.html"
useradd --system --user-group --home-dir /opt/traveller-accounts --shell /usr/sbin/nologin traveller-accounts
install -d -m 755 /opt/traveller-accounts
install -m 644 /var/tmp/TravellerAccounts.war /opt/traveller-accounts/accounts.war
umask 077
cat > /etc/traveller/mail.json <<'JSON'
{"enabled":false,"username":"richraybrandt@gmail.com","notificationTo":"richraybrandt@gmail.com","password":""}
JSON
cat > /etc/systemd/system/traveller-accounts.service <<'UNIT'
[Unit]
Description=Traveller login and registration
After=network.target mysql.service
[Service]
User=traveller-accounts
Group=traveller-accounts
WorkingDirectory=/opt/traveller-accounts
ExecStart=/usr/bin/java -Xms32m -Xmx160m -jar /opt/traveller-accounts/accounts.war
LoadCredential=database.json:/etc/traveller/database.json
LoadCredential=mail.json:/etc/traveller/mail.json
Restart=on-failure
RestartSec=5
NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=strict
ProtectHome=true
[Install]
WantedBy=multi-user.target
UNIT
chmod 644 /etc/systemd/system/traveller-accounts.service
systemctl daemon-reload
systemctl enable --now traveller-accounts
ready=0
for i in $(seq 1 45); do if curl -fsS http://127.0.0.1:8085/account/health; then ready=1; break; fi; sleep 2; done
test "$ready" = 1
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

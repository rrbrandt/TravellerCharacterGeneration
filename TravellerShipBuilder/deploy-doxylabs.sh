#!/bin/bash
set -euo pipefail
cd /var/tmp
echo '344482FA3B08BFCDCDB7E73BA583F5F14189AEF1AA85FD6ED01F9A742023F64C  TravellerShipBuilder.war' | sha256sum -c
echo '9DDDF013F0A3A6E871CA3325A0FF80F20C835305F0A5FADEF4ED3FB77A5FC9AF  traveller-portal-index.html' | sha256sum -c
systemctl is-active --quiet traveller-generator
test ! -e /opt/traveller-shipyard
test ! -e /var/www/traveller-portal
! ss -ltn | grep -q ':8084 '
python3 - <<'PY'
from pathlib import Path
p=Path('/etc/nginx/sites-available/traveller')
s=p.read_text()
assert s.count('return 302 /traveller/;')==1
assert 'location /shipyard/' not in s
PY
backup=/var/backups/traveller-portal-$(date -u +%Y%m%dT%H%M%SZ)
mkdir -p "$backup"
cp -a /etc/nginx/sites-available/traveller "$backup/nginx-traveller"
changed=0
rollback() { if [ "$changed" = 1 ]; then cp "$backup/nginx-traveller" /etc/nginx/sites-available/traveller; nginx -t && systemctl reload nginx; fi; systemctl stop traveller-shipyard || true; echo "Deployment failed; backup $backup"; }
trap rollback ERR
useradd --system --user-group --home-dir /opt/traveller-shipyard --shell /usr/sbin/nologin traveller-shipyard
install -d -m 755 /opt/traveller-shipyard /var/www/traveller-portal
install -m 644 TravellerShipBuilder.war /opt/traveller-shipyard/ship-builder.war
install -m 644 traveller-portal-index.html /var/www/traveller-portal/index.html
cat > /etc/systemd/system/traveller-shipyard.service <<'UNIT'
[Unit]
Description=Classic Traveller Book 2 Shipyard
After=network.target
[Service]
User=traveller-shipyard
Group=traveller-shipyard
WorkingDirectory=/opt/traveller-shipyard
ExecStart=/usr/bin/java -Xms32m -Xmx192m -jar /opt/traveller-shipyard/ship-builder.war --server.address=127.0.0.1 --server.port=8084 --server.servlet.context-path=/shipyard --shipbuilder.storage-dir=/var/lib/traveller-ships
Restart=on-failure
RestartSec=5
StateDirectory=traveller-ships
StateDirectoryMode=0750
NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=strict
ProtectHome=true
[Install]
WantedBy=multi-user.target
UNIT
systemctl daemon-reload
systemctl enable --now traveller-shipyard
ready=0
for attempt in $(seq 1 60); do if curl -fsS http://127.0.0.1:8084/shipyard/health; then ready=1; break; fi; sleep 2; done
test "$ready" = 1
changed=1
python3 - <<'PY'
from pathlib import Path
p=Path('/etc/nginx/sites-available/traveller')
s=p.read_text().replace('return 302 /traveller/;', 'root /var/www/traveller-portal;\n        try_files /index.html =404;')
marker='    location / {'
addition='''    location = /shipyard { return 301 /shipyard/; }
    location /shipyard/ {
        proxy_pass http://127.0.0.1:8084;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

'''
assert s.count(marker)==1
p.write_text(s.replace(marker,addition+marker))
PY
nginx -t
systemctl reload nginx
systemctl is-active traveller-generator traveller-shipyard nginx
echo "DEPLOYED backup=$backup"

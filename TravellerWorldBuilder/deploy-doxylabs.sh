#!/bin/bash
set -euo pipefail
echo '924CB7D3AD569DCEF887BA6FAD26ED7A937B90D0DAB2E26B327963CB22AEB4A0  /var/tmp/TravellerWorldBuilder-release.war' | sha256sum -c
test ! -e /opt/traveller-worldbuilder
test ! -e /var/lib/traveller-atlases
! ss -ltn | grep -q ':8086 '
systemctl is-active --quiet traveller-generator traveller-shipyard traveller-accounts nginx
backup=/var/backups/traveller-worldbuilder-$(date -u +%Y%m%dT%H%M%SZ)
mkdir -p "$backup"
cp -a /etc/nginx/sites-available/traveller "$backup/nginx-traveller"
cp -a /var/www/traveller-portal/{index.html,robots.txt,sitemap.xml} "$backup/"
changed=0
rollback(){
 if [ "$changed" = 1 ]; then
  cp "$backup/nginx-traveller" /etc/nginx/sites-available/traveller
  cp "$backup/"{index.html,robots.txt,sitemap.xml} /var/www/traveller-portal/
  nginx -t && systemctl reload nginx
 fi
 systemctl stop traveller-worldbuilder || true
 echo "World Builder deployment failed; prior public routing retained. Backup: $backup"
}
trap rollback ERR
useradd --system --user-group --home-dir /opt/traveller-worldbuilder --shell /usr/sbin/nologin traveller-worldbuilder
install -d -m 755 /opt/traveller-worldbuilder
install -m 644 /var/tmp/TravellerWorldBuilder-release.war /opt/traveller-worldbuilder/world-builder.war
cat > /etc/systemd/system/traveller-worldbuilder.service <<'UNIT'
[Unit]
Description=Classic Traveller Book 3 World Builder
After=network.target
[Service]
User=traveller-worldbuilder
Group=traveller-worldbuilder
WorkingDirectory=/opt/traveller-worldbuilder
ExecStart=/usr/bin/java -Xms32m -Xmx160m -jar /opt/traveller-worldbuilder/world-builder.war --server.address=127.0.0.1 --server.port=8086 --server.servlet.context-path=/worlds --server.servlet.session.cookie.secure=true --worldbuilder.storage-dir=/var/lib/traveller-atlases --worldbuilder.public-hosting=true --spring.thymeleaf.cache=true
StateDirectory=traveller-atlases
StateDirectoryMode=0750
Restart=on-failure
RestartSec=5
NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=strict
ProtectHome=true
[Install]
WantedBy=multi-user.target
UNIT
systemctl daemon-reload
systemctl enable --now traveller-worldbuilder
ready=0
for attempt in $(seq 1 45); do if curl -fsS http://127.0.0.1:8086/worlds/health 2>/dev/null; then ready=1; break; fi; sleep 2; done
test "$ready" = 1
curl -fsS http://127.0.0.1:8086/worlds/ > "$backup/startup-page.html"
grep -q 'DOXYLABS SHARED STORAGE' "$backup/startup-page.html"
changed=1
python3 - <<'PY'
from pathlib import Path
p=Path('/etc/nginx/sites-available/traveller')
s=p.read_text();marker='    location / {'
assert s.count(marker)==1 and 'location /worlds/' not in s
p.write_text(s.replace(marker,'''    location = /worlds { return 301 /worlds/; }
    location /worlds/ {
        client_max_body_size 1m;
        proxy_pass http://127.0.0.1:8086;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

'''+marker))
PY
nginx -t
install -m 644 /var/tmp/worldbuilder-portal.html /var/www/traveller-portal/index.html
install -m 644 /var/tmp/worldbuilder-robots.txt /var/www/traveller-portal/robots.txt
install -m 644 /var/tmp/worldbuilder-sitemap.xml /var/www/traveller-portal/sitemap.xml
systemctl reload nginx
echo "WORLD BUILDER DEPLOYED; backup=$backup"
systemctl is-active traveller-worldbuilder traveller-generator traveller-shipyard traveller-accounts nginx

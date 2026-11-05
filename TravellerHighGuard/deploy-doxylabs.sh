#!/bin/bash
set -euo pipefail
echo 'F5A135359ECA90B83CCFB94840111C3FD41046D2A22D5264220DF89B2563B5F8  /var/tmp/TravellerHighGuard-public.war' | sha256sum -c
test ! -e /opt/traveller-highguard
test ! -e /var/lib/traveller-highguard
! ss -ltn | grep -q ':8088 '
systemctl is-active --quiet traveller-shipyard traveller-mercenary traveller-worldbuilder traveller-generator traveller-accounts nginx
backup=/var/backups/traveller-highguard-$(date -u +%Y%m%dT%H%M%SZ)
mkdir -p "$backup"
cp -a /etc/nginx/sites-available/traveller "$backup/nginx-traveller"
cp -a /var/www/traveller-portal/index.html "$backup/index.html"
cp -a /var/www/traveller-portal/sitemap.xml "$backup/sitemap.xml"
rollback(){ cp "$backup/nginx-traveller" /etc/nginx/sites-available/traveller; cp "$backup/index.html" /var/www/traveller-portal/index.html; cp "$backup/sitemap.xml" /var/www/traveller-portal/sitemap.xml; nginx -t && systemctl reload nginx; systemctl stop traveller-highguard || true; echo "Routing rolled back; backup=$backup"; }
trap rollback ERR
useradd --system --user-group --home-dir /opt/traveller-highguard --shell /usr/sbin/nologin traveller-highguard
install -d -m 755 /opt/traveller-highguard
install -m 644 /var/tmp/TravellerHighGuard-public.war /opt/traveller-highguard/highguard.war
cat > /etc/systemd/system/traveller-highguard.service <<'UNIT'
[Unit]
Description=Classic Traveller Book 5 High Guard Ship Designer
After=network.target
[Service]
User=traveller-highguard
Group=traveller-highguard
SupplementaryGroups=traveller-shipyard
WorkingDirectory=/opt/traveller-highguard
ExecStart=/usr/bin/java -Xms32m -Xmx160m -jar /opt/traveller-highguard/highguard.war --server.address=127.0.0.1 --server.port=8088 --server.servlet.context-path=/highguard --server.servlet.session.cookie.secure=true --highguard.storage-dir=/var/lib/traveller-highguard --highguard.book2-dir=/var/lib/traveller-ships --highguard.portal-url=/ --highguard.book2-url=/shipyard/ --highguard.public-hosting=true --spring.thymeleaf.cache=true
StateDirectory=traveller-highguard
StateDirectoryMode=0750
Restart=on-failure
RestartSec=5
NoNewPrivileges=true
PrivateTmp=true
ProtectSystem=strict
ProtectHome=true
ReadOnlyPaths=/var/lib/traveller-ships
[Install]
WantedBy=multi-user.target
UNIT
systemctl daemon-reload
systemctl enable --now traveller-highguard
ready=0
for attempt in $(seq 1 40); do if curl -fsS http://127.0.0.1:8088/highguard/health 2>/dev/null; then ready=1; break; fi; sleep 2; done
test "$ready" = 1
curl -fsS http://127.0.0.1:8088/highguard/ > "$backup/startup.html"
grep -q 'shared with visitors' "$backup/startup.html"
python3 - <<'PY'
from pathlib import Path
p=Path('/etc/nginx/sites-available/traveller');s=p.read_text();marker='    location / {'
assert s.count(marker)==1 and 'location /highguard/' not in s
p.write_text(s.replace(marker,'''    location = /highguard { return 301 /highguard/; }
    location /highguard/ {
        client_max_body_size 2m;
        proxy_pass http://127.0.0.1:8088;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

'''+marker))
p=Path('/var/www/traveller-portal/index.html');s=p.read_text()
assert '/highguard/' not in s
s=s.replace('<div class="book">5</div>','<a class="book active" href="/highguard/" title="Book 5 · High Guard" style="text-decoration:none">5</a>').replace('FOUR APPLICATIONS','FIVE APPLICATIONS')
s=s.replace('</section>','''<article class="tool ship"><div class="tool-top"><div class="icon" aria-hidden="true"><svg viewBox="0 0 32 32"><path d="M16 2 29 25 20 22 18 30 16 27 14 30 12 22 3 25Z M16 2v25M3 25l13-10 13 10"/></svg></div><span class="tag">BOOK 5 / HIGH GUARD · 1980</span></div><h2>High Guard Ship Designer</h2><p>Design escorts and capital ships with scalable drives, armor, spinal weapons, batteries and screens. Bring a Book 2 design across as a separate copy.</p><ul class="features"><li>UP TO 1,000,000 TONS</li><li>POWER &amp; BATTERIES</li><li>BOOK 2 IMPORT</li></ul><a class="launch" href="/highguard/">Open High Guard <span aria-hidden="true">→</span></a><div class="secondary"><a href="/highguard/library">Saved High Guard ships ↗</a><span>VERSION 0.1</span></div></article></section>''',1)
s=s.replace('Character, ship, atlas and soldier libraries','Character, ship, atlas, soldier and High Guard libraries')
p.write_text(s)
p=Path('/var/www/traveller-portal/sitemap.xml');s=p.read_text();assert '/highguard/' not in s;p.write_text(s.replace('</urlset>','<url><loc>https://traveller.doxylabs.net/highguard/</loc></url>\n</urlset>'))
PY
nginx -t
systemctl reload nginx
curl --retry 5 --retry-all-errors --retry-delay 2 -fsS https://traveller.doxylabs.net/highguard/health
systemctl is-active traveller-highguard traveller-shipyard traveller-mercenary traveller-worldbuilder traveller-generator traveller-accounts nginx
echo "HIGH GUARD DEPLOYED; backup=$backup"

#!/bin/bash
set -euo pipefail
echo 'F334F835D9364716BF416BE2594A6FCF6E3F2B5A7800EB6F045AE84689460FAF  /var/tmp/TravellerMercenary-public.war' | sha256sum -c
test ! -e /opt/traveller-mercenary
! ss -ltn | grep -q ':8087 '
backup=/var/backups/traveller-mercenary-$(date -u +%Y%m%dT%H%M%SZ)
mkdir -p "$backup"
cp -a /etc/nginx/sites-available/traveller "$backup/nginx-traveller"
cp -a /var/www/traveller-portal/index.html "$backup/index.html"
rollback(){ cp "$backup/nginx-traveller" /etc/nginx/sites-available/traveller; cp "$backup/index.html" /var/www/traveller-portal/index.html; nginx -t && systemctl reload nginx; systemctl stop traveller-mercenary || true; echo "Rolled back routing; backup=$backup"; }
trap rollback ERR
useradd --system --user-group --home-dir /opt/traveller-mercenary --shell /usr/sbin/nologin traveller-mercenary
install -d -m 755 /opt/traveller-mercenary
install -m 644 /var/tmp/TravellerMercenary-public.war /opt/traveller-mercenary/mercenary.war
cat > /etc/systemd/system/traveller-mercenary.service <<'UNIT'
[Unit]
Description=Classic Traveller Book 4 Mercenary
After=network.target
[Service]
User=traveller-mercenary
Group=traveller-mercenary
WorkingDirectory=/opt/traveller-mercenary
ExecStart=/usr/bin/java -Xms32m -Xmx160m -jar /opt/traveller-mercenary/mercenary.war --server.address=127.0.0.1 --server.port=8087 --server.servlet.context-path=/mercenary --server.servlet.session.cookie.secure=true --mercenary.storage-dir=/var/lib/traveller-mercenaries --mercenary.portal-url=/ --mercenary.public-hosting=true --spring.thymeleaf.cache=true
StateDirectory=traveller-mercenaries
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
systemctl enable --now traveller-mercenary
ready=0
for attempt in $(seq 1 40); do if curl -fsS http://127.0.0.1:8087/mercenary/health; then ready=1; break; fi; sleep 2; done
test "$ready" = 1
curl -fsS http://127.0.0.1:8087/mercenary/ > "$backup/startup.html"
grep -q 'shared with other visitors' "$backup/startup.html"
python3 - <<'PY'
from pathlib import Path
p=Path('/etc/nginx/sites-available/traveller');s=p.read_text();marker='    location / {'
assert s.count(marker)==1 and 'location /mercenary/' not in s
s=s.replace(marker,'''    location = /mercenary { return 301 /mercenary/; }
    location /mercenary/ {
        client_max_body_size 32k;
        proxy_pass http://127.0.0.1:8087;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

'''+marker);p.write_text(s)
p=Path('/var/www/traveller-portal/index.html');s=p.read_text()
assert '/mercenary/' not in s and '</section>' in s
s=s.replace('<div class="book">4</div>','<a class="book active" href="/mercenary/" title="Book 4 · Mercenary" style="text-decoration:none">4</a>').replace('THREE APPLICATIONS','FOUR APPLICATIONS')
s=s.replace('</section>','''<article class="tool"><div class="tool-top"><div class="icon" aria-hidden="true"><svg viewBox="0 0 32 32"><path d="M16 3 27 8v8c0 6-6 11-11 14C11 27 5 22 5 16V8Z M11 16l4 4 7-9"/></svg></div><span class="tag">BOOK 4 / MERCENARY</span></div><h2>Mercenary Careers</h2><p>Join the Army or Marines. Take yearly postings, attend military schools, earn your rank, and leave the service with a story.</p><ul class="features"><li>YEARLY ASSIGNMENTS</li><li>SCHOOLS &amp; MEDALS</li><li>SAVED SOLDIERS</li></ul><a class="launch" href="/mercenary/">Open mercenary generator <span aria-hidden="true">→</span></a><div class="secondary"><a href="/mercenary/characters">Saved soldiers ↗</a><span>VERSION 0.1</span></div></article></section>''',1)
s=s.replace('Character, ship and atlas libraries','Character, ship, atlas and soldier libraries').replace('.tool.world{grid-column:1/-1}','')
p.write_text(s)
PY
nginx -t
systemctl reload nginx
curl -fsS https://traveller.doxylabs.net/mercenary/health
systemctl is-active traveller-mercenary traveller-worldbuilder traveller-generator traveller-shipyard traveller-accounts nginx
echo "DEPLOYED; backup=$backup"

#!/bin/bash
set -euo pipefail
test ! -e /var/www/traveller-portal/book0
backup=/var/backups/traveller-book0-$(date -u +%Y%m%dT%H%M%SZ)
mkdir -p "$backup"
cp -a /etc/nginx/sites-available/traveller "$backup/nginx-traveller"
cp -a /var/www/traveller-portal/{index.html,sitemap.xml} "$backup/"
rollback(){ cp "$backup/nginx-traveller" /etc/nginx/sites-available/traveller; cp "$backup/"{index.html,sitemap.xml} /var/www/traveller-portal/; nginx -t && systemctl reload nginx; }
trap rollback ERR
install -d -m 755 /var/www/traveller-portal/book0
install -m 644 /var/tmp/book0-introduction.html /var/www/traveller-portal/book0/index.html
python3 - <<'PY'
from pathlib import Path
p=Path('/etc/nginx/sites-available/traveller')
s=p.read_text();marker='    location = /worlds '
assert s.count(marker)==1 and 'location ^~ /book0/' not in s
p.write_text(s.replace(marker,'''    location = /book0 { return 301 /book0/; }
    location ^~ /book0/ {
        root /var/www/traveller-portal;
        index index.html;
        try_files $uri $uri/ =404;
    }

'''+marker))
PY
nginx -t
install -m 644 /var/tmp/book0-portal.html /var/www/traveller-portal/index.html
install -m 644 /var/tmp/book0-sitemap.xml /var/www/traveller-portal/sitemap.xml
systemctl reload nginx
echo "Book 0 informational page deployed; backup=$backup"

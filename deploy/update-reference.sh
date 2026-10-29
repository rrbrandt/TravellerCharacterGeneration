#!/usr/bin/env bash
set -euo pipefail
[[ $(id -u) == 0 ]]
payload=$(realpath "${1:?WAR path required}")
expected=2DD7D8D568D593380195C40AAF19C870FD1B808784D97B303318FE68E13ADEA5
[[ $(sha256sum "$payload" | cut -d' ' -f1 | tr '[:lower:]' '[:upper:]') == "$expected" ]]
unit=traveller-generator.service
target=/opt/traveller/traveller-generator.war
base=http://127.0.0.1:8082/traveller
[[ -f "$target" && -d /var/lib/traveller-characters ]]
backup=$(mktemp -d /var/tmp/traveller-before-nobility.XXXXXX)
cp -p "$target" "$backup/traveller.war"
rollback() {
 status=$?; trap - ERR
 journalctl -u "$unit" -n 30 --no-pager >&2 || true
 systemctl stop "$unit" || true
 cp -p "$backup/traveller.war" "$target"
 systemctl start "$unit"
 echo "Update failed; previous WAR restored from $backup. Saved characters preserved." >&2
 exit "$status"
}
trap rollback ERR
systemctl stop "$unit"
install -o "$(stat -c %u "$target")" -g "$(stat -c %g "$target")" -m "$(stat -c %a "$target")" "$payload" "$target"
systemctl start "$unit"
healthy=false
for attempt in $(seq 1 45); do
 if curl -fsS --max-time 5 "$base/characteristics" -o "$backup/characteristics.html" 2>"$backup/startup.log" && grep -q 'Social Standing titles' "$backup/characteristics.html"; then healthy=true; break; fi
 sleep 2
done
"$healthy"
grep -q 'VERSION 1.0' "$backup/characteristics.html"
curl -fsS --max-time 15 "$base/characteristic-reference.js" -o "$backup/reference.js"
grep -q 'reference-meaning' "$backup/reference.js"
curl -fsS --max-time 15 "$base/characters" -o "$backup/library.html"
grep -q 'Saved characters' "$backup/library.html"
systemctl is-active --quiet "$unit"
trap - ERR
echo "DEPLOYED version 1.0 characteristic reference. Backup: $backup. Character data and traffic guard unchanged."

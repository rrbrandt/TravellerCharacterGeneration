#!/usr/bin/env bash
set -euo pipefail
[[ $(id -u) == 0 ]] || { echo 'Run as root.' >&2; exit 1; }
payload=$(realpath "${1:?Usage: bash install-latest.sh /path/to/traveller.war}")
expected=34A9AC9EE03A9ADDC9FD5CD5D8600DEC4E6BFFFAB3832B48C3AFA2FAAECCEFCE
[[ $(sha256sum "$payload" | cut -d' ' -f1 | tr '[:lower:]' '[:upper:]') == "$expected" ]] || { echo 'WAR hash mismatch' >&2; exit 1; }
unit=traveller-generator.service
target=/opt/traveller/traveller-generator.war
base=http://127.0.0.1:8082/traveller
drop=/etc/systemd/system/$unit.d/50-character-storage.conf
data=/var/lib/traveller-characters
[[ -f "$target" ]]
[[ $(systemctl show "$unit" -p User --value) == traveller ]]
# Refuse to replace an existing custom storage configuration without inspection.
if systemctl cat "$unit" | grep -Eiq 'TRAVELLER_STORAGE_DIR|traveller.storage-dir|StateDirectory=.*traveller-characters'; then
  echo 'Existing character-storage configuration found; inspect it before running this first-install package.' >&2; exit 1
fi
working=$(systemctl show "$unit" -p WorkingDirectory --value)
[[ -z "$working" || ! -d "$working/data/characters" ]] || { echo 'Existing character data found; preserve and migrate it first.' >&2; exit 1; }
[[ ! -e "$drop" && ! -e "$data" ]] || { echo 'Storage path already exists; inspect before installing.' >&2; exit 1; }
backup=$(mktemp -d /var/tmp/traveller-before-20261029.XXXXXX)
cp -p "$target" "$backup/traveller.war"
systemctl cat "$unit" > "$backup/unit.txt"
rollback() {
 status=$?; trap - ERR
 journalctl -u "$unit" -n 35 --no-pager >&2 || true
 systemctl stop "$unit" || true
 cp -p "$backup/traveller.war" "$target"
 rm -f -- "$drop"
 systemctl daemon-reload
 systemctl start "$unit"
 echo "Update failed (exit $status); previous application restored. Backup: $backup. Character data retained in $data." >&2
 exit "$status"
}
trap rollback ERR
mkdir -p "$(dirname "$drop")"
printf '%s\n' '[Service]' 'StateDirectory=traveller-characters' 'StateDirectoryMode=0750' 'Environment="TRAVELLER_STORAGE_DIR=/var/lib/traveller-characters"' > "$drop"
systemctl stop "$unit"
install -o "$(stat -c %u "$target")" -g "$(stat -c %g "$target")" -m "$(stat -c %a "$target")" "$payload" "$target"
systemctl daemon-reload
systemctl start "$unit"
healthy=false
for attempt in $(seq 1 45); do
 if curl -fsS --max-time 5 "$base/characters" -o "$backup/library.html" 2>"$backup/startup.log" && grep -q 'Classic Traveller.*Books 0' "$backup/library.html"; then healthy=true; break; fi
 sleep 2
done
"$healthy"
curl -fsS --max-time 15 "$base/stage-navigation.js" -o "$backup/navigation.js"
grep -q 'character-save-form' "$backup/navigation.js"
curl -fsS --max-time 15 "$base/newcharacter" -o "$backup/newcharacter.html"
grep -q 'data-active-stage="0"' "$backup/newcharacter.html"
[[ -d "$data" && $(stat -c %U "$data") == traveller ]]
systemctl is-active --quiet "$unit"
trap - ERR
echo "DEPLOYED: Classic Traveller Books 0–8; locked stages, saved characters, and muster tables. Backup: $backup. Persistent data: $data."

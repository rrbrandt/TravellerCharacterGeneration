#!/usr/bin/env bash
set -euo pipefail
# Usage: sudo bash deploy/install-release.sh WAR UNIT LIVE_WAR HEALTH_BASE
payload=$(realpath "${1:?Release WAR required}")
unit=${2:?Existing systemd unit required}
target=$(realpath "${3:?Existing deployed WAR required}")
base=${4:?Loopback application base URL required}
[[ $(id -u) == 0 && -f "$payload" && -f "$target" ]]
[[ "$base" == http://127.0.0.1:* ]]
systemctl cat "$unit" >/dev/null
backup=$(mktemp -d /var/tmp/traveller-before-1.0.XXXXXX)
cp -p "$target" "$backup/traveller.war"
rollback() {
  trap - ERR
  systemctl stop "$unit" || true
  cp -p "$backup/traveller.war" "$target"
  systemctl start "$unit"
  echo "Deployment failed; previous WAR restored from $backup" >&2
}
trap rollback ERR
systemctl stop "$unit"
install -o "$(stat -c %u "$target")" -g "$(stat -c %g "$target")" -m "$(stat -c %a "$target")" "$payload" "$target"
systemctl start "$unit"
healthy=false
for attempt in $(seq 1 30); do
  if curl -fsS "$base/version.json" | grep -q '"version":"1.0.0"'; then healthy=true; break; fi
  sleep 2
done
"$healthy"
curl -fsS "$base/character" | grep -q 'Complete character history'
curl -fsS "$base/stage-navigation.js" | grep -q 'Final Character'
trap - ERR
echo "Traveller 1.0 deployed. Backup: $backup"

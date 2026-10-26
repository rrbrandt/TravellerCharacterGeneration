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
  status=$?
  trap - ERR
  echo "Verification failed (exit $status); restoring the previous WAR." >&2
  journalctl -u "$unit" -n 35 --no-pager >&2 || true
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
echo 'Waiting for Traveller 1.0 to start...'
for attempt in $(seq 1 60); do
  if curl -fsS --max-time 10 "$base/version.json" -o "$backup/version.json" 2>"$backup/startup-check.log" && grep -q '"version":"1.0.0"' "$backup/version.json"; then healthy=true; break; fi
  sleep 2
done
"$healthy"
echo 'Version 1.0 is running; checking the final page and navigation...'
# Download completely before matching. With pipefail, grep -q can close a pipe
# before curl has finished writing and incorrectly trigger rollback (SIGPIPE).
curl -fsS --max-time 20 "$base/character" -o "$backup/character.html"
grep -q 'Complete character history' "$backup/character.html"
curl -fsS --max-time 20 "$base/stage-navigation.js" -o "$backup/stage-navigation.js"
grep -q 'Final Character' "$backup/stage-navigation.js"
trap - ERR
echo "Traveller 1.0 deployed. Backup: $backup"

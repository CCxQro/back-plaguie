#!/usr/bin/env bash
# Driver for ZAP scan against Quarkus backend, iterating over 3 user roles.
set -u

ZAP="http://localhost:8081"
APIKEY="jnjpq62d4cdvr7sg0gkt84eun1"
TARGET="http://localhost:8080"
OPENAPI="$TARGET/openapi.json"
FIREBASE_AUTH_URL="${FIREBASE_AUTH_URL:?missing}"

log()  { echo "[$(date +%H:%M:%S)] $*"; }

zap() {
  # zap <path> [param=value...]
  local path="$1"; shift
  local url="$ZAP$path?apikey=$APIKEY"
  for kv in "$@"; do
    key="${kv%%=*}"; val="${kv#*=}"
    enc=$(python3 -c "import urllib.parse,sys;print(urllib.parse.quote(sys.argv[1],safe=''))" "$val")
    url="$url&$key=$enc"
  done
  curl -s "$url"
}

get_token() {
  local email="$1"
  curl -s -X POST "$FIREBASE_AUTH_URL" \
    -H "Content-Type: application/json" \
    -d "{\"email\":\"$email\",\"password\":\"12345678\",\"returnSecureToken\":true}" \
    | python3 -c "import sys,json;print(json.load(sys.stdin)['idToken'])"
}

set_auth_header() {
  local token="$1"
  zap "/JSON/replacer/action/removeRule/" "description=auth" >/dev/null
  zap "/JSON/replacer/action/addRule/" \
    "description=auth" \
    "enabled=true" \
    "matchType=REQ_HEADER" \
    "matchRegex=false" \
    "matchString=Authorization" \
    "replacement=Bearer $token"
}

wait_for() {
  # wait_for <view-path> <scanId> <label>
  local path="$1" scanid="$2" label="$3"
  while true; do
    resp=$(zap "$path" "scanId=$scanid")
    pct=$(echo "$resp" | python3 -c "import sys,json;print(json.load(sys.stdin).get('status','?'))" 2>/dev/null || echo "?")
    log "$label: $pct%"
    [ "$pct" = "100" ] && break
    sleep 15
  done
}

run_for_user() {
  local email="$1"
  log "=== ROLE: $email ==="
  local token
  token=$(get_token "$email")
  log "token fetched (len=${#token})"
  set_auth_header "$token"

  log "importing OpenAPI"
  zap "/JSON/openapi/action/importUrl/" "url=$OPENAPI" "hostOverride=$TARGET" >/dev/null

  log "starting spider"
  local sid_resp sid
  sid_resp=$(zap "/JSON/spider/action/scan/" "url=$TARGET" "recurse=true" "subtreeOnly=true")
  log "spider response: $sid_resp"
  sid=$(echo "$sid_resp" | python3 -c "import sys,json;print(json.load(sys.stdin)['scan'])")
  wait_for "/JSON/spider/view/status/" "$sid" "spider[$email]"

  log "starting active scan"
  local aid_resp aid
  aid_resp=$(zap "/JSON/ascan/action/scan/" "url=$TARGET" "recurse=true" "inScopeOnly=false")
  log "ascan response: $aid_resp"
  aid=$(echo "$aid_resp" | python3 -c "import sys,json;print(json.load(sys.stdin)['scan'])")
  wait_for "/JSON/ascan/view/status/" "$aid" "ascan[$email]"
}

# Set scope
zap "/JSON/context/action/newContext/" "contextName=plaguie" >/dev/null
zap "/JSON/context/action/includeInContext/" "contextName=plaguie" "regex=http://localhost:8080.*" >/dev/null

for u in admin3@gmail.com tec1@gmail.com agri1@gmail.com; do
  run_for_user "$u"
done

log "generating HTML report"
curl -s "$ZAP/OTHER/core/other/htmlreport/?apikey=$APIKEY" -o zap-report.html
log "report -> $(pwd)/zap-report.html"
log "DONE"

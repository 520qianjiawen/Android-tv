#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
: "${TV_ADDR:?请设置 TV_ADDR，例如 192.168.1.50:5555}"
adb connect "$TV_ADDR"
adb -s "$TV_ADDR" install -r "$ROOT/dist/tv-launcher-sony-debug.apk"
adb -s "$TV_ADDR" shell pm grant com.gathin.tvhome android.permission.ACCESS_FINE_LOCATION
adb -s "$TV_ADDR" shell am start -n com.gathin.tvhome/.MainActivity
adb -s "$TV_ADDR" shell cmd package set-home-activity --user 0 com.gathin.tvhome/.MainActivity
if [[ -n "${STOCK_LAUNCHER:-}" ]]; then
    adb -s "$TV_ADDR" shell pm disable-user --user 0 "$STOCK_LAUNCHER"
fi
adb -s "$TV_ADDR" shell input keyevent KEYCODE_HOME

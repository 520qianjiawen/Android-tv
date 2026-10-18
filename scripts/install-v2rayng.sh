#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
: "${TV_ADDR:?请设置 TV_ADDR，例如 192.168.1.50:5555}"
APK="${APK:-$ROOT/dist/v2rayNG-2.3.11-sony-remote-armeabi-v7a-debug.apk}"
adb connect "$TV_ADDR"
adb -s "$TV_ADDR" install -r "$APK"
adb -s "$TV_ADDR" shell am start -n com.v2ray.ang/.ui.main.MainActivity

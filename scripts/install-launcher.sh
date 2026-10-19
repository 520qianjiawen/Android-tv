#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
: "${TV_ADDR:?请设置 TV_ADDR，例如 192.168.1.50:5555 或 192.168.2.10:5555}"

echo "正在连接电视: $TV_ADDR..."
adb connect "$TV_ADDR"

BRAND="$(adb -s "$TV_ADDR" shell getprop ro.product.brand 2>/dev/null | tr -d '\r\n' || true)"
MODEL="$(adb -s "$TV_ADDR" shell getprop ro.product.model 2>/dev/null | tr -d '\r\n' || true)"
echo "检测到设备: Brand=[$BRAND], Model=[$MODEL]"

APK="${LAUNCHER_APK:-}"
if [[ -z "$APK" ]]; then
    if [[ "$BRAND" =~ (Xiaomi|Redmi|xiaomi|redmi) ]] && [[ -f "$ROOT/dist/tv-launcher-xiaomi-debug.apk" ]]; then
        APK="$ROOT/dist/tv-launcher-xiaomi-debug.apk"
    elif [[ -f "$ROOT/dist/tv-launcher-sony-debug.apk" ]]; then
        APK="$ROOT/dist/tv-launcher-sony-debug.apk"
    elif [[ -f "$ROOT/dist/tv-launcher-xiaomi-debug.apk" ]]; then
        APK="$ROOT/dist/tv-launcher-xiaomi-debug.apk"
    else
        echo "未找到安装包，请先执行 scripts/build-launcher.sh" >&2
        exit 1
    fi
fi

echo "安装桌面: $APK..."
adb -s "$TV_ADDR" install -r "$APK"

echo "授予权限与设置默认桌面..."
adb -s "$TV_ADDR" shell pm grant com.gathin.tvhome android.permission.ACCESS_FINE_LOCATION || true
adb -s "$TV_ADDR" shell am start -n com.gathin.tvhome/.MainActivity
adb -s "$TV_ADDR" shell cmd package set-home-activity --user 0 com.gathin.tvhome/.MainActivity

# 针对不同机型自动处理原厂桌面
if [[ -z "${STOCK_LAUNCHER:-}" ]]; then
    if [[ "$BRAND" =~ (Xiaomi|Redmi|xiaomi|redmi) ]]; then
        STOCK_LAUNCHER="com.mitv.tvhome"
    fi
fi

if [[ -n "${STOCK_LAUNCHER:-}" ]]; then
    echo "停用原厂桌面: $STOCK_LAUNCHER"
    adb -s "$TV_ADDR" shell pm disable-user --user 0 "$STOCK_LAUNCHER" || true
fi

adb -s "$TV_ADDR" shell input keyevent KEYCODE_HOME
echo "安装并设置完成！"

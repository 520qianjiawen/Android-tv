#!/usr/bin/env bash
set -euo pipefail
: "${TV_ADDR:?请设置 TV_ADDR，例如 192.168.2.10:5555}"

echo "正在连接小米/Redmi 电视: $TV_ADDR..."
adb connect "$TV_ADDR"

PACKAGES=(
    com.xm.webcontent
    com.mitv.cloudcontrol
    tv.samba.ssm
    com.xiaomi.mitv.csi
    com.google.android.boot.appsplashscreen
    com.google.android.leanbacklauncher.partnercustomizer
    com.xiaomi.mimusic2
    com.xiaomi.mitv.karaoke.service
    com.newtv.cboxtv
    com.xiaomi.eetv.litehome
    com.xiaomi.android.TV.audio
    com.mstar.android.media.tests
    com.xiaomi.mitv.upgrade
    com.mitv.screensaver
    com.xiaomi.mibox.lockscreen
    com.mitv.tvhome
)

echo "正在恢复小米电视应用..."
for pkg in "${PACKAGES[@]}"; do
    echo "启用: $pkg"
    adb -s "$TV_ADDR" shell pm enable --user 0 "$pkg" 2>/dev/null || true
done

echo "切回原厂 PatchWall 桌面..."
adb -s "$TV_ADDR" shell cmd package set-home-activity --user 0 com.mitv.tvhome/.MainActivity 2>/dev/null || true
adb -s "$TV_ADDR" shell input keyevent KEYCODE_HOME

echo "小米电视恢复完成！"

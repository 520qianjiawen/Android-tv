#!/usr/bin/env bash
set -euo pipefail
: "${TV_ADDR:?请设置 TV_ADDR，例如 192.168.2.10:5555}"

echo "正在连接小米/Redmi 电视: $TV_ADDR..."
adb connect "$TV_ADDR"

# 推荐精简的小米电视内置广告、推送、多余预装服务（保留声音处理、画质引擎、蓝牙遥控器与投屏）
PACKAGES=(
    com.xm.webcontent                    # 小米电视广告与推广页面
    com.mitv.cloudcontrol                # 云控策略
    tv.samba.ssm                         # Samba 电视行为收集与遥测
    com.xiaomi.mitv.csi                  # CSI 追踪与反馈服务
    com.google.android.boot.appsplashscreen # 开机广告屏
    com.google.android.leanbacklauncher.partnercustomizer # 厂商推荐定制
    com.xiaomi.mimusic2                  # 小米音乐预装
    com.xiaomi.mitv.karaoke.service      # 卡拉OK服务
    com.newtv.cboxtv                     # 央视影音预装
    com.xiaomi.eetv.litehome             # 简易模式桌面
    com.xiaomi.android.TV.audio          # 冗余音频服务
    com.mstar.android.media.tests        # 出厂测试应用
    com.xiaomi.mitv.upgrade              # 系统自动升级检测（防止被推送升级重新覆盖桌面）
    com.mitv.screensaver                 # 广告屏保
    com.xiaomi.mibox.lockscreen          # 锁屏广告
    com.mitv.tvhome                      # PatchWall 预装桌面
)

echo "开始停用冗余组件..."
for pkg in "${PACKAGES[@]}"; do
    echo "停用: $pkg"
    adb -s "$TV_ADDR" shell pm disable-user --user 0 "$pkg" 2>/dev/null || true
done

# 启用 HDMI 2.0 / 2.1 高画质模式 (4K 60Hz HDR / Apple TV 必需)
echo "开启 HDMI EDID 2.0/2.1 增强模式 (HDR / 4K60Hz)..."
adb -s "$TV_ADDR" shell settings put global tv_input_hdmi_edid_version 1

echo "小米电视精简完成！"

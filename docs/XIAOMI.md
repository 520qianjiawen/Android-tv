# 小米 / Redmi 电视适配与精简恢复

已在 Redmi / 小米电视（`MiTV-MTTP0`，硬件代号 `hugo`，MediaTek MT9632 平台 `ra2j`，Android 9，MIUI TV 稳定版 `21.10.29.3795`）完整验证。

---

## 适配说明

### 1. 信号源切换（HDMI 1/2/3、AV）与 Apple TV

- **根本原因**：国内小米电视固件（MIUI TV）移除了 Google Live Channels（`vnd.android.cursor.item/channel`），原生 Android TV 桌面直接调用 `TvContract` 会报错 `Not available on this TV`。
- **适配方案**：
  - TvHome 在小米设备上优先调用小米专属电视播放器：
    - 组件：`com.xiaomi.mitv.tvplayer/.ExternalSourceActivity`
    - 参数：`--ei input <id>`
      - HDMI 1 (`HW5`): `23`
      - HDMI 2 (`HW6`): `24`
      - HDMI 3 (`HW7`): `25`
      - AV (`HW2`): `2`
  - **长按快捷键**：长按顶部“Inputs”药丸按钮，会直接发送广播 `com.xiaomi.mitv.settings.INPUTSOURCE_POPUP`，弹出小米原生半透明信号源切换浮层。
  - **Apple TV / 4K 60Hz HDR 设置**：小米电视默认可能将 HDMI 设为 1.4 模式导致 Apple TV 显示“搜索信号中”或无信号。执行以下 ADB 命令开启增强模式：
    ```sh
    adb -s "$TV_ADDR" shell settings put global tv_input_hdmi_edid_version 1
    ```

### 2. 系统设置与 Wi-Fi 快捷键

标准 Android TV 意图在 MIUI TV 上不存在时，TvHome 自动降级跳转至小米原生设置应用：
- **系统设置**：`com.xiaomi.mitv.settings/.entry.MainActivity`
- **Wi-Fi 设置**：`com.xiaomi.mitv.settings/.wifi.MitvWifiActivity`

---

## 一键安装与设置

将 `$TV_ADDR` 设为小米电视的局域网 IP（需在“设置 → 关于 → 连续点击版本号开启开发者选项”，并开启 USB/网络调试）：

```sh
export TV_ADDR=192.168.2.10:5555
scripts/install-launcher.sh
```

脚本会自动检测到 Xiaomi / Redmi 设备，安装针对小米适配的桌面，并自动停用原厂 PatchWall 桌面（`com.mitv.tvhome`）。

---

## 系统精简与服务清单

运行以下脚本停用 PatchWall 资讯流、推荐广告、云控及冗余预装应用：

```sh
scripts/debloat-xiaomi.sh
```

### 停用清单说明

| 包名 | 功能描述 | 停用原因 |
| --- | --- | --- |
| `com.mitv.tvhome` | PatchWall 预装桌面 | 替换为轻量简洁的 Apple TV 风格 TvHome |
| `com.xm.webcontent` | 小米电视推广/资讯流 | 减少开机与主界面资讯广告加载 |
| `com.mitv.cloudcontrol` | 云控策略推送 | 阻止远程强制下发规则 |
| `tv.samba.ssm` | Samba 电视收视与遥测 | 遥测追踪服务，无实用功能 |
| `com.xiaomi.mitv.csi` | CSI 收集与反馈 | 后台常驻遥测 |
| `com.google.android.boot.appsplashscreen` | 开机广告屏 | 去除开机广告启动页 |
| `com.google.android.leanbacklauncher.partnercustomizer` | 合作伙伴定制推流 | 冗余推广插件 |
| `com.xiaomi.mimusic2` | 小米音乐 | 冗余预装多媒体 |
| `com.xiaomi.mitv.karaoke.service` | 卡拉OK服务 | 冗余后台服务 |
| `com.newtv.cboxtv` | 央视影音预装 | 冗余视频应用 |
| `com.xiaomi.eetv.litehome` | 简易模式桌面 | 备用桌面残留 |
| `com.xiaomi.android.TV.audio` | 冗余音频服务 | 冗余后台 |
| `com.mstar.android.media.tests` | 出厂测试应用 | 无需保留 |
| `com.xiaomi.mitv.upgrade` | 系统自动升级检测 | 防止静默升级后重置默认桌面 |
| `com.mitv.screensaver` | 广告画报屏保 | 带有推送与广告背景 |
| `com.xiaomi.mibox.lockscreen` | 锁屏展示 | 冗余锁屏页面 |

### 核心保留组件（严禁禁用）

- `com.xiaomi.mitv.tvplayer`：外部信号源（HDMI 1/2/3、AV）与电视图像显示核心。
- `com.xiaomi.mitv.settings`：图效、声音、输入法、网络与蓝牙管理。
- `com.xiaomi.mitv.soundpostprocess` / `com.xiaomi.mitv.sound`：杜比/DTS 音效后处理。
- `com.xiaomi.mitv.bluetoothremote`：蓝牙语音遥控器配对与固件通信。
- `com.xiaomi.milink.mishare.tv`：米联 / 无线投屏服务。

---

## 恢复原厂设置

如果需要恢复原厂所有应用与 PatchWall 桌面，运行：

```sh
scripts/restore-xiaomi.sh
```

或手动执行：

```sh
adb -s "$TV_ADDR" shell pm enable --user 0 com.mitv.tvhome
adb -s "$TV_ADDR" shell cmd package set-home-activity --user 0 com.mitv.tvhome/.MainActivity
adb -s "$TV_ADDR" shell input keyevent KEYCODE_HOME
```

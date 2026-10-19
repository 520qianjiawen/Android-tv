# Android TV · Sony & Xiaomi

索尼（Sony BRAVIA）与小米 / Redmi Android TV 桌面改造与 v2rayNG 遥控器适配，集中在一个仓库管理。

| 子项目 | 功能 | 目录 |
| --- | --- | --- |
| TvHome | Apple TV 风格桌面、固定应用栏、应用列表、输入源通道与原生设置适配 | [tv-launcher](tv-launcher/) |
| v2rayNG | Android 客户端，修复 D-pad 遥控器无法操作节点与启动按钮的问题 | [v2rayng](v2rayng/) |
| 安装包 | 本地调试构建的索尼与小米电视安装包 | [dist](dist/) |

## 机型与系统支持

已在以下两类典型实机设备上进行完整安装与输入源联动验证：

| 品牌 | 实机验证型号 | 系统环境 | 原厂桌面包名 | 信号源与系统入口机制 | 专属指南 |
| --- | --- | --- | --- | --- | --- |
| **索尼 (Sony)** | Sony BRAVIA 4K UR3 (MT5895) | Android TV 10 (32 位 ARM) | `com.dangbei.TVHomeLauncher` / Sony 原厂 | Android TV 标准 Live Channels 贯通通道；原生 Leanback 设置 | [docs/SONY.md](docs/SONY.md) |
| **小米 (Xiaomi / Redmi)** | Redmi / MiTV-MTTP0 (`hugo`, MT9632) | Android 9 / MIUI TV 21.10.29.3795 (32 位 ARM) | `com.mitv.tvhome` (PatchWall) | 小米专属 `tvplayer` 外部信号源直切 (`input` 23/24/25/2)；长按 Inputs 呼出原生选单；小米原生设置回退；HDMI EDID 2.0 增强 | [docs/XIAOMI.md](docs/XIAOMI.md) |

## 获取完整源码

```sh
git clone --recurse-submodules https://github.com/520qianjiawen/Android-tv.git
cd Android-tv
```

原生核心使用 Git 子模块固定到本次构建的源码修订。已克隆但子模块为空时执行 `git submodule update --init --recursive`。

## 通过电脑安装

电视开启开发者模式及网络调试（ADB），与电脑处于同一局域网。电脑需要 Android SDK Platform Tools 的 `adb`。

### 1. 索尼电视 (Sony BRAVIA)

将下面的示例地址替换成电视的实际 IP：

```sh
export TV_ADDR=192.168.1.50:5555
scripts/install-launcher.sh
scripts/install-v2rayng.sh
```

如需显式停用当贝原桌面：

```sh
STOCK_LAUNCHER=com.dangbei.TVHomeLauncher scripts/install-launcher.sh
```

### 2. 小米 / Redmi 电视 (MIUI TV)

将下面的示例地址替换成小米电视的实际 IP：

```sh
export TV_ADDR=192.168.2.10:5555
scripts/install-launcher.sh
scripts/install-v2rayng.sh

# 可选：一键精简 PatchWall 资讯流、开屏广告、后台云控及遥测组件
scripts/debloat-xiaomi.sh
```

> **提示**：`install-launcher.sh` 会自动检测电视品牌（`ro.product.brand`）。检测到小米/Redmi 电视时，会自动选用适配包，并在设置新桌面为默认后自动停用原厂 PatchWall 桌面（`com.mitv.tvhome`）。

`install-v2rayng.sh` 只尝试覆盖安装。**官方版与本地调试版签名不同，Android 会拒绝覆盖**；脚本不会自动卸载现有客户端。确需卸载重装时先备份节点数据。

## 桌面功能与特性

- **底栏布局**：电视 → VidHub → YouTube → 云视听小电视；Netflix 位于下方应用列表。
- **分类排列**：用户安装应用在前，系统预装应用在后；各组内按名称排序。
- **顶部药丸**：展开应用列表，顶部提供输入源（Inputs）、Wi-Fi 和系统设置入口。
- **物理信号源直达**：
  - **索尼电视**：通过标准 Android TV Live Channels（`TvContract`）直达 HDMI/AV 端口。
  - **小米电视**：通过小米原生播放器 `com.xiaomi.mitv.tvplayer/.ExternalSourceActivity` 直切信号源（HDMI 1=23、HDMI 2=24、HDMI 3=25、AV=2），支持无缝接入 Apple TV、PS5 等设备。
  - **快捷选单**：在小米电视上长按顶部“Inputs”按钮，可呼出原生半透明信号源浮层（广播 `com.xiaomi.mitv.settings.INPUTSOURCE_POPUP`）。
  - **Apple TV 4K HDR 增强**：针对小米电视 Apple TV 显示“搜索信号中”的问题，精简脚本自动开启 `tv_input_hdmi_edid_version 1`（HDMI 2.0 增强）。
- **设置与 Wi-Fi 回退**：在小米等未提供 Google 标准 Settings 意图的电视上，自动降级唤起厂商原生设置。
- **应用过滤**：内置过滤索尼当贝桌面（`com.dangbei.TVHomeLauncher`）与小米 PatchWall 桌面（`com.mitv.tvhome`）磁贴。

修改 [config.xml](tv-launcher/launcher/app/src/main/res/values/config.xml) 的 `dock_apps` 后重新构建，即可调整底部顺序。

## v2rayNG 遥控器修复

- 底部状态栏按右键进入启动/停止按钮，按左键返回。
- 节点名称按右键，进入分享 → 编辑 → 删除；双列模式进入“更多”。
- 各操作是独立焦点目标，获得焦点时显示边框。
- 节点重排后，焦点按节点身份保持。
- 触屏与键盘沿用同一套动作处理，避免一次确认键触发两次操作。

## 从源码构建

桌面使用 JDK 17+ / 21、Android SDK Platform 34 / Build Tools 34.0.0：

```sh
scripts/build-launcher.sh
```

v2rayNG 使用 JDK 21、Go 1.27、Android SDK Platform 37.0 / Build Tools 37.0.0、NDK 29.0.14206865（Go 核心）和 30.0.16248370（HEV）：

```sh
scripts/build-v2rayng.sh
```

分别设置对应的 `JAVA_HOME`，并设置 `ANDROID_HOME`。自定义 NDK 位置时设置 `CORE_NDK_HOME` 和 `NDK_HOME`。

## 文档与恢复

- [索尼桌面与应用恢复](docs/SONY.md)
- [小米电视适配与精简恢复](docs/XIAOMI.md)
- [测试记录和边界](docs/VALIDATION.md)
- [安装包说明与校验](dist/README.md)
- [上游来源与许可证](docs/UPSTREAM.md)

本仓库提供源码、构建脚本和安装包；本机凭据、节点、电视备份及 SDK/JDK 等构建环境不进入仓库。

# Android TV · Sony

索尼 Android TV 桌面改造与 v2rayNG 遥控器适配，集中在一个仓库管理。

| 子项目 | 功能 | 目录 |
| --- | --- | --- |
| TvHome | Apple TV 风格桌面、固定应用栏、应用列表与输入源入口 | [tv-launcher](tv-launcher/) |
| v2rayNG | Android 客户端，修复 D-pad 无法进入启动及节点操作按钮的问题 | [v2rayng](v2rayng/) |
| 安装包 | 本次构建的索尼电视版本 | [dist](dist/) |

已在 Sony BRAVIA 4K UR3、Android TV 10、32 位 ARM 环境安装。当前 v2rayNG 源码版本为 **2.3.11**。

## 获取完整源码

```sh
git clone --recurse-submodules https://github.com/520qianjiawen/Android-tv.git
cd Android-tv
```

原生核心使用 Git 子模块固定到本次构建的源码修订。已克隆但子模块为空时执行 `git submodule update --init --recursive`。

## 通过电脑安装

电视开启开发者模式及 USB/网络调试，与电脑处于同一局域网。电脑需要 Android SDK Platform Tools 的 `adb`。

将下面的示例地址替换成电视的实际 IP：

```sh
export TV_ADDR=192.168.1.50:5555
scripts/install-launcher.sh
scripts/install-v2rayng.sh
```

`install-launcher.sh` 会安装并设 TvHome 为默认桌面。确认新桌面可用后，可显式指定原桌面包名以停用它：

```sh
STOCK_LAUNCHER=com.dangbei.TVHomeLauncher scripts/install-launcher.sh
```

`install-v2rayng.sh` 只尝试覆盖安装。**官方版与本地调试版签名不同，Android 会拒绝覆盖**；脚本不会自动卸载现有客户端。确需卸载重装时先处理自己的节点数据，卸载会清除原设置。安装包不包含个人节点。

## 桌面功能与排序

- 底部：Netflix → YouTube → VidHub → 云视听小电视。
- 下方：用户安装应用在前，系统预装应用在后；各组内按名称排序。
- 遥控器向下展开应用列表，顶部提供输入源、Wi-Fi 和设置入口。
- 支持电视应用入口和普通应用入口，并按包名去重。

修改 [config.xml](tv-launcher/launcher/app/src/main/res/values/config.xml) 的 `dock_apps` 后重新构建，即可调整底部顺序。当前背景为黑色及跟随焦点应用的环境光，没有遥控器拖拽排序或图片壁纸设置界面。

## v2rayNG 遥控器修复

- 底部状态栏按右键进入启动/停止按钮，按左键返回。
- 节点名称按右键，进入分享 → 编辑 → 删除；双列模式进入“更多”。
- 各操作是独立焦点目标，获得焦点时显示边框。
- 节点重排后，焦点按节点身份保持。

触屏与键盘沿用同一套动作处理，避免一次确认键触发两次操作。

## 从源码构建

桌面使用 JDK 17、Android SDK Platform 34 / Build Tools 34.0.0：

```sh
scripts/build-launcher.sh
```

v2rayNG 使用 JDK 21、Go 1.27、Android SDK Platform 37.0 / Build Tools 37.0.0、NDK 29.0.14206865（Go 核心）和 30.0.16248370（HEV）：

```sh
scripts/build-v2rayng.sh
```

分别设置对应的 `JAVA_HOME`，并设置 `ANDROID_HOME`。自定义 NDK 位置时设置 `CORE_NDK_HOME` 和 `NDK_HOME`。v2rayNG 构建会初始化子模块、生成原生库、运行单元测试并生成 APK。

也可在 GitHub Actions 中手动运行 Android build 工作流。

## 验证与恢复

- [测试记录和边界](docs/VALIDATION.md)
- [索尼桌面恢复方法](docs/SONY.md)
- [安装包说明与校验](dist/README.md)
- [上游来源与许可证](docs/UPSTREAM.md)

本仓库提供源码、构建脚本和安装包；本机凭据、节点、电视备份及 SDK/JDK 等构建环境不进入仓库。

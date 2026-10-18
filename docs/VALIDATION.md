# 验证记录

## 桌面

本次桌面安装与截图检查确认：底部显示 Netflix、YouTube、VidHub、云视听小电视；下方用户安装应用排在系统预装应用前。检查过 Home 键返回新桌面、向下展开列表及方向键导航。

## v2rayNG

- 完整 2.3.11 项目 Kotlin/Java 编译成功。
- `:app:testPlaystoreDebugUnitTest`：99 项通过，无失败或跳过。
- `:app:assemblePlaystoreDebug` 与 `:app:assemblePlaystoreDebugAndroidTest` 成功。
- 四个声明的 ABI 均生成 HEV 库；交付安装包为 Sony 的 `armeabi-v7a` 版本。
- 界面隔离测试在 Sony BRAVIA 4K UR3 / Android 10 上 5 项通过：启动/停止焦点与动作、触屏与键盘动作一致、单列节点操作、双列更多入口、重排后焦点保持。测试使用修补后的真实组件与模拟动作接收器，不访问真实代理节点。
- 完整安装包已在同一台索尼电视安装。实际 D-pad 检查确认可从节点名称进入分享、编辑，反向可返回；从底部状态栏进入启动按钮。
- 遥控器确认键成功触发 VPN 授权流程，随后 CoreVpnService 成为前台服务。停止确认键已发送；之后按使用者要求结束进一步电视测试。
- 原有两个节点在重装后导入，并重新导出比较，链接完全一致。临时节点文件、剪贴板内容和传输工具已清理；这些内容不在仓库中。

## Not run

- 停止服务后最终状态的再次确认、重启电视后的代理行为和实际代理链路测速：使用者选择自行检查电视，未继续远程测试。
- TalkBack 朗读、全部浅色/深色与动态颜色组合、多分组/空列表/加载/错误状态的完整应用集成验证。Android 10 不支持系统动态颜色。
- 根目录新整理的跨平台构建脚本与 GitHub Actions 全流程：脚本做语法检查，安装包和原项目已执行上述构建；尚未在干净的新机器跑完整流水线。

源码中的 Android 仪器测试位于 `v2rayng/V2rayNG/app/src/androidTest/java/com/v2ray/ang/ui/main/MainRemoteNavigationTest.kt`。隔离测试通过不能代替以上未执行项目。

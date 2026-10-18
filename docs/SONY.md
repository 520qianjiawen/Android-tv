# 索尼桌面与应用恢复

恢复索尼/当贝原桌面：

```sh
adb -s "$TV_ADDR" shell pm enable --user 0 com.dangbei.TVHomeLauncher
adb -s "$TV_ADDR" shell cmd package set-home-activity --user 0 com.dangbei.TVHomeLauncher/com.dangbei.leard.leradlauncher.ui.sony.main.MainActivity
adb -s "$TV_ADDR" shell input keyevent KEYCODE_HOME
```

本次精简中，以下两个应用被停用：

| 包名 | 用途 |
| --- | --- |
| `com.dangbei.TVHomeLauncher` | 带推荐内容的原桌面 |
| `com.ktcp.tvvideo` | 预装视频应用及其推荐、保活后台 |

重新启用视频应用：

```sh
adb -s "$TV_ADDR" shell pm enable --user 0 com.ktcp.tvvideo
```

停用操作没有卸载系统应用。HDMI、音画处理、遥控器、投屏和系统更新不属于上述两项变更。

将 TvHome 切回默认桌面可重新运行 `scripts/install-launcher.sh`。

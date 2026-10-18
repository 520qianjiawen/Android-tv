#!/usr/bin/env bash
set -euo pipefail
ROOT="$(cd "$(dirname "$0")/.." && pwd)"
SOURCE="$ROOT/v2rayng"
: "${ANDROID_HOME:?请设置 Android SDK 的 ANDROID_HOME}"
: "${NDK_HOME:=$ANDROID_HOME/ndk/30.0.16248370}"
: "${CORE_NDK_HOME:=$ANDROID_HOME/ndk/29.0.14206865}"
export NDK_HOME
export GOBIN="$ROOT/.build/bin"
export PATH="$GOBIN:$PATH"
mkdir -p "$GOBIN" "$SOURCE/V2rayNG/app/libs" "$ROOT/dist"
git -C "$ROOT" submodule update --init --recursive
if [[ ! -f "$SOURCE/V2rayNG/app/libs/libv2ray.aar" ]]; then
    (
        cd "$SOURCE/AndroidLibXrayLite"
        MOBILE_VERSION="$(awk '$1 == "golang.org/x/mobile" { print $2; exit }' go.mod)"
        [[ -n "$MOBILE_VERSION" ]]
        go install "golang.org/x/mobile/cmd/gomobile@$MOBILE_VERSION"
        export ANDROID_NDK_HOME="$CORE_NDK_HOME"
        mkdir -p assets data
        bash gen_assets.sh download
        cp data/*.dat assets/
        gomobile init
        go mod download
        gomobile bind -v -androidapi 24 -trimpath -ldflags='-s -w -buildid= -checklinkname=0' -o "$SOURCE/V2rayNG/app/libs/libv2ray.aar" ./
    )
fi
(
    cd "$SOURCE"
    MAKEFLAGS="${MAKEFLAGS:--j8}" bash compile-hevtun.sh
    cp -R libs/. V2rayNG/app/libs/
    cd V2rayNG
    ./gradlew :app:testPlaystoreDebugUnitTest :app:compilePlaystoreDebugKotlin :app:assemblePlaystoreDebug --console=plain
    cp app/build/outputs/apk/playstore/debug/v2rayNG_2.3.11_armeabi-v7a.apk "$ROOT/dist/v2rayNG-2.3.11-sony-remote-armeabi-v7a-debug.apk"
)

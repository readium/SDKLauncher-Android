#!/bin/sh

rm -rf SDKLauncher-Android/app/build
rm -rf SDKLauncher-Android/build

cd readium-sdk/Platform/Android/
. ./clean.sh
cd -

cd readium-lcp-client/platform/android/
. ./clean.sh
cd -

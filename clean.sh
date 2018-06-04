#!/bin/sh

rm -r SDKLauncher-Android/app/build
rm -r SDKLauncher-Android/build

rm -r readium-sdk/Platform/Android/epub3/include

rm -r readium-sdk/Platform/Android/build
rm -r readium-sdk/Platform/Android/epub3/build
# rm -r readium-sdk/Platform/Android/epub3/libs
rm -r readium-sdk/Platform/Android/epub3/obj

rm -r readium-lcp-client/platform/android/build
rm -r readium-lcp-client/platform/android/lib/build
# rm -r readium-lcp-client/platform/android/lib/libs
rm -r readium-lcp-client/platform/android/lib/obj

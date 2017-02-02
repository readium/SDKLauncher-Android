_Note:  Please don't use the zip download feature on this repo as this repo uses submodules and this is not supported at present by github and will result in an incomplete copy of the repo._


Launcher-Android
---------------------
A small Android application to serve as a launcher/testbed for the Readium SDK. 

How to get source from github
-------------------------------
 git clone --recursive https://github.com/readium/SDKLauncher-Android.git

See a document on how to build the SDK and Launcher on Android [here](https://docs.google.com/document/d/1ebFQ-8BGoiamKO4K0ZiL1nH6b8wulJ-ORyZKiDEYi-8/edit)

Debug C++ code on Android Studio 2
----------------------------------

Gradle experimental build plugin and Android Studio 2 are required to debug C++ code.
By default the project uses stable version of gradle plugin but you can switch easily to the experimental one by adding the following line to local.properties file:
````
readium.ndk_debug=true
````

This settings will switch the build system to the gradle experimental version and allow you to debug, add breakpoints on the C++ code.

You must be careful that if you switch from the stable to the experimental version, you have first to delete all intermediate build files in:
- Platform/Android/epub3/build
- Platform/Android/include
- Platform/Android/obj
- Platform/Android/libs

If you forget to delete these files, the application will crash because of bad version of shared and static libraries.

Licensing info
----------------
Licensing information can be found in the file license.txt in the root of the repo, as well as in the source code itself.

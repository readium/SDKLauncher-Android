Launcher-Android
---------------------
A small Android application to serve as a launcher/testbed for the Readium SDK. 

Jenkins build status
----------------------
master [![Build Status](http://jenkinsmaster.datalogics-cloud.com:8080/buildStatus/icon?job=Readium-SDK-Launcher-Android-master)](http://jenkinsmaster.datalogics-cloud:8080/view/Readium-Launcher/job/Readium-SDK-Launcher-Android-master/)

develop [![Build Status](http://jenkinsmaster.datalogics-cloud.com:8080/buildStatus/icon?job=Readium-SDK-Launcher-Android-develop)](http://jenkinsmaster.datalogics-cloud:8080/view/Readium-Launcher/job/Readium-SDK-Launcher-Android-develop/)

How to get source from github
-------------------------------
 git clone --recursive https://github.com/readium/Launcher-Android.git

How to build Readium SDK
-------------------------------
````
cd readium-sdk/Platform/Android
./ndk-compile.sh build your-ndk-path
````
How to open eclispe project
----------------------
Use File->Import... menu, don't use File->New menu 

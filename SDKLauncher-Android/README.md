Launcher-Android
-----------------
A small Android application to serve as a launcher/testbed for the Readium SDK.

How to get source from github
------------------------------

* Clone the Launcher-Android repository and submodules
`git clone --recursive https://github.com/readium/Launcher-Android.git`

How to import the projects in Eclipse
--------------------------------------

* Import the ePub3-Library project into your Eclipse workspace
  The ePub3-Library project will build the libepub3.so and ePub3-Library.jar libraries that the SDKLauncher-Android project depends on.
  The project path is at:
  `<Launcher-Android base path>/readium-sdk/Platform/Android/`
  
  Note: For importing projects into Eclipse workspace use:
  `File -> Import... -> Existing Projects into Workspace -> select the subdirectory and the project...`
  
  More information about the ePub3-Library building at the readium-sdk readme if you need...

* Import the SDKLauncher-Android project into your Eclipse workspace
  This is the actual android application project.
  The project path is at:
  `<Launcher-Android base path>/SDKLauncher-Android/`

* Verify that the ePub3-Library dependency in SDKLauncher-Android project is correct at:
  `SDKLauncher-Android Properties -> Android -> Library`

How to build
-------------

If all the previous steps are done, so prepare a device or emulator and launch the app:
`Run as... -> Android Application`

Have fun...
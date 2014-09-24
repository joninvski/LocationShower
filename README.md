LocationShower
==============

Shows your location (duh!)

<img src="https://github.com/joninvski/LocationShower/raw/master/images/screenshot.png" alt="screenshot" width="230px">


Compile
-------

    ANDROID_HOME=/home/.../android/sdk; export ANDROID_HOME # Optional
    ./gradlew assemble                                      # Will generate both debug and release builds

Install on device
-----------------

    # Make sure emulator is running or connected to real device
    ./gradlew installDebug

Activity tests
--------------

    # Installs and runs the tests for Build 'debug' on connected devices.
    ./gradlew connectedAndroidTest # results in app/build/outputs/reports/androidTests/connected/index.html

    # Runs all the instrumentation test variations on all the connected devices
    ./gradlew spoon                # results in app/build/spoon/debug/index.html

Code quality
------------

    # Runs lint on all variants
    ./gradlew lint         # results in app/build/lint-results.html

    # Checks if the code is accordings with the code style
    ./gradlew app:checktyle   # results in app/build/reports/checkstyle/main.xml


Libraries Used
--------------

### Running

- Square's [Otto](http://square.github.io/otto/)
- Jake Wharton's [Butterknife](http://jakewharton.github.io/butterknife/)
- Jake Wharton's [Timber](https://github.com/JakeWharton/timber)
- keyboardsurfer's [Crouton](https://github.com/keyboardsurfer/Crouton)

### Testing/Quality

- [Checkstyle](http://checkstyle.sourceforge.net/)
- [Robotium](https://code.google.com/p/robotium)
- Square's [Spoon](http://square.github.io/spoon/)


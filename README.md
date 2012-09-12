# FFMPEG Library for Android

This project is a Java wrapper around an ffmpeg command line binary for use in
Android applications.  It depends on the
[android-ffmpeg](https://github.com/guardianproject/android-ffmpeg) project to
provide the ffmpeg binary.

## Building

Ensure `NDK_BASE` env variable is set to the location of your NDK, example:

    export NDK_BASE=/path/to/android-ndk

Then execute:

    git submodule update --init --recursive
    cd external/android-ffmpeg
    ./configure_make_everything.sh

Then build the project in Eclipse.

# FFMPEG Library for Android

This project is a Java wrapper around an ffmpeg command line binary for use in
Android applications.  It depends on the
[android-ffmpeg](https://github.com/guardianproject/android-ffmpeg) project to
provide the ffmpeg binary.

For ease of developer use, we've included the FFMPEG and Sox binaries in the project,
however, we strongly recommend you compile them yourselves using the steps below.

It is licensed under the GPLv3.

## Building

Ensure `NDK_BASE` env variable is set to the location of your NDK, example:

    export NDK_BASE=/path/to/android-ndk

Then execute:

    git submodule update --init --recursive
    cd external/android-ffmpeg
    ./configure_make_everything.sh

Once compiled, you will find the binaries at:

external/android-ffmpeg/ffmpeg/ffmpeg
external/android-ffmpeg/data/data/info.guardianproject.ffmpeg/app_opt/bin/sox

You should replace the files in res/raw with these.

Then build the project in Eclipse.

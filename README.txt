
This project is a Java wrapper around an ffmpeg command line binary for use in
Android applications.  It depends on the 'android-ffmpeg' project to provide
the ffmpeg binary.

Building
--------

git submodule init
git submodule update
cd external/android-ffmpeg
./configure_make_everything.sh

Then build the project in Eclipse.

package net.sourceforge.sox;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.ffmpeg.android.ShellUtils.ShellCallback;

import android.util.Log;

/**
 * Concatenates two files together with a crossfade of user
 * defined length.
 *
 * It is a Java port of the scripts/crossfade_cat.sh script
 * in the sox source tree.
 *
 * Original script by Kester Clegg, with modifications by Chris
 * Bagwell.
 *
 * @author Abel Luck
 *
 */
// TODO make runnable?
public class CrossfadeCat {
	private final static String TAG = "SOX-XFADE";
	private	SoxController mController;
	private String mFirstFile;
	private String mSecondFile;
	private double mFadeLength;

	public CrossfadeCat(SoxController controller, String firstFile, String secondFile, double fadeLength) {
		mController = controller;
		mFirstFile = firstFile;
		mSecondFile = secondFile;
		mFadeLength = fadeLength;
	}

	public void start() {
		// find length of first file
		double length = mController.getLength(mFirstFile);

		double trimLength = length - mFadeLength;

		// Obtain trimLength seconds of fade out position from the first File
		mController.trimAudio(mFirstFile, trimLength);

		//TODO finish
	}



}

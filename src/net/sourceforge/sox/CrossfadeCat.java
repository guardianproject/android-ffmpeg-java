package net.sourceforge.sox;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

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
	private String mFinalMix;

	public CrossfadeCat(SoxController controller, String firstFile, String secondFile, double fadeLength, String outFile) {
		mController = controller;
		mFirstFile = firstFile;
		mSecondFile = secondFile;
		mFadeLength = fadeLength;
		mFinalMix = outFile;
	}

	public boolean start() throws IOException {
		// find length of first file
		double length = mController.getLength(mFirstFile);

		double trimLength = length - mFadeLength;

		// Obtain trimLength seconds of fade out position from the first File
		String trimmedOne = mController.trimAudio(mFirstFile, trimLength, mFadeLength);
		if( trimmedOne == null )
			return abort();

		// We assume a fade out is needed (i.e., firstFile doesn't already fade out)

		String fadedOne = mController.fadeAudio(trimmedOne, "t", 0, mFadeLength, mFadeLength);
		if( fadedOne == null )
			return abort();
		// Get crossfade section from the second file
		String trimmedTwo = mController.trimAudio(mSecondFile, 0, mFadeLength);
		if( trimmedTwo == null )
			return abort();

		String fadedTwo = mController.fadeAudio(trimmedTwo, "t", mFadeLength, -1, -1);
		if( fadedTwo == null )
			return abort();

		// Mix crossfaded files together at full volume
		ArrayList<String> files = new ArrayList<String>();
		files.add(fadedOne);
		files.add(fadedTwo);

		String crossfaded = new File(mFirstFile).getCanonicalPath() + "-x-" + new File(mSecondFile).getName() +".wav";
		crossfaded = mController.combineMix(files, crossfaded);
		if( crossfaded == null )
			return abort();

		// Trim off crossfade sections from originals
		String trimmedThree = mController.trimAudio(mFirstFile, 0, trimLength);
		if( trimmedThree == null )
			return abort();
		
		String trimmedFour = mController.trimAudio(mSecondFile, mFadeLength, -1);
		if( trimmedFour == null )
			return abort();

		// Combine into final mix
		files.clear();
		files.add(trimmedThree);
		files.add(crossfaded);
		files.add(trimmedFour);
		mFinalMix = mController.combine(files, mFinalMix);
		return true;
	}

	
	private boolean abort() {
		return false;
	}


}

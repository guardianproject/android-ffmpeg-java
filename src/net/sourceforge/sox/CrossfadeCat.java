package net.sourceforge.sox;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Concatenates two files together with a crossfade of user
 * defined mClipLength.
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
		
		//double mClipLength = mController.getLength(mFirstFile);
	}

	public boolean start() throws Exception {
		// find mClipLength of first file
		

		// Obtain trimLength seconds of fade out position from the first File
		double firstFileLength = mController.getLength(mFirstFile);
		double trimLength = firstFileLength - mFadeLength;

		String trimmedOne = mController.trimAudio(mFirstFile, trimLength, mFadeLength);
		
		if( trimmedOne == null )
			throw new IOException("audio trim did not complete: " + mFirstFile);
		
			// We assume a fade out is needed (i.e., firstFile doesn't already fade out)

		String fadedOne = mController.fadeAudio(trimmedOne, "t", 0, mFadeLength, mFadeLength);
		if( fadedOne == null )
			throw new IOException("audio fade did not complete: " + trimmedOne);
		
		// Get crossfade section from the second file
		String trimmedTwo = mController.trimAudio(mSecondFile, 0, mFadeLength);
		if( trimmedTwo == null )
			throw new IOException("audio trim did not complete: " + mSecondFile);

		String fadedTwo = mController.fadeAudio(trimmedTwo, "t", mFadeLength, -1, -1);
		if( fadedTwo == null )
			throw new IOException("audio fade did not complete: " + trimmedTwo);

		// Mix crossfaded files together at full volume
		ArrayList<String> files = new ArrayList<String>();
		files.add(fadedOne);
		files.add(fadedTwo);

		String crossfaded = new File(mFirstFile).getCanonicalPath() + "-x-" + new File(mSecondFile).getName() +".wav";
		crossfaded = mController.combineMix(files, crossfaded);
		if( crossfaded == null )
			throw new IOException("crossfade did not complete");

		// Trim off crossfade sections from originals
		String trimmedThree = mController.trimAudio(mFirstFile, 0, trimLength);
		if( trimmedThree == null )
			throw new IOException("crossfade trim beginning did not complete");
		
		String trimmedFour = mController.trimAudio(mSecondFile, mFadeLength, -1);
		if( trimmedFour == null )
			throw new IOException("crossfade trim end did not complete");
		
		// Combine into final mix
		files.clear();
		files.add(trimmedThree);
		files.add(crossfaded);
		files.add(trimmedFour);
		mFinalMix = mController.combine(files, mFinalMix);
		
		if (mFinalMix == null)
			throw new IOException("final mix did not complete");
		
		return true;
	}


}

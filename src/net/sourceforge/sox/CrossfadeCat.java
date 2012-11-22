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
public class CrossfadeCat {
	private final static String TAG = "SOX-XFADE";
	SoxController mController;

	
	/**
	 * Retrieve the length of the file
	 * sox file.wav 2>&1 -n stat | grep Length | cut -d : -f 2 | cut -f 1
	 * @return the length in seconds
	 */
	public String length(String path) {
		ArrayList<String> cmd = new ArrayList<String>();

		cmd.add(path);
		cmd.add("-n");
		cmd.add("stat");
		
//		mController.execSox(cmd);
		return null;
	}
	
	
	
}

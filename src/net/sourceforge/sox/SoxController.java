package net.sourceforge.sox;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.ffmpeg.android.BinaryInstaller;
import org.ffmpeg.android.ShellUtils.ShellCallback;

import android.content.Context;
import android.util.Log;

public class SoxController {
	private final static String TAG = "SOX";
	String[] libraryAssets = {"sox"};
	private String soxBin;
	private File fileBinDir;
	private Context context;
	
	public SoxController(Context _context) throws FileNotFoundException, IOException {
		context = _context;
		fileBinDir = context.getDir("bin",0);

		if (!new File(fileBinDir, libraryAssets[0]).exists())
		{
			BinaryInstaller bi = new BinaryInstaller(context,fileBinDir);
			bi.installFromRaw();
		}
		
		soxBin = new File(fileBinDir,"sox").getAbsolutePath();

	}

	private class LengthParser implements ShellCallback {
		public String length = null;
		public int retValue = -1;
		
		@Override
		public void shellOut(String shellLine) {
			Log.e("sox", shellLine);
			if( !shellLine.startsWith("Length") )
				return;
			String[] split = shellLine.split(":");
			if(split.length != 2) return;
			
			length = split[1].trim();
		}
		
		@Override
		public void processComplete(int exitValue) {
			retValue = exitValue;
			
		}
	}
	/**
	 * Retrieve the length of the audio file
	 * sox file.wav 2>&1 -n stat | grep Length | cut -d : -f 2 | cut -f 1
	 * @return the length in seconds or null
	 */
	public String getLength(String path) {
		ArrayList<String> cmd = new ArrayList<String>();

		cmd.add(soxBin);
		cmd.add(path);
		cmd.add("-n");
		cmd.add("stat");

		LengthParser sc = new LengthParser();
		
		try {
			execSox(cmd, sc);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sc.length;
	}
	
	private void execSox(List<String> cmd, ShellCallback sc) throws IOException,
			InterruptedException {

		String soxBin = new File(fileBinDir, "sox").getAbsolutePath();
		Runtime.getRuntime().exec("chmod 700 " + soxBin);
		execProcess(cmd, sc);
	}

	private int execProcess(List<String> cmds, ShellCallback sc)
			throws IOException, InterruptedException {

		ProcessBuilder pb = new ProcessBuilder(cmds);
		pb.directory(fileBinDir);

		StringBuffer cmdlog = new StringBuffer();

		for (String cmd : cmds) {
			cmdlog.append(cmd);
			cmdlog.append(' ');
		}

		Log.v(TAG, cmdlog.toString());

		// pb.redirectErrorStream(true);
		Process process = pb.start();

		// any error message?
		StreamGobbler errorGobbler = new StreamGobbler(
				process.getErrorStream(), "ERROR", sc);

		// any output?
		StreamGobbler outputGobbler = new StreamGobbler(
				process.getInputStream(), "OUTPUT", sc);

		// kick them off
		errorGobbler.start();
		outputGobbler.start();

		int exitVal = process.waitFor();

		sc.processComplete(exitVal);

		return exitVal;
	}

	class StreamGobbler extends Thread {
		InputStream is;
		String type;
		ShellCallback sc;

		StreamGobbler(InputStream is, String type, ShellCallback sc) {
			this.is = is;
			this.type = type;
			this.sc = sc;
		}

		public void run() {
			try {
				InputStreamReader isr = new InputStreamReader(is);
				BufferedReader br = new BufferedReader(isr);
				String line = null;
				while ((line = br.readLine()) != null)
					if (sc != null)
						sc.shellOut(line);

			} catch (IOException ioe) {
				Log.e(TAG, "error reading shell slog", ioe);
			}
		}
	}
}

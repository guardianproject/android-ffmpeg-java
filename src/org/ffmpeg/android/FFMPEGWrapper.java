package org.ffmpeg.android;


import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;

import org.ffmpeg.android.ShellUtils.ShellCallback;

import android.content.Context;

public class FFMPEGWrapper {

	String[] libraryAssets = {"ffmpeg"};
	File fileBinDir;
	Context context;

	public FFMPEGWrapper(Context _context) throws FileNotFoundException, IOException {
		context = _context;
		fileBinDir = context.getDir("bin",0);

		if (!new File(fileBinDir,libraryAssets[0]).exists())
		{
			BinaryInstaller bi = new BinaryInstaller(context,fileBinDir);
			bi.installFromRaw();
		}
	}
	
	private void execFFMPEG (String cmd, ShellCallback sc) throws Exception {
	
		String ffmpegBin = new File(fileBinDir,"ffmpeg").getAbsolutePath();
		Runtime.getRuntime().exec("chmod 700 " +ffmpegBin);
    	
		execProcess (cmd.split(" "), sc);
	}
	
	private void execProcess(String[] cmds, ShellCallback sc) throws Exception {		
        
		
			ProcessBuilder pb = new ProcessBuilder(cmds);
			pb.redirectErrorStream(true);
	    	Process process = pb.start();      
	    	
			BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
	
			String line;
			
			while ((line = reader.readLine()) != null)
			{
				if (sc != null)
					sc.shellOut(line.toCharArray());
			}

			
	}
	
	public class FFMPEGArg
	{
		String key;
		String value;
		
		public static final String ARG_VIDEOCODEC = "vcodec";
		public static final String ARG_VERBOSITY = "v";
		public static final String ARG_FILE_INPUT = "-i";
		public static final String ARG_SIZE = "-s";
		public static final String ARG_FRAMERATE = "-r";
		public static final String ARG_FORMAT = "-f";
		public static final String ARG_BITRATE = "-b";
		
		
	}
	
	public void processVideo(MediaDesc in, MediaDesc out, ShellCallback sc) throws Exception {
		
		processVideo(new File(in.path),new File(out.path), 
				out.format,in.duration,in.width,in.height,out.width,out.height,out.fps,out.kbitrate,out.vcodec,out.acodec,sc);
		
	}
	
	
	
	public void processVideo(File inputFile, File outputFile, String format, int mDuration,
			int iWidth, int iHeight, int oWidth, int oHeight, String frameRate, int kbitRate, String vcodec, String acodec, ShellCallback sc) throws Exception {
		
		float widthMod = ((float)oWidth)/((float)iWidth);
		float heightMod = ((float)oHeight)/((float)iHeight);
		
		if (vcodec == null)
			vcodec = "copy";//"libx264"
		
		if (acodec == null)
			acodec = "copy";
		
    	String ffmpegBin = new File(fileBinDir,"ffmpeg").getAbsolutePath();
		Runtime.getRuntime().exec("chmod 700 " +ffmpegBin);
    	
    	String[] ffmpegCommand = {ffmpegBin, "-y", FFMPEGArg.ARG_FILE_INPUT, inputFile.getPath(), 
				"-vcodec", vcodec, 
				FFMPEGArg.ARG_BITRATE, kbitRate+"k", 
				"-s",  oWidth + "x" + oHeight, 
				"-r", ""+frameRate,
				"-acodec", acodec,
				"-f", format,
				outputFile.getPath()};
    	
    	//./ffmpeg -y -i test.mp4 -vframes 999999  -vf 'redact=blurbox.txt [out] [d], [d]nullsink' -acodec copy outputa.mp4
    	
    	//ffmpeg -v 10 -y -i /sdcard/org.witness.sscvideoproto/videocapture1042744151.mp4 -vcodec libx264
    	//-b 3000k -s 720x480 -r 30 -acodec copy -f mp4 -vf 'redact=/data/data/org.witness.sscvideoproto/redact_unsort.txt'
    	///sdcard/org.witness.sscvideoproto/new.mp4
    	
    	//"-vf" , "redact=" + Environment.getExternalStorageDirectory().getPath() + "/" + PACKAGENAME + "/redact_unsort.txt",

    	
    	// Need to make sure this will create a legitimate mp4 file
    	//"-acodec", "ac3", "-ac", "1", "-ar", "16000", "-ab", "32k",
    	

    	/*
    	String[] ffmpegCommand = {"/data/data/"+PACKAGENAME+"/ffmpeg", "-v", "10", "-y", "-i", recordingFile.getPath(), 
    					"-vcodec", "libx264", "-b", "3000k", "-vpre", "baseline", "-s", "720x480", "-r", "30",
    					//"-vf", "drawbox=10:20:200:60:red@0.5",
    					"-vf" , "\"movie="+ overlayImage.getPath() +" [logo];[in][logo] overlay=0:0 [out]\"",
    					"-acodec", "copy",
    					"-f", "mp4", savePath.getPath()+"/output.mp4"};
    	*/
    	
    	execProcess(ffmpegCommand, sc);
	    
	}
	
	public void concatAndTrimFiles (ArrayList<MediaDesc> videos,MediaDesc out,  ShellCallback sc) throws Exception
	{
		StringBuffer cmd = new StringBuffer();
		
		for (MediaDesc vdesc : videos)
		{
			//ffmpeg -i $i -ss 00:00:03 -t 5 -f mpeg -;
			cmd.append("ffmpeg -i ");
			cmd.append(vdesc.path);
			
			if (vdesc.startTime != null)
			{
				cmd.append(" -ss ");
				cmd.append(vdesc.startTime);
			}
			
			cmd.append(" -t ");
			cmd.append(vdesc.duration);
			
			//everything to mpeg!
			cmd.append(" -f mpeg -;");
		}
		
		//cmd="${cmd} ) | ffmpeg -y -i - -threads 8
		cmd.append("ffmpeg -y -i - -threads 8");
		cmd.append(out.path);

		execFFMPEG(cmd.toString(), sc);
    	
		
	}
	
	class FileMover {

		InputStream inputStream;
		File destination;
		
		public FileMover(InputStream _inputStream, File _destination) {
			inputStream = _inputStream;
			destination = _destination;
		}
		
		public void moveIt() throws IOException {
		
			OutputStream destinationOut = new BufferedOutputStream(new FileOutputStream(destination));
				
			int numRead;
			byte[] buf = new byte[1024];
			while ((numRead = inputStream.read(buf) ) >= 0) {
				destinationOut.write(buf, 0, numRead);
			}
			    
			destinationOut.flush();
			destinationOut.close();
		}
	}

}



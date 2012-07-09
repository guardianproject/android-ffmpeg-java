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
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.ffmpeg.android.ShellUtils.ShellCallback;

import android.content.Context;
import android.util.Log;

public class FfmpegController {

	String[] libraryAssets = {"ffmpeg"};
	File fileBinDir;
	Context context;
	
	private final static String TAG = "FFMPEG";

	public FfmpegController(Context _context) throws FileNotFoundException, IOException {
		context = _context;
		fileBinDir = context.getDir("bin",0);

		if (!new File(fileBinDir,libraryAssets[0]).exists())
		{
			BinaryInstaller bi = new BinaryInstaller(context,fileBinDir);
			bi.installFromRaw();
		}
	}
	
	private void execFFMPEG (List<String> cmd, ShellCallback sc) throws Exception {
	
		String ffmpegBin = new File(fileBinDir,"ffmpeg").getAbsolutePath();
		Runtime.getRuntime().exec("chmod 700 " +ffmpegBin);
    	
		execProcess (cmd, sc);
	}
	
	
	
	private int execProcess(List<String> cmds, ShellCallback sc) throws Exception {		
        
		ProcessBuilder pb = new ProcessBuilder(cmds);
		pb.directory(fileBinDir);
		
	//	pb.redirectErrorStream(true);
    	Process process = pb.start();    
    	
    
    	  // any error message?
        StreamGobbler errorGobbler = new 
            StreamGobbler(process.getErrorStream(), "ERROR", sc);            
        
    	 // any output?
        StreamGobbler outputGobbler = new 
            StreamGobbler(process.getInputStream(), "OUTPUT", sc);
            
        // kick them off
        errorGobbler.start();
        outputGobbler.start();
     

        int exitVal = process.waitFor();
        
        return exitVal;


		
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
		public static final String ARG_BITRATE_VIDEO = "-b:v";
		
		
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
		
    	String ffmpegBin = "ffmpeg";
    	
    	String[] ffmpegCommand = {ffmpegBin, "-y", FFMPEGArg.ARG_FILE_INPUT, inputFile.getPath(), 
				"-vcodec", vcodec, 
				FFMPEGArg.ARG_BITRATE_VIDEO, kbitRate+"k", 
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
    	
    	
    	execProcess(java.util.Arrays.asList(ffmpegCommand), sc);
	    
	}
	
	public void concatAndTrimFiles (ArrayList<MediaDesc> videos,MediaDesc out,  ShellCallback sc) throws Exception
	{
		String ffmpegBin = new File(fileBinDir,"ffmpeg").getAbsolutePath();
    	
		int idx = 0;
		
		for (MediaDesc vdesc : videos)
		{
			ArrayList<String> cmd = new ArrayList<String>();

			//ffmpeg -i $i -ss 00:00:03 -t 5 -f mpeg -;
			
			cmd.add(ffmpegBin);
			cmd.add("-y");
			cmd.add("-i");
			cmd.add(vdesc.path);
			
			if (vdesc.startTime != null)
			{
				cmd.add("-ss");
				cmd.add(vdesc.startTime);
			}
			
			if (vdesc.duration > 0)
			{
				cmd.add("-t");
				cmd.add("" + vdesc.duration);
			}
			
			if (out.kbitrate > 0)
			{
				cmd.add(FFMPEGArg.ARG_BITRATE_VIDEO);
				cmd.add(out.kbitrate + "k");
				
			}
			
			if (out.width > 0)
			{
				cmd.add(FFMPEGArg.ARG_SIZE);
				cmd.add(out.width + "x" + out.height);
			}
			
			//everything to mpeg!
			cmd.add("-f");
			cmd.add("mpeg");
			cmd.add(out.path + '.' + idx++ + ".mpg");

			execFFMPEG(cmd, sc);
		}
		
		
		//cmd="${cmd} ) | ffmpeg -y -i - -threads 8
		StringBuffer cmdRun = new StringBuffer();
		
		cmdRun.append("cat ");
		
		idx = 0;
		
		for (MediaDesc vdesc : videos)
		{
			
			cmdRun.append(out.path + '.' + idx++ + ".mpg" + " ");
			
		}
		
		cmdRun.append("> ");
		cmdRun.append(out.path + ".full.mpg");
		
		Log.d(TAG,"cat cmd: " + cmdRun.toString());
		
		String[] cmds = {"sh","-c",cmdRun.toString()};
		Runtime.getRuntime().exec(cmds).waitFor();
		
		ArrayList<String> cmd = new ArrayList<String>();

		//cmd="${cmd} ) | ffmpeg -y -i - -threads 8
		cmd.add(ffmpegBin);
		cmd.add("-y");
		
		cmd.add("-i");
		cmd.add(out.path + ".full.mpg");

		if (out.kbitrate > 0)
		{
			cmd.add(FFMPEGArg.ARG_BITRATE_VIDEO);
			cmd.add(out.kbitrate + "k");
			
		}
		
		if (out.width > 0)
		{
			cmd.add(FFMPEGArg.ARG_SIZE);
			cmd.add(out.width + "x" + out.height);
		
		}
		
		if (out.vcodec != null)
		{
			cmd.add("-vcodec");
			cmd.add(out.vcodec);
		}
		
		if (out.acodec != null)
		{
			cmd.add("-acodec");
			cmd.add(out.acodec);
		}
		
		
	//	cmd.add("-threads");
	//	cmd.add("8");
	//	cmd.add("-strict");
	//	cmd.add("experimental");
		cmd.add(out.path);

		execFFMPEG(cmd, sc);

    	
		
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
	
	private void killVideoProcessor ()
	{
		int killDelayMs = 300;

		String ffmpegBin = new File(context.getDir("bin",0),"ffmpeg").getAbsolutePath();

		int procId = -1;
		
		while ((procId = ShellUtils.findProcessId(ffmpegBin)) != -1)
		{
			
			Log.d(TAG, "Found PID=" + procId + " - killing now...");
			
			String[] cmd = { ShellUtils.SHELL_CMD_KILL + ' ' + procId + "" };
			
			try { 
			ShellUtils.doShellCommand(cmd,new ShellCallback ()
			{

				@Override
				public void shellOut(String msg) {
					// TODO Auto-generated method stub
					
				}
				
			}, false, false);
			Thread.sleep(killDelayMs); }
			catch (Exception e){}
		}
	}


	class StreamGobbler extends Thread
	{
	    InputStream is;
	    String type;
	    ShellCallback sc;
	    
	    StreamGobbler(InputStream is, String type, ShellCallback sc)
	    {
	        this.is = is;
	        this.type = type;
	        this.sc = sc;
	    }
	    
	    public void run()
	    {
	        try
	        {
	            InputStreamReader isr = new InputStreamReader(is);
	            BufferedReader br = new BufferedReader(isr);
	            String line=null;
	            while ( (line = br.readLine()) != null)
	            	sc.shellOut(line);
	                
	            } catch (IOException ioe)
	              {
	                Log.e(TAG,"error reading shell slog",ioe);
	              }
	    }
	}
}





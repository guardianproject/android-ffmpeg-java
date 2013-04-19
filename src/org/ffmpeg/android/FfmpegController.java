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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.ffmpeg.android.ShellUtils.ShellCallback;

import android.content.Context;
import android.util.Log;

public class FfmpegController {

	File fileBinDir;
	Context mContext;
	private String ffmpegBin;
	
	private final static String TAG = "FFMPEG";
	
	private File mFileTemp;
	
	public FfmpegController(Context context, File fileTemp) throws FileNotFoundException, IOException {
		mContext = context;
		mFileTemp = fileTemp;
		
		checkBinary ();
		
	}
	
	private void checkBinary () throws FileNotFoundException, IOException
	{
		fileBinDir = mContext.getDir("bin",0);

		long assetLength = mContext.getResources().openRawResourceFd(R.raw.ffmpeg).getLength();
		File fileBin = new File(fileBinDir,"ffmpeg");
		
		if (!fileBin.exists() || fileBin.length() != assetLength)
		{
			BinaryInstaller bi = new BinaryInstaller(mContext,fileBinDir);
			bi.installFromRaw();
		}
		
		ffmpegBin = fileBin.getAbsolutePath();

	}
	
	private void execFFMPEG (List<String> cmd, ShellCallback sc) throws IOException, InterruptedException {
	
		String ffmpegBin = new File(fileBinDir,"ffmpeg").getAbsolutePath();
		Runtime.getRuntime().exec("chmod 700 " +ffmpegBin);
    	
		execProcess (cmd, sc);
	}
	
	private int execProcess(List<String> cmds, ShellCallback sc) throws IOException, InterruptedException {		
        
		ProcessBuilder pb = new ProcessBuilder(cmds);
		pb.directory(fileBinDir);
		
		StringBuffer cmdlog = new StringBuffer();

		for (String cmd : cmds)
		{
			cmdlog.append(cmd);
			cmdlog.append(' ');
		}
		
		Log.v(TAG,cmdlog.toString());
		
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
        
        sc.processComplete(exitVal);
        
        return exitVal;


		
}
	
	public class FFMPEGArg
	{
		String key;
		String value;
		
		public static final String ARG_VIDEOCODEC = "-vcodec";
		public static final String ARG_AUDIOCODEC = "-acodec";
		
		public static final String ARG_VIDEOBITSTREAMFILTER = "-vbsf";
		public static final String ARG_AUDIOBITSTREAMFILTER = "-absf";
		
		public static final String ARG_VERBOSITY = "-v";
		public static final String ARG_FILE_INPUT = "-i";
		public static final String ARG_SIZE = "-s";
		public static final String ARG_FRAMERATE = "-r";
		public static final String ARG_FORMAT = "-f";
		public static final String ARG_BITRATE_VIDEO = "-b:v";
		
		public static final String ARG_BITRATE_AUDIO = "-b:a";
		public static final String ARG_CHANNELS_AUDIO = "-ac";
		public static final String ARG_FREQ_AUDIO = "-ar";
		
		public static final String ARG_STARTTIME = "-ss";
		public static final String ARG_DURATION = "-t";
		
		
	}
	
	public void processVideo(MediaDesc in, MediaDesc out, boolean enableExperimental, ShellCallback sc) throws Exception {
		
    	ArrayList<String> cmd = new ArrayList<String>();

		cmd.add(ffmpegBin);
		cmd.add("-y");
		
		if (in.format != null)
		{
			cmd.add(FFMPEGArg.ARG_FORMAT);
			cmd.add(in.format);
		}
	
		if (in.videoCodec != null)
		{
			cmd.add(FFMPEGArg.ARG_VIDEOCODEC);
			cmd.add(in.videoCodec);
		}
		
		if (in.audioCodec != null)
		{
			cmd.add(FFMPEGArg.ARG_AUDIOCODEC);
			cmd.add(in.audioCodec);
		}
		
		cmd.add("-i");
		cmd.add(in.path);
		
		if (out.videoBitrate > 0)
		{
			cmd.add(FFMPEGArg.ARG_BITRATE_VIDEO);
			cmd.add(out.videoBitrate + "k");
		}
		
		if (out.width > 0)
		{
			cmd.add(FFMPEGArg.ARG_SIZE);
			cmd.add(out.width + "x" + out.height);
		
		}
		if (out.videoFps != null)
		{
			cmd.add(FFMPEGArg.ARG_FRAMERATE);
			cmd.add(out.videoFps);
		}
		
		if (out.videoCodec != null)
		{
			cmd.add(FFMPEGArg.ARG_VIDEOCODEC);
			cmd.add(out.videoCodec);
		}
		
		if (out.videoBitStreamFilter != null)
		{
			cmd.add(FFMPEGArg.ARG_VIDEOBITSTREAMFILTER);
			cmd.add(out.videoBitStreamFilter);
		}
		
		
		if (out.videoFilter != null)
		{
			cmd.add("-vf");
			cmd.add(out.videoFilter);
		}
		
		if (out.audioCodec != null)
		{
			cmd.add(FFMPEGArg.ARG_AUDIOCODEC);
			cmd.add(out.audioCodec);
		}
		
		if (out.audioBitStreamFilter != null)
		{
			cmd.add(FFMPEGArg.ARG_AUDIOBITSTREAMFILTER);
			cmd.add(out.audioBitStreamFilter);
		}
		if (out.audioChannels > 0)
		{
			cmd.add(FFMPEGArg.ARG_CHANNELS_AUDIO);
			cmd.add(out.audioChannels+"");
		}
		
		if (out.audioBitrate > 0)
		{
			cmd.add(FFMPEGArg.ARG_BITRATE_AUDIO);
			cmd.add(out.audioBitrate + "k");
		}
		
		if (out.format != null)
		{
			cmd.add("-f");
			cmd.add(out.format);
		}
		
		if (enableExperimental)
		{
			cmd.add("-strict");
			cmd.add("-2");//experimental
		}
		
		cmd.add(out.path);

		execFFMPEG(cmd, sc);
	    
	}
	
	
	public MediaDesc createSlideshowFromImagesAndAudio (ArrayList<MediaDesc> images, MediaDesc audio, int width, int height, int durationPerSlide, String bitrate, String outPath, ShellCallback sc) throws Exception
	{

		final String imageBasePath = new File(mFileTemp,"image-").getAbsolutePath();
		final String imageBaseVariablePath = imageBasePath + "%03d.jpg";
		
		MediaDesc result = new MediaDesc ();

		result.path = outPath;
		
		ArrayList<String> cmd = new ArrayList<String>();
		
	
		String newImagePath = null;
		int imageCounter = 0;
		
		MediaDesc imageCover = images.get(0); //add the first image twice
		
		cmd = new ArrayList<String>();
		cmd.add(ffmpegBin);
		cmd.add("-y");
		
		cmd.add("-i");
		cmd.add(imageCover.path);
		
		if (width != -1 && height != -1)
		{
			cmd.add("-s");
			cmd.add(width + "x" + height);
		}
		
		newImagePath = imageBasePath + String.format("%03d", imageCounter++) + ".jpg";
		cmd.add(newImagePath);
		
		execFFMPEG(cmd, sc);
		
		for (MediaDesc image : images)
		{
			cmd = new ArrayList<String>();
			cmd.add(ffmpegBin);
			cmd.add("-y");
			
			cmd.add("-i");
			cmd.add(image.path);
			
			if (width != -1 && height != -1)
			{
				cmd.add("-s");
				cmd.add(width + "x" + height);
			}
			
			newImagePath = imageBasePath + String.format("%03d", imageCounter++) + ".jpg";
			cmd.add(newImagePath);
			
			execFFMPEG(cmd, sc);
			
			
		}
		
		//then combine them
		cmd = new ArrayList<String>();
		
		cmd.add(ffmpegBin);
		cmd.add("-y");
		
		cmd.add("-loop");
		cmd.add("0");
		
		cmd.add("-f");
		cmd.add("image2");
		
		cmd.add("-r");
		cmd.add("1/" + durationPerSlide);
		
		cmd.add("-i");
		cmd.add(imageBaseVariablePath);
		
		cmd.add("-strict");
		cmd.add("-2");//experimental
	
		String fileTempMpg = new File(mFileTemp,"tmp.mpg").getAbsolutePath();
		
		cmd.add(fileTempMpg);
		
		execFFMPEG(cmd, sc);
		
		//now combine and encode
		cmd = new ArrayList<String>();
		
		cmd.add(ffmpegBin);
		cmd.add("-y");
		
		cmd.add("-i");
		cmd.add(fileTempMpg);
		
		if (audio != null && audio.path != null)
		{
			cmd.add("-i");
			cmd.add(audio.path);
			
			cmd.add("-map");
			cmd.add("0:0");
			
			cmd.add("-map");
			cmd.add("1:0");
			
			cmd.add(FFMPEGArg.ARG_AUDIOCODEC);
			cmd.add("aac");
			
			cmd.add(FFMPEGArg.ARG_BITRATE_AUDIO);
			cmd.add("128k");
			
		}
		
		cmd.add("-strict");
		cmd.add("-2");//experimental
		
		cmd.add(FFMPEGArg.ARG_VIDEOCODEC);
		cmd.add("libx264");

		cmd.add(FFMPEGArg.ARG_BITRATE_VIDEO);
		cmd.add(bitrate);
		
		cmd.add(result.path);
		
		
		execFFMPEG(cmd, sc);
		
		return result;
	}
	
	/*
	 * ffmpeg -y -loop 0 -f image2 -r 0.5 -i image-%03d.jpg -s:v 1280x720 -b:v 1M \
   -i soundtrack.mp3 -t 01:05:00 -map 0:0 -map 1:0 out.avi
   
   -loop_input – loops the images. Disable this if you want to stop the encoding when all images are used or the soundtrack is finished.

-r 0.5 – sets the framerate to 0.5, which means that each image will be shown for 2 seconds. Just take the inverse, for example if you want each image to last for 3 seconds, set it to 0.33.

-i image-%03d.jpg – use these input files. %03d means that there will be three digit numbers for the images.

-s 1280x720 – sets the output frame size.

-b 1M – sets the bitrate. You want 500MB for one hour, which equals to 4000MBit in 3600 seconds, thus a bitrate of approximately 1MBit/s should be sufficient.

-i soundtrack.mp3 – use this soundtrack file. Can be any format.

-t 01:05:00 – set the output length in hh:mm:ss format.

out.avi – create this output file. Change it as you like, for example using another container like MP4.
	 */
	public MediaDesc combineAudioAndVideo (MediaDesc videoIn, MediaDesc audioIn, MediaDesc out, ShellCallback sc) throws Exception
	{
		ArrayList<String> cmd = new ArrayList<String>();

		cmd.add(ffmpegBin);
		cmd.add("-y");
		
		cmd.add("-shortest");
		
		cmd.add("-i");
		cmd.add(audioIn.path);
		
		cmd.add("-i");
		cmd.add(videoIn.path);
		
		cmd.add(FFMPEGArg.ARG_AUDIOCODEC);
		if (out.audioCodec != null)
			cmd.add(out.audioCodec);
		else
			cmd.add("copy");

		cmd.add(FFMPEGArg.ARG_VIDEOCODEC);
		if (out.videoCodec != null)
			cmd.add(out.videoCodec);
		else
			cmd.add("copy");
		
		if (out.videoBitrate != -1)
		{
			cmd.add(FFMPEGArg.ARG_BITRATE_VIDEO);
			cmd.add(out.videoBitrate + "k");
		}
		
		if (out.audioBitrate != -1)
		{
			cmd.add(FFMPEGArg.ARG_BITRATE_AUDIO);
			cmd.add(out.audioBitrate + "k");
		}

		cmd.add("-strict");
		cmd.add("-2");//experimental

		cmd.add(out.path);
		
		execFFMPEG(cmd, sc);
		
		
		return out;
		
	}
	
	public MediaDesc convertImageToMP4 (MediaDesc mediaIn, int duration, String outPath, ShellCallback sc) throws Exception
	{
		MediaDesc result = new MediaDesc ();
		ArrayList<String> cmd = new ArrayList<String>();

		// ffmpeg -loop 1 -i IMG_1338.jpg -t 10 -r 29.97 -s 640x480 -qscale 5 test.mp4
		
		cmd = new ArrayList<String>();
		
		//convert images to MP4
		cmd.add(ffmpegBin);
		cmd.add("-y");
		
		cmd.add("-loop");
		cmd.add("1");
		
		cmd.add("-i");
		cmd.add(mediaIn.path);
		
		cmd.add(FFMPEGArg.ARG_FRAMERATE);
		cmd.add(mediaIn.videoFps);
		
		cmd.add("-t");
		cmd.add(duration + "");
		
		cmd.add("-qscale");
		cmd.add("5"); //a good value 1 is best 30 is worst
		
		if (mediaIn.width != -1)
		{
			cmd.add(FFMPEGArg.ARG_SIZE);
			cmd.add(mediaIn.width + "x" + mediaIn.height);
		//	cmd.add("-vf");
		//	cmd.add("\"scale=-1:" + mediaIn.width + "\"");
		}
		
		if (mediaIn.videoBitrate != -1)
		{
			cmd.add(FFMPEGArg.ARG_BITRATE_VIDEO);
			cmd.add(mediaIn.videoBitrate + "");
		}
		
	
	//	-ar 44100 -acodec pcm_s16le -f s16le -ac 2 -i /dev/zero -acodec aac -ab 128k \ 
	//	-map 0:0 -map 1:0
		
		result.path = outPath;
		result.videoBitrate = mediaIn.videoBitrate;
		result.videoFps = mediaIn.videoFps;
		result.mimeType = "video/mp4";

		cmd.add(result.path);
		
		execFFMPEG(cmd, sc);
		
		return result;
	}
	
	//based on this gist: https://gist.github.com/3757344
	//ffmpeg -i input1.mp4 -vcodec copy -vbsf h264_mp4toannexb -acodec copy part1.ts
	//ffmpeg -i input2.mp4 -c copy -bsf:v h264_mp4toannexb -f mpegts intermediate2.ts

	public MediaDesc convertToMP4Stream (MediaDesc mediaIn, String outPath, ShellCallback sc) throws Exception
	{
		ArrayList<String> cmd = new ArrayList<String>();

		MediaDesc mediaOut = mediaIn.clone();
		
		String mediaPath = mediaIn.path;
		
		cmd = new ArrayList<String>();
		
		cmd.add(ffmpegBin);
		cmd.add("-y");
		cmd.add("-i");
		cmd.add(mediaPath);
		
		if (mediaIn.startTime != null)
		{
			cmd.add(FFMPEGArg.ARG_STARTTIME);
			cmd.add(mediaIn.startTime);
		}
		
		if (mediaIn.duration != null)
		{
			cmd.add(FFMPEGArg.ARG_DURATION);
			cmd.add(mediaIn.duration);
		}

		cmd.add("-c");
		cmd.add("copy");
	
		cmd.add(FFMPEGArg.ARG_VIDEOBITSTREAMFILTER);
		cmd.add("h264_mp4toannexb");
		
		cmd.add("-f");
		cmd.add("mpegts");
		
		mediaOut.path = outPath;
		
		cmd.add(mediaOut.path);

		execFFMPEG(cmd, sc);
		
		return mediaOut;
	}
	
	
	public MediaDesc convertToWaveAudio (MediaDesc mediaIn, String outPath, int sampleRate, int channels, ShellCallback sc) throws Exception
	{
		ArrayList<String> cmd = new ArrayList<String>();

		cmd.add(ffmpegBin);
		cmd.add("-y");
		cmd.add("-i");
		cmd.add(mediaIn.path);
		
		if (mediaIn.startTime != null)
		{
			cmd.add("-ss");
			cmd.add(mediaIn.startTime);
		}
		
		if (mediaIn.duration != null)
		{
			cmd.add("-t");
			cmd.add(mediaIn.duration);
		}
		
		cmd.add("-ar");
		cmd.add(sampleRate + "");
		
		cmd.add("-ac");
		cmd.add(channels + "");
		
		cmd.add("-vn");
		
		MediaDesc mediaOut = mediaIn.clone();
		mediaOut.path = outPath;
		
		cmd.add(mediaOut.path);

		execFFMPEG(cmd, sc);
		
		return mediaOut;
	}
	
	public MediaDesc convertTo3GPAudio (MediaDesc mediaIn, MediaDesc mediaOut, ShellCallback sc) throws Exception
	{
		ArrayList<String> cmd = new ArrayList<String>();

		cmd.add(ffmpegBin);
		cmd.add("-y");
		cmd.add("-i");
		cmd.add(mediaIn.path);
		
		if (mediaIn.startTime != null)
		{
			cmd.add("-ss");
			cmd.add(mediaIn.startTime);
		}
		
		if (mediaIn.duration != null)
		{
			cmd.add("-t");
			cmd.add(mediaIn.duration);
		}
		
		cmd.add("-vn");
		
		if (mediaOut.audioCodec != null)
		{
			cmd.add("-acodec");
			cmd.add(mediaOut.audioCodec);
		}
		
		if (mediaOut.audioBitrate != -1)
		{
			cmd.add("-ab");
			cmd.add(mediaOut.audioBitrate + "k");
		}
		
		cmd.add("-y");
		cmd.add("-strict");
		cmd.add("-2");
		
		cmd.add(mediaOut.path);

		execFFMPEG(cmd, sc);
		
		return mediaOut;
	}
	
	public MediaDesc convert (MediaDesc mediaIn, String outPath, ShellCallback sc) throws Exception
	{
		ArrayList<String> cmd = new ArrayList<String>();

		cmd.add(ffmpegBin);
		cmd.add("-y");
		cmd.add("-i");
		cmd.add(mediaIn.path);
		
		if (mediaIn.startTime != null)
		{
			cmd.add("-ss");
			cmd.add(mediaIn.startTime);
		}
		
		if (mediaIn.duration != null)
		{
			cmd.add("-t");
			cmd.add(mediaIn.duration);
		}
		
		
		MediaDesc mediaOut = mediaIn.clone();
		mediaOut.path = outPath;
		
		cmd.add(mediaOut.path);

		execFFMPEG(cmd, sc);
		
		return mediaOut;
	}
	
	public MediaDesc convertToMPEG (MediaDesc mediaIn, String outPath, ShellCallback sc) throws Exception
	{
		ArrayList<String> cmd = new ArrayList<String>();

		cmd.add(ffmpegBin);
		cmd.add("-y");
		cmd.add("-i");
		cmd.add(mediaIn.path);
		
		if (mediaIn.startTime != null)
		{
			cmd.add("-ss");
			cmd.add(mediaIn.startTime);
		}
		
		if (mediaIn.duration != null)
		{
			cmd.add("-t");
			cmd.add(mediaIn.duration);
		}
		

		//cmd.add("-strict");
		//cmd.add("experimental");
		
		//everything to mpeg
		cmd.add("-f");
		cmd.add("mpeg");
		
		MediaDesc mediaOut = mediaIn.clone();
		mediaOut.path = outPath + ".mpg";
		
		cmd.add(mediaOut.path);

		execFFMPEG(cmd, sc);
		
		return mediaOut;
	}
	
	public void concatAndTrimFilesMPEG (ArrayList<MediaDesc> videos,MediaDesc out, boolean preConvert, ShellCallback sc) throws Exception
	{
    	
		int idx = 0;
		
		if (preConvert)
		{
			for (MediaDesc mdesc : videos)
			{
				if (mdesc.path == null)
					continue;
			
				//extract MPG video
				ArrayList<String> cmd = new ArrayList<String>();
	
				cmd.add(ffmpegBin);
				cmd.add("-y");
				cmd.add("-i");
				cmd.add(mdesc.path);
				
				if (mdesc.startTime != null)
				{
					cmd.add("-ss");
					cmd.add(mdesc.startTime);
				}
				
				if (mdesc.duration != null)
				{
					cmd.add("-t");
					cmd.add(mdesc.duration);
				}
				
				/*
				cmd.add ("-acodec");
				cmd.add("pcm_s16le");
				
				cmd.add ("-vcodec");
				cmd.add("mpeg2video");
				*/
				
				if (out.audioCodec == null)
					cmd.add("-an"); //no audio
	
				//cmd.add("-strict");
				//cmd.add("experimental");
				
				//everything to mpeg
				cmd.add("-f");
				cmd.add("mpeg");
				cmd.add(out.path + '.' + idx + ".mpg");
	
				execFFMPEG(cmd, sc);
	
				idx++;
			}
		}
		
		StringBuffer cmdRun = new StringBuffer();
		
		cmdRun.append("cat ");
		
		idx = 0;
		
		for (MediaDesc vdesc : videos)
		{
			if (vdesc.path == null)
				continue;
	
			if (preConvert)
				cmdRun.append(out.path).append('.').append(idx++).append(".mpg").append(' '); //leave a space at the end!
			else
				cmdRun.append(vdesc.path).append(' ');
		}
		
		String mCatPath = out.path + ".full.mpg";
		
		cmdRun.append("> ");
		cmdRun.append(mCatPath);
		
		Log.d(TAG,"cat cmd: " + cmdRun.toString());
		
		String[] cmds = {"sh","-c",cmdRun.toString()};
		Runtime.getRuntime().exec(cmds).waitFor();
		
		
		MediaDesc mInCat = new MediaDesc();
		mInCat.path = mCatPath;
		
		processVideo(mInCat, out, false, sc);
		
		out.path = mCatPath;
	}
	
	public void extractAudio (MediaDesc mdesc, String audioFormat, File audioOutPath, ShellCallback sc) throws IOException, InterruptedException 
	{
		
		//no just extract the audio
		ArrayList<String> cmd = new ArrayList<String>();

		cmd.add(ffmpegBin);
		cmd.add("-y");
		cmd.add("-i");
		cmd.add(mdesc.path);
		
		cmd.add("-vn");
		
		if (mdesc.startTime != null)
		{
			cmd.add("-ss");
			cmd.add(mdesc.startTime);
		}
		
		if (mdesc.duration != null)
		{
			cmd.add("-t");
			cmd.add(mdesc.duration);
		}
		
					
		cmd.add("-f");
		cmd.add(audioFormat); //wav
		
		//everything to WAV!
		cmd.add(audioOutPath.getAbsolutePath());

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
	
	public int killVideoProcessor (boolean asRoot, boolean waitFor)
	{
		int killDelayMs = 300;

		String ffmpegBin = new File(mContext.getDir("bin",0),"ffmpeg").getAbsolutePath();

		int result = -1;
		
		int procId = -1;
		
		while ((procId = ShellUtils.findProcessId(ffmpegBin)) != -1)
		{
			
			Log.d(TAG, "Found PID=" + procId + " - killing now...");
			
			String[] cmd = { ShellUtils.SHELL_CMD_KILL + ' ' + procId + "" };
			
			try { 
			result = ShellUtils.doShellCommand(cmd,new ShellCallback ()
			{

				@Override
				public void shellOut(String msg) {
					
					Log.d(TAG,"Killing ffmpeg:" + msg);
					
				}

				@Override
				public void processComplete(int exitValue) {
					
					
				}
				
			}, asRoot, waitFor);
			Thread.sleep(killDelayMs); }
			catch (Exception e){}
		}
		
		return result;
	}


	public void concatAndTrimFilesMP4Stream (ArrayList<MediaDesc> videos,MediaDesc out, boolean mediaNeedsConversion, ShellCallback sc) throws Exception
	{
	
		StringBuffer sbCat = new StringBuffer();
		
		
		ArrayList<String> alCleanupPaths = new ArrayList<String>();
		
		int tmpIdx = 0;
		
		for (MediaDesc vdesc : videos)
		{
			File fileOut = new File(mFileTemp,tmpIdx + ".ts");
			MediaDesc mdOut = convertToMP4Stream(vdesc,fileOut.getAbsolutePath(), sc);		
			alCleanupPaths.add(mdOut.path);
			
			if (sbCat.length()>0)
				sbCat.append("|");
			
			sbCat.append(mdOut.path);
			tmpIdx++;
		}
		
		//ffmpeg -i "concat:intermediate1.ts|intermediate2.ts" -c copy -bsf:a aac_adtstoasc output.mp4
		ArrayList<String> cmd = new ArrayList<String>();

		cmd.add(ffmpegBin);
		cmd.add("-y");
		cmd.add("-i");
		cmd.add("\"concat:" + sbCat.toString() + "\"");
		
		cmd.add("-c");
		cmd.add("copy");
					
		cmd.add("-bsf:a");
		cmd.add("aac_adtstoasc");
		
		cmd.add(out.path);

		execFFMPEG(cmd, sc);
		
		
	}
	
	private void cleanup (ArrayList<String> pathsToDelete)
	{
		for (String path : pathsToDelete)
		{
			File fileDel = new File(path);
			fileDel.delete();
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
	            	if (sc != null)
	            		sc.shellOut(line);
	                
	            } catch (IOException ioe)
	              {
	                Log.e(TAG,"error reading shell slog",ioe);
	              }
	    }
	}
}

/*
 * Main options:
-L                  show license
-h                  show help
-?                  show help
-help               show help
--help              show help
-version            show version
-formats            show available formats
-codecs             show available codecs
-bsfs               show available bit stream filters
-protocols          show available protocols
-filters            show available filters
-pix_fmts           show available pixel formats
-sample_fmts        show available audio sample formats
-loglevel loglevel  set libav* logging level
-v loglevel         set libav* logging level
-debug flags        set debug flags
-report             generate a report
-f fmt              force format
-i filename         input file name
-y                  overwrite output files
-n                  do not overwrite output files
-c codec            codec name
-codec codec        codec name
-pre preset         preset name
-t duration         record or transcode "duration" seconds of audio/video
-fs limit_size      set the limit file size in bytes
-ss time_off        set the start time offset
-itsoffset time_off  set the input ts offset
-itsscale scale     set the input ts scale
-timestamp time     set the recording timestamp ('now' to set the current time)
-metadata string=string  add metadata
-dframes number     set the number of data frames to record
-timelimit limit    set max runtime in seconds
-target type        specify target file type ("vcd", "svcd", "dvd", "dv", "dv50", "pal-vcd", "ntsc-svcd", ...)
-xerror             exit on error
-frames number      set the number of frames to record
-tag fourcc/tag     force codec tag/fourcc
-filter filter_list  set stream filterchain
-stats              print progress report during encoding
-attach filename    add an attachment to the output file
-dump_attachment filename  extract an attachment into a file
-bsf bitstream_filters  A comma-separated list of bitstream filters
-dcodec codec       force data codec ('copy' to copy stream)

Advanced options:
-map file.stream[:syncfile.syncstream]  set input stream mapping
-map_channel file.stream.channel[:syncfile.syncstream]  map an audio channel from one stream to another
-map_meta_data outfile[,metadata]:infile[,metadata]  DEPRECATED set meta data information of outfile from infile
-map_metadata outfile[,metadata]:infile[,metadata]  set metadata information of outfile from infile
-map_chapters input_file_index  set chapters mapping
-benchmark          add timings for benchmarking
-dump               dump each input packet
-hex                when dumping packets, also dump the payload
-re                 read input at native frame rate
-loop_input         deprecated, use -loop
-loop_output        deprecated, use -loop
-vsync              video sync method
-async              audio sync method
-adrift_threshold threshold  audio drift threshold
-copyts             copy timestamps
-copytb source      copy input stream time base when stream copying
-shortest           finish encoding within shortest input
-dts_delta_threshold threshold  timestamp discontinuity delta threshold
-copyinkf           copy initial non-keyframes
-q q                use fixed quality scale (VBR)
-qscale q           use fixed quality scale (VBR)
-streamid streamIndex:value  set the value of an outfile streamid
-muxdelay seconds   set the maximum demux-decode delay
-muxpreload seconds  set the initial demux-decode delay
-fpre filename      set options from indicated preset file

Video options:
-vframes number     set the number of video frames to record
-r rate             set frame rate (Hz value, fraction or abbreviation)
-s size             set frame size (WxH or abbreviation)
-aspect aspect      set aspect ratio (4:3, 16:9 or 1.3333, 1.7777)
-bits_per_raw_sample number  set the number of bits per raw sample
-croptop size       Removed, use the crop filter instead
-cropbottom size    Removed, use the crop filter instead
-cropleft size      Removed, use the crop filter instead
-cropright size     Removed, use the crop filter instead
-padtop size        Removed, use the pad filter instead
-padbottom size     Removed, use the pad filter instead
-padleft size       Removed, use the pad filter instead
-padright size      Removed, use the pad filter instead
-padcolor color     Removed, use the pad filter instead
-vn                 disable video
-vcodec codec       force video codec ('copy' to copy stream)
-sameq              use same quantizer as source (implies VBR)
-same_quant         use same quantizer as source (implies VBR)
-pass n             select the pass number (1 or 2)
-passlogfile prefix  select two pass log file name prefix
-vf filter list     video filters
-b bitrate          video bitrate (please use -b:v)
-dn                 disable data

Advanced Video options:
-pix_fmt format     set pixel format
-intra              use only intra frames
-vdt n              discard threshold
-rc_override override  rate control override for specific intervals
-deinterlace        deinterlace pictures
-psnr               calculate PSNR of compressed frames
-vstats             dump video coding statistics to file
-vstats_file file   dump video coding statistics to file
-intra_matrix matrix  specify intra matrix coeffs
-inter_matrix matrix  specify inter matrix coeffs
-top                top=1/bottom=0/auto=-1 field first
-dc precision       intra_dc_precision
-vtag fourcc/tag    force video tag/fourcc
-qphist             show QP histogram
-force_fps          force the selected framerate, disable the best supported framerate selection
-force_key_frames timestamps  force key frames at specified timestamps
-vbsf video bitstream_filters  deprecated
-vpre preset        set the video options to the indicated preset

Audio options:
-aframes number     set the number of audio frames to record
-aq quality         set audio quality (codec-specific)
-ar rate            set audio sampling rate (in Hz)
-ac channels        set number of audio channels
-an                 disable audio
-acodec codec       force audio codec ('copy' to copy stream)
-vol volume         change audio volume (256=normal)
-rmvol volume       rematrix volume (as factor)

 */

/*
 * //./ffmpeg -y -i test.mp4 -vframes 999999  -vf 'redact=blurbox.txt [out] [d], [d]nullsink' -acodec copy outputa.mp4
    	
    	//ffmpeg -v 10 -y -i /sdcard/org.witness.sscvideoproto/videocapture1042744151.mp4 -vcodec libx264
    	//-b 3000k -s 720x480 -r 30 -acodec copy -f mp4 -vf 'redact=/data/data/org.witness.sscvideoproto/redact_unsort.txt'
    	///sdcard/org.witness.sscvideoproto/new.mp4
    	
    	//"-vf" , "redact=" + Environment.getExternalStorageDirectory().getPath() + "/" + PACKAGENAME + "/redact_unsort.txt",

    	
    	// Need to make sure this will create a legitimate mp4 file
    	//"-acodec", "ac3", "-ac", "1", "-ar", "16000", "-ab", "32k",

    	
    	String[] ffmpegCommand = {"/data/data/"+PACKAGENAME+"/ffmpeg", "-v", "10", "-y", "-i", recordingFile.getPath(), 
    					"-vcodec", "libx264", "-b", "3000k", "-vpre", "baseline", "-s", "720x480", "-r", "30",
    					//"-vf", "drawbox=10:20:200:60:red@0.5",
    					"-vf" , "\"movie="+ overlayImage.getPath() +" [logo];[in][logo] overlay=0:0 [out]\"",
    					"-acodec", "copy",
    					"-f", "mp4", savePath.getPath()+"/output.mp4"};
    	
    	
    	

//ffmpeg -i source-video.avi -s 480x320 -vcodec mpeg4 -acodec aac -ac 1 -ar 16000 -r 13 -ab 32000 -aspect 3:2 output-video.mp4/


 */


/* concat doesn't seem to work
cmd.add("-i");

StringBuffer concat = new StringBuffer();

for (int i = 0; i < videos.size(); i++)
{
	if (i > 0)
		concat.append("|");
	
	concat.append(out.path + '.' + i + ".wav");
	
}

cmd.add("concat:\"" + concat.toString() + "\"");
*/


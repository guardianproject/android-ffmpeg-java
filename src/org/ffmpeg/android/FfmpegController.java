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
import java.util.List;
import java.util.Locale;
import java.util.StringTokenizer;

import org.ffmpeg.android.ShellUtils.ShellCallback;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.util.Log;

public class FfmpegController {

	
	private String mFfmpegBin;
	
	private final static String TAG = "FFMPEG";
	
	private File mFileTemp;
	
	private String mCmdCat = "sh cat";
	
	public FfmpegController(Context context, File fileTemp) throws FileNotFoundException, IOException {		
		mFileTemp = fileTemp;
		
		installBinaries(context, false);
	}
	
	public void installBinaries(Context context, boolean overwrite)
	{
		mFfmpegBin = installBinary(context, R.raw.ffmpeg, "ffmpeg", overwrite);
	}
	
	public String getBinaryPath ()
	{
		return mFfmpegBin;
	}
	
	private static String installBinary(Context ctx, int resId, String filename, boolean upgrade) {
		try {
			File f = new File(ctx.getDir("bin", 0), filename);
			if (f.exists()) {
				f.delete();
			}
			copyRawFile(ctx, resId, f, "0755");
			return f.getCanonicalPath();
		} catch (Exception e) {
			Log.e(TAG, "installBinary failed: " + e.getLocalizedMessage());
			return null;
		}
	}
	
	/**
	 * Copies a raw resource file, given its ID to the given location
	 * @param ctx context
	 * @param resid resource id
	 * @param file destination file
	 * @param mode file permissions (E.g.: "755")
	 * @throws IOException on error
	 * @throws InterruptedException when interrupted
	 */
	private static void copyRawFile(Context ctx, int resid, File file, String mode) throws IOException, InterruptedException
	{
		final String abspath = file.getAbsolutePath();
		// Write the iptables binary
		final FileOutputStream out = new FileOutputStream(file);
		final InputStream is = ctx.getResources().openRawResource(resid);
		byte buf[] = new byte[1024];
		int len;
		while ((len = is.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		out.close();
		is.close();
		// Change the permissions
		Runtime.getRuntime().exec("chmod "+mode+" "+abspath).waitFor();
	}

	
	
	private void execFFMPEG (List<String> cmd, ShellCallback sc, File fileExec) throws IOException, InterruptedException {
	
		enablePermissions();
		
		execProcess (cmd, sc, fileExec);
	}
	
	private void enablePermissions () throws IOException
	{
		Runtime.getRuntime().exec("chmod 700 " + mFfmpegBin);
    	
	}
	
	private void execFFMPEG (List<String> cmd, ShellCallback sc) throws IOException, InterruptedException {
		execFFMPEG (cmd, sc, new File(mFfmpegBin).getParentFile());
	}
	
	private int execProcess(List<String> cmds, ShellCallback sc, File fileExec) throws IOException, InterruptedException {		
        
		//ensure that the arguments are in the correct Locale format
		for (String cmd :cmds)
		{
			cmd = String.format(Locale.US, "%s", cmd);
		}
		
		ProcessBuilder pb = new ProcessBuilder(cmds);
		pb.directory(fileExec);
		
		StringBuffer cmdlog = new StringBuffer();

		for (String cmd : cmds)
		{
			cmdlog.append(cmd);
			cmdlog.append(' ');
		}
		
		sc.shellOut(cmdlog.toString());
		
		//pb.redirectErrorStream(true);
		
		Process process = pb.start();    
    

		// any error message?
		StreamGobbler errorGobbler = new StreamGobbler(
				process.getErrorStream(), "ERROR", sc);

    	 // any output?
        StreamGobbler outputGobbler = new 
            StreamGobbler(process.getInputStream(), "OUTPUT", sc);

        errorGobbler.start();
        outputGobbler.start();

        int exitVal = process.waitFor();
        
        sc.processComplete(exitVal);
        
        return exitVal;
		
	}
	

	private int execProcess(String cmd, ShellCallback sc, File fileExec) throws IOException, InterruptedException {		
        
		//ensure that the argument is in the correct Locale format
		cmd = String.format(Locale.US, "%s", cmd);
		
		ProcessBuilder pb = new ProcessBuilder(cmd);
		pb.directory(fileExec);

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
	
	
	public class Argument
	{
		String key;
		String value;
		
		public static final String VIDEOCODEC = "-vcodec";
		public static final String AUDIOCODEC = "-acodec";
		
		public static final String VIDEOBITSTREAMFILTER = "-vbsf";
		public static final String AUDIOBITSTREAMFILTER = "-absf";
		
		public static final String VERBOSITY = "-v";
		public static final String FILE_INPUT = "-i";
		public static final String SIZE = "-s";
		public static final String FRAMERATE = "-r";
		public static final String FORMAT = "-f";
		public static final String BITRATE_VIDEO = "-b:v";
		
		public static final String BITRATE_AUDIO = "-b:a";
		public static final String CHANNELS_AUDIO = "-ac";
		public static final String FREQ_AUDIO = "-ar";
		
		public static final String STARTTIME = "-ss";
		public static final String DURATION = "-t";
		
		
	}
	
	public void processVideo(Clip in, Clip out, boolean enableExperimental, ShellCallback sc) throws Exception {
		
    	ArrayList<String> cmd = new ArrayList<String>();

		cmd.add(mFfmpegBin);
		cmd.add("-y");
		
		if (in.format != null)
		{
			cmd.add(Argument.FORMAT);
			cmd.add(in.format);
		}
	
		if (in.videoCodec != null)
		{
			cmd.add(Argument.VIDEOCODEC);
			cmd.add(in.videoCodec);
		}
		
		if (in.audioCodec != null)
		{
			cmd.add(Argument.AUDIOCODEC);
			cmd.add(in.audioCodec);
		}
		
		cmd.add("-i");
		cmd.add(new File(in.path).getCanonicalPath());
		
		if (out.videoBitrate > 0)
		{
			cmd.add(Argument.BITRATE_VIDEO);
			cmd.add(out.videoBitrate + "k");
		}
		
		if (out.width > 0)
		{
			cmd.add(Argument.SIZE);
			cmd.add(out.width + "x" + out.height);
		
		}
		if (out.videoFps != null)
		{
			cmd.add(Argument.FRAMERATE);
			cmd.add(out.videoFps);
		}
		
		if (out.videoCodec != null)
		{
			cmd.add(Argument.VIDEOCODEC);
			cmd.add(out.videoCodec);
		}
		
		if (out.videoBitStreamFilter != null)
		{
			cmd.add(Argument.VIDEOBITSTREAMFILTER);
			cmd.add(out.videoBitStreamFilter);
		}
		
		
		if (out.videoFilter != null)
		{
			cmd.add("-vf");
			cmd.add(out.videoFilter);
		}
		
		if (out.audioCodec != null)
		{
			cmd.add(Argument.AUDIOCODEC);
			cmd.add(out.audioCodec);
		}
		
		if (out.audioBitStreamFilter != null)
		{
			cmd.add(Argument.AUDIOBITSTREAMFILTER);
			cmd.add(out.audioBitStreamFilter);
		}
		if (out.audioChannels > 0)
		{
			cmd.add(Argument.CHANNELS_AUDIO);
			cmd.add(out.audioChannels+"");
		}
		
		if (out.audioBitrate > 0)
		{
			cmd.add(Argument.BITRATE_AUDIO);
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
		
		cmd.add(new File(out.path).getCanonicalPath());

		execFFMPEG(cmd, sc);
	    
	}
	
	
	public Clip createSlideshowFromImagesAndAudio (ArrayList<Clip> images, Clip audio,  Clip out, int durationPerSlide, ShellCallback sc) throws Exception
	{

		final String imageBasePath = new File(mFileTemp,"image-").getCanonicalPath();
		final String imageBaseVariablePath = imageBasePath + "%03d.jpg";
		
		
		ArrayList<String> cmd = new ArrayList<String>();
		
	
		String newImagePath = null;
		int imageCounter = 0;
		
		Clip imageCover = images.get(0); //add the first image twice
		
		cmd = new ArrayList<String>();
		cmd.add(mFfmpegBin);
		cmd.add("-y");
		
		cmd.add("-i");
		cmd.add(new File(imageCover.path).getCanonicalPath());
		
		if (out.width != -1 && out.height != -1)
		{
			cmd.add("-s");
			cmd.add(out.width + "x" + out.height);
		}
		
		newImagePath = imageBasePath + String.format(Locale.US, "%03d", imageCounter++) + ".jpg";
		cmd.add(newImagePath);
		
		execFFMPEG(cmd, sc);
		
		for (Clip image : images)
		{
			cmd = new ArrayList<String>();
			cmd.add(mFfmpegBin);
			cmd.add("-y");
			
			cmd.add("-i");
			cmd.add(new File(image.path).getCanonicalPath());
			
			if (out.width != -1 && out.height != -1)
			{
				cmd.add("-s");
				cmd.add(out.width + "x" + out.height);
			}
			
			newImagePath = imageBasePath + String.format(Locale.US, "%03d", imageCounter++) + ".jpg";
			cmd.add(newImagePath);
			
			execFFMPEG(cmd, sc);
			
			
		}
		
		//then combine them
		cmd = new ArrayList<String>();
		
		cmd.add(mFfmpegBin);
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
	
		String fileTempMpg = new File(mFileTemp,"tmp.mpg").getCanonicalPath();
		
		cmd.add(fileTempMpg);
		
		execFFMPEG(cmd, sc);
		
		//now combine and encode
		cmd = new ArrayList<String>();
		
		cmd.add(mFfmpegBin);
		cmd.add("-y");
		
		cmd.add("-i");
		cmd.add(fileTempMpg);
		
		if (audio != null && audio.path != null)
		{
			cmd.add("-i");
			cmd.add(new File(audio.path).getCanonicalPath());
			
			cmd.add("-map");
			cmd.add("0:0");
			
			cmd.add("-map");
			cmd.add("1:0");
			
			cmd.add(Argument.AUDIOCODEC);
			cmd.add("aac");
			
			cmd.add(Argument.BITRATE_AUDIO);
			cmd.add("128k");
			
		}
		
		cmd.add("-strict");
		cmd.add("-2");//experimental
		
		cmd.add(Argument.VIDEOCODEC);
		

		if (out.videoCodec != null)
			cmd.add(out.videoCodec);
		else
			cmd.add("mpeg4");

		if (out.videoBitrate != -1)
		{
			cmd.add(Argument.BITRATE_VIDEO);
			cmd.add(out.videoBitrate + "k");
		}
		
		cmd.add(new File(out.path).getCanonicalPath());
		
		
		execFFMPEG(cmd, sc);
		
		return out;
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
	public Clip combineAudioAndVideo (Clip videoIn, Clip audioIn, Clip out, ShellCallback sc) throws Exception
	{
		ArrayList<String> cmd = new ArrayList<String>();

		cmd.add(mFfmpegBin);
		cmd.add("-y");

		cmd.add("-i");
		cmd.add(new File(videoIn.path).getCanonicalPath());
		
		cmd.add("-i");
		cmd.add(new File(audioIn.path).getCanonicalPath());
		

		cmd.add("-strict");
		cmd.add("-2");//experimental
		
		cmd.add(Argument.AUDIOCODEC);
		if (out.audioCodec != null)
			cmd.add(out.audioCodec);
		else
		{
			cmd.add("copy");
			
		}

		cmd.add(Argument.VIDEOCODEC);
		if (out.videoCodec != null)
			cmd.add(out.videoCodec);
		else
		{
			cmd.add("copy");
		}
		
		if (out.videoBitrate != -1)
		{
			cmd.add(Argument.BITRATE_VIDEO);
			cmd.add(out.videoBitrate + "k");
		}
		
		if (out.videoFps != null)
		{
			cmd.add(Argument.FRAMERATE);
			cmd.add(out.videoFps);
		}
		
		if (out.audioBitrate != -1)
		{
			cmd.add(Argument.BITRATE_AUDIO);
			cmd.add(out.audioBitrate + "k");
		}
		cmd.add("-y");
		
		cmd.add("-cutoff");
		cmd.add("15000");
		
		if (out.width > 0)
		{
			cmd.add(Argument.SIZE);
			cmd.add(out.width + "x" + out.height);
		
		}
		
		if (out.format != null)
		{
			cmd.add("-f");
			cmd.add(out.format);
		}

		File fileOut = new File(out.path);
		cmd.add(fileOut.getCanonicalPath());
		
		execFFMPEG(cmd, sc);
		
		
		return out;
		
	}
	
	public Clip convertImageToMP4 (Clip mediaIn, int duration, String outPath, ShellCallback sc) throws Exception
	{
		Clip result = new Clip ();
		ArrayList<String> cmd = new ArrayList<String>();

		// ffmpeg -loop 1 -i IMG_1338.jpg -t 10 -r 29.97 -s 640x480 -qscale 5 test.mp4
		
		cmd = new ArrayList<String>();
		
		//convert images to MP4
		cmd.add(mFfmpegBin);
		cmd.add("-y");
		
		cmd.add("-loop");
		cmd.add("1");
		
		cmd.add("-i");
		cmd.add(new File(mediaIn.path).getCanonicalPath());
		
		cmd.add(Argument.FRAMERATE);
		cmd.add(mediaIn.videoFps);
		
		cmd.add("-t");
		cmd.add(duration + "");
		
		cmd.add("-qscale");
		cmd.add("5"); //a good value 1 is best 30 is worst
		
		if (mediaIn.width != -1)
		{
			cmd.add(Argument.SIZE);
			cmd.add(mediaIn.width + "x" + mediaIn.height);
		//	cmd.add("-vf");
		//	cmd.add("\"scale=-1:" + mediaIn.width + "\"");
		}
		
		if (mediaIn.videoBitrate != -1)
		{
			cmd.add(Argument.BITRATE_VIDEO);
			cmd.add(mediaIn.videoBitrate + "");
		}
		
	
	//	-ar 44100 -acodec pcm_s16le -f s16le -ac 2 -i /dev/zero -acodec aac -ab 128k \ 
	//	-map 0:0 -map 1:0
		
		result.path = outPath;
		result.videoBitrate = mediaIn.videoBitrate;
		result.videoFps = mediaIn.videoFps;
		result.mimeType = "video/mp4";

		cmd.add(new File(result.path).getCanonicalPath());
		
		execFFMPEG(cmd, sc);
		
		return result;
	}
	
	//based on this gist: https://gist.github.com/3757344
	//ffmpeg -i input1.mp4 -vcodec copy -vbsf h264_mp4toannexb -acodec copy part1.ts
	//ffmpeg -i input2.mp4 -c copy -bsf:v h264_mp4toannexb -f mpegts intermediate2.ts

	public Clip convertToMP4Stream (Clip mediaIn, String startTime, double duration, String outPath, ShellCallback sc) throws Exception
	{ 
		ArrayList<String> cmd = new ArrayList<String>();

		Clip mediaOut = new Clip();
		mediaOut.path = outPath;
		
		String mediaPath = new File(mediaIn.path).getCanonicalPath();
		
		cmd = new ArrayList<String>();
		
		cmd.add(mFfmpegBin);
		cmd.add("-y");

		if (startTime != null)
		{
			cmd.add(Argument.STARTTIME);
			cmd.add(startTime);
		}
		
		if (duration != -1)
		{
			cmd.add(Argument.DURATION);
			
			double dValue = mediaIn.duration;
			int hours = (int)(dValue/3600f);
			dValue -= (hours*3600);
			
			cmd.add("0");
			cmd.add(String.format(Locale.US,"%s",hours));
			cmd.add(":");
			
			int min = (int)(dValue/60f);
			dValue -= (min*60);
			
			cmd.add("0");
			cmd.add(String.format(Locale.US,"%s",min));
			cmd.add(":");
			
			cmd.add(String.format(Locale.US,"%f",dValue));
			
			//cmd.add("00:00:" + String.format(Locale.US,"%f",mediaIn.duration));
			
			
		}
		
		cmd.add("-i");
		cmd.add(mediaPath);
		
		cmd.add("-f");
		cmd.add("mpegts");
		
		cmd.add("-c");
		cmd.add("copy");
		
		cmd.add("-an");
		
		//cmd.add(Argument.VIDEOBITSTREAMFILTER);
		cmd.add("-bsf:v");
		cmd.add("h264_mp4toannexb");
		
		File fileOut = new File(mediaOut.path);
		mediaOut.path = fileOut.getCanonicalPath();
		
		cmd.add(mediaOut.path);

		execFFMPEG(cmd, sc);
		
		return mediaOut;
	}
	
	
	public Clip convertToWaveAudio (Clip mediaIn, String outPath, int sampleRate, int channels, ShellCallback sc) throws Exception
	{
		ArrayList<String> cmd = new ArrayList<String>();

		cmd.add(mFfmpegBin);
		cmd.add("-y");
		
		if (mediaIn.startTime != null)
		{
			cmd.add("-ss");
			cmd.add(mediaIn.startTime);
		}
		
		if (mediaIn.duration != -1)
		{
			cmd.add("-t");
			cmd.add(String.format(Locale.US,"%f",mediaIn.duration));
		}
		
		cmd.add("-i");
		cmd.add(new File(mediaIn.path).getCanonicalPath());
		
		
		cmd.add("-ar");
		cmd.add(sampleRate + "");
		
		cmd.add("-ac");
		cmd.add(channels + "");
		
		cmd.add("-vn");
		
		Clip mediaOut = new Clip();
		
		File fileOut = new File(outPath);
		mediaOut.path = fileOut.getCanonicalPath();
		
		cmd.add(mediaOut.path);

		execFFMPEG(cmd, sc);
		
		return mediaOut;
	}
	
	public Clip convertTo3GPAudio (Clip mediaIn, Clip mediaOut, ShellCallback sc) throws Exception
	{
		ArrayList<String> cmd = new ArrayList<String>();

		cmd.add(mFfmpegBin);
		cmd.add("-y");
		cmd.add("-i");
		cmd.add(new File(mediaIn.path).getCanonicalPath());
		
		if (mediaIn.startTime != null)
		{
			cmd.add("-ss");
			cmd.add(mediaIn.startTime);
		}
		
		if (mediaIn.duration != -1)
		{
			cmd.add("-t");
			cmd.add(String.format(Locale.US,"%f",mediaIn.duration));

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
		
		cmd.add("-strict");
		cmd.add("-2");

		File fileOut = new File(mediaOut.path);
		
		cmd.add(fileOut.getCanonicalPath());

		execFFMPEG(cmd, sc);
		
		return mediaOut;
	}
	
	public Clip convert (Clip mediaIn, String outPath, ShellCallback sc) throws Exception
	{
		ArrayList<String> cmd = new ArrayList<String>();

		cmd.add(mFfmpegBin);
		cmd.add("-y");
		cmd.add("-i");
		cmd.add(new File(mediaIn.path).getCanonicalPath());
		
		if (mediaIn.startTime != null)
		{
			cmd.add("-ss");
			cmd.add(mediaIn.startTime);
		}
		
		if (mediaIn.duration != -1)
		{
			cmd.add("-t");
			cmd.add(String.format(Locale.US,"%f",mediaIn.duration));

		}
		
		
		Clip mediaOut = new Clip();
		

		File fileOut = new File(outPath);
		 
		mediaOut.path = fileOut.getCanonicalPath();
		
		cmd.add(mediaOut.path);

		execFFMPEG(cmd, sc);
		
		return mediaOut;
	}
	
	public Clip convertToMPEG (Clip mediaIn, String outPath, ShellCallback sc) throws Exception
	{
		ArrayList<String> cmd = new ArrayList<String>();

		cmd.add(mFfmpegBin);
		cmd.add("-y");
		cmd.add("-i");
		cmd.add(new File(mediaIn.path).getCanonicalPath());
		
		if (mediaIn.startTime != null)
		{
			cmd.add("-ss");
			cmd.add(mediaIn.startTime);
		}
		
		if (mediaIn.duration != -1)
		{
			cmd.add("-t");
			cmd.add(String.format(Locale.US,"%f",mediaIn.duration));

		}
		

		//cmd.add("-strict");
		//cmd.add("experimental");
		
		//everything to mpeg
		cmd.add("-f");
		cmd.add("mpeg");
		
		Clip mediaOut = mediaIn.clone();
		
		File fileOut = new File(outPath);
		
		mediaOut.path = fileOut.getCanonicalPath();
		
		cmd.add(mediaOut.path);

		execFFMPEG(cmd, sc);
		
		return mediaOut;
	}
	
	public void concatAndTrimFilesMPEG (ArrayList<Clip> videos,Clip out, boolean preConvert, ShellCallback sc) throws Exception
	{
    	
		int idx = 0;
		
		if (preConvert)
		{
			for (Clip mdesc : videos)
			{
				if (mdesc.path == null)
					continue;
			
				//extract MPG video
				ArrayList<String> cmd = new ArrayList<String>();
	
				cmd.add(mFfmpegBin);
				cmd.add("-y");
				cmd.add("-i");
				cmd.add(mdesc.path);
				
				if (mdesc.startTime != null)
				{
					cmd.add("-ss");
					cmd.add(mdesc.startTime);
				}
				
				if (mdesc.duration != -1)
				{
					cmd.add("-t");
					cmd.add(String.format(Locale.US,"%f",mdesc.duration));

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
		
		cmdRun.append(mCmdCat);
		
		idx = 0;
		
		for (Clip vdesc : videos)
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
		
		String[] cmds = {"sh","-c",cmdRun.toString()};
		Runtime.getRuntime().exec(cmds).waitFor();
		
		
		Clip mInCat = new Clip();
		mInCat.path = mCatPath;
		
		processVideo(mInCat, out, false, sc);
		
		out.path = mCatPath;
	}
	
	public void extractAudio (Clip mdesc, String audioFormat, File audioOutPath, ShellCallback sc) throws IOException, InterruptedException 
	{
		
		//no just extract the audio
		ArrayList<String> cmd = new ArrayList<String>();

		cmd.add(mFfmpegBin);
		cmd.add("-y");
		cmd.add("-i");
		cmd.add(new File(mdesc.path).getCanonicalPath());
		
		cmd.add("-vn");
		
		if (mdesc.startTime != null)
		{
			cmd.add("-ss");
			cmd.add(mdesc.startTime);
		}
		
		if (mdesc.duration != -1)
		{
			cmd.add("-t");
			cmd.add(String.format(Locale.US,"%f",mdesc.duration));

		}
					
		cmd.add("-f");
		cmd.add(audioFormat); //wav
		
		//everything to WAV!
		cmd.add(audioOutPath.getCanonicalPath());

		execFFMPEG(cmd, sc);
		
	}
	
	private class FileMover {

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
	
	public int killVideoProcessor (boolean asRoot, boolean waitFor) throws IOException
	{
		int killDelayMs = 300;

		int result = -1;
		
		int procId = -1;
		
		while ((procId = ShellUtils.findProcessId(mFfmpegBin)) != -1)
		{
			
		//	Log.d(TAG, "Found PID=" + procId + " - killing now...");
			
			String[] cmd = { ShellUtils.SHELL_CMD_KILL + ' ' + procId + "" };
			
			try { 
			result = ShellUtils.doShellCommand(cmd,new ShellCallback ()
			{

				@Override
				public void shellOut(String msg) {
					
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


	public Clip trim (Clip mediaIn, boolean withSound, String outPath, ShellCallback sc) throws Exception
	{
		ArrayList<String> cmd = new ArrayList<String>();

		Clip mediaOut = new Clip();
		
		String mediaPath = mediaIn.path;
		
		cmd = new ArrayList<String>();
		
		cmd.add(mFfmpegBin);
		cmd.add("-y");
				
		if (mediaIn.startTime != null)
		{
			cmd.add(Argument.STARTTIME);
			cmd.add(mediaIn.startTime);
		}
		
		if (mediaIn.duration != -1)
		{
			cmd.add("-t");
			cmd.add(String.format(Locale.US,"%f",mediaIn.duration));

		}

		cmd.add("-i");
		cmd.add(mediaPath);

		if (!withSound)
			cmd.add("-an");
		
		cmd.add("-strict");
		cmd.add("-2");//experimental
	
		mediaOut.path = outPath;
		
		cmd.add(mediaOut.path);

		execFFMPEG(cmd, sc);
		
		return mediaOut;
	}
		
	public void concatAndTrimFilesMP4Stream (ArrayList<Clip> videos,Clip out, boolean preconvertClipsToMP4, boolean useCatCmd, ShellCallback sc) throws Exception
	{
	

		File fileExportOut = new File(out.path);
		
		StringBuffer sbCat = new StringBuffer();
		
		int tmpIdx = 0;
		
		
		for (Clip vdesc : videos)
		{
			
			Clip mdOut = null;
			
			if (preconvertClipsToMP4)
			{
				File fileOut = new File(mFileTemp,tmpIdx + "-trim.mp4");
				if (fileOut.exists())
					fileOut.delete();
				
				boolean withSound = false;
				
				mdOut = trim(vdesc,withSound,fileOut.getCanonicalPath(), sc);
			
				fileOut = new File(mFileTemp,tmpIdx + ".ts");
				if (fileOut.exists())
					fileOut.delete();
				
				mdOut = convertToMP4Stream(mdOut,null,-1,fileOut.getCanonicalPath(), sc);		
			}
			else
			{
				File fileOut = new File(mFileTemp,tmpIdx + ".ts");
				if (fileOut.exists())
					fileOut.delete();
				mdOut = convertToMP4Stream(vdesc,vdesc.startTime,vdesc.duration,fileOut.getCanonicalPath(), sc);		
			}
			
			if (mdOut != null)
			{
				if (sbCat.length()>0)
					sbCat.append("|");
				
				sbCat.append(new File(mdOut.path).getCanonicalPath());
				tmpIdx++;
			}
		}

		File fileExportOutTs = new File(fileExportOut.getCanonicalPath() + ".ts");
		
		if (useCatCmd)
		{

			//cat 0.ts 1.ts > foo.ts
			StringBuffer cmdBuff = new StringBuffer();
			
			cmdBuff.append(mCmdCat);
			cmdBuff.append(" ");
			
			StringTokenizer st = new StringTokenizer(sbCat.toString(),"|");
			
			while(st.hasMoreTokens())
				cmdBuff.append(st.nextToken()).append(" ");
			
			cmdBuff.append("> ");
			
			cmdBuff.append(fileExportOut.getCanonicalPath() + ".ts");

			Runtime.getRuntime().exec(cmdBuff.toString());

			ArrayList<String> cmd = new ArrayList<String>();
				
			cmd = new ArrayList<String>();
				
			cmd.add(mFfmpegBin);
			cmd.add("-y");
			cmd.add("-i");

			cmd.add(fileExportOut.getCanonicalPath() + ".ts");
			
			cmd.add("-c");
			cmd.add("copy");
			
			cmd.add("-an");
		
			cmd.add(fileExportOut.getCanonicalPath());
			
			execFFMPEG(cmd, sc, null);

			
		}
		else
		{
		
			//ffmpeg -i "concat:intermediate1.ts|intermediate2.ts" -c copy -bsf:a aac_adtstoasc output.mp4
			ArrayList<String> cmd = new ArrayList<String>();
	
			cmd.add(mFfmpegBin);
			cmd.add("-y");
			cmd.add("-i");
			cmd.add("concat:" + sbCat.toString());
			
			cmd.add("-c");
			cmd.add("copy");
			
			cmd.add("-an");
			
			cmd.add(fileExportOut.getCanonicalPath());
	
			execFFMPEG(cmd, sc);
			
		}
		
		if ((!fileExportOut.exists()) || fileExportOut.length() == 0)
		{
			throw new Exception("There was a problem rendering the video: " + fileExportOut.getCanonicalPath());
		}
		
		
	}
	
	public Clip getInfo (Clip in) throws IOException, InterruptedException
	{
		ArrayList<String> cmd = new ArrayList<String>();
		
		cmd = new ArrayList<String>();
			
		cmd.add(mFfmpegBin);
		cmd.add("-y");
		cmd.add("-i");

		cmd.add(new File(in.path).getCanonicalPath());
		
		InfoParser ip = new InfoParser(in);
		execFFMPEG(cmd,ip, null);

		try{Thread.sleep(200);}
		catch (Exception e){}
	
		
		return in;
		
	}
	
	private class InfoParser implements ShellCallback {
		
		private Clip mMedia;
		private int retValue;
		
		public InfoParser (Clip media)
		{
			mMedia = media;
		}

		@Override
		public void shellOut(String shellLine) {
			if (shellLine.contains("Duration:"))
			{
				
//		  Duration: 00:01:01.75, start: 0.000000, bitrate: 8184 kb/s

				String[] timecode = shellLine.split(",")[0].split(":");

				
				double duration = 0;
				
				duration = Double.parseDouble(timecode[1].trim())*60*60; //hours
				duration += Double.parseDouble(timecode[2].trim())*60; //minutes
				duration += Double.parseDouble(timecode[3].trim()); //seconds
				
				mMedia.duration = duration;
				
			
			}
			
			//   Stream #0:0(eng): Video: h264 (High) (avc1 / 0x31637661), yuv420p, 1920x1080, 16939 kb/s, 30.02 fps, 30 tbr, 90k tbn, 180k tbc
			else if (shellLine.contains(": Video:"))
			{
				String[] line = shellLine.split(":");
				String[] videoInfo = line[3].split(",");
				
				mMedia.videoCodec = videoInfo[0];
			}
			
			//Stream #0:1(eng): Audio: aac (mp4a / 0x6134706D), 48000 Hz, stereo, s16, 121 kb/s
			else if (shellLine.contains(": Audio:"))
			{
				String[] line = shellLine.split(":");
				String[] audioInfo = line[3].split(",");
				
				mMedia.audioCodec = audioInfo[0];
				
			}
			    
	
	//
    //Stream #0.0(und): Video: h264 (Baseline), yuv420p, 1280x720, 8052 kb/s, 29.97 fps, 90k tbr, 90k tbn, 180k tbc
    //Stream #0.1(und): Audio: mp2, 22050 Hz, 2 channels, s16, 127 kb/s
    
		}

		@Override
		public void processComplete(int exitValue) {
			retValue = exitValue;

		}
	}
	
	private class StreamGobbler extends Thread
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
	             //   Log.e(TAG,"error reading shell slog",ioe);
	            	ioe.printStackTrace();
	              }
	    }
	}
	
	public static Bitmap getVideoFrame(String videoPath,long frameTime) throws Exception {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        
        try {
            retriever.setDataSource(videoPath);      
            return retriever.getFrameAtTime(frameTime, MediaMetadataRetriever.OPTION_CLOSEST);
       
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
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


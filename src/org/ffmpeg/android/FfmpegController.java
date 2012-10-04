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

	String[] libraryAssets = {"ffmpeg"};
	File fileBinDir;
	Context context;
	private String ffmpegBin;
	
	private final static String TAG = "FFMPEG";

	public FfmpegController(Context _context) throws FileNotFoundException, IOException {
		context = _context;
		fileBinDir = context.getDir("bin",0);

		if (!new File(fileBinDir,libraryAssets[0]).exists())
		{
			BinaryInstaller bi = new BinaryInstaller(context,fileBinDir);
			bi.installFromRaw();
		}
		
		ffmpegBin = new File(fileBinDir,"ffmpeg").getAbsolutePath();

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
	
	public MediaDesc combineAudioAndVideo (MediaDesc videoIn, MediaDesc audioIn, String outPath, ShellCallback sc) throws Exception
	{
		MediaDesc result = new MediaDesc ();
		ArrayList<String> cmd = new ArrayList<String>();

		cmd.add(ffmpegBin);
		cmd.add("-y");
		
		cmd.add("-i");
		cmd.add(audioIn.path);
		
		cmd.add("-i");
		cmd.add(videoIn.path);
		
		
		cmd.add(FFMPEGArg.ARG_AUDIOCODEC);
		if (audioIn.audioCodec != null)
			cmd.add(audioIn.audioCodec);
		else
			cmd.add("copy");

		cmd.add(FFMPEGArg.ARG_VIDEOCODEC);
		if (videoIn.videoCodec != null)
			cmd.add(videoIn.videoCodec);
		else
			cmd.add("copy");
		
		//cmd.add(FFMPEGArg.ARG_VIDEOBITSTREAMFILTER);
		//cmd.add("h264_mp4toannexb");
		
		
		if (videoIn.videoBitrate != -1)
		{
			cmd.add(FFMPEGArg.ARG_BITRATE_VIDEO);
			cmd.add(videoIn.videoBitrate + "k");
		}
		
		if (audioIn.audioBitrate != -1)
		{
			cmd.add(FFMPEGArg.ARG_BITRATE_AUDIO);
			cmd.add(audioIn.audioBitrate + "k");
		}

		cmd.add("-strict");
		cmd.add("-2");//experimental
		
		result.path = outPath;
		cmd.add(result.path);
		
		execFFMPEG(cmd, sc);
		
		//ffmpeg -i audio.wav -i video.mp4 -acodec copy -vcodec copy output.mp4
		
		
		return result;
		
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
	public MediaDesc convertToMP4Stream (MediaDesc mediaIn, String outPath, boolean preconvertMP4, ShellCallback sc) throws Exception
	{
		ArrayList<String> cmd = new ArrayList<String>();

		MediaDesc mediaOut = mediaIn.clone();
		
		String mediaPath = mediaIn.path;
		
		if (preconvertMP4)
		{
			MediaDesc mediaOut2 = new MediaDesc();
			mediaOut2.path =  outPath + "-tmp.mp4";
			mediaOut2.audioCodec = "aac";
			mediaOut2.videoCodec = "libx264";
			mediaOut2.videoFps = "29.97";
			mediaOut2.videoBitrate = 1200;
			mediaOut2.audioBitrate = 128;
			processVideo(mediaIn, mediaOut2, true, sc);
			
			mediaPath = mediaOut2.path;
		}
		
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

		if (mediaIn.videoFilter == null)
		{
			cmd.add(FFMPEGArg.ARG_VIDEOCODEC);
			cmd.add("copy");
		}
		else
		{
			cmd.add(FFMPEGArg.ARG_VIDEOCODEC);
			cmd.add("libx264");
		
			cmd.add("-vf");
			cmd.add(mediaIn.videoFilter);
			
			if (mediaIn.videoBitrate != -1)
			{
				cmd.add(FFMPEGArg.ARG_BITRATE_VIDEO);
				cmd.add(mediaIn.videoBitrate + "k");
			}
		}
		
		cmd.add(FFMPEGArg.ARG_VIDEOBITSTREAMFILTER);
		cmd.add("h264_mp4toannexb");
		
		cmd.add(FFMPEGArg.ARG_AUDIOCODEC);
		cmd.add("copy");
		
		mediaOut.path = outPath + ".ts";
		
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
				//cmd.add("-an"); //no audio
	
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

	//	mInCat.format = "mpeg";
	//	mInCat.audioCodec = "mp2";
	//	mInCat.videoCodec = "mpeg1video";
		
		processVideo(mInCat, out, false, sc);
		
		out.path = mCatPath;
	}
	
	public void concatAndTrimFilesMP4Stream (ArrayList<MediaDesc> videos,MediaDesc out, boolean preConvert, ShellCallback sc) throws Exception
	{
    	
		StringBuffer cmdRun = new StringBuffer();
		
		cmdRun.append("cat ");
		
		for (MediaDesc vdesc : videos)
		{
			if (vdesc.path == null)
				continue;
	
			cmdRun.append(vdesc.path).append(' ');
		}
		
		String mCatPath = out.path + ".full.ts";
		
		cmdRun.append("> ");
		cmdRun.append(mCatPath);
		
		Log.d(TAG,"cat cmd: " + cmdRun.toString());
		
		String[] cmds = {"sh","-c",cmdRun.toString()};
		Runtime.getRuntime().exec(cmds).waitFor();
		
		MediaDesc mInCat = new MediaDesc();
		mInCat.path = mCatPath;
		
		if( out.videoFilter == null ) {
			out.videoCodec = "copy";
		}

		out.audioCodec = "copy";
		out.audioBitStreamFilter = "aac_adtstoasc";
		
		//ffmpeg -y -i parts.ts -acodec copy -absf aac_adtstoasc parts.mp4
		
		processVideo(mInCat, out, false, sc);
		
		//out.path = mCatPath;
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

		String ffmpegBin = new File(context.getDir("bin",0),"ffmpeg").getAbsolutePath();

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
					// TODO Auto-generated method stub
					
				}
				
			}, asRoot, waitFor);
			Thread.sleep(killDelayMs); }
			catch (Exception e){}
		}
		
		return result;
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

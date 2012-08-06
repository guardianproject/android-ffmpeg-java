package org.ffmpeg.android;

public class MediaDesc {

	public int width;
	public int height;
	
	public String videoCodec;
	public String videoFps;
	public int videoBitrate;
	
	public String audioCodec;
	public int audioChannels;
	public int audioBitrate;
	public String audioQuality;
	public int audioVolume;
	
	public String path;
	public String format;
	public String mimeType;
	
	public String startTime;
	public String duration;
	
	public String videoFilter;
	public String audioFilter;
	
	public String qscale;
	public String aspect;
	public int passCount = 1; //default
		
}

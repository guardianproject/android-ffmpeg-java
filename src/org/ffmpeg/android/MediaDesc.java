package org.ffmpeg.android;

public class MediaDesc implements Cloneable
{

	public int width;
	public int height;
	
	public String videoCodec;
	public String videoFps;
	public int videoBitrate;
	public String videoBitStreamFilter;
	
	public String audioCodec;
	public int audioChannels;
	public int audioBitrate;
	public String audioQuality;
	public int audioVolume;
	public String audioBitStreamFilter;
	
	public String path;
	public String format;
	public String mimeType;
	
	public String startTime; //00:00:00 format
	public String duration; //00:00:00 format
	
	public String videoFilter;
	public String audioFilter;
	
	public String qscale;
	public String aspect;
	public int passCount = 1; //default
		
	public MediaDesc clone ()  throws CloneNotSupportedException
	{
		return (MediaDesc)super.clone();
	}
}

package org.ffmpeg.android;

public class Clip implements Cloneable
{

	public int width = -1;
	public int height = -1;
	
	public String videoCodec;
	public String videoFps;
	public int videoBitrate = -1;
	public String videoBitStreamFilter;
	
	public String audioCodec;
	public int audioChannels = -1;
	public int audioBitrate = -1;
	public String audioQuality;
	public int audioVolume = -1;
	public String audioBitStreamFilter;
	
	public String path;
	public String format;
	public String mimeType;
	
	public String startTime; //00:00:00 or seconds format
	public double duration = -1; //00:00:00 or seconds format
	
	public String videoFilter;
	public String audioFilter;
	
	public String qscale;
	public String aspect;
	public int passCount = 1; //default
		
	public Clip ()
	{
		
	}
	
	public Clip (String path)
	{
		this.path = path;
	}
	
	public Clip clone ()  throws CloneNotSupportedException
	{
		return (Clip)super.clone();
	}

	public boolean isImage() {
		if (mimeType != null)
			return mimeType.startsWith("image");
		else
			return false;
	}

	public boolean isVideo() {
		if (mimeType != null)
			return mimeType.startsWith("video");
		else
			return false;
	}

	public boolean isAudio() {
		if (mimeType != null)
			return mimeType.startsWith("audio");
		else
			return false;
	}
}

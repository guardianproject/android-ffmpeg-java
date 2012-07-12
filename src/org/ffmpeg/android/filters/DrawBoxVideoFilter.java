package org.ffmpeg.android.filters;

public class DrawBoxVideoFilter extends VideoFilter {

	public int x;
	public int y;
	public int width;
	public int height;
	public String color;
	
	public DrawBoxVideoFilter (int x, int y, int width, int height, String color)
	{
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.color = color;
	}
	
	public String toString ()
	{
		return "drawbox=" + x + ':' + y + ':' + width + ':' + height + ':' + color;
	}
}

//+drawbox=@var{x}:@var{y}:@var{width}:@var{height}:@var{color}

//drawbox=10:20:200:60:red@@0.5

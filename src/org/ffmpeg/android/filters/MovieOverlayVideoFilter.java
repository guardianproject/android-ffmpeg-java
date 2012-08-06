package org.ffmpeg.android.filters;

import java.io.File;

public class MovieOverlayVideoFilter extends VideoFilter {

	private File fileMovieOverlay;
	
	public MovieOverlayVideoFilter (File fileMovieOverlay)
	{
		this.fileMovieOverlay = fileMovieOverlay;
	}
	
	public String toString ()
	{
		if (fileMovieOverlay != null)
			return "movie=" + fileMovieOverlay.getAbsolutePath() + " [logo];[in][logo] overlay=0:0 [out]\"";
		else
			return "";
				
	}
}

//"\"movie="+ overlayImage.getPath() +" [logo];[in][logo] overlay=0:0 [out]\"",
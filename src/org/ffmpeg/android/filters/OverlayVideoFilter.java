package org.ffmpeg.android.filters;

import java.io.File;

/**
 * @class overlay overlay one image or video on top of another
 * 
 * @desc x is the x coordinate of the overlayed video on the main video, 
 * y is the y coordinate. The parameters are expressions containing 
 * the following parameters:
 * <pre>
 *      	main_w, main_h
 *          main input width and height
 *
 *       W, H
 *           same as main_w and main_h
 *
 *       overlay_w, overlay_h
 *           overlay input width and height
 *
 *       w, h
 *           same as overlay_w and overlay_h
 * </pre>          
 * @examples
 * <pre>draw the overlay at 10 pixels from the bottom right
 * corner of the main video.
 * 		main_w-overlay_w-10
 * 		main_h-overlay_h-10
 * draw the overlay in the bottom left corner of the input
 *  10
 *  main_h-overlay_h-10 [out]</pre>
 *
 */
public class OverlayVideoFilter extends VideoFilter {

	public  File overlayFile;
	public  String xParam, yParam;
	
	public OverlayVideoFilter() {
		
	}
	
	public OverlayVideoFilter (File fileMovieOverlay, int x, int y)
	{
		this.overlayFile = fileMovieOverlay;
		this.xParam = Integer.toString(x);
		this.yParam = Integer.toString(y);		
	}
	
	public OverlayVideoFilter (File fileMovieOverlay, String xExpression, String yExpression)
	{
		this.overlayFile = fileMovieOverlay;
		this.xParam = xExpression;
		this.yParam = yExpression;
	}
	
	public String getFilterString ()
	{
		if (overlayFile != null)
			return "movie="
				  + overlayFile.getAbsolutePath()
				  + " [logo];[in][logo] "
				  + "overlay=" + xParam + ":" + yParam
				  + " [out]";
		else
			return "";
				
	}
}

//"\"movie="+ overlayImage.getPath() +" [logo];[in][logo] overlay=0:0 [out]\"",
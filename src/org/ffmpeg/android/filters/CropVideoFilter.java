package org.ffmpeg.android.filters;

public class CropVideoFilter extends VideoFilter {

	private String mOutWidth;
	private String mOutHeight;
	private String mX;
	private String mY;
	
	public CropVideoFilter (String width, String height, String x, String y)
	{
		mOutWidth = width;
		mOutHeight = height;
		mX = x;
		mY = y;
	}
	
	@Override
	public String getFilterString() {
		
		StringBuffer result = new StringBuffer();
		
		result.append("crop=");
		
		if (mOutWidth != null)
			result.append(mOutWidth).append(":");
		
		if (mOutHeight != null)
			result.append(mOutHeight).append(":");
		
		if (mX != null)
			result.append(mX).append(":");
		
		if (mY != null)
			result.append(mY).append(":");
		
		result.deleteCharAt(result.length()-1); //remove the last semicolon!
		
		return result.toString();
	}

}

/*
Crop the input video to out_w:out_h:x:y:keep_aspect

The keep_aspect parameter is optional, if specified and set to a non-zero value will force the output display aspect ratio to be the same of the input, by changing the output sample aspect ratio.

The out_w, out_h, x, y parameters are expressions containing the following constants:

‘x, y’
the computed values for x and y. They are evaluated for each new frame.

‘in_w, in_h’
the input width and height

‘iw, ih’
same as in_w and in_h

‘out_w, out_h’
the output (cropped) width and height

‘ow, oh’
same as out_w and out_h

‘a’
same as iw / ih

‘sar’
input sample aspect ratio

‘dar’
input display aspect ratio, it is the same as (iw / ih) * sar

‘hsub, vsub’
horizontal and vertical chroma subsample values. For example for the pixel format "yuv422p" hsub is 2 and vsub is 1.

‘n’
the number of input frame, starting from 0

‘pos’
the position in the file of the input frame, NAN if unknown

‘t’
timestamp expressed in seconds, NAN if the input timestamp is unknown

The out_w and out_h parameters specify the expressions for the width and height of the output (cropped) video. They are evaluated just at the configuration of the filter.

The default value of out_w is "in_w", and the default value of out_h is "in_h".

The expression for out_w may depend on the value of out_h, and the expression for out_h may depend on out_w, but they cannot depend on x and y, as x and y are evaluated after out_w and out_h.

The x and y parameters specify the expressions for the position of the top-left corner of the output (non-cropped) area. They are evaluated for each frame. If the evaluated value is not valid, it is approximated to the nearest valid value.

The default value of x is "(in_w-out_w)/2", and the default value for y is "(in_h-out_h)/2", which set the cropped area at the center of the input image.

The expression for x may depend on y, and the expression for y may depend on x.

Follow some examples:

 	
# crop the central input area with size 100x100
crop=100:100

# crop the central input area with size 2/3 of the input video
"crop=2/3*in_w:2/3*in_h"

# crop the input video central square
crop=in_h

# delimit the rectangle with the top-left corner placed at position
# 100:100 and the right-bottom corner corresponding to the right-bottom
# corner of the input image.
crop=in_w-100:in_h-100:100:100

# crop 10 pixels from the left and right borders, and 20 pixels from
# the top and bottom borders
"crop=in_w-2*10:in_h-2*20"

# keep only the bottom right quarter of the input image
"crop=in_w/2:in_h/2:in_w/2:in_h/2"

# crop height for getting Greek harmony
"crop=in_w:1/PHI*in_w"

# trembling effect
"crop=in_w/2:in_h/2:(in_w-out_w)/2+((in_w-out_w)/2)*sin(n/10):(in_h-out_h)/2 +((in_h-out_h)/2)*sin(n/7)"

# erratic camera effect depending on timestamp
"crop=in_w/2:in_h/2:(in_w-out_w)/2+((in_w-out_w)/2)*sin(t*10):(in_h-out_h)/2 +((in_h-out_h)/2)*sin(t*13)"

# set x depending on the value of y
"crop=in_w/2:in_h/2:y:10+10*sin(n/10)"
*/
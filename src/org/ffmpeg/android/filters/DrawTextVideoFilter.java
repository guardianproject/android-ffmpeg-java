package org.ffmpeg.android.filters;

import java.io.File;

public class DrawTextVideoFilter extends VideoFilter {

	private String mX;
	private String mY;
	private String mText;
	private String mFontColor;
	private int mFontSize;
	private File mFileFont;
	private int mBox;
	private String mBoxColor;
	
	public final static String X_CENTERED = "(w-text_w)/2";
	public final static String Y_CENTERED = "(h-text_h-line_h)/2";
	
	public final static String X_LEFT = "0";
	public final static String Y_BOTTOM = "(h-text_h-line_h)";
	
	public DrawTextVideoFilter (String text)
	{
		mX = X_CENTERED;
		mY = Y_CENTERED;
				
		mText = text;
		mFontColor = "white";
		mFontSize = 36;
		mFileFont = new File("/system/fonts/Roboto-Regular.ttf");
    	if (!mFileFont.exists())
    		mFileFont = new File("/system/fonts/DroidSerif-Regular.ttf");
    	
    	mBox = 1;
    	mBoxColor = "black@0.5";//0x00000000@1
    			
	}
	
	public DrawTextVideoFilter (String text, String x, String y, String fontColor, int fontSize, File fontFile, boolean showBox, String boxColor, String boxOpacity)
	{
		mX = x;
		mY = y;
		
		mText = text;
		mFontColor = fontColor;
		mFontSize = fontSize;
		
		mFileFont = fontFile;
    	
    	mBox = showBox? 1 : 0;
    	mBoxColor = boxColor + '@' + boxOpacity;
    			
	}
	
	@Override
	public String getFilterString() {
		
		StringBuffer result = new StringBuffer ();
		result.append("drawtext=");
		result.append("fontfile='").append(mFileFont.getAbsolutePath()).append("':");
		result.append("text='").append(mText).append("':");
		result.append("x=").append(mX).append(":");
		result.append("y=").append(mY).append(":");
		result.append("fontcolor=").append(mFontColor).append(":");
		result.append("fontsize=").append(mFontSize).append(":");
		result.append("box=").append(mBox).append(":");
		result.append("boxcolor=").append(mBoxColor);
		
		return result.toString();
	}

}

/*
 * 	//mdout.videoFilter = "drawtext=fontfile=/system/fonts/DroidSans.ttf: text='this is awesome':x=(w-text_w)/2:y=H-60 :fontcolor=white :box=1:boxcolor=0x00000000@1";
    			
    	File fontFile = new File("/system/fonts/Roboto-Regular.ttf");
    	if (!fontFile.exists())
    		fontFile = new File("/system/fonts/DroidSans.ttf");
    	
    	mdout.videoFilter = "drawtext=fontfile='" + fontFile.getAbsolutePath() + "':text='this is awesome':x=(main_w-text_w)/2:y=50:fontsize=24:fontcolor=white";
    	*/

/**

 
 /system/fonts

AndroidClock.ttf
AndroidClock_Highlight.ttf
AndroidClock_Solid.ttf
AndroidEmoji.ttf
AnjaliNewLipi-light.ttf
Clockopia.ttf
DroidNaskh-Regular-SystemUI.ttf
DroidNaskh-Regular.ttf
DroidSans-Bold.ttf
DroidSans.ttf
DroidSansArmenian.ttf
DroidSansDevanagari-Regular.ttf
DroidSansEthiopic-Regular.ttf
DroidSansFallback.ttf
DroidSansGeorgian.ttf
DroidSansHebrew-Bold.ttf
DroidSansHebrew-Regular.ttf
DroidSansMono.ttf
DroidSansTamil-Bold.ttf
DroidSansTamil-Regular.ttf
DroidSansThai.ttf
DroidSerif-Bold.ttf
DroidSerif-BoldItalic.ttf
DroidSerif-Italic.ttf
DroidSerif-Regular.ttf
Lohit-Bengali.ttf
Lohit-Kannada.ttf
Lohit-Telugu.ttf
MTLmr3m.ttf
Roboto-Bold.ttf
Roboto-BoldItalic.ttf
Roboto-Italic.ttf
Roboto-Light.ttf
Roboto-LightItalic.ttf
Roboto-Regular.ttf
RobotoCondensed-Bold.ttf
RobotoCondensed-BoldItalic.ttf
RobotoCondensed-Italic.ttf
RobotoCondensed-Regular.ttf
*/
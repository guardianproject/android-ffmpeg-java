package org.ffmpeg.android.filters;

public class FadeVideoFilter extends VideoFilter {

	private String mAction; //in our out
	private int mStart;
	private int mLength;
	
	public FadeVideoFilter (String action, int start, int length)
	{
		mAction = action;
		mStart = start;
		mLength = length;
	}
	
	@Override
	public String getFilterString() {
		
		StringBuffer result = new StringBuffer ();
		result.append("fade=");
		result.append(mAction).append(':').append(mStart).append(':').append(mLength);

		return result.toString();
	}

}

///fade=in:0:25, fade=out:975:25


package org.ffmpeg.android.filters;

/*
 * works for video and images
 * 0 = 90CounterCLockwise and Vertical Flip (default)
1 = 90Clockwise
2 = 90CounterClockwise
3 = 90Clockwise and Vertical Flip
 */
public class TransposeVideoFilter extends VideoFilter {

	private int mTranspose = -1;
	
	public final static int NINETY_COUNTER_CLOCKWISE_AND_VERTICAL_FLIP = 0;
	public final static int NINETY_CLOCKWISE = 1;
	public final static int NINETY_COUNTER_CLOCKWISE = 2;
	public final static int NINETY_CLOCKWISE_AND_VERTICAL_FLIP = 3;
	
	public TransposeVideoFilter (int transpose)
	{
		mTranspose = transpose;
	}
	
	@Override
	public String getFilterString() {
		
		return "transpose=" + mTranspose;
	}

}

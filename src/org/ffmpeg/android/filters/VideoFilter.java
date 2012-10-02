package org.ffmpeg.android.filters;

import java.util.ArrayList;
import java.util.Iterator;

public abstract class VideoFilter {

	public abstract String getFilterString ();
	
	public static String format (ArrayList<VideoFilter> listFilters)
	{
		StringBuffer result = new StringBuffer();
		
		Iterator<VideoFilter> it = listFilters.iterator();
		VideoFilter vf;
		
		while (it.hasNext())
		{
			vf = it.next();
			result.append(vf.getFilterString());
			
			if (it.hasNext())
				result.append(",");
		}
		return result.toString();
	}
}

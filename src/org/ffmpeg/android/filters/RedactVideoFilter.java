package org.ffmpeg.android.filters;

import java.io.File;

public class RedactVideoFilter extends VideoFilter {

	private File fileRedactList;
	
	public RedactVideoFilter (File fileRedactList)
	{
		this.fileRedactList = fileRedactList;
	}
	
	public String getFilterString ()
	{
		if (fileRedactList != null)
			return "redact=" + fileRedactList.getAbsolutePath();
		else
			return "";
				
	}
}

//redact=blurbox.txt [out] [d], [d]nullsink
//"redact=" + Environment.getExternalStorageDirectory().getPath() + "/" + PACKAGENAME + "/redact_unsort.txt",
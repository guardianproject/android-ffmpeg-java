package org.ffmpeg.android.test;

import java.io.File;

import org.ffmpeg.android.Clip;

public class Tests {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		
		
		
		String[] testpaths = {
			//	"/home/n8fr8/Desktop/smcampmovie",
			//	"/home/n8fr8/Desktop/smcampmovie2",
				"/home/n8fr8/Desktop/sm3"
				
		};
		
		int idx = -1;

		 double fadeLen = 1;
		 
		for (String testpath : testpaths)
		{
			idx++;
			
			System.out.println ("************************************");
			System.out.println ("CONCAT TEST: " + testpath);
			
			File fileVideoOutput = new File("/tmp/test" + idx + ".mp4");
			fileVideoOutput.delete();
			
			ConcatTest.test(testpath, "/tmp", fileVideoOutput.getCanonicalPath(), fadeLen);
			
			if (!fileVideoOutput.exists())
			{
				System.out.println("FAIL!! > output file did not get created: " + fileVideoOutput.getCanonicalPath());
				continue;
			}
			else
				System.out.println("SUCCESS!! > " + fileVideoOutput.getCanonicalPath());
		
			System.out.println ("************************************");
			System.out.println ("CROSSFADE TEST: " + testpath);
			
			File fileAudioOutput = new File("/tmp/test" + idx + ".3gp");
			fileAudioOutput.delete();
			CrossfadeTest.test(testpath, "/tmp", fileAudioOutput.getCanonicalPath(),fadeLen);
			if (!fileAudioOutput.exists())
			{
				System.out.println("FAIL!! > output file did not get created: " + fileAudioOutput.getCanonicalPath());
				continue;
			}
			else
				System.out.println("SUCCESS!! > " + fileAudioOutput.getCanonicalPath());
			
			System.out.println ("************************************");
			System.out.println ("MIX TEST: " + testpath);
			
			File fileMix = new File("/tmp/test" + idx + "mix.mp4");
			fileMix.delete();
			Clip clipMixOut = new Clip(fileMix.getCanonicalPath());
			MixTest.test("/tmp", fileVideoOutput.getCanonicalPath(), fileAudioOutput.getCanonicalPath(), clipMixOut);
			if (!fileMix.exists())
				System.out.println("FAIL!! > output file did not get created: " + fileMix.getCanonicalPath());
			else
				System.out.println("SUCCESS!! > " + fileMix.getCanonicalPath());
			
			
		}
		
		System.out.println("**********************");
		System.out.println("*******FIN**********");
		
	}

}

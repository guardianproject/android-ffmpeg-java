package org.ffmpeg.android.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Locale;

import net.sourceforge.sox.CrossfadeCat;
import net.sourceforge.sox.SoxController;

import org.ffmpeg.android.Clip;
import org.ffmpeg.android.FfmpegController;
import org.ffmpeg.android.ShellUtils;

public class CrossfadeTest {

	
	public static void test (String videoRoot, String fileTmpPath, String clipOutPath, double fadeLen) throws Exception
	{
		 File fileTmp = new File(fileTmpPath);
		 File fileAppRoot = new File("");
		 File fileVideoRoot = new File(videoRoot);
			
		 String fadeType = "l";
		 int sampleRate = 22050;
		 int channels = 1;
		 
		 FfmpegController ffmpegc = new FfmpegController (null, fileTmp);
		     
		 Clip clipOut = new Clip();
		 clipOut.path = clipOutPath;
		 clipOut.audioCodec="aac";
		 clipOut.audioBitrate=56;

		 
		 ArrayList<Clip> listVideos = new ArrayList<Clip>();
			
			String[] fileList = fileVideoRoot.list();
			for (String fileVideo : fileList)
			{
				if (fileVideo.endsWith("mp4"))
				{
					Clip clip = new Clip();
					clip.path = new File(fileVideoRoot,fileVideo).getCanonicalPath();
					//clip.startTime = "00:00:03";
					//clip.duration = "00:00:02";
					
					ffmpegc.getInfo(clip);
					
					//System.out.println("clip " + fileVideo + " duration=" + clip.duration);
					
					listVideos.add(clip);
					
				}
			}
		 
		 //now add 1 second cross fade to each audio file and cat them together
		 SoxController sxCon = new SoxController(null, fileAppRoot, new ShellUtils.ShellCallback() {
			
			@Override
			public void shellOut(String shellLine) {

			//	System.out.println("sxCon> " + shellLine);

			}
			
			@Override
			public void processComplete(int exitValue) {

				
				if (exitValue != 0)
				{
					System.err.println("sxCon> EXIT=" + exitValue);
					
					RuntimeException re = new RuntimeException("non-zero exit: " + exitValue);
					re.printStackTrace();
					throw re;
				}

			}
		});
		
		 ArrayList<Clip> alAudio = new ArrayList<Clip>();
		 
		 //convert each input file to a WAV so we can use Sox to process
		 int wavIdx = 0;
		 
		 for (Clip mediaIn : listVideos)
		 {
			if (new File(mediaIn.path).exists())
			{

				if (mediaIn.audioCodec == null)
				{
					//there is no audio track so let's generate silence
					
					
				}
				else
				{
			    	Clip audioOut = ffmpegc.convertToWaveAudio(mediaIn, new File(fileTmp, wavIdx+".wav").getCanonicalPath(),sampleRate,channels, new ShellUtils.ShellCallback() {
						
						@Override
						public void shellOut(String shellLine) {
							
						//	System.out.println("convertToWav> " + shellLine);
	
						}
						
						@Override
						public void processComplete(int exitValue) {
	
							if (exitValue != 0)
							{

								System.err.println("convertToWav> EXIT=" + exitValue);
								
								RuntimeException re = new RuntimeException("non-zero exit: " + exitValue);
								re.printStackTrace();
								throw re;
							}
						}
					});
			    	
			    	alAudio.add(audioOut);
			    	
			    	/*
		    		float duration = (float) sxCon.getLength(new File(audioOut.path).getCanonicalPath());
		    		
			    	if (mediaIn.duration == null)
			    	{	
			    		mediaIn.duration = String.format(Locale.US, "%f", duration);
			    	}*/
			    	ffmpegc.getInfo(mediaIn);
			    		
	
			    	wavIdx++;
				}
			}
			else
			{
				throw new FileNotFoundException(mediaIn.path);
			}
		 }

		 if (alAudio.size() > 0)
		 {
			 String fileOut = alAudio.get(0).path;

			 System.out.println("mix length=" + sxCon.getLength(fileOut));
			 	
			 for (int i = 1; i < alAudio.size(); i++)
			 {		
				 
				 File fileAdd = new File(alAudio.get(i).path);
				 				 
				 CrossfadeCat xCat = new CrossfadeCat(sxCon, fileOut, fileAdd.getCanonicalPath(), fadeLen, fileOut);
				 xCat.start();
				 
				 fileAdd.deleteOnExit();
				 
				 System.out.println("mix length=" + sxCon.getLength(fileOut));
				 
			 }
	
		       
			 //1 second fade in and fade out, t = triangle or linear
		       //String fadeLenStr = sxCon.formatTimePeriod(fadeLen);
		     

			 
			 String fadeFileOut = sxCon.fadeAudio(fileOut, fadeType, fadeLen, sxCon.getLength(fileOut)-fadeLen, fadeLen);
			 
			 //now export the final file to our requested output format		    mOut.mimeType = AppConstants.MimeTypes.MP4_AUDIO;
	
			 Clip mdFinalIn = new Clip();
			 mdFinalIn.path = fadeFileOut;
			 
			 
			 System.out.println ("final duration: " + sxCon.getLength(fadeFileOut));
			 
			 Clip exportOut = ffmpegc.convertTo3GPAudio(mdFinalIn, clipOut, new ShellUtils.ShellCallback() {
				
				@Override
				public void shellOut(String shellLine) {
					
					//System.out.println("convertTo3gp> " + shellLine);
				}
				
				@Override
				public void processComplete(int exitValue) {
	
					if (exitValue < 0)
					{
						RuntimeException re = new RuntimeException("non-zero exit: " + exitValue);
						re.printStackTrace();
						throw re;
					}
				}
			});
		 }
	}

}

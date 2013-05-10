package org.ffmpeg.android;

import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.util.Log;

public class MediaUtils {

	public static Bitmap getVideoFrame(String videoPath,long frameTime) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        
        try {
            retriever.setDataSource(videoPath);      
            return retriever.getFrameAtTime(frameTime, MediaMetadataRetriever.OPTION_CLOSEST);
        } catch (IllegalArgumentException ex) {
            Log.w("FFMPEG.MediaUtils", "illegal argument exception");
            
        } catch (RuntimeException ex) {
        	Log.w("FFMPEG.MediaUtils", "error getting video frame");
                                } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
            }
        }
        return null;
    }
	
}

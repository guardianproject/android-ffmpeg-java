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
            Log.e("MediaUtils", "error getting video frame", ex);
            
        } catch (RuntimeException ex) {
        	Log.e("MediaUtils", "error getting video frame", ex);
                                } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
            }
        }
        return null;
    }
	
}

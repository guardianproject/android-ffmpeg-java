package org.ffmpeg.android.test;

import java.io.File;
import java.util.ArrayList;

import org.ffmpeg.android.filters.CropVideoFilter;
import org.ffmpeg.android.filters.DrawBoxVideoFilter;
import org.ffmpeg.android.filters.DrawTextVideoFilter;
import org.ffmpeg.android.filters.FadeVideoFilter;
import org.ffmpeg.android.filters.TransposeVideoFilter;
import org.ffmpeg.android.filters.VideoFilter;

import android.app.Activity;
import android.content.Context;

public class FilterTest  {


	public void test (Context context)
	{
    	ArrayList<VideoFilter> listFilters = new ArrayList<VideoFilter>();

    	int height = 480;
    	int width = 720;
    	int lowerThird = height / 3;    	
    	DrawBoxVideoFilter vf = new DrawBoxVideoFilter(0,height-lowerThird,width,lowerThird,100,"blue",context);
    	
    	DrawTextVideoFilter vfTitle = 
    			new DrawTextVideoFilter("Rikshaw Rock and Roll",
    					DrawTextVideoFilter.X_CENTERED,DrawTextVideoFilter.Y_CENTERED,
    					"green",
    					38,
    					new File("/system/fonts/DroidSerif-Regular.ttf"),
    					true,
    					"yellow",
    					"0.5");
    	
    	float fps = 29.97f;
    	int fadeTime = (int)(fps*3);
    	//fades in first 3 seconds
    	FadeVideoFilter vfFadeIn = new FadeVideoFilter("in",0,fadeTime);
    	
    	//fades out last 50 frames
    	int totalFrames = (int)(14.37*29.97);
    	FadeVideoFilter vfFadeOut = new FadeVideoFilter("out",totalFrames-fadeTime,fadeTime);
    	
    	//crops video in 100 pixels on each side
    	CropVideoFilter vfCrop = new CropVideoFilter("in_w-100","in_h-100","100","100");
    	
    	//rotates video 90 degress clockwise
    	TransposeVideoFilter vfTranspose = new TransposeVideoFilter(TransposeVideoFilter.NINETY_CLOCKWISE);
    	
    	listFilters.add(vfTranspose);
    	listFilters.add(vfCrop);
    	listFilters.add(vfTitle);
    	listFilters.add(vfFadeIn);
    	listFilters.add(vfFadeOut);
    
	}
}

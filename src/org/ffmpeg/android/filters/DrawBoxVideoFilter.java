package org.ffmpeg.android.filters;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class DrawBoxVideoFilter extends OverlayVideoFilter {

	public int x;
	public int y;
	public int width;
	public int height;
	public String color;
	
	public DrawBoxVideoFilter (int x, int y, int width, int height, int alpha, String color, File tmpDir) throws Exception
	{
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.color = color;
		
		if( alpha < 0 || alpha > 255 ) {
			throw new IllegalArgumentException("Alpha must be an integer betweeen 0 and 255");
		}
		Paint paint = new Paint();
		paint.setAlpha(alpha);

	
		Bitmap bitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		bitmap.eraseColor(Color.parseColor(color));
		
		Bitmap temp_box = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(temp_box);
		canvas.drawBitmap(bitmap, 0, 0, paint);
		
		File outputFile;
		outputFile = File.createTempFile("box_"+width+height+color, ".png", tmpDir);
		FileOutputStream os = new FileOutputStream(outputFile);
		temp_box.compress(Bitmap.CompressFormat.PNG, 100, os);
		overlayFile = outputFile;
		xParam = Integer.toString(x);
		yParam = Integer.toString(y);
		
	}
}

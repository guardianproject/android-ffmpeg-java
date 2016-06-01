package com.code;

import android.app.Activity;
import android.graphics.Bitmap;
import android.media.MediaMetadataRetriever;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import org.ffmpeg.android.R;
import org.ffmpeg.android.test.MixTest;

import java.io.File;
import java.io.FileOutputStream;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * @author Chad
 * @title com.code
 * @description
 * @modifier
 * @date
 * @since 16/5/31 上午12:04
 **/
public class MainActivity extends Activity {

    @InjectView(R.id.text_mix)
    Button textMix;
    String mp4FilePath = "/storage/emulated/0/.mofunshow/movies/90331/20160520192149267651000519.mp4";
    String bgMp3FilePath = "/storage/emulated/0/.mofunshow/movies/90331/20160520191953876045000830.aac";
    String jsonFilePath = "/storage/emulated/0/.mofunshow/movies/90331/20160520192149267651000519.json";
    String bgWavFilePath = "/storage/emulated/0/.mofunshow/movies/90331/2015082214101800000358814.wav";
    String mp4OutPath = "/storage/emulated/0/.mofunshow/movies/90331/test_out.mp4";
    String mp4AllPath = "/storage/emulated/0/.mofunshow/movies/90331/test_out_all.mp4";
    String pngPath = "/storage/emulated/0/.mofunshow/movies/90331/1.png";
    String pngTempPath = "/storage/emulated/0/.mofunshow/movies/90331/temp.png";
    String tempPath = "/storage/emulated/0/ffmpeg/";
    @InjectView(R.id.test_merge)
    Button testMerge;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        ButterKnife.inject(this);
        new File(tempPath).mkdirs();
        File[] files = new File("/system/fonts/").listFiles();
        for (File file : files) {
            String fileName = file.getName().toLowerCase();
            if (fileName.contains("miui")) {
                if (fileName.toLowerCase().equals("miui-regular")) {
                    Config.SYSTEM_DEFAULT_FONT_PATH = file.getAbsolutePath();
                    break;
                }
                Config.SYSTEM_DEFAULT_FONT_PATH = file.getAbsolutePath();
                break;
            }
        }
        textMix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                List<DialogItem> dialog_list;
//                dialog_list = new Gson().fromJson(FileUtil.readJsonFile(jsonFilePath), new TypeToken<ArrayList<DialogItem>>() {}.getType());
//                try {
//                    MixTest.test(tempPath,mp4FilePath,bgMp3FilePath,new Clip(mp4OutPath),getApplicationContext());
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }

//                try {
//                    List<String> list = new ArrayList<String>();
//                    list.add(getImageFileFromVideo(mp4FilePath,pngTempPath));
//                    MixTest.testJpegToMp4(mp4FilePath,tempPath,list,mp4OutPath,getApplicationContext());
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
                try {
                    MixTest.testMakeLastFrameFilter(getImageFileFromVideo(mp4FilePath, pngTempPath), getTimeLengthFromVideo(mp4FilePath), mp4FilePath, tempPath, mp4OutPath, getApplicationContext());
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });
        testMerge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    MixTest.testMergeMp4(mp4FilePath, mp4OutPath, tempPath, mp4AllPath, getApplicationContext());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

    }

    public Bitmap getBitmapsFromVideo(String mp4FilePath) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(mp4FilePath);
        // 取得视频的长度(单位为毫秒)
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        // 取得视频的长度(单位为秒)
        int seconds = Integer.valueOf(time) / 1000;
        // 得到每一秒时刻的bitmap比如第一秒,第二秒
        bitmap = retriever.getFrameAtTime(seconds * 1000 * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
        return bitmap;
    }

    public long getTimeLengthFromVideo(String mp4FilePath) {
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(mp4FilePath);
        // 取得视频的长度(单位为毫秒)
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        // 取得视频的长度(单位为秒)
        int milins = Integer.valueOf(time);
        return milins;
    }

    public String getImageFileFromVideo(String mp4FilePath, String outPath) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(mp4FilePath);
        // 取得视频的长度(单位为毫秒)
        String time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
        // 取得视频的长度(单位为秒)
        int millSeconds = Integer.valueOf(time);
        int seconds = millSeconds / 1000;
        // 得到每一秒时刻的bitmap比如第一秒,第二秒
        bitmap = retriever.getFrameAtTime(millSeconds * 1000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC);
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(outPath);
            bitmap.compress(Bitmap.CompressFormat.PNG, 80, fos);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outPath;
    }
}

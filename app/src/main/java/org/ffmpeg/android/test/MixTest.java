package org.ffmpeg.android.test;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Environment;
import android.text.TextPaint;
import android.util.Log;

import com.coremedia.iso.boxes.Container;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;

import org.ffmpeg.android.Clip;
import org.ffmpeg.android.FfmpegController;
import org.ffmpeg.android.R;
import org.ffmpeg.android.ShellUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.LinkedList;
import java.util.List;

public class MixTest {

    public static void test(String fileTmpPath, String videoClipPath, String audioClipPath, final Clip clipOut, Context context) throws Exception {
        File fileTmp = new File(fileTmpPath);
        File fileAppRoot = new File("");

        final FfmpegController fc = new FfmpegController(context, fileTmp);

        final Clip clipVideo = new Clip(videoClipPath);
        //fc.getInfo(clipVideo);
        clipVideo.videoCodec = "mp4";
        final Clip clipAudio = new Clip(audioClipPath);
        //fc.getInfo(clipAudio);
        clipAudio.audioCodec = "aac";
        final Clip clipTemp = new Clip("/storage/emulated/0/.mofunshow/movies/90331/test_temp.mp4");
        final Clip clipPic = new Clip("/storage/emulated/0/.mofunshow/movies/90331/1.png");
//		fc.removeVideoAudio(clipVideo, clipTemp, new ShellUtils.ShellCallback() {
//			@Override
//			public void shellOut(String shellLine) {
//				System.out.println("removeVideoAudio> " + shellLine);
//			}
//
//			@Override
//			public void processComplete(int exitValue) {
//				if (exitValue != 0)
//					System.err.println("removeVideoAudio concat non-zero exit: " + exitValue);
//				try {
//					fc.combineAudioAndVideo(clipTemp, clipAudio, clipOut, new ShellUtils.ShellCallback() {
//
//                        @Override
//                        public void shellOut(String shellLine) {
//                            System.out.println("MIX> " + shellLine);
//                        }
//
//                        @Override
//                        public void processComplete(int exitValue) {
//
//                            if (exitValue != 0)
//                                System.err.println("combineAudioAndVideo concat non-zero exit: " + exitValue);
//                        }
//                    });
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//			}
//		});
//		fc.combineAudioAndVideo(clipVideo, clipAudio, clipOut, new ShellUtils.ShellCallback() {
//
//			@Override
//			public void shellOut(String shellLine) {
//				System.out.println("MIX> " + shellLine);
//			}
//
//			@Override
//			public void processComplete(int exitValue) {
//
//				if (exitValue != 0)
//					System.err.println("combineAudioAndVideo concat non-zero exit: " + exitValue);
//			}
//		});
//		fc.convertImageToMP4(clipPic, 3, "/storage/emulated/0/.mofunshow/movies/90331/test_temp.mp4", new ShellUtils.ShellCallback() {
//			@Override
//			public void shellOut(String shellLine) {
//				System.out.println("convertImageToMP4> " + shellLine);
//			}
//
//			@Override
//			public void processComplete(int exitValue) {
//				System.out.println("convertImageToMP4> complete");
//			}
//		});


    }

    public static void testJpegToMp4(final String mp4FilePath, String fileTmpPath, List<String> picFiles, final String clipOut, Context context) throws Exception {
        File fileTmp = new File(fileTmpPath);
        final FfmpegController fc = new FfmpegController(context, fileTmp);
        final String clipTemp = "/storage/emulated/0/.mofunshow/movies/90331/test_temp.mp4";
        String clipLogo = "/storage/emulated/0/.mofunshow/movies/90331/logo.png";
        final Clip clipPic = new Clip(picFiles.get(0));
        clipPic.height = 450;
        clipPic.width = 800;
        clipPic.videoBitrate = 256;

        Clip logo = new Clip(clipLogo);

        fc.convertImageToMP4(clipPic, logo, 2, clipOut, new ShellUtils.ShellCallback() {
            @Override
            public void shellOut(String shellLine) {
                System.out.println("convertImageToMP4> " + shellLine);
            }

            @Override
            public void processComplete(int exitValue) {
                System.out.println("convertImageToMP4> complete");
//				List<String> fileList = new ArrayList<String>();
//				fileList.add(mp4FilePath);
////				fileList.add(clipOut);
//				fileList.add(clipOut);
//				newClipMethod(fileList,clipTemp);
//				try {
//					fc.filterMp4Complex(new Clip(clipOut),  clipPic,clipTemp, new ShellUtils.ShellCallback() {
//                        @Override
//                        public void shellOut(String shellLine) {
//							System.out.println("filterMp4Complex> " + shellLine);
//                        }
//
//                        @Override
//                        public void processComplete(int exitValue) {
//							System.out.println("filterMp4Complex> complete");
//                        }
//                    });
//				} catch (Exception e) {
//					e.printStackTrace();
//				}
//                try {
//                    fc.mergMp4(new Clip(mp4FilePath), new Clip(clipOut), clipTemp, new ShellUtils.ShellCallback() {
//                        @Override
//                        public void shellOut(String shellLine) {
//                            System.out.println("mergMp4> " + shellLine);
//                        }
//
//                        @Override
//                        public void processComplete(int exitValue) {
//                            System.out.println("mergMp4> complete");
//                        }
//                    });
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
            }
        });


    }

    public static void testMergeMp4(FfmpegController fc, final String mp4FilePath, final String filterPath, String fileTmpPath, final String clipOut, Context context) throws Exception {
        File fileTmp = new File(fileTmpPath);

        String content = "file '" + mp4FilePath + "'\r\n"
                + "file '" + filterPath + "'";

        if (fc == null) {
            fc = new FfmpegController(context, fileTmp);
        }
        fc.mergMp4(saveConfig(content, fileTmpPath), clipOut, new ShellUtils.ShellCallback() {
            @Override
            public void shellOut(String shellLine) {
                System.out.println("mergMp4> " + shellLine);
            }

            @Override
            public void processComplete(int exitValue) {
                System.out.println("mergMp4> complete");
            }
        });


    }

    private static String saveConfig(String content, String tempPath) {
        String textName = "config.txt";
        writeTxtToFile(content, tempPath, textName);
        return tempPath + "/" + textName;

    }

    // 将字符串写入到文本文件中
    public static void writeTxtToFile(String strcontent, String filePath, String fileName) {
        //生成文件夹之后，再生成文件，不然会出错
        makeFilePath(filePath, fileName);

        String strFilePath = filePath + fileName;
        // 每次写入时，都换行写
        String strContent = strcontent + "\r\n";
        try {
            File file = new File(strFilePath);
            if (!file.exists()) {
                Log.d("TestFile", "Create the file:" + strFilePath);
                file.getParentFile().mkdirs();
                file.createNewFile();
            } else {
                file.delete();
            }
            RandomAccessFile raf = new RandomAccessFile(file, "rwd");
            raf.seek(file.length());
            raf.write(strContent.getBytes());
            raf.close();
        } catch (Exception e) {
            Log.e("TestFile", "Error on write File:" + e);
        }
    }

    // 生成文件
    public static File makeFilePath(String filePath, String fileName) {
        File file = null;
        makeRootDirectory(filePath);
        try {
            file = new File(filePath + fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return file;
    }

    // 生成文件夹
    public static void makeRootDirectory(String filePath) {
        File file = null;
        try {
            file = new File(filePath);
            if (!file.exists()) {
                file.mkdir();
            }
        } catch (Exception e) {
            Log.i("error:", e + "");
        }

    }

    public static void newClipMethod(List<String> fileList, String output) {
        List<Movie> moviesList = new LinkedList<Movie>();
        try {
            for (String file : fileList) {
                moviesList.add(MovieCreator.build(file));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<Track> videoTracks = new LinkedList<Track>();
        List<Track> audioTracks = new LinkedList<Track>();
        for (Movie m : moviesList) {
            for (Track t : m.getTracks()) {
                if (t.getHandler().equals("soun")) {
                    audioTracks.add(t);
                }
                if (t.getHandler().equals("vide")) {
                    videoTracks.add(t);
                }
            }
        }

        Movie result = new Movie();

        try {
            if (audioTracks.size() > 0) {
                result.addTrack(audioTracks.get(0));
            }
            if (videoTracks.size() > 0) {
                result.addTrack(new AppendTrack(videoTracks.toArray(new Track[videoTracks.size()])));
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        Container out = new DefaultMp4Builder().build(result);

        try {
            FileChannel fc = new FileOutputStream(new File(output)).getChannel();
            out.writeContainer(fc);
            fc.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        moviesList.clear();
        fileList.clear();

    }

    public static void testMakeLastFrameFilter(String lastFrame, long timeLength, final String mp4FilePath, final String fileTmpPath, final String clipOut, final Context context) throws Exception {
        File fileTmp = new File(fileTmpPath);
        float timeSecond = timeLength / 1000f;
        final FfmpegController fc = new FfmpegController(context, fileTmp, false);
        final String tempMp4 = fileTmpPath + "/" + "temp_filter.mp4";

        Bitmap bitmap = BitmapFactory.decodeFile(lastFrame);
        Clip logo = new Clip(createLogoWall("主演:香肠嘴猪八戒™の개암Athens❄️冰.", bitmap.getWidth(), context));

        Clip mp4ClipOrg = new Clip(mp4FilePath);
        mp4ClipOrg.width = bitmap.getWidth();
        mp4ClipOrg.height = bitmap.getHeight();
        mp4ClipOrg.frameRate = 24;
        fc.makeLastFrameFilter2(lastFrame, timeSecond - 2f, mp4ClipOrg, logo, 0.7f, tempMp4, new ShellUtils.ShellCallback() {
            @Override
            public void shellOut(String shellLine) {
                System.out.println("makeLastFrameFilter> " + shellLine);
            }

            @Override
            public void processComplete(int exitValue) {
                System.out.println("makeLastFrameFilter> complete");
                try {
                    testMergeMp4(fc, mp4FilePath, tempMp4, fileTmpPath, clipOut, context);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


    }

    public static Bitmap readBitmap(int resid, Context context) {
        BitmapFactory.Options opt = new BitmapFactory.Options();
        opt.inPreferredConfig = Bitmap.Config.ARGB_8888;
        opt.inPurgeable = true;
        opt.inInputShareable = true;
//获取资源图片
        InputStream input = context.getResources().openRawResource(resid);
        return BitmapFactory.decodeStream(input, null, opt);
    }

    private static String createLogoWall(String text, int fixWidth, Context context) {
        String outPath = Environment.getExternalStorageDirectory() + "/logo_user.png";
        new File(outPath).delete();
        Bitmap bitmap = getDrawBitMap(readBitmap(R.raw.end_wallpaper_eng, context), text, fixWidth);
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

    public static Bitmap getDrawBitMap(Bitmap bmp, String text, int fixWidth) {
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int newWidth = fixWidth;
        Bitmap newBitmap = Bitmap.createBitmap(newWidth, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(newBitmap);
        canvas.drawBitmap(bmp, (newWidth - width) / 2, 0, null);
        TextPaint textPaint = new TextPaint();
        textPaint.setAntiAlias(true);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(21.0F);
//        StaticLayout sl = new StaticLayout(text, textPaint, newBitmap.getWidth() - 8, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);
//        canvas.translate(width / 2 + 30, height / 2 + 15);
//        sl.draw(canvas);
        canvas.drawText(text, newWidth / 2 - 30, height / 2 + 30, textPaint);
        return newBitmap;
    }
}

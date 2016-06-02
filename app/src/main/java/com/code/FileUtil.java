package com.code;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Administrator on 13-8-19.
 */
public class FileUtil {


    public static boolean writeJsonFile(String file_name, String json_content) {
        FileOutputStream fout = null;

        try {
            fout = new FileOutputStream(file_name);
            byte[] bytes = json_content.getBytes();
            fout.write(bytes);

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            if (fout != null) {
                try {
                    fout.flush();
                    fout.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return true;
    }

    public static String readJsonFile(String file_name) {
        String json_content = "";
        try {
            FileInputStream fin = new FileInputStream(file_name);
            int length = fin.available();
            byte[] buffer = new byte[length];
            fin.read(buffer);
            json_content = new String(buffer, "UTF-8");
            fin.close();
        } catch (Exception e) {
            e.printStackTrace();
            return json_content;
        }
        return json_content;
    }
}

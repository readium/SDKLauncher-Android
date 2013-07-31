/**
 * file download implementation
 * 
 */
package org.readium.sdk.test.util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import android.os.Environment;

public class Util {
    private static String cachePath = "readium_test";
    
    public static final String getCachePath() {
        return cachePath;
    }

    public static final void setCachePath(String cachePath) {
        Util.cachePath = cachePath;
    }

    public static void download(String dLName) {
        String fileName = dLName;
        OutputStream output = null;
        
        boolean bookExist = false;
        
        try {
            String tempDL = URLEncoder.encode(dLName, "UTF-8").replace("+",
                    "%20");
            String temp = "http://192.168.66.254/incoming/epub/test/data/"
                    + tempDL;
            URL url = new URL(temp);
            
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            String SDCard = Environment.getExternalStorageDirectory() + "";
            String pathName = SDCard + "/" + cachePath + "/" + fileName;

            File file = new File(pathName);
            if (fileName == "TestCase.xml" && file.exists())
                file.delete();
            
            if (file.exists()) {
                System.out.println("exits");
                bookExist = true;
            } else {
                InputStream input = conn.getInputStream();
                String dir = SDCard + "/" + cachePath;
                new File(dir).mkdir();
                file.createNewFile();
                output = new FileOutputStream(file);
                byte[] buffer = new byte[4 * 1024];
                int len;
                while ((len = input.read(buffer)) != -1) {
                    output.write(buffer, 0, len);
                }
                output.flush();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (!bookExist) {
                try {
                    output.close();
                    System.out.println("download success");
                } catch (IOException e) {
                    System.out.println("download fail");
                    e.printStackTrace();
                }
            }
        }
    }
}

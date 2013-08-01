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
import android.os.Environment;
import android.util.Log;

public class Util {
    private static final String TAG = "Download";
    /**
     * local cache
     */
    private static String cachePath = Environment.getExternalStorageDirectory() + "/readium_test";
    /**
     * test case config url
     */
    private static String config_url = "https://raw.github.com/readium/Launcher-Android/afd/Readium_SDK_Test_Android/TestCase.xml";
    private static String config_file = "TestCase.xml";

    public static final String getConfig_url() {
        return config_url;
    }

    public static final String getConfig_file() {
        return config_file;
    }

    public static final String getCachePath() {
        return cachePath;
    }
    public static final String getConfigFullName(){
        return cachePath + "/" + config_file;
    }
    public static final void setCachePath(String cachePath) {
        Util.cachePath = cachePath;
    }

    /**
     * check dir is exist or not, create dir when is not exist
     * 
     * @param dir
     *            the dir name string
     */
    public static void createDirWhenNotExist(String dir) {
        File f = new File(dir);
        if (!f.exists()) {
            // create
            f.mkdir();
        }
    }

    /**
     * download file by url, and save file to cache path
     * 
     * @param fileUrl
     *            the file download url string
     * @param fileName
     *            the file name
     */
    public static void download(String fileUrl, String fileName) {
        OutputStream output = null;

        boolean bookExist = false;

        // get config xml check
        if ("" == fileName) {
            fileUrl = config_url;
            fileName = "TestCase.xml";
        }

        try {
            URL url = new URL(fileUrl);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            String pathName = getCachePath();
            String fullName = getConfigFullName();

            Util.createDirWhenNotExist(pathName);

            File file = new File(fullName);

            // if get config file, clean local config and get newest file.
            if (fileName == "TestCase.xml" && file.exists())
                file.delete();

            if (file.exists()) {
                Log.i(TAG, "File " + fullName + " exist, skip.");
                bookExist = true;
            } else { // download...

                InputStream input = conn.getInputStream();

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
            Log.i(TAG, "Malformed URL exception:" + fileUrl);
            e.printStackTrace();
        } catch (IOException e) {
            Log.i(TAG, "IO exception:" + fileName);
            e.printStackTrace();
        } finally {
            if (!bookExist) {
                try {
                    output.close();
                    Log.i(TAG, "Download success:" + fileUrl);
                } catch (IOException e) {
                    Log.i(TAG, "Download fail:" + fileUrl);
                    e.printStackTrace();
                }
            }
        }
    }
}

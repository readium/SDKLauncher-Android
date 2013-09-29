/**
 * file download implementation
 * 
 */
package org.readium.sdk.test.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.os.Environment;
import android.util.Log;
import org.readium.sdk.android.Container;
import org.readium.sdk.android.Package;
import org.readium.sdk.android.SpineItem;
import org.readium.sdk.android.components.navigation.NavigationTable;

public class Util {
    private static final String TAG = "Download";
    /**
     * local cache
     */
    private static String cachePath = Environment.getExternalStorageDirectory()
            + "/readium_test";
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

    public static final String getConfigFullName() {
        return cachePath + "/" + config_file;
    }

    public static final String getFullName(String name) {
        return cachePath + "/" + name;
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

            String sLen = conn.getHeaderField("content-Length");
            int iLen = (null == sLen) ? 0 : Integer.parseInt(sLen);

            String pathName = getCachePath();
            String fullName = getFullName(fileName);

            Util.createDirWhenNotExist(pathName);

            File file = new File(fullName);

            if (iLen > 0 && iLen < file.length())
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

    public static void saveEPubJson(String json) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(
                    getCachePath() + "/epub.json"));
            out.write(json);
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String format(String s) {
        return (null == s) ? "null" : "\"" + s + "\"";
    }

    private static String getContainerJson(Container c) {
        return "\"version\":" + format(c.getVersion()) + ",\"name\":"
                + format(c.getName()) + "";
    }

    private static String getSpineItemsJson(Package p, List<SpineItem> l) {
        int n = 0;
        List<String> list = new ArrayList<String>();
        String json = "\"spineItems\":";
        for (Iterator<SpineItem> i = l.iterator(); i.hasNext();) {
            SpineItem si = i.next();
            Log.i("LOGPACK",
                    si.getHref() + " uncompress size:"
                            + p.getArchiveInfoSize(si.getHref()));
            String s = "{\"idRef\":" + Util.format(si.getIdRef()) + ","
                    + "\"href\":" + Util.format(si.getHref()) + ","
                    + "\"pageSpread\":" + Util.format(si.getPageSpread()) + ","
                    + "\"archiveInfoSize\":"
                    + p.getArchiveInfoSize(si.getHref()) + ","
                    + "\"renditionLayout\":"
                    + Util.format(si.getRenditionLayout()) + "}";

            list.add(s);

            n = n + 1;
            if (n % 100 == 0) {
                Log.v(TAG, "count = " + n);
            }
        }

        return json + list.toString();
    }

    private static String getListJson(String name, List<String> l) {
        String json = name + ":";
        List<String> list = new ArrayList<String>();
        for (Iterator<String> i = l.iterator(); i.hasNext();) {
            list.add(Util.format(i.next()));
        }
        return json + list.toString();
    }

    private static String getNavigationTableJson(String name, NavigationTable n) {
        String json = "";
        if (null == n) {
            json = name + ":{}";
        } else {
            json = name + ":{";
            json = json + "\"title\":" + Util.format(n.getTitle())
                    + ",\"sourceHref\":" + Util.format(n.getSourceHref()) + "}";
        }
        return json;
    }

    private static String getPackage(Package p) {
        Log.i("LOGPACK", "Book:" + p.getTitle());
        Log.i("LOGPACK", "UniqueID:" + p.getUniqueID());
        return "\"package\":{\"title\":"
                + format(p.getTitle())
                + ",\"subtitle\":"
                + format(p.getSubtitle())
                + ",\"shortTitle\":"
                + format(p.getShortTitle())
                + ",\"collectionTitle\":"
                + format(p.getCollectionTitle())
                + ",\"editionTitle\":"
                + format(p.getEditionTitle())
                + ",\"expandedTitle\":"
                + format(p.getExpandedTitle())
                + ",\"fullTitle\":"
                + format(p.getFullTitle())
                + ",\"uniqueID\":"
                + format(p.getUniqueID())
                + ",\"urlSafeUniqueID\":"
                + format(p.getUrlSafeUniqueID())
                + ",\"packageID\":"
                + format(p.getPackageID())
                + ",\"basePath\":"
                + format(p.getBasePath())
                + ",\"type\":"
                + format(p.getType())
                + ",\"version\":"
                + format(p.getVersion())
                + ",\"isbn\":"
                + format(p.getIsbn())
                + ",\"language\":"
                + format(p.getLanguage())
                + ",\"copyrightOwner\":"
                + format(p.getCopyrightOwner())
                + ",\"source\":"
                + format(p.getSource())
                + ",\"authors\":"
                + format(p.getAuthors())
                + ",\"modificationDate\":"
                + format(p.getModificationDate())
                + ",\"pageProgressionDirection\":"
                + format(p.getPageProgressionDirection())
                + ","
                + getListJson("\"authorList\"", p.getAuthorList())
                + ","
                + getListJson("\"subjects\"", p.getSubjects())
                + ","
                + getSpineItemsJson(p, p.getSpineItems())
                + ","
                + getNavigationTableJson("\"tableOfContents\"",
                        p.getTableOfContents())
                + ","
                + getNavigationTableJson("\"listOfFigures\"",
                        p.getListOfFigures())
                + ","
                + getNavigationTableJson("\"listOfIllustrations\"",
                        p.getListOfIllustrations())
                + ","
                + getNavigationTableJson("\"listOfTables\"",
                        p.getListOfTables()) + ","
                + getNavigationTableJson("\"pageList\"", p.getPageList()) + "}";
    }

    public static String getJson(String test, Container c) {
        Package p = c.getDefaultPackage();
        String json = "";
        json = "{\"test\":" + test + ",\"container\":";
        json += "{" + getContainerJson(c) + "," + getPackage(p) + "}}";
        return json;
    }
}

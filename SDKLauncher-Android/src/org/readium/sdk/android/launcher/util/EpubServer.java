/*
 * EpubServer.java
 * SDKLauncher-Android
 *
 * Created by Yonathan Teitelbaum (Mantano) on 2013-09-03.
 * Copyright (c) 2012-2013 The Readium Foundation and contributors.
 * 
 * The Readium SDK is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.readium.sdk.android.launcher.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.readium.sdk.android.Package;

import android.util.Log;
import fi.iki.elonen.NanoHTTPD;

/**
 * This small web server will serve media files such as audio and video.
 */
public class EpubServer extends NanoHTTPD {

	private static final String TAG = "EpubServer";
	public static final String HTTP_HOST = "localhost";
	public static final int HTTP_PORT = 8080;
    /**
     * Hashtable mapping (String)FILENAME_EXTENSION -> (String)MIME_TYPE
     */
    private static final Map<String, String> MIME_TYPES;

    private final Package mPackage;
    private final boolean quiet;
    
    static {
    	Map<String, String> tmpMap = new HashMap<String, String>();
    	tmpMap.put("css", "text/css");
    	tmpMap.put("htm", "text/html");
    	tmpMap.put("html", "text/html");
    	tmpMap.put("xml", "text/xml");
    	tmpMap.put("java", "text/x-java-source, text/java");
    	tmpMap.put("txt", "text/plain");
    	tmpMap.put("asc", "text/plain");
    	tmpMap.put("gif", "image/gif");
    	tmpMap.put("jpg", "image/jpeg");
    	tmpMap.put("jpeg", "image/jpeg");
    	tmpMap.put("png", "image/png");
    	tmpMap.put("mp3", "audio/mpeg");
    	tmpMap.put("m3u", "audio/mpeg-url");
    	tmpMap.put("mp4", "video/mp4");
    	tmpMap.put("ogv", "video/ogg");
    	tmpMap.put("flv", "video/x-flv");
    	tmpMap.put("mov", "video/quicktime");
    	tmpMap.put("swf", "application/x-shockwave-flash");
    	tmpMap.put("js", "application/javascript");
    	tmpMap.put("pdf", "application/pdf");
    	tmpMap.put("doc", "application/msword");
    	tmpMap.put("ogg", "application/x-ogg");
    	tmpMap.put("zip", "application/octet-stream");
    	tmpMap.put("exe", "application/octet-stream");
    	tmpMap.put("class", "application/octet-stream");
    	tmpMap.put("webm", "video/webm");
    	MIME_TYPES = Collections.unmodifiableMap(tmpMap);
    }

    public EpubServer(String host, int port, Package pckg, boolean quiet) {
        super(host, port);
        this.mPackage = pckg;
        this.quiet = quiet;
    }

    Package getPackage() {
        return mPackage;
    }
	
	public void startServer() {
		try {
			start();
		} catch (IOException e) {
			Log.e(TAG, ""+e.getMessage());
		}
	}

    /**
	 * Serves file from homeDir and its' subdirectories (only). Uses only URI,
	 * ignores all headers and HTTP parameters.
	 */
	Response serveFile(String uri, Map<String, String> header, Package pckg) {
		Response res = null;

		final int contentLength = pckg.getArchiveInfoSize(uri);
		if (contentLength == 0) {
			res = new Response(Response.Status.NOT_FOUND,
					NanoHTTPD.MIME_PLAINTEXT, "Error 404, file not found.");
		}

		if (res == null) {
			// Get MIME type from file name extension, if possible
			String mime = null;
			int dot = uri.lastIndexOf('.');
			if (dot >= 0) {
				mime = MIME_TYPES.get(uri.substring(dot + 1).toLowerCase());
			}
			if (mime == null) {
				mime = NanoHTTPD.MIME_DEFAULT_BINARY;
			}

			// Calculate etag
			String etag = Integer.toHexString((pckg.getUniqueID()
					+ pckg.getModificationDate() + "" + pckg.getBasePath())
					.hashCode());

			long startFrom = 0;
			long endAt = -1;
			String range = header.get("range");
//			Log.d(TAG, "range: "+range);
			if (range != null) {
				if (range.startsWith("bytes=")) {
					range = range.substring("bytes=".length());
					int minus = range.indexOf('-');
					try {
						if (minus > 0) {
							startFrom = Long.parseLong(range.substring(0, minus));
							endAt = Long.parseLong(range.substring(minus + 1));
						}
					} catch (NumberFormatException ignored) {
						Log.e(TAG, "NumberFormatException: "+ignored.getMessage());
					}
				}
			}

			// Change return code and add Content-Range header when skipping is requested
			if (range != null && startFrom >= 0) {
				if (startFrom >= contentLength) {
					res = new Response(Response.Status.RANGE_NOT_SATISFIABLE,
							NanoHTTPD.MIME_PLAINTEXT, "");
					res.addHeader("Content-Range", "bytes 0-0/" + contentLength);
					res.addHeader("ETag", etag);
				} else {
					if (endAt < 0) {
						endAt = contentLength - 1;
					}
					long newLen = endAt - startFrom + 1;
					if (newLen < 0) {
						newLen = 0;
					}

					InputStream is = pckg.getInputStream(uri);
					try {
						is.skip(startFrom);
					} catch (IOException e) {
						Log.e(TAG, "InputStream.skip("+startFrom+") failed: "+e.getMessage(), e);
					}

					res = new Response(Response.Status.PARTIAL_CONTENT, mime, is);
					res.addHeader("Content-Range", "bytes " + startFrom + "-"
							+ endAt + "/" + contentLength);
				}
			} else {
				if (etag.equals(header.get("if-none-match"))) {
					res = new Response(Response.Status.NOT_MODIFIED, mime, "");
				} else {
                    InputStream is = pckg.getInputStream(uri);
					res = new Response(Response.Status.OK, mime, is);
				}
			}
			res.addHeader("ETag", etag);
		}
		// Announce that the file server accepts partial content requests
		res.addHeader("Accept-Ranges", "bytes");

		return res;
	}

	@Override
	public Response serve(String uri, Method method,
			Map<String, String> header, Map<String, String> parms,
			Map<String, String> files) {
		if (!quiet) {
			Log.d(TAG, method + " '" + uri + "' ");

			Iterator<String> e = header.keySet().iterator();
			while (e.hasNext()) {
				String value = e.next();
				Log.d(TAG, "  HDR: '" + value + "' = '" + header.get(value) + "'");
			}
			e = parms.keySet().iterator();
			while (e.hasNext()) {
				String value = e.next();
				Log.d(TAG, "  PRM: '" + value + "' = '" + parms.get(value) + "'");
			}
			e = files.keySet().iterator();
			while (e.hasNext()) {
				String value = e.next();
				Log.d(TAG, "  UPLOADED: '" + value + "' = '" + files.get(value) + "'");
			}
		}
		uri = uri.startsWith("/") ? uri.substring(1) : uri;
		return serveFile(uri, header, getPackage());
	}
}

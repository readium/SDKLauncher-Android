/*
 * EpubServer.java
 * SDKLauncher-Android
 *
 * Created by Yonathan Teitelbaum (Mantano) on 2013-09-03.
 */
//  Copyright (c) 2014 Readium Foundation and/or its licensees. All rights reserved.
//  Redistribution and use in source and binary forms, with or without modification, 
//  are permitted provided that the following conditions are met:
//  1. Redistributions of source code must retain the above copyright notice, this 
//  list of conditions and the following disclaimer.
//  2. Redistributions in binary form must reproduce the above copyright notice, 
//  this list of conditions and the following disclaimer in the documentation and/or 
//  other materials provided with the distribution.
//  3. Neither the name of the organization nor the names of its contributors may be 
//  used to endorse or promote products derived from this software without specific 
//  prior written permission.
//
//  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
//  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
//  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
//  IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
//  INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, 
//  BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, 
//  DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF 
//  LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE 
//  OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED 
//  OF THE POSSIBILITY OF SUCH DAMAGE

package org.readium.sdk.android.launcher.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.readium.sdk.android.ManifestItem;
import org.readium.sdk.android.Package;
import org.readium.sdk.android.PackageResource;
import org.readium.sdk.android.util.ResourceInputStream;

import android.util.Log;

import com.koushikdutta.async.AsyncServer;
import com.koushikdutta.async.http.server.AsyncHttpServer;
import com.koushikdutta.async.http.server.AsyncHttpServerRequest;
import com.koushikdutta.async.http.server.AsyncHttpServerResponse;
import com.koushikdutta.async.http.server.HttpServerRequestCallback;

/**
 * This small web server will serve media files such as audio and video.
 */
public class EpubServer implements HttpServerRequestCallback {

	public interface DataPreProcessor {
		byte[] handle(byte[] data, String mime, String uriPath,
				ManifestItem item);
	}

	private static final String TAG = "EpubServer";
	public static final String HTTP_HOST = "127.0.0.1";
	public static final int HTTP_PORT = 8080;
	/**
	 * Hashtable mapping (String)FILENAME_EXTENSION -> (String)MIME_TYPE
	 */
	public static final Map<String, String> MIME_TYPES;

	private final Package mPackage;
	private final boolean quiet;

	private final DataPreProcessor dataPreProcessor;

	static {
		Map<String, String> tmpMap = new HashMap<String, String>();
		tmpMap.put("html", "application/xhtml+xml"); // FORCE
		tmpMap.put("xhtml", "application/xhtml+xml"); // FORCE
		tmpMap.put("xml", "application/xml"); // FORCE
		tmpMap.put("htm", "text/html");
		tmpMap.put("css", "text/css");
		tmpMap.put("java", "text/x-java-source, text/java");
		tmpMap.put("txt", "text/plain");
		tmpMap.put("asc", "text/plain");
		tmpMap.put("gif", "image/gif");
		tmpMap.put("jpg", "image/jpeg");
		tmpMap.put("jpeg", "image/jpeg");
		tmpMap.put("png", "image/png");
		tmpMap.put("mp3", "audio/mpeg");
		tmpMap.put("m3u", "audio/mpeg-url");
		tmpMap.put("mp4", "video/mp4"); // could be audio!
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

	AsyncHttpServer mHttpServer;
	AsyncServer mAsyncServer;
	String mHostName;
	int mPortNumber;

	public EpubServer(String host, int port, Package pckg, boolean quiet,
			DataPreProcessor dataPreProcessor) {

		this.mHostName = host;
		this.mPortNumber = port;
		this.mPackage = pckg;
		this.quiet = quiet;
		this.dataPreProcessor = dataPreProcessor;
		this.mHttpServer = new AsyncHttpServer();
		this.mAsyncServer = new AsyncServer();

		mHttpServer.get(".*", this);
	}

	Package getPackage() {
		return mPackage;
	}

	public void startServer() {
		try {
			mAsyncServer.listen(InetAddress.getByName(mHostName), mPortNumber,
					mHttpServer.getListenCallback());
		} catch (UnknownHostException e) {
			Log.e(TAG, "" + e.getMessage());
		}
	}

	public void stop() {
		mHttpServer.stop();
		mAsyncServer.stop();
	}

	private final Object criticalSectionSynchronizedLock = new Object();

	@Override
	public void onRequest(AsyncHttpServerRequest request,
			AsyncHttpServerResponse response) {

		String uri = request.getPath();

		if (!quiet) {
			Log.d(TAG, request.getMethod() + " '" + uri + "' ");

			Iterator<String> e = request.getHeaders().getMultiMap().keySet()
					.iterator();
			while (e.hasNext()) {
				String value = e.next();
				Log.d(TAG, "  HDR: '" + value + "' = '"
						+ request.getHeaders().get(value) + "'");
			}
			e = request.getQuery().keySet().iterator();
			while (e.hasNext()) {
				String value = e.next();
				Log.d(TAG, "  PRM: '" + value + "' = '"
						+ request.getQuery().get(value) + "'");
			}
		}

		String httpPrefix = "http://" + HTTP_HOST + ":" + HTTP_PORT + "/";
		int iHttpPrefix = uri.indexOf(httpPrefix);
		uri = iHttpPrefix == 0 ? uri.substring(httpPrefix.length()) : uri;
		uri = uri.startsWith("/") ? uri.substring(1) : uri;

		int indexOfQ = uri.indexOf('?');
		if (indexOfQ >= 0) {
			uri = uri.substring(0, indexOfQ);
		}

		int indexOfSharp = uri.indexOf('#');
		if (indexOfSharp >= 0) {
			uri = uri.substring(0, indexOfSharp);
		}

		Package pckg = getPackage();

		int contentLength = -1;
		synchronized (criticalSectionSynchronizedLock) {
			contentLength = pckg.getArchiveInfoSize(uri);
		}

		if (contentLength <= 0) {
			response.code(404);
			response.send("Error 404, file not found.");
			return;
		}

		String mime = null;
		int dot = uri.lastIndexOf('.');
		if (dot >= 0) {
			mime = MIME_TYPES.get(uri.substring(dot + 1).toLowerCase());
		}
		if (mime == null) {
			mime = "application/octet-stream";
		}

		ManifestItem item = pckg.getManifestItem(uri);
		String contentType = item != null ? item.getMediaType() : null;
		if (!mime.equals("application/xhtml+xml")
				&& !mime.equals("application/xml") // FORCE
				&& contentType != null && contentType.length() > 0) {
			mime = contentType;
		}

		PackageResource packageResource = pckg.getResourceAtRelativePath(uri);

		boolean isHTML = mime.equals("text/html")
				|| mime.equals("application/xhtml+xml");

		if (isHTML) {
			//Pre-process HTML data as a whole
			byte[] data = packageResource.readDataFull();

			byte[] data_ = dataPreProcessor.handle(data, mime, uri, item);
			if (data_ != null) {
				data = data_;
			}

			response.setContentType(mime);
			response.sendStream(new ByteArrayInputStream(data), data.length);

		} else {
			boolean isRange = request.getHeaders().get("range") != null;

			ResourceInputStream is;
			synchronized (criticalSectionSynchronizedLock) {
				Log.d(TAG, "NEW STREAM:" + request.getPath());
				is = (ResourceInputStream) packageResource
						.getInputStream(isRange);

				int updatedContentLength = packageResource.getContentLength();
				if (updatedContentLength != contentLength) {
					Log.e(TAG, "UPDATED CONTENT LENGTH! "
							+ updatedContentLength + "<--" + contentLength);
				}
			}

			ByteRangeInputStream bis = new ByteRangeInputStream(is, isRange,
					criticalSectionSynchronizedLock);
			try {
				response.sendStream(bis, bis.available());
			} catch (IOException e) {
				response.code(500);
				response.end();
				Log.e(TAG, e.toString());
			}

		}

	}

}

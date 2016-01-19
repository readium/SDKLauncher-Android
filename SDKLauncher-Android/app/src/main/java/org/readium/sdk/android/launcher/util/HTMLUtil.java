/*
 * HTMLUtil.java
 * SDKLauncher-Android
 *
 * Created by Yonathan Teitelbaum (Mantano) on 2013-09-02.
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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This utility class replaces all the local video and audio URI references by a
 * fully qualified URL (e.g. http://localhost:8080/) so that the WebView will
 * connect to the local server. This enables the 'server-side' to stream the
 * content.
 */
public class HTMLUtil {

	public static String htmlByInjectingIntoHead(String html,
			String headHtmToInsert) {
		if (html == null || html.length() == 0 || headHtmToInsert == null
				|| headHtmToInsert.length() == 0) {
			return html;
		}

		String pattern = "<head[^>]*>";
		Matcher matcher = Pattern.compile(pattern).matcher(html);

		if (matcher.find()) {
			int headTagEndPos = matcher.end();
			return new StringBuilder(html).insert(headTagEndPos,
					headHtmToInsert).toString();
		} else {
			return html;
		}
	}

	// public static String htmlByReplacingMediaURLsInHTML(String html,
	// String relativePath, String packageUUID) {
	// if (html == null || html.length() == 0 || relativePath == null
	// || relativePath.length() == 0 || packageUUID == null
	// || packageUUID.length() == 0) {
	// return html;
	// }
	//
	// String[] mediaTags = new String[] { "audio", "video" };
	// for (String token : mediaTags) {
	//
	// int i = 0;
	//
	// String token0 = "<" + token;
	// String token1 = "</" + token + ">";
	// StringBuilder sb = new StringBuilder();
	//
	// while (i < html.length()) {
	// int lastLocation = i;
	// int location = html.indexOf(token0, i);
	// if (location == -1) {
	// break;
	// }
	// i = location + token0.length();
	// sb.append(html.substring(lastLocation, i));
	//
	// int nextLtLocation = html.indexOf("<", i);
	// int nextGtLocation = html.indexOf(">", i);
	// if (nextLtLocation == -1 && nextGtLocation == -1) {
	// break;
	// }
	// int endFragmentLocation;
	// if (nextGtLocation < nextLtLocation
	// && html.charAt(nextGtLocation - 1) == '/') {
	// // <audio ... />
	// endFragmentLocation = nextGtLocation;
	// } else {
	// // <audio> ... </audio>
	// int endTagLocation = html.indexOf(token1, i);
	// endFragmentLocation = endTagLocation;
	// if (endTagLocation == -1) {
	// break;
	// }
	// }
	// String originalFragment = html
	// .substring(i, endFragmentLocation);
	// String fragment = updateSourceAttributesInFragment(
	// originalFragment, relativePath, packageUUID);
	//
	// sb.append(fragment);
	//
	// i += originalFragment.length();
	// }
	// sb.append(html.substring(i, html.length()));
	// html = sb.toString();
	// }
	//
	// return html;
	// }
	//
	// private static String updateSourceAttributesInFragment(String fragment,
	// String relativePath, String packageUUID) {
	// // Strip off the HTML file name.
	// String relativeParentPath = getFullPath(relativePath);
	// int i = 0;
	// while (i < fragment.length()) {
	// int indexOfSrc = fragment.indexOf("src", i);
	// if (indexOfSrc == -1) {
	// break;
	// }
	//
	// i = indexOfSrc + "src".length();
	// int indexOfEquals = -1;
	//
	// for (int j = i; j < fragment.length(); j++) {
	// char ch = fragment.charAt(j);
	//
	// if (ch == '=') {
	// indexOfEquals = j;
	// break;
	// }
	//
	// if (ch != ' ' && ch != '\r' && ch != '\n') {
	// break;
	// }
	// }
	//
	// if (indexOfEquals == -1) {
	// continue;
	// }
	//
	// i = indexOfEquals + 1;
	//
	// int indexOfApos = -1;
	// int indexOfQuote = -1;
	//
	// for (int j = i; j < fragment.length(); j++) {
	// char ch = fragment.charAt(j);
	// if (ch == '\'') {
	// indexOfApos = j;
	// break;
	// }
	// if (ch == '\"') {
	// indexOfQuote = j;
	// break;
	// }
	// if (ch != ' ' && ch != '\r' && ch != '\n') {
	// break;
	// }
	// }
	//
	// int p0 = -1;
	// int p1 = -1;
	//
	// if (indexOfApos != -1) {
	// p0 = indexOfApos + 1;
	//
	// for (int j = p0; j < fragment.length(); j++) {
	// char ch = fragment.charAt(j);
	//
	// if (ch == '\'') {
	// p1 = j;
	// break;
	// }
	// }
	// } else if (indexOfQuote != -1) {
	// p0 = indexOfQuote + 1;
	// for (int j = p0; j < fragment.length(); j++) {
	// char ch = fragment.charAt(j);
	// if (ch == '\"') {
	// p1 = j;
	// break;
	// }
	// }
	// }
	//
	// if (p0 == -1 || p1 == -1) {
	// continue;
	// }
	//
	// i = p0;
	//
	// String srcValue = fragment.substring(p0, p1);
	//
	// if (srcValue.indexOf(':') != -1) {
	// // It's probably a URL with a scheme of some kind.
	// continue;
	// }
	//
	// String path = new File(relativeParentPath, srcValue).getPath();
	// Uri uri = Uri.parse(path);
	// path = uri.getPath();
	//
	// if (path.startsWith("/")) {
	// path = path.substring(1);
	// }
	//
	// path = MessageFormat.format("http://{0}:{1}/{2}", EpubServer.HTTP_HOST,
	// ""+EpubServer.HTTP_PORT, path);
	//
	// fragment = fragment.substring(0, p0) + path + fragment.substring(p1);
	// }
	// return fragment;
	// }
	//
	// /**
	// * Does the work of getting the path.
	// *
	// * @param filename the filename
	// * @return the path
	// */
	// private static String getFullPath(String filename) {
	// if (filename == null) {
	// return null;
	// }
	// int prefix = getPrefixLength(filename);
	// if (prefix < 0) {
	// return null;
	// }
	// if (prefix >= filename.length()) {
	// return getPrefix(filename);
	// }
	// int index = indexOfLastSeparator(filename);
	// if (index < 0) {
	// return filename.substring(0, prefix);
	// }
	// int end = Math.max(1, index + 1);
	// return filename.substring(0, end);
	// }
	//
	// /**
	// * Returns the index of the last directory separator character.
	// *
	// * @param filename the filename
	// * @return the index of the last separator character,
	// * or -1 if there is no such character
	// */
	// public static int indexOfLastSeparator(String filename) {
	// if (filename == null) {
	// return -1;
	// }
	// return filename.lastIndexOf(File.separatorChar);
	// }
	//
	// /**
	// * Gets the prefix from a full filename
	// *
	// * @param filename the filename to query, null returns null
	// * @return the prefix of the file, null if invalid
	// */
	// public static String getPrefix(String filename) {
	// if (filename == null) {
	// return null;
	// }
	// int len = getPrefixLength(filename);
	// if (len < 0) {
	// return null;
	// }
	// if (len > filename.length()) {
	// return filename + File.separatorChar;
	// }
	// return filename.substring(0, len);
	// }
	//
	// /**
	// * Returns the length of the filename prefix
	// *
	// * @param filename the filename to find the prefix in, null returns -1
	// * @return the length of the prefix, -1 if invalid or null
	// */
	// public static int getPrefixLength(String filename) {
	// if (filename == null) {
	// return -1;
	// }
	// int len = filename.length();
	// if (len == 0) {
	// return 0;
	// }
	// char ch0 = filename.charAt(0);
	// if (len == 1) {
	// return isSeparator(ch0) ? 1 : 0;
	// } else {
	// char ch1 = filename.charAt(1);
	// if (isSeparator(ch0) && isSeparator(ch1)) {
	// int pos = filename.indexOf(File.separatorChar, 2);
	// if (pos == -1 && pos == 2) {
	// return -1;
	// }
	// return pos + 1;
	// } else {
	// return isSeparator(ch0) ? 1 : 0;
	// }
	// }
	// }
	//
	// /**
	// * Checks if the character is a separator.
	// *
	// * @param ch the character to check
	// * @return true if it is a separator character
	// */
	// private static boolean isSeparator(char ch) {
	// return ch == File.separatorChar;
	// }
}

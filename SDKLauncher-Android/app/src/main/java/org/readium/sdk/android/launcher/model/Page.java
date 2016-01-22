/*
 * Page.java
 * SDKLauncher-Android
 *
 * Created by Yonathan Teitelbaum (Mantano) on 2013-07-30.
 * Copyright (c) 2012-2013 The Readium Foundation and contributors.
 * 
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

package org.readium.sdk.android.launcher.model;

public class Page {
	private final int mSpineItemPageIndex;
	private final int mSpineItemPageCount;
	private final String mIdref;
	private final int mSpineItemIndex;
	
	public Page(int spineItemPageIndex, int spineItemPageCount,
			String idref, int spineItemIndex) {
		mSpineItemPageIndex = spineItemPageIndex;
		mSpineItemPageCount = spineItemPageCount;
		mIdref = idref;
		mSpineItemIndex = spineItemIndex;
	}

	public int getSpineItemPageIndex() {
		return mSpineItemPageIndex;
	}

	public int getSpineItemPageCount() {
		return mSpineItemPageCount;
	}

	public String getIdref() {
		return mIdref;
	}

	public int getSpineItemIndex() {
		return mSpineItemIndex;
	}

	@Override
	public String toString() {
		return "Page [spineItemPageIndex=" + mSpineItemPageIndex
				+ ", spineItemPageCount=" + mSpineItemPageCount + ", idref="
				+ mIdref + ", spineItemIndex=" + mSpineItemIndex + "]";
	}
}
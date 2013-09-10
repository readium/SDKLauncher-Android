/*
 * Page.java
 * SDKLauncher-Android
 *
 * Created by Yonathan Teitelbaum (Mantano) on 2013-07-30.
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
package org.readium.sdk.android.launcher.model;

public class Page {
	private final int spineItemPageIndex;
	private final int spineItemPageCount;
	private final String idref;
	private final int spineItemIndex;
	
	public Page(int spineItemPageIndex, int spineItemPageCount,
			String idref, int spineItemIndex) {
		this.spineItemPageIndex = spineItemPageIndex;
		this.spineItemPageCount = spineItemPageCount;
		this.idref = idref;
		this.spineItemIndex = spineItemIndex;
	}

	public int getSpineItemPageIndex() {
		return spineItemPageIndex;
	}

	public int getSpineItemPageCount() {
		return spineItemPageCount;
	}

	public String getIdref() {
		return idref;
	}

	public int getSpineItemIndex() {
		return spineItemIndex;
	}

	@Override
	public String toString() {
		return "Page [spineItemPageIndex=" + spineItemPageIndex
				+ ", spineItemPageCount=" + spineItemPageCount + ", idref="
				+ idref + ", spineItemIndex=" + spineItemIndex + "]";
	}
}
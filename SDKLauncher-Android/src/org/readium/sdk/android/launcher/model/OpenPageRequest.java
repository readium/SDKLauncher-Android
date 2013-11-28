package org.readium.sdk.android.launcher.model;

import org.json.JSONException;
import org.json.JSONObject;

public class OpenPageRequest {

	private final String idref;
	private final Integer spineItemPageIndex;
	private final String elementCfi;
	private final String contentRefUrl;
	private final String sourceFileHref;
	private final String elementId;
	
	public static OpenPageRequest fromIdref(String idref) {
		return new OpenPageRequest(idref, 0, null, null, null, null);
	}
	
	public static OpenPageRequest fromIdrefAndIndex(String idref, int spineItemPageIndex) {
		return new OpenPageRequest(idref, spineItemPageIndex, null, null, null, null);
	}
	
	public static OpenPageRequest fromIdrefAndCfi(String idref, String elementCfi) {
		return new OpenPageRequest(idref, null, elementCfi, null, null, null);
	}
	
	public static OpenPageRequest fromContentUrl(String contentRefUrl, String sourceFileHref) {
		return new OpenPageRequest(null, null, null, contentRefUrl, sourceFileHref, null);
	}
	
	public static OpenPageRequest fromElementId(String idref, String elementId){
		return new OpenPageRequest(idref, null, null, null, null, elementId);
	}
	
	private OpenPageRequest(String idref, Integer spineItemPageIndex,
			String elementCfi, String contentRefUrl, String sourceFileHref, String elementId) {
		this.idref = idref;
		this.spineItemPageIndex = spineItemPageIndex;
		this.elementCfi = elementCfi;
		this.contentRefUrl = contentRefUrl;
		this.sourceFileHref = sourceFileHref;
		this.elementId = elementId;
	}
	
	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("idref", idref);
		json.put("spineItemPageIndex", spineItemPageIndex);
		json.put("elementCfi", elementCfi);
		json.put("contentRefUrl", contentRefUrl);
		json.put("sourceFileHref", sourceFileHref);
		json.put("elementId", elementId);
		return json;
	}
	
	public static OpenPageRequest fromJSON(String data) throws JSONException {
		JSONObject json = new JSONObject(data);
        Integer spineItemPageIndex = json.has("spineItemPageIndex") ? json.getInt("spineItemPageIndex") : null;
        return new OpenPageRequest(json.optString("idref", null), spineItemPageIndex,
                // get elementCfi and then contentCFI (from bookmarkData) if it was empty
                json.optString("elementCfi", json.optString("contentCFI", null)),
                json.optString("contentRefUrl", null),
                json.optString("sourceFileHref", null), json.optString("elementId", null));
	}
}

package org.readium.sdk.android.launcher;

import org.readium.sdk.android.Credential;

import android.content.Context;

public class DrmInitialize {
	Credential credential;
	void initialize(Context context){
		credential = new Credential();
		credential.initalize(context);
	}
}

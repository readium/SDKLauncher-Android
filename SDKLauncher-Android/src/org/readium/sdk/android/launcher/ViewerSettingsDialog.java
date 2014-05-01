/*
 * ViewerSettingsDialog.java
 * SDKLauncher-Android
 *
 * Created by Yonathan Teitelbaum (Mantano) on 2013-07-30.
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

package org.readium.sdk.android.launcher;

import org.readium.sdk.android.launcher.model.ViewerSettings;

import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

/**
 * This dialog displays the viewer settings to the user.
 * The model is represented by the class {@link ViewerSettings}
 *
 */
public class ViewerSettingsDialog extends DialogFragment {
	
	/**
	 * Interface to notify the listener when a viewer settings have been changed.
	 */
	public interface OnViewerSettingsChange {
		public void onViewerSettingsChange(ViewerSettings settings);
	}

	protected static final String TAG = "ViewerSettingsDialog";
	
	private OnViewerSettingsChange mListener;

	private ViewerSettings mOriginalSettings;
	
	public ViewerSettingsDialog(OnViewerSettingsChange listener, ViewerSettings originalSettings) {
		mListener = listener;
		mOriginalSettings = originalSettings;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		getDialog().setTitle(R.string.settings);
		View dialogView = inflater.inflate(R.layout.viewer_settings, container);
		
		final CheckBox syntheticSpread = (CheckBox) dialogView.findViewById(R.id.syntheticSpread);
		syntheticSpread.setChecked(mOriginalSettings.isSyntheticSpread());
		
		final EditText fontSizeText = (EditText) dialogView.findViewById(R.id.fontSize);
		fontSizeText.setText("" + mOriginalSettings.getFontSize());
		
		final EditText columnGapText = (EditText) dialogView.findViewById(R.id.columnGap);
		columnGapText.setText("" + mOriginalSettings.getColumnGap());
		
		Button ok = (Button) dialogView.findViewById(R.id.ok);
		ok.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				if (mListener != null) {
					int fontSize = parseString(fontSizeText.getText().toString(), 100);
					int columnGap = parseString(columnGapText.getText().toString(), 20);
					ViewerSettings settings = new ViewerSettings(syntheticSpread.isChecked(), fontSize, columnGap);
					mListener.onViewerSettingsChange(settings);
				}
				dismiss();
			}

			private int parseString(String s, int defaultValue) {
				try {
					return Integer.parseInt(s);
				} catch (Exception e) {
					Log.e(TAG, ""+e.getMessage(), e);
				}
				return defaultValue;
			}
		});
		
		Button cancel = (Button) dialogView.findViewById(R.id.cancel);
		cancel.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				dismiss();
			}
		});

		return dialogView;
	}

}

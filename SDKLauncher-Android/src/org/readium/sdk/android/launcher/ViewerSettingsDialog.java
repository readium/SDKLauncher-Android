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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

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
	public Dialog onCreateDialog(
			Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View dialogView = inflater.inflate(R.layout.viewer_settings, null);

        final RadioGroup spreadGroup = (RadioGroup) dialogView.findViewById(R.id.spreadSettings);
        switch (mOriginalSettings.getSyntheticSpreadMode()) {
            case AUTO:
                spreadGroup.check(R.id.spreadAuto);
                break;
            case DOUBLE:
                spreadGroup.check(R.id.spreadDouble);
                break;
            case SINGLE:
                spreadGroup.check(R.id.spreadSingle);
                break;
        }

        final RadioGroup scrollGroup = (RadioGroup) dialogView.findViewById(R.id.scrollSettings);
        switch (mOriginalSettings.getScrollMode()) {
            case AUTO:
                scrollGroup.check(R.id.scrollAuto);
                break;
            case DOCUMENT:
                scrollGroup.check(R.id.scrollDocument);
                break;
            case CONTINUOUS:
                scrollGroup.check(R.id.scrollContinuous);
                break;
        }

        final EditText fontSizeText = (EditText) dialogView.findViewById(R.id.fontSize);
        fontSizeText.setText("" + mOriginalSettings.getFontSize());

        final EditText columnGapText = (EditText) dialogView.findViewById(R.id.columnGap);
        columnGapText.setText("" + mOriginalSettings.getColumnGap());


        builder.setView(dialogView)
                .setTitle(R.string.settings)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        if (mListener != null) {
                            int fontSize = parseString(fontSizeText.getText().toString(), 100);
                            int columnGap = parseString(columnGapText.getText().toString(), 20);

                            ViewerSettings.SyntheticSpreadMode syntheticSpreadMode = null;
                            switch (spreadGroup.getCheckedRadioButtonId()) {
                                case R.id.spreadAuto:
                                    syntheticSpreadMode = ViewerSettings.SyntheticSpreadMode.AUTO;
                                    break;
                                case R.id.spreadSingle:
                                    syntheticSpreadMode = ViewerSettings.SyntheticSpreadMode.SINGLE;
                                    break;
                                case R.id.spreadDouble:
                                    syntheticSpreadMode = ViewerSettings.SyntheticSpreadMode.DOUBLE;
                                    break;
                            }

                            ViewerSettings.ScrollMode scrollMode = null;
                            switch (scrollGroup.getCheckedRadioButtonId()) {
                                case R.id.scrollAuto:
                                    scrollMode = ViewerSettings.ScrollMode.AUTO;
                                    break;
                                case R.id.scrollDocument:
                                    scrollMode = ViewerSettings.ScrollMode.DOCUMENT;
                                    break;
                                case R.id.scrollContinuous:
                                    scrollMode = ViewerSettings.ScrollMode.CONTINUOUS;
                                    break;
                            }

                            ViewerSettings settings = new ViewerSettings(syntheticSpreadMode, scrollMode, fontSize, columnGap);
                            mListener.onViewerSettingsChange(settings);
                        }
                        dismiss();
                    }

                    private int parseString(String s, int defaultValue) {
                        try {
                            return Integer.parseInt(s);
                        } catch (Exception e) {
                            Log.e(TAG, "" + e.getMessage(), e);
                        }
                        return defaultValue;
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                       dismiss();
                    }
                });

        return builder.create();
	}

}

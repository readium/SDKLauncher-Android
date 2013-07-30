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
		Log.i(TAG, "mOriginalSettings: "+mOriginalSettings);
		
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

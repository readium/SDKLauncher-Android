package org.readium.sdk.android.launcher;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

import org.readium.sdk.lcp.Acquisition;

public class AcquisitionDialogFragment extends DialogFragment {
    private Listener mListener;

    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface Listener {
        public void onAcquisitionDialogCancel(DialogFragment dialog);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        ProgressDialog progressDialog = new ProgressDialog(getActivity());

        progressDialog.setMessage("Download epub");

        progressDialog.setIndeterminate(false);
        progressDialog.setMax(100);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);

        progressDialog.setCancelable(true);
        progressDialog.setCanceledOnTouchOutside(true);

        progressDialog.setOnCancelListener(
                new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        mListener.onAcquisitionDialogCancel(AcquisitionDialogFragment.this);
                    }
                }
        );

        progressDialog.setOnDismissListener(
                new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        mListener.onAcquisitionDialogCancel(AcquisitionDialogFragment.this);
                    }
                }
        );

        return progressDialog;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener.onAcquisitionDialogCancel(AcquisitionDialogFragment.this);
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (Listener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement ProgressDialogListener");
        }
    }
}

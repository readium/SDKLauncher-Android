package org.readium.sdk.android.launcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.InputType;
import android.widget.EditText;

public class PassphraseDialogFragment extends DialogFragment {

//    String mPassphraseHint;
//    public PassphraseDialogFragment(String hint) {
//        super();
//        mPassphraseHint = hint;
//    }

    PassphraseDialogListener mListener;


    /* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
    public interface PassphraseDialogListener {
        public void onPassphraseDialogPositiveClick(DialogFragment dialog, String passPhrase);

        public void onPassphraseDialogNegativeClick(DialogFragment dialog);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Set up the input
        final EditText input = new EditText(getActivity());

        // Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        //String hint = mPassphraseHint;
        String hint = getString(R.string.passphrase);
        Bundle bundle = getArguments();
        if (bundle != null) {
            String hint_ = bundle.getString("passHint", "");

            if (hint_ != null && hint.length() > 0) {
                hint = hint_;
            }
        }

        builder.setMessage(hint)
                .setPositiveButton(R.string.generic_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User validated the passphrase
                        mListener.onPassphraseDialogPositiveClick(PassphraseDialogFragment.this, input.getText().toString());
                    }
                })
                .setNegativeButton(R.string.generic_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // User cancelled the dialog
                        mListener.onPassphraseDialogNegativeClick(PassphraseDialogFragment.this);
                    }
                });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    // Override the Fragment.onAttach() method to instantiate the NoticeDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
            mListener = (PassphraseDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement PassphraseDialogListener");
        }
    }
}

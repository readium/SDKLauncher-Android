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

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.ProgressCallback;
import com.koushikdutta.ion.Response;

import org.apache.commons.io.FilenameUtils;
import org.readium.sdk.android.Container;
import org.readium.sdk.android.EPub3;
import org.readium.sdk.android.SdkErrorHandler;
import org.readium.sdk.android.launcher.model.BookmarkDatabase;
import org.readium.sdk.lcp.CredentialHandler;
import org.readium.sdk.lcp.DoneCallback;
import org.readium.sdk.lcp.License;
import org.readium.sdk.lcp.NetProvider;
import org.readium.sdk.lcp.Service;
import org.readium.sdk.lcp.ServiceFactory;
import org.readium.sdk.lcp.StatusDocumentHandler;
import org.readium.sdk.lcp.StatusDocumentProcessing;
import org.readium.sdk.lcp.StorageProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

/**
 * @author chtian
 */
public class ContainerList extends FragmentActivity
        implements SdkErrorHandler, PassphraseDialogFragment.PassphraseDialogListener,
        AcquisitionDialogFragment.Listener {

    private Context context;
    private Stack<String> m_SdkErrorHandler_Messages = null;
    private License mLicense;

//#if ENABLE_NET_PROVIDER_ACQUISITION
//    private Acquisition mAcquisition;

    private StatusDocumentProcessing mStatusDocumentProcessing;

    private AcquisitionDialogFragment mAcquisitionDialogFragment;
    private Container mContainer;
    private String mBookName; // Name of the selected book
    private String mBookPath; // Path of the selected book
    private Service mLcpService;

    Future<Response<InputStream>> mRequest = null;

    protected abstract class SdkErrorHandlerMessagesCompleted {
        Intent m_intent = null;

        public SdkErrorHandlerMessagesCompleted(Intent intent) {
            m_intent = intent;
        }

        public void done() {
            if (m_intent != null) {
                once();
                m_intent = null;
            }
        }

        public abstract void once();
    }

    public void showAcquisitionDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        AcquisitionDialogFragment newFragment = new AcquisitionDialogFragment();
        newFragment.show(fragmentManager, "dialog");
        mAcquisitionDialogFragment = newFragment;
    }

    /**
     * Set the progress bar value
     *
     * @param value Progress value between 0 and 1
     */
    public void progressAcquisitionDialog(float value) {
        if (mAcquisitionDialogFragment == null) {
            return;
        }

        ProgressDialog dialog = (ProgressDialog) mAcquisitionDialogFragment.getDialog();

        if (dialog == null) {
            // Progress dialog is not visible
            return;
        }

        dialog.setProgress((int) (value * 100.0));
    }

    public void removeAcquisitionDialog() {
        if (mAcquisitionDialogFragment == null) {
            return;
        }

        mAcquisitionDialogFragment.dismiss();
        mAcquisitionDialogFragment = null;
    }

    @Override
    public void onAcquisitionDialogCancel(DialogFragment dialog) {
        //#if ENABLE_NET_PROVIDER_ACQUISITION
//        if (mAcquisition != null) {
//            // Cancel download
//            mAcquisition.cancel();
//            mAcquisition = null;
//        }

        boolean requestWasNull = mRequest == null;
        if (!requestWasNull) {
            Future<Response<InputStream>> req = mRequest;
            mRequest = null;
            req.cancel();
        }
        if (requestWasNull) return;

        removeAcquisitionDialog();

        notifyAcquisitionDialogFail();
    }

    public void showPassphraseDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();

        PassphraseDialogFragment newFragment = new PassphraseDialogFragment();

        String hint = mLicense.getPassphraseHint();
        Bundle bundle = new Bundle();
        bundle.putString("passHint", hint);
        newFragment.setArguments(bundle);

        newFragment.show(fragmentManager, "dialog");
    }

    @Override
    public void onPassphraseDialogPositiveClick(DialogFragment dialog, String passPhrase) {
        // User touched the dialog's positive button
        if (passPhrase.isEmpty())
            return;

        mLicense.decrypt(passPhrase);

        if (!mLicense.isDecrypted()) {
            // Unable to decrypt license with the given passphrase
            Toast.makeText(context, "Wrong passphrase: " + mBookName, Toast.LENGTH_LONG).show();
            return;
        }

        decryptAndOpenSelectedBook();
    }

    @Override
    public void onPassphraseDialogNegativeClick(DialogFragment dialog) {
        // User touched the dialog's negative button
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.container_list);
        context = this;

        BookmarkDatabase.initInstance(getApplicationContext());
        final ListView view = (ListView) findViewById(R.id.containerList);


        String epubFolder = getIntent().getStringExtra("epubFolder");
        final File epubpath = new File(epubFolder);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            boolean test = Environment.isExternalStorageEmulated(epubpath);
            boolean breakpoint = true;
        }

        final List<String> list = getInnerBooks(epubpath);

        BookListAdapter bookListAdapter = new BookListAdapter(this, list);
        view.setAdapter(bookListAdapter);

        if (list.isEmpty()) {
            Toast.makeText(
                    context,
                    "No ebooks found: " + epubpath,
                    Toast.LENGTH_LONG).show();
        }

        // Initialize epub3
        // Call it before initializing lcp service to initialize readium filters
        EPub3.initialize();

        // Loads the native lib and sets the path to use for cache
//#if ENABLE_ZIP_ARCHIVE_WRITER
//        EPub3.setCachePath(getCacheDir().getAbsolutePath());

        // Initialize lcp
        // Load certificate
        String certContent = "";
        try {
            InputStream is = getAssets().open("lcp/lcp.crt");
            byte[] data = new byte[is.available()];
            is.read(data);
            is.close();
            certContent = new String(data, "UTF-8");
        } catch (IOException e) {
            // TODO
        }

        StorageProvider storageProvider = new StorageProvider(getApplicationContext());

//#if !DISABLE_NET_PROVIDER
        NetProvider netProvider = new NetProvider(getApplicationContext());

        mLcpService = ServiceFactory.build(
                certContent, storageProvider,

//#if !DISABLE_NET_PROVIDER
                netProvider,
                new CredentialHandler() {
                    @Override
                    public void decrypt(License license) {
                        if (mLicense != null) {
                            // TODO mLicense nativePtr cleanup?
                        }
                        mLicense = license;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showPassphraseDialog();
                            }
                        });
                    }
                },

                new StatusDocumentHandler() {
                    @Override
                    public void process(License license) {

                        if (mLicense != null) {
                            // TODO mLicense nativePtr cleanup?
                        }
                        mLicense = license;

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                launchStatusDocumentProcessing();
                            }
                        });
                    }
                });


        view.setOnItemClickListener(new ListView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                mBookName = list.get(arg2);
                mBookPath = epubpath.getPath() + "/" + mBookName;

                if (FilenameUtils.getExtension(mBookName).equals("lcpl")) {
                    downloadAndOpenSelectedBook();
                } else {
                    decryptAndOpenSelectedBook();
                }
            }
        });
    }

    private void checkLink_RENEW(final StatusDocumentProcessing lsd, final DoneCallback doneCallback_checkLink_RENEW) {

        if (!lsd.isActive()) {
            doneCallback_checkLink_RENEW.Done(false);
            return;
        }

        if (!lsd.hasRenewLink()) {
            doneCallback_checkLink_RENEW.Done(false);
            return;
        }

        showStatusDocumentDialog_RETURN_RENEW("renew", new DoneCallback() {
            @Override
            public void Done(boolean done) {
                if (!done) {
                    doneCallback_checkLink_RENEW.Done(false);
                    return;
                }
                lsd.doRenew(doneCallback_checkLink_RENEW);
            }
        });
    }

    private void checkLink_RETURN(final StatusDocumentProcessing lsd, final DoneCallback doneCallback_checkLink_RETURN) {

        if (!lsd.isActive()) {
            doneCallback_checkLink_RETURN.Done(false);
            return;
        }

        if (!lsd.hasReturnLink()) {
            doneCallback_checkLink_RETURN.Done(false);
            return;
        }

        showStatusDocumentDialog_RETURN_RENEW("return", new DoneCallback() {
            @Override
            public void Done(boolean done) {
                if (!done) {
                    doneCallback_checkLink_RETURN.Done(false);
                    return;
                }
                lsd.doReturn(doneCallback_checkLink_RETURN);
            }
        });
    }

    private AlertDialog showStatusDocumentDialog_RETURN_RENEW(String msgType, final DoneCallback doneCallback_showStatusDocumentDialog_RETURN_RENEW) {

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(ContainerList.this.context);

        alertBuilder.setTitle("LCP EPUB => LSD [" + msgType + "]?");
        alertBuilder.setMessage("License Status Document [" + msgType + "] LCP EPUB?");

        alertBuilder.setOnCancelListener(
                new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        doneCallback_showStatusDocumentDialog_RETURN_RENEW.Done(false);
                    }
                }
        );

        alertBuilder.setOnDismissListener(
                new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                    }
                }
        );

        alertBuilder.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();

                        doneCallback_showStatusDocumentDialog_RETURN_RENEW.Done(true);
                    }
                }
        );
        alertBuilder.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }
        );

        alertBuilder.setCancelable(true);
        AlertDialog alert = alertBuilder.create();
        alert.setCanceledOnTouchOutside(true);

        alert.show(); //async!

        return alert;
    }

    public void launchStatusDocumentProcessing() {

        final AlertDialog alertDialogStatusDocumentProcessingCancel = showStatusDocumentDialog();

        //mLicense
        //mLcpService
        //mBookName
        //mBookPath

        if (mStatusDocumentProcessing != null) {
            mStatusDocumentProcessing.cancel();
            mStatusDocumentProcessing = null;
        }

        StatusDocumentProcessing.IDeviceIDManager deviceIDManager = new StatusDocumentProcessing.IDeviceIDManager() {

            private final String PREF_KEY_DEVICE_ID = "READIUM_LCP_LSD_DEVICE_ID";
            private final String PREF_KEY_DEVICE_ID_CHECK = "READIUM_LCP_LSD_DEVICE_ID_CHECK_";

            @Override
            public String getDeviceNAME() {
                return "Android device";
            }

            @Override
            public String getDeviceID() {
                SharedPreferences sharedPrefs_DEVICEID = ContainerList.this.context.getSharedPreferences(
                        PREF_KEY_DEVICE_ID, Context.MODE_PRIVATE);
                String pref_DEVICEID = sharedPrefs_DEVICEID.getString(PREF_KEY_DEVICE_ID, null);

                if (pref_DEVICEID == null) {

                    String id = UUID.randomUUID().toString();

                    // TODO: weird MAC address on my device...not sure it's reliable (Wifi-ADB, LLDB debug session).
//                try {
//                    WifiManager wm = (WifiManager) ContainerList.this.context.getSystemService(Context.WIFI_SERVICE);
//                    id = wm.getConnectionInfo().getMacAddress();
//                } catch(Exception ex){
//                    // ignore
//                }

                    SharedPreferences.Editor editor = sharedPrefs_DEVICEID.edit();
                    editor.putString(PREF_KEY_DEVICE_ID, id);
                    editor.commit();

                    return id;
                }

                return pref_DEVICEID;
            }

            @Override
            public String checkDeviceID(String key) {

                String PREF_ID = PREF_KEY_DEVICE_ID_CHECK + key;

                SharedPreferences sharedPrefs = ContainerList.this.context.getSharedPreferences(
                        PREF_ID, Context.MODE_PRIVATE);
                String pref = sharedPrefs.getString(PREF_ID, null);
                return pref;
            }

            @Override
            public void recordDeviceID(String key) {

                String PREF_ID = PREF_KEY_DEVICE_ID_CHECK + key;

                SharedPreferences sharedPrefs = ContainerList.this.context.getSharedPreferences(
                        PREF_ID, Context.MODE_PRIVATE);
//              String pref = sharedPrefs.getString(PREF_ID, null);
                SharedPreferences.Editor editor = sharedPrefs.edit();
//                if (pref != null) {
//                    editor.remove(PREF_ID);
//                }

                String id = getDeviceID();

                editor.putString(PREF_ID, id);
                editor.commit();
            }
        };

        mStatusDocumentProcessing = new StatusDocumentProcessing(ContainerList.this.context, mLcpService, mBookPath, mLicense, deviceIDManager);

        mStatusDocumentProcessing.start(new StatusDocumentProcessing.Listener(mStatusDocumentProcessing) {
            @Override
            public void onStatusDocumentProcessingComplete_(final StatusDocumentProcessing lsd) {

                // assert sdp == mStatusDocumentProcessing (unless null)

                if (mStatusDocumentProcessing == null) {
                    return;
                }
                mStatusDocumentProcessing = null;

                if (lsd.wasCancelled()) {
                    return;
                }

                // StatusDocumentProcessing uses Ion lib to create HTTP requests,
                // so the responses are received in a UI thread callback,
                // so in principle this is redundant ...
                // but we keep it anyway to ensure UI thread
                // (just in case the underlying implementation changes)
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        alertDialogStatusDocumentProcessingCancel.dismiss();

                        // Note that when the license is updated (injected) inside the EPUB archive,
                        // the LCPL file has a different canonical form, and therefore the user passphrase
                        // is asked again (even though it probably is exactly the same).
                        // This is because the passphrase is cached in secure storage based on unique keys
                        // for each LCPL file, based on their canonical form (serialised JSON syntax).
                        if (!lsd.isInitialized() || // e.g. network timeout during LSD HTTP fetch3
                                lsd.hasLicenseUpdatePending()) {

                            // Note: this should always be EPUB, not LCPL
                            if (FilenameUtils.getExtension(mBookName).equals("lcpl")) {
                                downloadAndOpenSelectedBook();
                            } else {
                                decryptAndOpenSelectedBook();
                            }

                            return;
                        }

                        // The renew + return LSD interactions are invoked here for demonstration purposes only.
                        // A real-word app would probably expose the return link in a very different fashion,
                        // and may even not necessarily expose the return / renew interactions at the app level (to the end-user),
                        // instead: via an intermediary online service / web page, controlled by the content provider.
                        checkLink_RENEW(lsd, new DoneCallback() {
                            @Override
                            public void Done(final boolean done_checkLink_RENEW) {

                                if (done_checkLink_RENEW) {

                                    // Note: this should always be EPUB, not LCPL
                                    if (FilenameUtils.getExtension(mBookName).equals("lcpl")) {
                                        downloadAndOpenSelectedBook();
                                    } else {
                                        decryptAndOpenSelectedBook();
                                    }

                                    return;
                                }

                                checkLink_RETURN(lsd, new DoneCallback() {
                                    @Override
                                    public void Done(final boolean done_checkLink_RETURN) {

                                        // Note: this should always be EPUB, not LCPL
                                        if (FilenameUtils.getExtension(mBookName).equals("lcpl")) {
                                            downloadAndOpenSelectedBook();
                                        } else {
                                            decryptAndOpenSelectedBook();
                                        }
                                    }
                                });
                            }
                        });
                    }
                });
            }
        });
    }

    public AlertDialog showStatusDocumentDialog() {
//        Toast.makeText(ContainerList.this, "LCP EPUB => License Status Document in progress...", Toast.LENGTH_SHORT)
//                .show();

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);

        alertBuilder.setTitle("LCP EPUB => LSD ...");
        alertBuilder.setMessage("License Status Document in progress...");

        alertBuilder.setOnCancelListener(
                new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        if (mStatusDocumentProcessing != null) {
                            mStatusDocumentProcessing.cancel();
                            mStatusDocumentProcessing = null;
                        }
                    }
                }
        );

        alertBuilder.setOnDismissListener(
                new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                    }
                }
        );
//
//        alertBuilder.setPositiveButton("Okay",
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                    }
//                }
//        );
        alertBuilder.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }
        );

        alertBuilder.setCancelable(true);
        AlertDialog alert = alertBuilder.create();
        alert.setCanceledOnTouchOutside(true);

        alert.show(); //async!

        return alert;
    }

    private void notifyAcquisitionDialogFail() {

//
//                Toast.makeText(ContainerList.this, "LCP EPUB download not completed.", Toast.LENGTH_SHORT)
//                        .show();

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);

        alertBuilder.setTitle("LCP EPUB acquisition ...");
        alertBuilder.setMessage("Download not completed.");

        alertBuilder.setOnCancelListener(
                new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                    }
                }
        );

        alertBuilder.setOnDismissListener(
                new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                    }
                }
        );

        alertBuilder.setPositiveButton("Okay",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }
        );

//                alertBuilder.setNegativeButton("...",
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                dialog.cancel();
//                            }
//                        }
//                );

        alertBuilder.setCancelable(true);
        AlertDialog alert = alertBuilder.create();
        alert.setCanceledOnTouchOutside(true);

        alert.show(); //async!
    }

    @TargetApi(Build.VERSION_CODES.N)
    public Locale getCurrentLocale() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return getResources().getConfiguration().getLocales().get(0);
        } else {
            //noinspection deprecation
            return getResources().getConfiguration().locale;
        }
    }

    /**
     * Acquire epub defined in the LCPL license and open it
     */
    private void downloadAndOpenSelectedBook() {

        InputStream licenseInputStream = null;
        try {
            // Book path is a reference of an LCPL file
            licenseInputStream = new FileInputStream(mBookPath);
        } catch (FileNotFoundException e) {
            // TODO
            return;
        }

        String licenseContent = "";
        try {
            byte[] data = new byte[licenseInputStream.available()];
            licenseInputStream.read(data);
            licenseInputStream.close();
            licenseContent = new String(data, "UTF-8");
        } catch (IOException e) {
            // TODO
            return;
        }

        m_SdkErrorHandler_Messages = new Stack<>();
        EPub3.setSdkErrorHandler(ContainerList.this);
        mLicense = mLcpService.openLicense(licenseContent);
        EPub3.setSdkErrorHandler(null);

        if (mLicense == null) {
            // If license is NULL, it means that an error has occured
            // TODO: Throws an exception instead of returning null license
            Toast.makeText(ContainerList.this, "LCP EPUB license failed to initiate.", Toast.LENGTH_SHORT).show();


            SdkErrorHandlerMessagesCompleted callback = new SdkErrorHandlerMessagesCompleted(null) {
                @Override
                public void once() {
                    // will not be called because passed INTENT is null
                }
            };

            // async!
            popSdkErrorHandlerMessage(context, callback);

            return;
        }


//        // Store downloaded epub in a temporary file
//        File outputDir = this.context.getCacheDir();
//        final File outputFile;
//        try {
//            outputFile = File.createTempFile("readium-lcp", ".epub", outputDir);
//        } catch (IOException e) {
//
//            Toast.makeText(ContainerList.this, "LCP EPUB file download failed to initiate.", Toast.LENGTH_SHORT)
//                    .show();
//            return;
//        }

        File lcplFile = new File(mBookPath);
        File outputDir = lcplFile.getParentFile();
        final File outputFile = new File(outputDir, lcplFile.getName() + ".epub");

        mBookPath = outputFile.getAbsolutePath();
        mBookName = outputFile.getName();


        final String url = mLicense.getLink_Publication();
        // no strict check for content-type
        // See https://github.com/readium/readium-lcp-client/issues/15

//        final AsyncHttpRequestFactory current = Ion.getDefault(context).configure().getAsyncHttpRequestFactory();
//        Ion.getDefault(context).configure().setAsyncHttpRequestFactory(new AsyncHttpRequestFactory() {
//            @Override
//            public AsyncHttpRequest createAsyncHttpRequest(Uri uri, String method, Headers headers) {
//                AsyncHttpRequest ret = current.createAsyncHttpRequest(uri, method, headers);
//                ret.setTimeout(1000);
//                return ret;
//            }
//        });
//

        showAcquisitionDialog();

        progressAcquisitionDialog(0.0f);

        if (mRequest != null) {
            Future<Response<InputStream>> req = mRequest;
            mRequest = null;
            req.cancel();
        }

//        new AsyncTask<Void, Void, Void>() {
//            @Override
//            protected Void doInBackground(Void... params) {

//        Timer timer = new Timer();
//        timer.schedule(new TimerTask() {
//            @Override
//            public void run() {
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {

        Locale currentLocale = getCurrentLocale();
        String langCode = currentLocale.toString().replace('_', '-');
        langCode = langCode + ",en-US;q=0.7,en;q=0.5";

        mRequest = Ion.with(ContainerList.this.context)
                .load("GET", url)
                .setLogging("Readium Ion", Log.VERBOSE)
                .progress(new ProgressCallback() {
                    @Override
                    public void onProgress(long downloaded, long total) {

                        if (mRequest == null) return;

                        // total is -1 when HTTP content-length header is not set.
                        if (total < downloaded) {
                            total = downloaded * 2;
                        }
                        float value = (downloaded / (float) total);
                        progressAcquisitionDialog(value);
                    }
                }) // not UI thread
                //.progressHandler(callback) // UI thread
                //.setTimeout(AsyncHttpRequest.DEFAULT_TIMEOUT) //30000
                .setTimeout(6000)

                // TODO: comment this in production! (this is only for testing a local HTTP server)
                //.setHeader("X-Add-Delay", "2s")

                // LCP / LSD server with message localization
                .setHeader("Accept-Language", langCode)

                .asInputStream()
                .withResponse()

                // UI thread
                .setCallback(new FutureCallback<Response<InputStream>>() {
                    @Override
                    public void onCompleted(Exception e, Response<InputStream> response) {

                        if (mRequest == null) return;
                        mRequest = null;

                        InputStream inputStream = response != null ? response.getResult() : null;
                        int httpResponseCode = response != null ? response.getHeaders().code() : 0;
                        if (e != null || inputStream == null
                                || httpResponseCode < 200 || httpResponseCode >= 300) {

                            progressAcquisitionDialog(1.0f);
                            removeAcquisitionDialog();

                            notifyAcquisitionDialogFail();

                            return;
                        }

                        try {
                            progressAcquisitionDialog(0.0f);

                            FileOutputStream outputStream = new FileOutputStream(outputFile);
                            //inputStream.transferTo(outputStream);

                            int length = 0;
                            try {
                                String strLength = response.getHeaders().getHeaders().get("Content-Length");
                                length = Integer.parseInt(strLength);
                                //length = inputStream.available();
                            } catch (Exception exc) {
                                // ignore
                            }

                            byte[] buf = new byte[16384];
                            int n;
                            int total = 0;
                            while ((n = inputStream.read(buf)) > 0) {
                                total += n;
                                outputStream.write(buf, 0, n);

                                if (length < total) {
                                    length = total * 2;
                                }
                                float val = (total / (float) length);

                                progressAcquisitionDialog(val);
                            }
                            inputStream.close();
                            outputStream.flush();
                            outputStream.close();

                            mLcpService.injectLicense(outputFile.getAbsolutePath(), mLicense);

                            // ensure file I/O in native layer is complete (buffer flush, filesystem sync, etc.)
                            Timer timer = new Timer();
                            timer.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            decryptAndOpenSelectedBook();
                                        }
                                    });
                                }
                            }, 500); // 500ms additional delay after EPUB download seems acceptable?

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        } finally {
                            progressAcquisitionDialog(1.0f);
                            removeAcquisitionDialog();
                        }
                    }
                })
//                .write(new File(dstPath))
//                .setCallback(callback)
        ;


//            }
//        });
//            }

//}, 500);

//                return null;
//            }
//    }.execute();
    }

    private void decryptAndOpenSelectedBook() {

        Toast.makeText(ContainerList.this, "OPEN: " + mBookName, Toast.LENGTH_SHORT).show();

        m_SdkErrorHandler_Messages = new Stack<>();
        EPub3.setSdkErrorHandler(ContainerList.this);
        mContainer = EPub3.openBook(mBookPath);
        EPub3.setSdkErrorHandler(null);

        if (mContainer != null) {
            openSelectedBook();
        } else {

            SdkErrorHandlerMessagesCompleted callback = new SdkErrorHandlerMessagesCompleted(null) {
                @Override
                public void once() {
                    // will not be called because passed INTENT is null
                }
            };

            // async!
            popSdkErrorHandlerMessage(context, callback);
        }
    }

    private void openSelectedBook() {
        ContainerHolder.getInstance().put(mContainer.getNativePtr(), mContainer);

        Intent intent = new Intent(getApplicationContext(), BookDataActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(Constants.BOOK_NAME, mBookName);
        intent.putExtra(Constants.CONTAINER_ID, mContainer.getNativePtr());

        SdkErrorHandlerMessagesCompleted callback = new SdkErrorHandlerMessagesCompleted(intent) {
            @Override
            public void once() {
                startActivity(m_intent);
            }
        };

        // async!
        popSdkErrorHandlerMessage(context, callback);
    }

    // async!
    private void popSdkErrorHandlerMessage(final Context ctx, final SdkErrorHandlerMessagesCompleted callback) {
        if (m_SdkErrorHandler_Messages != null) {

            if (m_SdkErrorHandler_Messages.size() == 0) {
                m_SdkErrorHandler_Messages = null;
                callback.done();
                return;
            }

            String message = m_SdkErrorHandler_Messages.pop();

            AlertDialog.Builder alertBuilder = new AlertDialog.Builder(ctx);

            alertBuilder.setTitle("Warning: " + mBookName);
            alertBuilder.setMessage(message);

            alertBuilder.setOnCancelListener(
                    new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            m_SdkErrorHandler_Messages = null;
                            callback.done();
                        }
                    }
            );

            alertBuilder.setOnDismissListener(
                    new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            popSdkErrorHandlerMessage(ctx, callback);
                        }
                    }
            );

            alertBuilder.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }
            );
            alertBuilder.setNegativeButton("Ignore all",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    }
            );

            alertBuilder.setCancelable(false);
            AlertDialog alert = alertBuilder.create();
            alert.setCanceledOnTouchOutside(false);

            alert.show(); //async!
        } else {
            callback.done();
        }
    }

    @Override
    public boolean handleSdkError(String message, boolean isSevereEpubError) {

        System.out.println("SdkErrorHandler: " + message + " (" + (isSevereEpubError ? "warning" : "info") + ")");

        if (m_SdkErrorHandler_Messages != null && isSevereEpubError) {
            m_SdkErrorHandler_Messages.push(message);
        }

        // never throws an exception
        return true;
    }

    // get books in /sdcard/epubtest path
    private List<String> getInnerBooks(File epubpath) {
        List<String> list = new ArrayList<String>();

        File[] files = epubpath.listFiles();
        if (files != null) {
            for (File f : files) {
                if (!f.isFile()) {
                    continue;
                }

                // Get file extension
                String name = f.getName();
                String ext = FilenameUtils.getExtension(name);

                if (!ext.equals("epub") && !ext.equals("lcpl")) {
                    continue;
                }

                // Only add epub and lcpl files
                list.add(name);
                Log.i("books", name);
            }
        }
        Collections.sort(list, new Comparator<String>() {

            @Override
            public int compare(String s1, String s2) {
                return s1.compareToIgnoreCase(s2);
            }

        });
        return list;
    }
}
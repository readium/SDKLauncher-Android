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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.io.FilenameUtils;
import org.readium.sdk.android.EPub3;
import org.readium.sdk.android.Container;
import org.readium.sdk.android.launcher.model.BookmarkDatabase;
import org.readium.sdk.android.SdkErrorHandler;
import org.readium.sdk.lcp.Acquisition;
import org.readium.sdk.lcp.CredentialHandler;
import org.readium.sdk.lcp.StatusDocumentHandler;
import org.readium.sdk.lcp.License;
import org.readium.sdk.lcp.NetProvider;
import org.readium.sdk.lcp.Service;
import org.readium.sdk.lcp.ServiceFactory;
import org.readium.sdk.lcp.StatusDocumentProcessing;
import org.readium.sdk.lcp.StorageProvider;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
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

import android.app.AlertDialog;
import android.content.DialogInterface;

import com.koushikdutta.ion.builder.Builders;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.koushikdutta.async.future.Future;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.async.http.AsyncHttpRequest;
import com.koushikdutta.async.http.Headers;
import com.koushikdutta.ion.Ion;
import com.koushikdutta.ion.loader.AsyncHttpRequestFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chtian
 *
 */
public class ContainerList extends FragmentActivity
        implements SdkErrorHandler, PassphraseDialogFragment.PassphraseDialogListener,
        AcquisitionDialogFragment.Listener
{

    public abstract class StatusDocumentProcessingListener implements StatusDocumentProcessing.IListener {
        final private StatusDocumentProcessing m_StatusDocumentProcessing;
        public StatusDocumentProcessingListener(StatusDocumentProcessing sdp) {
            m_StatusDocumentProcessing = sdp;
        }
        public void onStatusDocumentProcessingComplete() {
            this.onStatusDocumentProcessingComplete_(m_StatusDocumentProcessing);
        }
        public abstract void onStatusDocumentProcessingComplete_(StatusDocumentProcessing sdp);
    }

    private Context context;
    private Stack<String> m_SdkErrorHandler_Messages = null;
    private License mLicense;
    //#if ENABLE_NET_PROVIDER
//    private Acquisition mAcquisition;
    private StatusDocumentProcessing mStatusDocumentProcessing;
    private AcquisitionDialogFragment mAcquisitionDialogFragment;
    private Container mContainer;
    private String mBookName; // Name of the selected book
    private String mBookPath; // Path of the selected book
    private Service mLcpService;


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

        dialog.setProgress((int)(value*100.0));
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
        //#if ENABLE_NET_PROVIDER
//        if (mAcquisition != null) {
//            // Cancel download
//            mAcquisition.cancel();
//            mAcquisition = null;
//        }

        removeAcquisitionDialog();
    }

    public void showPassphraseDialog() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        PassphraseDialogFragment newFragment = new PassphraseDialogFragment();
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
            Toast.makeText(context, "Bad passphrase", Toast.LENGTH_LONG).show();
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
                    epubpath + "/ is empty, copy epub3 test file first please.",
                    Toast.LENGTH_LONG).show();
        }

        // Initialize epub3
        // Call it before initializing lcp service to initialize readium filters
        EPub3.initialize();

        // Loads the native lib and sets the path to use for cache
        EPub3.setCachePath(getCacheDir().getAbsolutePath());

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
        //#if ENABLE_NET_PROVIDER
//        NetProvider netProvider = new NetProvider(getApplicationContext());
        mLcpService = ServiceFactory.build(
                certContent, storageProvider,
                //#if ENABLE_NET_PROVIDER
//                netProvider,
                new CredentialHandler() {
                    @Override
                    public void decrypt(License license) {
                        mLicense = license;

                        showPassphraseDialog();
//
//                        Timer timer = new Timer();
//                        timer.schedule(new TimerTask() {
//                            @Override
//                            public void run() {
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                    }
//                                });
//                            }
//                        }, 1000);
                    }
                },
                new StatusDocumentHandler() {
                    @Override
                    public void process(License license) {
                        mLicense = license;

                        AlertDialog alert = showStatusDocumentDialog();
                        launchStatusDocumentProcessing(alert);
//
//                        Timer timer = new Timer();
//                        timer.schedule(new TimerTask() {
//                            @Override
//                            public void run() {
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                    }
//                                });
//                            }
//                        }, 1000);
                    }
                });


        view.setOnItemClickListener(new ListView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                mBookName = list.get(arg2);
                mBookPath = epubpath.getPath() + "/" + mBookName;

                Toast.makeText(context, "Select " + mBookName, Toast.LENGTH_SHORT).show();

                if (FilenameUtils.getExtension(mBookName).equals("lcpl")) {
                    downloadAndOpenSelectedBook();
                } else {
                    decryptAndOpenSelectedBook();
                }
//
//                Timer timer = new Timer();
//                timer.schedule(new TimerTask() {
//                    @Override
//                    public void run() {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                            }
//                        });
//                    }
//                }, 1000);
            }
        });
    }

    public void launchStatusDocumentProcessing(final AlertDialog alertDialog) {

        //mLicense
        //mLcpService
        //mBookName
        //mBookPath

        if (mStatusDocumentProcessing != null) {
            mStatusDocumentProcessing.cancel();
            mStatusDocumentProcessing = null;
        }

        mStatusDocumentProcessing = mLicense.createStatusDocumentProcessing(mBookPath);

        if (mStatusDocumentProcessing != null) {

            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {

                    mStatusDocumentProcessing.start(new StatusDocumentProcessingListener(mStatusDocumentProcessing) {
                        @Override
                        public void onStatusDocumentProcessingComplete_(StatusDocumentProcessing sdp) {

                            mStatusDocumentProcessing = null;

                            if (sdp.wasCancelled()) {
                                return;
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    alertDialog.dismiss();

                                    Toast.makeText(context, "Loading: " + mBookName, Toast.LENGTH_SHORT).show();

                                    if (FilenameUtils.getExtension(mBookName).equals("lcpl")) {
                                        downloadAndOpenSelectedBook();
                                    } else {
                                        decryptAndOpenSelectedBook();
                                    }
                                }
                            });
//
//                            Timer timer = new Timer();
//                            timer.schedule(new TimerTask() {
//                                @Override
//                                public void run() {
//                                    runOnUiThread(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                        }
//                                    });
//                                }
//                            }, 1000);
                        }
                    });
                    return null;
                }
            }.execute();
        }
    }

    public AlertDialog showStatusDocumentDialog() {
        Toast.makeText(ContainerList.this, "LCP EPUB => License Status Document in progress...", Toast.LENGTH_SHORT)
                .show();

        AlertDialog.Builder alertBuilder  = new AlertDialog.Builder(context);

        alertBuilder.setTitle("LCP EPUB => LSD ...");
        alertBuilder.setMessage("License Status Document in progress...");

        alertBuilder.setCancelable(true);

        alertBuilder.setOnCancelListener(
                new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        if (mStatusDocumentProcessing != null) {
                            mStatusDocumentProcessing.cancel();
                            mStatusDocumentProcessing = null;

                            mLcpService.SetLicenseStatusDocumentProcessingCancelled();
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

        AlertDialog alert = alertBuilder.create();
        alert.setCanceledOnTouchOutside(false);

        alert.show(); //async!

        return alert;
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
        }

        mLicense = mLcpService.openLicense(licenseInputStream);

        // Store downloaded epub in a temporary file
        File outputDir = this.context.getCacheDir();
        File outputFile = null;
        try {
            outputFile = File.createTempFile("readium-lcp", ".epub", outputDir);
        } catch (IOException e) {
            // TODO
        }

        // New book local path is the temporary path
        mBookPath = outputFile.getAbsolutePath();
        mBookName = outputFile.getName();


        //#if !ENABLE_NET_PROVIDER

        String url = mLicense.publicationLink;

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

        Future<InputStream> request = Ion.with(this.context)
                .load(url)
                .setLogging("Ion", Log.VERBOSE)
                .progress(callback) // not UI thread
                //.progressHandler(callback) // UI thread
                //.setTimeout(AsyncHttpRequest.DEFAULT_TIMEOUT) //30000
                .setTimeout(6000)
                //.setHeader(name, value)
                .asInputStream().setCallback(new FutureCallback<InputStream>() {
                    @Override
                    public void onCompleted(Exception e, InputStream inputStream) {
                        if (e != null) {
                            callback.onCompleted(e, null);
                            return;
                        }
                        if (inputStream == null) {
                            callback.onCompleted(null, null);
                            return;
                        }
                        File destFile = new File(destPath);
                        try {
                            FileOutputStream outputStream = new FileOutputStream(destFile);
                            //inputStream.transferTo(outputStream);

                            byte[] buf = new byte[4096];
                            int n;
                            int total = 0;
                            while((n = inputStream.read(buf))>0){
                                total += n;
                                outputStream.write(buf, 0, n);
                            }
                            outputStream.close();
                            inputStream.close();
                            callback.onCompleted(null, destFile);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                            callback.onCompleted(ex, null);
                        }
                    }
                })
//                .write(new File(dstPath))
//                .setCallback(callback)
                ;
//

        //#if ENABLE_NET_PROVIDER
//
//        // If there is a current acquisition cancel it
//        if (mAcquisition != null) {
//            // Cancel download
//            mAcquisition.cancel();
//            mAcquisition = null;
//        }
//
//        mAcquisition = mLicense.createAcquisition(mBookPath);
//
//        if (mAcquisition != null) {
//
//            showAcquisitionDialog();
//
//            mAcquisition.start(new Acquisition.Listener() {
//                @Override
//                public void onAcquisitionStarted() {
//                    // noop
//                }
//
//                @Override
//                public void onAcquisitionEnded() {
//
//                    // redundant check for cancelled
//                    if (mAcquisition == null) {
//                        return;
//                    }
//
//                    mAcquisition = null;
//
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//
//                            progressAcquisitionDialog(1.0f);
//
//                            removeAcquisitionDialog();
//
//                            Toast.makeText(ContainerList.this, "LCP EPUB download success.", Toast.LENGTH_SHORT)
//                                    .show();
//
//                            decryptAndOpenSelectedBook();
//                        }
//                    });
////
////                    Timer timer = new Timer();
////                    timer.schedule(new TimerTask() {
////                        @Override
////                        public void run() {
////                            runOnUiThread(new Runnable() {
////                                @Override
////                                public void run() {
////                                }
////                            });
////                        }
////                    }, 1000);
//                }
//
//                @Override
//                public void onAcquisitionProgressed(float value) {
//                    // Update progress bar value
//                    progressAcquisitionDialog(value);
//                }
//
//                @Override
//                public void onAcquisitionCanceled() {
//
//                    mAcquisition = null;
//
//                    runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//
//                            progressAcquisitionDialog(1.0f);
//
//                            removeAcquisitionDialog();
//
//                            Toast.makeText(ContainerList.this, "LCP EPUB download failed.", Toast.LENGTH_SHORT)
//                                    .show();
//
//                            AlertDialog.Builder alertBuilder  = new AlertDialog.Builder(context);
//
//                            alertBuilder.setTitle("LCP EPUB acquisition ...");
//                            alertBuilder.setMessage("Download not completed.");
//
//                            alertBuilder.setCancelable(true);
//
//                            alertBuilder.setOnCancelListener(
//                                    new DialogInterface.OnCancelListener() {
//                                        @Override
//                                        public void onCancel(DialogInterface dialog) {
//                                        }
//                                    }
//                            );
//
//                            alertBuilder.setOnDismissListener(
//                                    new DialogInterface.OnDismissListener() {
//                                        @Override
//                                        public void onDismiss(DialogInterface dialog) {
//                                        }
//                                    }
//                            );
//
//                            alertBuilder.setPositiveButton("Okay",
//                                    new DialogInterface.OnClickListener() {
//                                        @Override
//                                        public void onClick(DialogInterface dialog, int which) {
//                                            dialog.dismiss();
//                                        }
//                                    }
//                            );
////                                    alertBuilder.setNegativeButton("...",
////                                            new DialogInterface.OnClickListener() {
////                                                @Override
////                                                public void onClick(DialogInterface dialog, int which) {
////                                                    dialog.cancel();
////                                                }
////                                            }
////                                    );
//
//                            AlertDialog alert = alertBuilder.create();
//                            alert.setCanceledOnTouchOutside(true);
//
//                            alert.show(); //async!
//                        }
//                    });
////
////                    Timer timer = new Timer();
////                    timer.schedule(new TimerTask() {
////                        @Override
////                        public void run() {
////                            runOnUiThread(new Runnable() {
////                                @Override
////                                public void run() {
////                                }
////                            });
////                        }
////                    }, 1000);
//                }
//            });
//        }
    }

    private void decryptAndOpenSelectedBook() {
        m_SdkErrorHandler_Messages = new Stack<>();
        EPub3.setSdkErrorHandler(ContainerList.this);
        mContainer = EPub3.openBook(mBookPath);
        EPub3.setSdkErrorHandler(null);

        if (mContainer != null) {
            // Container opening succeed
            openSelectedBook();
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
    private void popSdkErrorHandlerMessage(final Context ctx, final SdkErrorHandlerMessagesCompleted callback)
    {
        if (m_SdkErrorHandler_Messages != null) {

            if (m_SdkErrorHandler_Messages.size() == 0) {
                m_SdkErrorHandler_Messages = null;
                callback.done();
                return;
            }

            String message = m_SdkErrorHandler_Messages.pop();

            AlertDialog.Builder alertBuilder  = new AlertDialog.Builder(ctx);

            alertBuilder.setTitle("EPUB warning");
            alertBuilder.setMessage(message);

            alertBuilder.setCancelable(false);

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

            alertBuilder.setPositiveButton("Ignore",
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

            AlertDialog alert = alertBuilder.create();
            alert.setCanceledOnTouchOutside(false);

            alert.show(); //async!
        }
        else {
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
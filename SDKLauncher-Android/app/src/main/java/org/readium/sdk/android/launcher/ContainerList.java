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

import org.apache.commons.io.FilenameUtils;
import org.readium.sdk.android.Container;
import org.readium.sdk.android.EPub3;
import org.readium.sdk.android.SdkErrorHandler;
import org.readium.sdk.android.launcher.model.BookmarkDatabase;

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
        implements SdkErrorHandler {

    private Context context;
    private Stack<String> m_SdkErrorHandler_Messages = null;

    private Container mContainer;
    private String mBookName; // Name of the selected book
    private String mBookPath; // Path of the selected book
    
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

        view.setOnItemClickListener(new ListView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {
                mBookName = list.get(arg2);
                mBookPath = epubpath.getPath() + "/" + mBookName;

				checkAndOpenSelectedBook();
            }
        });
    }

    private void checkAndOpenSelectedBook() {

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

                if (!ext.equals("epub")) {
                    continue;
                }

                // Only add epub files
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
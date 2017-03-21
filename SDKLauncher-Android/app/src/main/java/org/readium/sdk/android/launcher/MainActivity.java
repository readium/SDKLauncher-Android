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

import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.nononsenseapps.filepicker.FilePickerActivity;

import java.io.File;

import org.readium.sdk.lcp.ServiceFactory;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import io.fabric.sdk.android.Fabric;

public class MainActivity extends Activity {

    private static final int STOPSPLASH = 0;
    private static final long SPLASHTIME = 500;
    private final String testPath = "epubtest";

    //@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 0 && resultCode == Activity.RESULT_OK) {
//            if (data.getBooleanExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false)) {
//                // For JellyBean and above
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
//                    ClipData clip = data.getClipData();
//
//                    if (clip != null) {
//                        for (int i = 0; i < clip.getItemCount(); i++) {
//                            Uri uri = clip.getItemAt(i).getUri();
//                            // Do something with the URI
//                        }
//                    }
//                    // For Ice Cream Sandwich
//                } else {
//                    ArrayList<String> paths = data.getStringArrayListExtra
//                            (FilePickerActivity.EXTRA_PATHS);
//
//                    if (paths != null) {
//                        for (String path: paths) {
//                            Uri uri = Uri.parse(path);
//                            // Do something with the URI
//                        }
//                    }
//                }
//
//            }

            Uri uri = data.getData();

            Intent listIntent = new Intent(getApplicationContext(),
                    ContainerList.class);
            listIntent.putExtra("epubFolder", uri.getPath());
            startActivity(listIntent);
            MainActivity.this.finish();
        }
    }

    // handler for splash screen
    private Handler splashHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case STOPSPLASH:

                File sdcard = Environment.getExternalStorageDirectory();
                File epubpath = new File(sdcard, testPath);
                epubpath.mkdirs();

                //String path = epubpath.getPath();
                //Uri uri = Uri.parse(path);

                Intent i = new Intent(MainActivity.this.getApplicationContext(), FilePickerActivity.class);
                // Intent i = new Intent(Intent.ACTION_GET_CONTENT);

                // Set these depending on your use case. These are the defaults.
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_MULTIPLE, false);
                i.putExtra(FilePickerActivity.EXTRA_ALLOW_CREATE_DIR, false);
                i.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_DIR);

                i.putExtra(FilePickerActivity.EXTRA_START_PATH, epubpath.getPath());

                startActivityForResult(i, 0);

                //Crashlytics.logException(new Exception("Crashlytics ready."));

                break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CrashlyticsCore core = new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build();

        Crashlytics kit = new Crashlytics.Builder().core(core).build(); //new Crashlytics()
        //kit.setDebugMode(true);

        //Fabric.with(this, kit);
        final Fabric fabric = new Fabric.Builder(this)
                .kits(kit)
                .debuggable(false)
                .build();
        Fabric.with(fabric);

//        Fabric.with(this, new Crashlytics());

        setContentView(R.layout.activity_main);
        Message msg = new Message();
        msg.what = STOPSPLASH;
        splashHandler.sendMessageDelayed(msg, SPLASHTIME);
    }

}
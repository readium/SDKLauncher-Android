/*
 * WebViewActivity.java
 * SDKLauncher-Android
 *
 * Created by Yonathan Teitelbaum (Mantano) on 2013-07-10.
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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class WebViewActivity extends Activity implements
        ViewerSettingsDialog.OnViewerSettingsChange {

    private static final String TAG = "WebViewActivity";
    private EpubWebViewFragment mWebViewFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);

        Intent intent = getIntent();
        if (intent.getFlags() == Intent.FLAG_ACTIVITY_NEW_TASK) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                if (findViewById(R.id.webview_fragment_container) != null) {
                    if (savedInstanceState != null) {
                        return;
                    }
                    mWebViewFragment = new EpubWebViewFragment();

                    mWebViewFragment.setArguments(getIntent().getExtras());
                    getFragmentManager().beginTransaction()
                            .add(R.id.webview_fragment_container, mWebViewFragment)
                            .commit();
                }
            }
        }
    }


    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int itemId = item.getItemId();
        switch (itemId) {
            case R.id.add_bookmark:
                mWebViewFragment.readiumJSApi.bookmarkCurrentPage();
                return true;
            case R.id.settings:
                showSettings();
                return true;
            case R.id.mo_previous:
                mWebViewFragment.readiumJSApi.previousMediaOverlay();
                return true;
            case R.id.mo_play:
                mWebViewFragment.readiumJSApi.toggleMediaOverlay();
                return true;
            case R.id.mo_pause:
                mWebViewFragment.readiumJSApi.toggleMediaOverlay();
                return true;
            case R.id.mo_next:
                mWebViewFragment.readiumJSApi.nextMediaOverlay();
                return true;
        }
        return false;
    }

    public void onClick(View v) {
        if (v.getId() == R.id.left) {
            mWebViewFragment.readiumJSApi.openPageLeft();
        } else if (v.getId() == R.id.right) {
            mWebViewFragment.readiumJSApi.openPageRight();
        }
    }

    private void showSettings() {

    }

    @Override
    public void onViewerSettingsChange(ViewerSettings viewerSettings) {
        updateSettings(viewerSettings);
    }

    private void updateSettings(ViewerSettings viewerSettings) {

        mWebViewFragment.readiumJSApi.updateSettings(viewerSettings);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.web_view, menu);

        MenuItem mo_previous = menu.findItem(R.id.mo_previous);
        MenuItem mo_next = menu.findItem(R.id.mo_next);
        MenuItem mo_play = menu.findItem(R.id.mo_play);
        MenuItem mo_pause = menu.findItem(R.id.mo_pause);

        // show menu only when its reasonable
        mo_previous.setVisible(false);
        mo_next.setVisible(false);
        mo_play.setVisible(false);
        mo_pause.setVisible(false);

        return true;
    }


}

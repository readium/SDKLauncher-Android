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

import java.util.List;

import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import org.readium.sdk.android.Container;
import org.readium.sdk.android.launcher.model.Bookmark;
import org.readium.sdk.android.launcher.model.BookmarkDatabase;
import org.readium.sdk.android.launcher.model.OpenPageRequest;

public class BookmarksActivity extends Activity {
	
    protected static final String TAG = "BookmarksActivity";
	private Context context;
    private Button back;
	private Container container;
	private long containerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_marks);

        context = this;
        back = (Button) findViewById(R.id.backToBookView7);
        Intent intent = getIntent();
        if (intent.getFlags() == Intent.FLAG_ACTIVITY_NEW_TASK) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                String value = extras.getString(Constants.BOOK_NAME);
                containerId = extras.getLong(Constants.CONTAINER_ID);
                container = ContainerHolder.getInstance().get(containerId);
                back.setText(value);
            }
        }

        final ListView itmes = (ListView) findViewById(R.id.bookmarks);

        this.setListViewContent(itmes, BookmarkDatabase.getInstance().getBookmarks(container.getName()));

        initListener();
    }

    private void setListViewContent(final ListView listView, final List<Bookmark> bookmarks) {
        final BookmarkListAdapter bookListAdapter = new BookmarkListAdapter(this, bookmarks);
        listView.setAdapter(bookListAdapter);
        listView.setOnItemClickListener(new ListView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            	Bookmark bookmark = bookmarks.get(position);
        		Intent intent = new Intent(context, WebViewActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        		intent.putExtra(Constants.CONTAINER_ID, containerId);
        		OpenPageRequest openPageRequest = OpenPageRequest.fromIdrefAndCfi(bookmark.getIdref(), bookmark.getContentCfi());
        		try {
					intent.putExtra(Constants.OPEN_PAGE_REQUEST_DATA, openPageRequest.toJSON().toString());
            		startActivity(intent);
				} catch (JSONException e) {
					Log.e(TAG, ""+e.getMessage(), e);
				}
            }
        });
        listView.setOnItemLongClickListener(new ListView.OnItemLongClickListener() {

            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
            	new AlertDialog.Builder(context).
            	setTitle(R.string.bookmarks).
            	setMessage(R.string.delete_bookmark).
            	setNegativeButton(android.R.string.cancel, null).
            	setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						bookmarks.remove(position);
						BookmarkDatabase.getInstance().setBookmarks(container.getName(), bookmarks);
						bookListAdapter.notifyDataSetChanged();
					}
				}).show();
        		return true;
            }
        });
    }

    private void initListener() {
        back.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
}

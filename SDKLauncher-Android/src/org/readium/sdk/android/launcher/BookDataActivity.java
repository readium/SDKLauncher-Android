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

import java.util.Arrays;

import org.readium.sdk.android.Container;
import org.readium.sdk.android.EPub3;
import org.readium.sdk.android.launcher.model.BookmarkDatabase;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

public class BookDataActivity extends Activity {

	private Context context;
	private Container container;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_data);

        context = this;
        Intent intent = getIntent();
        if (intent.getFlags() == Intent.FLAG_ACTIVITY_NEW_TASK) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                String value = extras.getString(Constants.BOOK_NAME);
                getActionBar().setTitle(value);
                container = ContainerHolder.getInstance().get(extras.getLong(Constants.CONTAINER_ID));
                if (container == null) {
                	finish();
                	return;
                }
            }
        }

        initMetadata();
        initPageList();
        initBookmark();
    }

    private void initBookmark() {
        int number = BookmarkDatabase.getInstance().getBookmarks(container.getName()).size();
        final ListView bookmark = (ListView) findViewById(R.id.bookmark);
        String bookmarks = "Bookmarks (" + number + ")";
        String[] bookmark_values = new String[] { bookmarks };

        Class<?>[] classList = new Class<?>[] { BookmarksActivity.class };
        this.setListViewContent(bookmark, bookmark_values, classList);
	}

	private void initPageList() {
        final ListView pageList = (ListView) findViewById(R.id.pageList);
        String[] pageList_values = new String[] {
                getString(R.string.list_of_figures),
                getString(R.string.list_of_illustrations),
                getString(R.string.list_of_tables),
                getString(R.string.page_list),
                getString(R.string.table_of_contents) };

        Class<?>[] classList = new Class<?>[] { 
        		ListOfFiguresActivity.class,
        		ListOfIllustrationsActivity.class,
        		ListOfTablesActivity.class,
        		PageListActivity.class,
        		TableOfContentsActivity.class };
        this.setListViewContent(pageList, pageList_values, classList);
	}

	private void initMetadata() {
        final ListView metadata = (ListView) findViewById(R.id.metaData);
        String[] metadata_values = new String[] { 
        		getString(R.string.metadata),
                getString(R.string.spine_items) };

        Class<?>[] classList = new Class<?>[] { 
        		MetaDataActivity.class,
        		SpineItemsActivity.class };
        this.setListViewContent(metadata, metadata_values, classList);
	}

	private void setListViewContent(ListView view, String[] stringArray,final Class<?>[] classes) {
        BookListAdapter bookListAdapter = new BookListAdapter(this, Arrays.asList(stringArray));
        view.setAdapter(bookListAdapter);
        view.setOnItemClickListener(new ListView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
                Intent intent = new Intent(context, classes[arg2]);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Constants.BOOK_NAME, container.getName());
                intent.putExtra(Constants.CONTAINER_ID, container.getNativePtr());
                
                startActivity(intent);
            }
        });
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
        initBookmark();
    }
    
    @Override
    public void onBackPressed() {
    	super.onBackPressed();
    	if (container != null) {
    		ContainerHolder.getInstance().remove(container.getNativePtr());
    		
    		// Close book (need to figure out if this is the best place...)
    		EPub3.closeBook(container);
    	}
    }
}

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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.readium.sdk.android.EPub3;
import org.readium.sdk.android.Container;
import org.readium.sdk.android.launcher.model.BookmarkDatabase;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

/**
 * @author chtian
 * 
 */
public class ContainerList extends Activity {
    private Context context;
    private final String testPath = "epubtest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_list);
        context = this;
        BookmarkDatabase.initInstance(getApplicationContext());
        final ListView view = (ListView) findViewById(R.id.containerList);

        final List<String> list = getInnerBooks();

        BookListAdapter bookListAdapter = new BookListAdapter(this, list);
        view.setAdapter(bookListAdapter);

        if (list.isEmpty()) {
            Toast.makeText(
                    context,
                    Environment.getExternalStorageDirectory().getAbsolutePath()
                            + "/" + testPath
                            + "/ is empty, copy epub3 test file first please.",
                    Toast.LENGTH_LONG).show();
        }

        view.setOnItemClickListener(new ListView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {

                Toast.makeText(context, "Select " + list.get(arg2),
                        Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getApplicationContext(),
                        BookDataActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Constants.BOOK_NAME, list.get(arg2));

                String path = Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/" + testPath + "/" + list.get(arg2);
                
                Container container = EPub3.openBook(path);
                ContainerHolder.getInstance().put(container.getNativePtr(), container);
                intent.putExtra(Constants.CONTAINER_ID, container.getNativePtr());
                startActivity(intent);
            }
        });
        
        // Loads the native lib and sets the path to use for cache
        EPub3.setCachePath(getCacheDir().getAbsolutePath());
    }

    // get books in /sdcard/epubtest path
    private List<String> getInnerBooks() {
        List<String> list = new ArrayList<String>();
        File sdcard = Environment.getExternalStorageDirectory();
        File epubpath = new File(sdcard, "epubtest");
        epubpath.mkdirs();
        File[] files = epubpath.listFiles();
		if (files != null) {
	        for (File f : files) {
	            if (f.isFile()) {
	                String name = f.getName();
	                if (name.length() > 5
	                        && name.substring(name.length() - 5).equals(".epub")) {
	
	                    list.add(name);
	                    Log.i("books", name);
	                }
	            }
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

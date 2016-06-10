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

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import org.readium.sdk.android.Container;
import org.readium.sdk.android.Package;
import org.readium.sdk.android.components.navigation.NavigationElement;
import org.readium.sdk.android.components.navigation.NavigationPoint;
import org.readium.sdk.android.components.navigation.NavigationTable;
import org.readium.sdk.android.launcher.model.OpenPageRequest;

public abstract class NavigationTableActivity extends Activity {
	
    private static final String TAG = "NavigationTableActivity";
	private Context context;
    private Button back;
    protected Package pckg;
	protected long containerId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.table_of_contents);

        context = this;
        back = (Button) findViewById(R.id.backToBookView6);
        Intent intent = getIntent();
        if (intent.getFlags() == Intent.FLAG_ACTIVITY_NEW_TASK) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                String value = extras.getString(Constants.BOOK_NAME);
                back.setText(value);
                containerId = extras.getLong(Constants.CONTAINER_ID);
                Container container = ContainerHolder.getInstance().get(containerId);
                if (container == null) {
                	finish();
                	return;
                }
                pckg = container.getDefaultPackage();
            }
        }

        final ListView items = (ListView) findViewById(R.id.tableOfContents);

        this.setListViewContent(items, getNavigationTable());

        initListener();
    }

	protected abstract NavigationTable getNavigationTable();

    protected void setListViewContent(ListView view, final NavigationTable navigationTable) {
    	List<String> list = flatNavigationTable(navigationTable, new ArrayList<String>(), "");
    	final List<NavigationElement> navigationElements = flatNavigationTable(navigationTable, new ArrayList<NavigationElement>());
        BookListAdapter bookListAdapter = new BookListAdapter(this, list);
        view.setAdapter(bookListAdapter);
        view.setOnItemClickListener(new ListView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                    long arg3) {
            	NavigationElement navigation = navigationElements.get(arg2);
            	if (navigation instanceof NavigationPoint) {
            		NavigationPoint point = (NavigationPoint) navigation;
            		Log.i(TAG, "Open webview at : "+point.getContent());
            		Intent intent = new Intent(NavigationTableActivity.this, WebViewActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            		intent.putExtra(Constants.CONTAINER_ID, containerId);
            		OpenPageRequest openPageRequest = OpenPageRequest.fromContentUrl(point.getContent(), navigationTable.getSourceHref());
            		try {
						intent.putExtra(Constants.OPEN_PAGE_REQUEST_DATA, openPageRequest.toJSON().toString());
	            		startActivity(intent);
					} catch (JSONException e) {
						Log.e(TAG, ""+e.getMessage(), e);
					}
            	}
                Toast.makeText(context, "this is item " + navigation.getTitle(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<String> flatNavigationTable(NavigationElement parent,
			List<String> list, String shift) {
    	String newShift = shift + "   ";
        for (NavigationElement ne : parent.getChildren()) {
            list.add(shift + ne.getTitle()+" ("+ne.getChildren().size()+")");
            flatNavigationTable(ne, list, newShift);
		}
		return list;
	}

    private List<NavigationElement> flatNavigationTable(NavigationElement parent,
			List<NavigationElement> list) {
        for (NavigationElement ne : parent.getChildren()) {
            list.add(ne);
            flatNavigationTable(ne, list);
		}
		return list;
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


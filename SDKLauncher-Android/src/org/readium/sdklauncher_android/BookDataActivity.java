package org.readium.sdklauncher_android;

import java.util.ArrayList;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class BookDataActivity extends Activity {
	private Context context;
	private Button back;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.book_data);

		context = this;
		back = (Button) findViewById(R.id.backToContainerView);
		TextView bookname = (TextView) findViewById(R.id.bookname);
		Intent intent = getIntent();
		if (intent.getFlags() == Intent.FLAG_ACTIVITY_NEW_TASK) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				String value = extras.getString("bookname");
				bookname.setText(value);
			}
		}
		final ListView metadata = (ListView) findViewById(R.id.metaData);
		final ListView pageList = (ListView) findViewById(R.id.pageList);
		final ListView bookmark = (ListView) findViewById(R.id.bookmark);
		int number = 0;
		String bookmarks = "Bookmarks(" + number + ")";
		String[] metadata_values = new String[] { "Metadata", "Spine" };
		String[] pageList_values = new String[] { "List of Figures",
				"List of Illustrations", "List of Tables", "Page List",
				"Table of Contents" };
		String[] bookmark_values = new String[] { bookmarks };

		this.setListViewContent(metadata, metadata_values);
		this.setListViewContent(pageList, pageList_values);
		this.setListViewContent(bookmark, bookmark_values);
		initListener();
	}

	private void setListViewContent(ListView view, String[] stringArray) {
		final ArrayList<String> list = new ArrayList<String>();
		for (int i = 0; i < stringArray.length; i++) {
			list.add(stringArray[i]);
		}
		BookListAdapter bookListAdapter = new BookListAdapter(this, list);
		view.setAdapter(bookListAdapter);
		view.setOnItemClickListener(new ListView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				Toast.makeText(context, "this is item " + list.get(arg2),
						Toast.LENGTH_SHORT).show();
			}
		});
	}

	private void initListener() {
		back.setOnClickListener(new Button.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}
}

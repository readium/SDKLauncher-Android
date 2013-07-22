package org.readium.sdk.android.launcher;

import java.util.List;

import org.readium.sdk.android.launcher.model.Bookmark;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class BookmarkListAdapter extends ArrayAdapter<Bookmark> {

	public BookmarkListAdapter(Context context, List<Bookmark> bookmarks) {
		super(context, android.R.layout.simple_list_item_2, bookmarks);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_2, null);
		}
		Bookmark bookmark = getItem(position);
		TextView textView1 = (TextView) convertView.findViewById(android.R.id.text1);
		textView1.setText(bookmark.getTitle());
		TextView textView2 = (TextView) convertView.findViewById(android.R.id.text2);
		textView2.setText(bookmark.getIdref() + " - " + bookmark.getContentCfi());
		return convertView;
	}

}

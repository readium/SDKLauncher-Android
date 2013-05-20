package org.readium.sdklauncher_android;
import java.util.ArrayList;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class BookListAdapter extends BaseAdapter {
	private ArrayList<String> mData;
	private Context context;
	public BookListAdapter(Context context,ArrayList<String> list) {
		this.mData = list;
		this.context = context;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mData.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mData.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder = null;
		if (convertView == null) {
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(
					R.layout.book_item, null);
//			holder.img = (ImageView) convertView.findViewById(R.id.afd_img);
//			holder.title = (TextView) convertView.findViewById(R.id.afd_title);
			holder.info = (TextView) convertView.findViewById(R.id.bookname_item);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
//		holder.img.setBackgroundResource((Integer) mData.get(position).get(
//				"img"));
//		holder.title.setText((String) mData.get(position).get("title"));
		holder.info.setText(mData.get(position));
		return convertView;
	}
	
	public final class ViewHolder {
//		public ImageView img;
//		public TextView title;
		public TextView info;
	}
}

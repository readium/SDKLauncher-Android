/**
 * 
 */
package org.readium.sdklauncher_android;

import java.io.IOException;
import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.container_list);
        context = this;
        final ListView view = (ListView)findViewById(R.id.containerList);
        //String[] values = new String[]{"Android", "iPhone", "iPod"};
        
        final ArrayList<String> list = getInnerBooks();
        
        BookListAdapter bookListAdapter = new BookListAdapter(this,list);
        view.setAdapter(bookListAdapter);
        view.setOnItemClickListener(new ListView.OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				Toast.makeText(context, "this is item "+list.get(arg2), Toast.LENGTH_SHORT).show();
				Intent intent = new Intent(getApplicationContext(),
                        BookDataActivity.class);
				intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				intent.putExtra("bookname", list.get(arg2));
                startActivity(intent);
                //ContainerList.this.finish();
			}  	
        });
    }
    private ArrayList<String> getInnerBooks() {
    	ArrayList<String> list = new ArrayList<String>();
		AssetManager assetManager = context.getAssets();
		try {
			String[] files = assetManager.list("TestData");
			for (int i = 0; i < files.length; i++) {
				String temp = files[i];
				if (temp.length() > 5
						&& temp.substring(temp.length() - 5).equals(".epub")) {

					list.add(files[i]);
					Log.e("the books", files[i]);
				}
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return list;
	}

}

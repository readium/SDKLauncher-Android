/**
 * 
 */
package org.readium.sdklauncher_android;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
        String[] values = new String[]{"Android", "iPhone", "iPod"};
        
        final ArrayList<String> list = new ArrayList<String>();
        for(int i = 0; i < values.length;i++){
            list.add(values[i]);
        }
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

}

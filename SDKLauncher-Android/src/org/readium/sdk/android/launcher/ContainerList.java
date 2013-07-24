/**
 * 
 */
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

                Container container = EPub3.openBook(Environment.getExternalStorageDirectory().getAbsolutePath()
                        + "/" + testPath + "/" + list.get(arg2));
                ContainerHolder.getInstance().put(container.getNativePtr(), container);
                intent.putExtra(Constants.CONTAINER_ID, container.getNativePtr());
                startActivity(intent);
            }
        });
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

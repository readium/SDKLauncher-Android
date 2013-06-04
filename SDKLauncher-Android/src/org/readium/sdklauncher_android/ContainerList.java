/**
 * 
 */
package org.readium.sdklauncher_android;

import java.io.File;
import java.util.ArrayList;

import org.readium.sdk.android.EPub3;

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
        final ListView view = (ListView) findViewById(R.id.containerList);

        final ArrayList<String> list = getInnerBooks();

        BookListAdapter bookListAdapter = new BookListAdapter(this, list);
        view.setAdapter(bookListAdapter);

        if (0 == list.size()) {
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

                // TODO: Get book content object.....
                EPub3 epub = new EPub3();
                int handle = epub.openBook(Environment
                        .getExternalStorageDirectory().getAbsolutePath()
                        + "/"
                        + testPath + "/" + list.get(arg2));
                // ---------------------------

                Intent intent = new Intent(getApplicationContext(),
                        BookDataActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("bookname", list.get(arg2));
                startActivity(intent);
            }
        });
    }

    // get books in /sdcard/epubtest path
    private ArrayList<String> getInnerBooks() {
        ArrayList<String> list = new ArrayList<String>();
        File sdcard = Environment.getExternalStorageDirectory();
        File epubpath = new File(sdcard, "epubtest");
        for (File f : epubpath.listFiles()) {
            if (f.isFile()) {
                String name = f.getName();
                if (name.length() > 5
                        && name.substring(name.length() - 5).equals(".epub")) {

                    list.add(name);
                    Log.i("books", name);
                }
            }
        }

        return list;
    }

}

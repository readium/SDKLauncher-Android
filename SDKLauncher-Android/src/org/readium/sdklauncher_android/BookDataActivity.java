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
    private TextView bookname;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.book_data);

        context = this;
        back = (Button) findViewById(R.id.backToContainerView);
        bookname = (TextView) findViewById(R.id.bookname);
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

        String[] metadata_values = new String[] { getString(R.string.metadata),
                getString(R.string.spine_items) };

        ArrayList<Class<?>> classList = new ArrayList<Class<?>>();
        classList.add(MetaDataActivity.class);
        classList.add(MetaDataActivity.class);
        this.setListViewContent(metadata, metadata_values, classList);
        classList = null;
        
        String[] pageList_values = new String[] {
                getString(R.string.list_of_figures),
                getString(R.string.list_of_illustrations),
                getString(R.string.list_of_tables),
                getString(R.string.page_list),
                getString(R.string.table_of_contents) };
        
        classList = new ArrayList<Class<?>>();
        classList.add(MetaDataActivity.class);
        classList.add(MetaDataActivity.class);
        classList.add(MetaDataActivity.class);
        classList.add(MetaDataActivity.class);
        classList.add(MetaDataActivity.class);
        this.setListViewContent(pageList, pageList_values, classList);
        classList = null;

        int number = 0;
        String bookmarks = "Bookmarks(" + number + ")";
        String[] bookmark_values = new String[] { bookmarks };

        classList = new ArrayList<Class<?>>();
        classList.add(MetaDataActivity.class);
        this.setListViewContent(bookmark, bookmark_values, classList);
        classList = null;

        initListener();
    }

    private void setListViewContent(ListView view, String[] stringArray,final ArrayList<Class<?>> classList) {
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
                Toast.makeText(context, "this is item " + Integer.toString(arg2),
                        Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(getApplicationContext(),
                		classList.get(arg2));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra("bookname", bookname.getText());
                startActivity(intent);
            }
        });
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

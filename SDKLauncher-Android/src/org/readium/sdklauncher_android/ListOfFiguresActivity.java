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
import android.widget.Toast;

public class ListOfFiguresActivity extends Activity {
    private Context context;
    private Button back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.list_of_figures);

        context = this;
        back = (Button) findViewById(R.id.backToBookView2);
        Intent intent = getIntent();
        if (intent.getFlags() == Intent.FLAG_ACTIVITY_NEW_TASK) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                String value = extras.getString("bookname");
                back.setText(value);
            }
        }

        // TODO:......
        final ListView itmes = (ListView) findViewById(R.id.listOfFigures);

        // TODO:Add itmes to array.....
        String[] metadata_values = new String[] { "figure 1", "figure 2" };

        this.setListViewContent(itmes, metadata_values);

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
                Toast.makeText(context, "this is item " + list.get(arg2),
                        Toast.LENGTH_SHORT).show();
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

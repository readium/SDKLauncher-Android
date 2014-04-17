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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.readium.sdk.android.Container;
import org.readium.sdk.android.Package;

public class MetaDataActivity extends Activity {

	private Button back;
	private Package pckg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.meta_data);

        back = (Button) findViewById(R.id.backToBookView);
        
        Intent intent = getIntent();
        if (intent.getFlags() == Intent.FLAG_ACTIVITY_NEW_TASK) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                String value = extras.getString(Constants.BOOK_NAME);
                back.setText(value);
                Container container = ContainerHolder.getInstance().get(extras.getLong(Constants.CONTAINER_ID));
                pckg = container.getDefaultPackage();
            }
        }
        if (pckg != null) {
        	setStringOnTextView(R.id.metadata_title, R.string.metadata_title, pckg.getTitle());
        	setStringOnTextView(R.id.metadata_subtitle, R.string.metadata_subtitle, pckg.getSubtitle());
        	setStringOnTextView(R.id.metadata_full_title, R.string.metadata_full_title, pckg.getFullTitle());
        	setStringOnTextView(R.id.metadata_package_id, R.string.metadata_package_id, pckg.getPackageID());
        	setStringOnTextView(R.id.metadata_isbn, R.string.metadata_isbn, pckg.getIsbn());
        	setStringOnTextView(R.id.metadata_language, R.string.metadata_language, pckg.getLanguage());
        	setStringOnTextView(R.id.metadata_copyright_owner, R.string.metadata_copyright_owner, pckg.getCopyrightOwner());
        	setStringOnTextView(R.id.metadata_source, R.string.metadata_source, pckg.getSource());
        	setStringOnTextView(R.id.metadata_subjects, R.string.metadata_subjects, pckg.getSubjects());
        	setStringOnTextView(R.id.metadata_modification_date, R.string.metadata_modification_date, pckg.getModificationDate());
        	setStringOnTextView(R.id.metadata_authors, R.string.metadata_authors, pckg.getAuthorList());
        }

        initListener();
    }

    private void setStringOnTextView(int viewId, int messageId, Object... args) {
        ((TextView) findViewById(viewId)).setText(getString(messageId, args));
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

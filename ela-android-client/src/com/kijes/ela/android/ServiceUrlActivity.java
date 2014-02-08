/*
 * Copyright (c) 2014 Rafal Kijewski <kijes@kijes.com>
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package com.kijes.ela.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class ServiceUrlActivity extends Activity {
    private static final String TAG = "ServiceUrlActivity";
    private static final boolean D = false;

    public static String SERVICE_URL_PROPERTY = "SERVICE_URL_PROPERTY";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.service_url);

        setResult(Activity.RESULT_CANCELED);

        EditText serviceUrlEdit = (EditText)findViewById(R.id.service_url_edit);
        String serviceUrl = getIntent().getExtras().getString(SERVICE_URL_PROPERTY);
        if (serviceUrl != null) {
        	serviceUrlEdit.setText(serviceUrl);
        }

        Button saveButton = (Button) findViewById(R.id.service_url_button_save);
        saveButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                EditText serviceUrlEdit = (EditText)findViewById(R.id.service_url_edit);

                String url = serviceUrlEdit.getText().toString(); 
                Intent intent = new Intent();
                intent.putExtra(SERVICE_URL_PROPERTY, url);
                setResult(Activity.RESULT_OK, intent);
                finish();            	
            }
        });
    }
}

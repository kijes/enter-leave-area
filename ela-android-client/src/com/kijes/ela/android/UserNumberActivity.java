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

public class UserNumberActivity extends Activity {
    private static final String TAG = "UserNumberActivity";
    private static final boolean D = false;

    public static String USER_NUMBER_PROPERTY = "USER_NUMBER_PROPERTY";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.user_number);

        setResult(Activity.RESULT_CANCELED);
        EditText userNumberEdit = (EditText)findViewById(R.id.user_number_edit);
        String userNumber = getIntent().getExtras().getString(USER_NUMBER_PROPERTY);
        if (userNumber != null) {
        	userNumberEdit.setText(userNumber);
        }

        Button saveButton = (Button)findViewById(R.id.user_number_button_save);
        saveButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {

                EditText userNumberEdit = (EditText)findViewById(R.id.user_number_edit);

                String userNumber = userNumberEdit.getText().toString(); 
                Intent intent = new Intent();
                intent.putExtra(USER_NUMBER_PROPERTY, userNumber);
                setResult(Activity.RESULT_OK, intent);
                finish();            	
            }
        });
    }
}

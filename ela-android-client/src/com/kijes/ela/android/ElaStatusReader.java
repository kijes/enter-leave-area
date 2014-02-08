/*
 * Copyright (c) 2014 Rafal Kijewski <kijes@kijes.com>
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package com.kijes.ela.android;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ElaStatusReader extends Activity {

    private static final String TAG = "ElaStatusReader";
    private static final boolean D = false;

    private static final int OPTION_SERVICE_URL = 1;
    private static final int OPTION_USER_NUMBER = 2;

    public static final int MESSAGE_REQUEST_TRY = 1;
    public static final int MESSAGE_REQUEST_OK = 2;
    public static final int MESSAGE_REQUEST_FAIL = 3;
    
    private ElaService service;
    private ElaServiceProperties props;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(D) Log.e(TAG, "onCreate");

        setContentView(R.layout.main);

        if (!isNetworkConnected()) {
            Toast.makeText(this, "Network connection not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        props = new ElaServiceProperties(getApplicationContext());
        loadProperties();
        service = new ElaService(msgHandler, props);

        updateUserNumber();
        setUserStatus(R.string.main_status_label_unknown, R.style.UnknownStatusFont);
        
        Button refreshButton = (Button)findViewById(R.id.button_refresh);
        refreshButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
            	service.refresh();
            }
        });
	}

	private void loadProperties() {
		props.load();
	}
	
	private void storeProperties() {
		props.store();
	}
	
    @Override
    public void onStart() {
        super.onStart();
        if(D) Log.e(TAG, "onStart");
    }

    @Override
    public void onStop() {
        super.onStop();
        if(D) Log.e(TAG, "onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if(D) Log.e(TAG, "onDestroy");
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(D) Log.d(TAG, "onActivityResult:" + resultCode);

        boolean propertiesUpdated = false;
        switch (requestCode) {
        case OPTION_SERVICE_URL:
            if (resultCode == Activity.RESULT_OK) {
                String serviceUrl = data.getExtras().getString(ServiceUrlActivity.SERVICE_URL_PROPERTY);
                props.setServiceUrl(serviceUrl);
                propertiesUpdated = true;
                if(D) Log.e(TAG, "onActivityResult:"+serviceUrl);
            }
            break;
        case OPTION_USER_NUMBER:
            if (resultCode == Activity.RESULT_OK) {
                String userNumber = data.getExtras().getString(UserNumberActivity.USER_NUMBER_PROPERTY);
                props.setUserNumber(userNumber);
                updateUserNumber();
                propertiesUpdated = true;
                if(D) Log.e(TAG, "onActivityResult:"+userNumber);
            }
            break;
        }
        if (propertiesUpdated) {
        	storeProperties();
        }
    }

	@Override
    public synchronized void onResume() {
        super.onResume();
        if(D) Log.e(TAG, "onResume");
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        if(D) Log.e(TAG, "onPause");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent propertyIntent = null;
        switch (item.getItemId()) {
        case R.id.option_service_url:
        	propertyIntent = new Intent(this, ServiceUrlActivity.class);
        	propertyIntent.putExtra(ServiceUrlActivity.SERVICE_URL_PROPERTY, props.getServiceUrl());
            startActivityForResult(propertyIntent, OPTION_SERVICE_URL);
            return true;
        case R.id.option_user_number:
        	propertyIntent = new Intent(this, UserNumberActivity.class);
        	propertyIntent.putExtra(UserNumberActivity.USER_NUMBER_PROPERTY, props.getUserNumber());
            startActivityForResult(propertyIntent, OPTION_USER_NUMBER);
            return true;
        }
        return false;
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
    private final void setStatus(int resId) {
        final ActionBar actionBar = getActionBar();
        actionBar.setSubtitle(resId);
    }

    private final void setStatus(CharSequence subTitle) {
        final ActionBar actionBar = getActionBar();
        actionBar.setSubtitle(subTitle);
    }

    private void setUserStatus(int textResId, int styleResId) {
        TextView statusText = (TextView)findViewById(R.id.status_text);
        statusText.setText(getString(textResId));
        statusText.setTextAppearance(this, styleResId);
    }

    private void updateUserNumber() {
        TextView userNumberText = (TextView)findViewById(R.id.user_number_text);
        userNumberText.setText(props.getUserNumber());
    }

    private boolean isNetworkConnected() {
    	ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    	NetworkInfo ni = cm.getActiveNetworkInfo();
    	return ni == null ? false : true;
    }
    
    private final Handler msgHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_REQUEST_TRY:
                if(D) Log.i(TAG, "MESSAGE_REQUEST_TRY");
                setStatus(R.string.main_status_requesting_status);
                break;
            case MESSAGE_REQUEST_OK:
                if(D) Log.i(TAG, "MESSAGE_REQUEST_OK");
                setStatus(R.string.main_status_updated_status);
                switch (msg.arg1) {
                case ElaService.STATUS_OK_USER_ENTERED_AREA: 
                	setUserStatus(R.string.main_status_label_entered_area, R.style.EnterStatusFont);
                	break;
                case ElaService.STATUS_OK_USER_LEFT_AREA: 
                	setUserStatus(R.string.main_status_label_left_area, R.style.LeaveStatusFont);
                	break;
                }
                break;
            case MESSAGE_REQUEST_FAIL:
                if(D) Log.i(TAG, "MESSAGE_REQUEST_FAIL");
                setUserStatus(R.string.main_status_label_unknown, R.style.UnknownStatusFont);
                String errorMsg = "";
                switch (msg.arg1) {
                case ElaService.STATUS_ERROR_CONNECTION_PROBLEM:
                	errorMsg = getString(R.string.main_status_error_connection_problem);
                	break;
                case ElaService.STATUS_ERROR_MALFORMED_MESSAGE:
                	errorMsg = getString(R.string.main_status_error_malformed_message);
                	break;
                case ElaService.STATUS_ERROR_RESOURCE_PROBLEM:
                	errorMsg = getString(R.string.main_status_error_resource_problem);
                	break;
                }
                setStatus(errorMsg);
                break;
            }
        }
    };    
}

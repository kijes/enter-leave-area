/*
 * Copyright (c) 2014 Rafal Kijewski <kijes@kijes.com>
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package com.kijes.ela.android;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

import android.content.Context;
import android.util.Log;

public class ElaServiceProperties {
    private static final String TAG = "ElaServiceProperties";
    private static final boolean D = false;

    private static final String ELA_SERVICE_URL = "elaServiceUrl";
    private static final String ELA_USER_NUMBER = "elaUserNumber";
    private static final String PROPERTY_FILE_NAME = "ElaService.properties";

    private Properties props = new Properties();
    private Context context;
    
    public ElaServiceProperties(Context context) {
    	this.context = context; 
    }
        
    private void loadDefaults() {
        setProperty(ELA_SERVICE_URL, "http://localhost:8080");
        setProperty(ELA_USER_NUMBER, "00000");
    }

    public void load() {
    	if (D) Log.d(TAG, "Loading properties from ["+PROPERTY_FILE_NAME+"]");
        loadDefaults();
        FileInputStream fis = null;
		try {
			fis = context.openFileInput(PROPERTY_FILE_NAME);
			props.load(fis);
		} catch (FileNotFoundException e1) {
			if (D) Log.d(TAG, "Property file ["+PROPERTY_FILE_NAME+"] not found");
		} catch (IOException e2) {
			if (D) Log.d(TAG, "Error reading property file ["+PROPERTY_FILE_NAME+"]");
		} finally {
			try {
				if (fis != null) {
					fis.close();
				}
			} catch (IOException e2) {
			}
		}
    }

    public void store() {
    	if (D) Log.d(TAG, "Storing properties in ["+PROPERTY_FILE_NAME+"]");
        FileOutputStream fos = null;
        try {
        	fos = context.openFileOutput(PROPERTY_FILE_NAME, Context.MODE_PRIVATE);
        	props.store(fos, "Storing properties for ElaService");
        } catch (FileNotFoundException e1) {
        	if (D) Log.d(TAG, "Property file ["+PROPERTY_FILE_NAME+"] not found");
        } catch (IOException e2) {
        	if (D) Log.d(TAG, "Error saving property file ["+PROPERTY_FILE_NAME+"]");
        } finally {
			try {
				if (fos != null) {
					fos.close();
				}
			} catch (IOException e2) {
			}
		}
    }

    public String getServiceUrl() {
    	String value = getProperty(ELA_SERVICE_URL);
    	if (D) Log.d(TAG, "ELA_SERVICE_URL="+value);
    	return value;
    }

    public void setServiceUrl(String serviceUrl) {
        setProperty(ELA_SERVICE_URL, serviceUrl);
    }

    public String getUserNumber() {
        String value = getProperty(ELA_USER_NUMBER);
        if (D) Log.d(TAG, "ELA_CREW_NUMBER="+value);
        return value;
    }

    public void setUserNumber(String crewNumber) {
        setProperty(ELA_USER_NUMBER, crewNumber);
    }

    private String getProperty(String key) {
        return props.getProperty(key.toLowerCase(Locale.getDefault()));
    }

    private void setProperty(String key, String value) {
        props.put(key.toLowerCase(Locale.getDefault()), value);
    }
}

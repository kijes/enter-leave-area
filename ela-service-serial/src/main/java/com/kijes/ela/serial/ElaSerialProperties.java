/*
 * Copyright (c) 2014 Rafal Kijewski <kijes@kijes.com>
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package com.kijes.ela.serial;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ElaSerialProperties {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ElaSerialProperties.class);

    private static final String SERIAL_PORT = "serialPort";
    private static final String BAUD_RATE = "baudRate";
    private static final String SERVICE_URL = "serviceUrl";

    private Properties props = new Properties();

    public ElaSerialProperties() {
    }

    private void loadDefaults() {
        props.put(SERIAL_PORT, "COM3");
        props.put(BAUD_RATE, "9600");
        props.put(SERVICE_URL, "http://localhost:8080");
    }

    public void load() {
        LOGGER.debug("Loading properties");
        loadDefaults();
        try {
            InputStream stream = new FileInputStream(new File("ElaSerial.properties"));
            load(stream);
        } catch (Exception e) {
            LOGGER.warn("Unable to load properties");
        }
    }

    private void load(InputStream is) throws IOException {
        Properties tmpProps = new Properties();
        tmpProps.load(is);
        for (String propName : tmpProps.stringPropertyNames()) {
            setProperty(propName, tmpProps.getProperty(propName));
        }
    }

    private static boolean isPropertyTrue(String property) {
        return property != null &&
                (property.equalsIgnoreCase("Y") ||
                        property.equalsIgnoreCase("yes") ||
                        property.equals("1") ||
                        property.equalsIgnoreCase("true"));
    }

    public String getSerialPort() {
        return getProperty(SERIAL_PORT);
    }

    public int getBaudRate() {
        String property = getProperty(BAUD_RATE);
        return Integer.parseInt(property);
    }

    public String getServiceUrl() {
        return getProperty(SERVICE_URL);
    }

    private String getProperty(String key, String defaultValue) {
        String value = getProperty(key);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    private String getProperty(String key) {
        String property = props.getProperty(key.toLowerCase());
        if (property == null)
            property = System.getProperty(key);
        return property;
    }

    private void setProperty(String key, String value) {
        props.put(key.toLowerCase(), value);
    }
}

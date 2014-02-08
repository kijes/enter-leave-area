/*
 * Copyright (c) 2014 Rafal Kijewski <kijes@kijes.com>
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package com.kijes.ela.common;

public class ElaStatus {

    public static final String PROPERTY_STATUS_CODE = "status_code";
    public static final String PROPERTY_STATUS_MESSAGE = "status_message";
    private ElaStatusCode statusCode;
    private String message;

    public ElaStatus(ElaStatusCode statusCode, String message) {
        this. statusCode = statusCode;
        this.message = message;
    }

    public ElaStatusCode getCode() {
        return statusCode;
    }

    public String getMessage() {
        return message;
    }

    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("StatusCode [");
        str.append(statusCode.toString());
        str.append("], ");
        str.append("StatusMessage [");
        str.append(message);
        str.append("]");
        return str.toString();
    }
}

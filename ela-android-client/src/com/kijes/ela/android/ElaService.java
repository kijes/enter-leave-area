/*
 * Copyright (c) 2014 Rafal Kijewski <kijes@kijes.com>
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package com.kijes.ela.android;

import android.os.Handler;
import android.util.Log;
import org.json.JSONException;
import org.json.JSONObject;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.Protocol;
import org.restlet.engine.Engine;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

public class ElaService {
    private static final String TAG = "ElaService";
    private static final boolean D = false;

    public static final int STATUS_OK_USER_ENTERED_AREA = 0;
    public static final int STATUS_OK_USER_LEFT_AREA = 1;
    public static final int STATUS_ERROR_CONNECTION_PROBLEM = 2;
    public static final int STATUS_ERROR_MALFORMED_MESSAGE = 3;
    public static final int STATUS_ERROR_RESOURCE_PROBLEM = 4;

    private ServiceThread serviceThread;
    private ClientResource service;

    private Handler handler;
    private AtomicBoolean threadFinished = new AtomicBoolean(true);
    private Client client = new Client(new Context(), Protocol.HTTP);
    private ElaServiceProperties props;

    public ElaService(Handler handler, ElaServiceProperties props) {
        this.handler = handler;
        this.props = props;

        if (D) {
            Engine.setLogLevel(Level.FINEST);
            Engine.setRestletLogLevel(Level.FINEST);
        }
    }

    public synchronized void refresh() {
        if (threadFinished.get()) {
            threadFinished.set(false);
            start();
        }
    }

    private void start() {
        if (D) Log.d(TAG, "start");

        if (D) Log.d(TAG, "Using:" + props.getServiceUrl());
        service = new ClientResource(props.getServiceUrl());
        service.setNext(client);

        serviceThread = new ServiceThread(threadFinished);
        serviceThread.start();
    }

    private void updateStatus(int reqStatus, int userStatus) {
        handler.obtainMessage(reqStatus, userStatus, -1).sendToTarget();
    }

    public UserStatus getUserStatus(String userId) {
        if (D) Log.d(TAG, "getUserStatus [" + userId + "]");

        ClientResource res = service.getChild("/ela/user/" + userId);

        UserStatus userStatus = null;
        try {
            JsonRepresentation jsonRep = new JsonRepresentation(res.get());
            JSONObject jsonUser = jsonRep.getJsonObject();
            if (D) Log.d(TAG, "JSON RES:" + jsonUser.toString());
            Boolean enteredArea = Boolean.valueOf(jsonUser.getString("entered_area"));
            if (enteredArea) {
                userStatus = new UserStatus(ElaStatusReader.MESSAGE_REQUEST_OK, STATUS_OK_USER_ENTERED_AREA);
            } else {
                userStatus = new UserStatus(ElaStatusReader.MESSAGE_REQUEST_OK, STATUS_OK_USER_LEFT_AREA);
            }
        } catch (ResourceException e1) {
            userStatus = new UserStatus(ElaStatusReader.MESSAGE_REQUEST_FAIL, STATUS_ERROR_RESOURCE_PROBLEM);
        } catch (IOException e3) {
            userStatus = new UserStatus(ElaStatusReader.MESSAGE_REQUEST_FAIL, STATUS_ERROR_CONNECTION_PROBLEM);
        } catch (JSONException e4) {
            userStatus = new UserStatus(ElaStatusReader.MESSAGE_REQUEST_FAIL, STATUS_ERROR_MALFORMED_MESSAGE);
        }
        if (D) Log.d(TAG, "Single user [" + userId + "] retrieved");
        return userStatus;
    }

    private class UserStatus {
        private int reqStatus = -1;
        private int userStatus = -1;

        public UserStatus(int reqStatus, int userStatus) {
            this.reqStatus = reqStatus;
            this.userStatus = userStatus;
        }

        public int getReqStatus() {
            return reqStatus;
        }

        public int getUserStatus() {
            return userStatus;
        }
    }

    private class ServiceThread extends Thread {

        private AtomicBoolean threadFinished;

        public ServiceThread(AtomicBoolean threadFinished) {
            this.threadFinished = threadFinished;
        }

        public void run() {
            Log.i(TAG, "run");

            setName("ElaServiceThread");

            updateStatus(ElaStatusReader.MESSAGE_REQUEST_TRY, -1);

            UserStatus userStatus = getUserStatus(props.getUserNumber());
            updateStatus(userStatus.getReqStatus(), userStatus.getUserStatus());

            threadFinished.set(true);
        }
    }
}

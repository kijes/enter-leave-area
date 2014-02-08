/*
 * Copyright (c) 2014 Rafal Kijewski <kijes@kijes.com>
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package com.kijes.ela.client;

import com.kijes.ela.common.ElaUser;
import com.kijes.ela.common.ElaStatus;
import com.kijes.ela.common.EnterLeaveArea;
import com.kijes.ela.common.InvalidDataException;
import com.kijes.ela.common.JSONSerializers;
import org.json.JSONObject;
import org.restlet.Client;
import org.restlet.Context;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.ext.httpclient.HttpClientHelper;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.ResourceException;
import org.restlet.security.Authenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.PasswordAuthentication;
import java.util.Collection;

public class ElaServiceClient {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ElaServiceClient.class);

    private ClientResource service;

    public ElaServiceClient() {
    }

    public boolean isConnected() {
        return service != null;
    }

    public void connect(String url, String httpProxy) {
        if (service == null) {
            LOGGER.info("Connecting to [{}]", url);
            Client client = new Client(new Context(), Protocol.HTTP);
            if (false) { //TODO
                client.getContext().getParameters().add("proxyHost", "www-ad-proxy.sabre.com");
                client.getContext().getParameters().add("proxyPort", "80");
                HttpClientHelper httpClientHelp = new HttpClientHelper(client);
            }
            service = new ClientResource(url);
            service.setNext(client);
        }
    }

    public Collection<ElaUser> getUsers()  {
        LOGGER.info("Requesting list of users");

        ClientResource res = service.getChild("/ela/user/");
        Collection<ElaUser> elaUsers = null;

        try {
            JsonRepresentation jsonRep = new JsonRepresentation(res.get());
            JSONObject jsonUserList = jsonRep.getJsonObject();
            LOGGER.info("JSON RES:" + jsonUserList.toString());
            elaUsers = JSONSerializers.usersFromJson(jsonUserList);
        } catch(ResourceException e1) {
            try {
                res.getResponseEntity().exhaust();
            } catch (IOException e2) {
            }
            throw new RuntimeException("Resource error", e1);
        } catch(IOException e3) {
            throw new RuntimeException("Connection error", e3);
        } catch(InvalidDataException e4) {
            throw new RuntimeException(e4.getMessage());
        }
        LOGGER.info("User list retrieved");
        return elaUsers;
    }

    public ElaUser getUser(String userId) {
        LOGGER.info("Requesting GET USER for user [" + userId + "]");

        ClientResource res = service.getChild("/ela/user/" + userId);
        ElaUser elaUser = null;

        try {
            JsonRepresentation jsonRep = new JsonRepresentation(res.get());
            JSONObject jsonUser = jsonRep.getJsonObject();
            LOGGER.info("JSON RES:" + jsonUser.toString());
            elaUser = JSONSerializers.userFromJson(jsonUser);
        } catch(ResourceException e1) {
            try {
                res.getResponseEntity().exhaust();
            } catch (IOException e2) {
            }
            throw new RuntimeException("Resource error", e1);
        } catch(IOException e3) {
            throw new RuntimeException("Connection error", e3);
        } catch(InvalidDataException e4) {
            throw new RuntimeException(e4.getMessage());
        }
        LOGGER.info("User ["+userId+"] retrieved");
        return elaUser;
    }

    public ElaStatus userEnter(String cardId) {
        LOGGER.info("Requesting USER ENTER for card [" + cardId + "]");

        ElaStatus status = null;
        try {
            ClientResource res = service.getChild(
                    "/ela/enter/" + cardId);

            EnterLeaveArea enterLeaveArea = new EnterLeaveArea(true);
            Representation rep = new StringRepresentation(JSONSerializers.enterLeaveAreaToJson(enterLeaveArea).toString(), MediaType.APPLICATION_JSON);
            LOGGER.info("JSON REQ:"+rep.toString());
            JsonRepresentation jsonRep = new JsonRepresentation(res.put(rep));
            JSONObject jsonStatus = jsonRep.getJsonObject();
            LOGGER.info("JSON RES:"+jsonStatus.toString());
            status = JSONSerializers.statusFromJson(jsonStatus);
            LOGGER.info("STATUS:"+status.toString());
        } catch (IOException e) {
            throw new RuntimeException("Connection error", e);
        }
        LOGGER.info("User entered for card [" + cardId + "]");
        return status;
    }

    public ElaStatus userLeave(String cardId) {
        LOGGER.info("Requesting USER LEAVE for card [" + cardId + "]");

        ClientResource res = service.getChild(
                "/ela/leave/" + cardId);

        ElaStatus status = null;
        try {
            EnterLeaveArea enterLeaveArea = new EnterLeaveArea(false);
            Representation rep = new StringRepresentation(JSONSerializers.enterLeaveAreaToJson(enterLeaveArea).toString(), MediaType.APPLICATION_JSON);
            LOGGER.info("JSON REQ:"+rep.toString());
            JsonRepresentation jsonRep = new JsonRepresentation(res.put(rep));
            JSONObject jsonStatus = jsonRep.getJsonObject();
            LOGGER.info("JSON RES:"+jsonStatus.toString());
            status = JSONSerializers.statusFromJson(jsonStatus);
            LOGGER.info("STATUS:"+status.toString());
        } catch (IOException e) {
            throw new RuntimeException("Connection error", e);
        }
        LOGGER.info("User left for card [" + cardId + "]");
        return status;
    }

    public ElaStatus createUser(String userId, String cardId) {
        LOGGER.info("Requesting USER CREATE for user [" + userId + "]");
        ElaUser elaUser = new ElaUser(userId, cardId);
        ClientResource res = service.getChild("/ela/user/");

        ElaStatus status = null;
        try {
            Representation rep = new StringRepresentation(JSONSerializers.userToJson(elaUser).toString(), MediaType.APPLICATION_JSON);
            LOGGER.info("JSON REQ:"+rep.toString());
            JsonRepresentation jsonRep = new JsonRepresentation(res.post(rep));
            JSONObject jsonStatus = jsonRep.getJsonObject();
            LOGGER.info("JSON RES:"+jsonStatus.toString());
            status = JSONSerializers.statusFromJson(jsonStatus);
            LOGGER.info("STATUS:"+status.toString());
        } catch (IOException e) {
            throw new RuntimeException("Connection error", e);
        }
        LOGGER.info("ElaUser [" + userId + "] created");
        return status;
    }

    public ElaStatus deleteUser(String userId) {
        LOGGER.info("Requesting USER DELETE for user " + userId);
        ClientResource userRes = service.getChild("/ela/user/" + userId);

        ElaStatus status = null;
        try {
            JsonRepresentation jsonRep = new JsonRepresentation(userRes.delete());
            JSONObject jsonStatus = jsonRep.getJsonObject();
            LOGGER.info("JSON RES:"+jsonStatus.toString());
            status = JSONSerializers.statusFromJson(jsonStatus);
            LOGGER.info("STATUS:"+status.toString());
        } catch (IOException e) {
            throw new RuntimeException("Connection error", e);
        }
        LOGGER.info("User [" + userId + "] deleted");
        return status;
    }
}

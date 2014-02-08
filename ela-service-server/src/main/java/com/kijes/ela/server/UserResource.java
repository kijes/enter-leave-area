/*
 * Copyright (c) 2014 Rafal Kijewski <kijes@kijes.com>
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package com.kijes.ela.server;

import com.kijes.ela.common.ElaUser;
import com.kijes.ela.common.ElaStatus;
import com.kijes.ela.common.ElaStatusCode;
import com.kijes.ela.common.JSONSerializers;
import org.json.JSONObject;
import org.restlet.data.MediaType;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Delete;
import org.restlet.resource.Get;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class UserResource extends BaseResource {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(UserResource.class);

    private ElaUser elaUser;
    private String userId;

    @Override
    protected void doInit() throws ResourceException {
        userId = (String) getRequest().getAttributes().get("userId");
        elaUser = getUserByUserId(userId);
        LOGGER.info("Working with user ["+ userId +"]");
        setExisting(elaUser != null);
    }

    @Delete
    public Representation removeUser() {

        LOGGER.info("Handling DELETE");

        JsonRepresentation result = null;
        try {
            if (elaUser != null) {
                deleteUser(elaUser.getUserId());
                result = new JsonRepresentation(JSONSerializers.statusToJson(new ElaStatus(ElaStatusCode.PASS, "ElaUser deleted")));
            } else {
                result = new JsonRepresentation(JSONSerializers.statusToJson(new ElaStatus(ElaStatusCode.FAIL, "ElaUser not found")));
            }
        } catch (Exception e) {
            result = new JsonRepresentation(JSONSerializers.statusToJson(new ElaStatus(ElaStatusCode.FAIL, "Unable to delete elaUser")));
        }
        setStatus(Status.SUCCESS_OK);
        LOGGER.info("JSON RES:"+result.getJsonObject().toString());
        return result;
    }

    @Put
    public Representation storeUser(Representation entity) throws IOException {

        LOGGER.info("Handling PUT");

        JsonRepresentation jsonRep = new JsonRepresentation(entity);
        JSONObject jsonUser = jsonRep.getJsonObject();
        LOGGER.info("JSON REQ:" + jsonUser.toString());
        JsonRepresentation result = null;
        try {
            ElaUser newElaUser = JSONSerializers.userFromJson(jsonUser);
            if (elaUser == null) {
                createUser(newElaUser);
                result = new JsonRepresentation(JSONSerializers.statusToJson(new ElaStatus(ElaStatusCode.PASS, "ElaUser created")));
                setStatus(Status.SUCCESS_CREATED);
            } else {
                updateUser(newElaUser);
                result = new JsonRepresentation(JSONSerializers.statusToJson(new ElaStatus(ElaStatusCode.PASS, "ElaUser updated")));
                setStatus(Status.SUCCESS_OK);
            }
        } catch (Exception e) {
            result = new JsonRepresentation(JSONSerializers.statusToJson(new ElaStatus(ElaStatusCode.FAIL, "Unable to create/update elaUser:"+e.getMessage())));
            setStatus(Status.SUCCESS_OK);
        }
        LOGGER.info("JSON RES:"+result.getJsonObject().toString());
        return result;
    }

    @Get
    public Representation toJson() {
        LOGGER.info("Handling GET");
        JSONObject jsonUser = JSONSerializers.userToJson(elaUser);
        LOGGER.info("JSON RES:" + jsonUser.toString());
        return new StringRepresentation(jsonUser.toString(), MediaType.APPLICATION_JSON);
    }
}
/*
 * Copyright (c) 2014 Rafal Kijewski <kijes@kijes.com>
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package com.kijes.ela.server;

import com.kijes.ela.common.ElaUser;
import com.kijes.ela.common.ElaStatus;
import com.kijes.ela.common.ElaStatusCode;
import com.kijes.ela.common.EnterLeaveArea;
import com.kijes.ela.common.JSONSerializers;
import org.json.JSONObject;
import org.restlet.data.Status;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Put;
import org.restlet.resource.ResourceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class EnterLeaveAreaResource extends BaseResource {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(EnterLeaveAreaResource.class);

    private ElaUser user;
    private String cardId;

    @Override
    protected void doInit() throws ResourceException {
        cardId = (String) getRequest().getAttributes().get("cardId");
        user = getUserByCardId(cardId);
        LOGGER.info("Working with card [" + cardId + "]");
        setExisting(user != null);
    }

    @Put
    public Representation doEnterLeave(Representation entity) throws IOException {

        LOGGER.info("Handling PUT");

        JsonRepresentation jsonRep = new JsonRepresentation(entity);
        JSONObject jsonEnterLeave = jsonRep.getJsonObject();
        LOGGER.info("JSON REQ:"+jsonEnterLeave.toString());

        JsonRepresentation result = null;
        try {
            EnterLeaveArea enterLeave = JSONSerializers.enterLeaveAreaFromJson(jsonEnterLeave);
            if (user != null) {
                ElaStatusCode statusCode = null;
                String statusMessage = null;
                if (enterLeave.isEnteredArea()) {
                    statusCode = user.enterArea();
                    if (statusCode == ElaStatusCode.PASS) {
                        updateUser(user);
                        statusMessage = "User entered the area";
                    } else if (statusCode == ElaStatusCode.WARN) {
                        statusMessage = "User already in the area";
                    } else {
                        throw new RuntimeException("Unknown status code");
                    }
                } else {
                    statusCode = user.leaveArea();
                    if (statusCode == ElaStatusCode.PASS) {
                        updateUser(user);
                        statusMessage = "User left the area";
                    } else if (statusCode == ElaStatusCode.WARN) {
                        statusMessage = "User already left the area";
                    } else {
                        throw new RuntimeException("Unknown status code");
                    }
                }
                result = new JsonRepresentation(JSONSerializers.statusToJson(new ElaStatus(statusCode, statusMessage)));
            } else {
                result = new JsonRepresentation(JSONSerializers.statusToJson(new ElaStatus(ElaStatusCode.FAIL, "ElaUser not found")));
            }
        } catch (Exception e) {
            result = new JsonRepresentation(JSONSerializers.statusToJson(new ElaStatus(ElaStatusCode.FAIL, "Unable to enter/leave the area")));
        }
        setStatus(Status.SUCCESS_OK);

        LOGGER.info("JSON RES:"+result.getJsonObject().toString());
        return result;
    }
}

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
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UsersResource extends BaseResource {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(UsersResource.class);

    @Post
    public Representation acceptUser(Representation entity) {

        LOGGER.info("Handling POST");

        ElaUser user = null;
        JsonRepresentation result = null;
        try {
            JsonRepresentation jsonRep = new JsonRepresentation(entity);
            JSONObject jsonUser = jsonRep.getJsonObject();
            LOGGER.info("JSON REQ:" + jsonUser.toString());
            user = JSONSerializers.userFromJson(jsonUser);

            createUser(user);
            setStatus(Status.SUCCESS_CREATED);
            result = new JsonRepresentation(JSONSerializers.statusToJson(new ElaStatus(ElaStatusCode.PASS, "ElaUser created")));
            result.setLocationRef(getRequest().getResourceRef().getIdentifier() + "/" + user.getUserId());
            LOGGER.info("ElaUser [" + user.getUserId() + "] created");
        } catch (Exception e) {
            setStatus(Status.SUCCESS_OK);
            result = new JsonRepresentation(JSONSerializers.statusToJson(new ElaStatus(ElaStatusCode.FAIL, "Unable to create elaUser:"+e.getMessage())));
            LOGGER.error("Unable to create elaUser");
        }
        LOGGER.info("JSON RES:"+result.getJsonObject().toString());
        return result;
    }

    @Get
    public Representation toJson() {
        LOGGER.info("Handling GET");
        JSONObject jsonUsers = JSONSerializers.usersToJson(getUsers());
        LOGGER.info("JSON RES:" + jsonUsers.toString());
        return new StringRepresentation(jsonUsers.toString(), MediaType.APPLICATION_JSON);
    }
}

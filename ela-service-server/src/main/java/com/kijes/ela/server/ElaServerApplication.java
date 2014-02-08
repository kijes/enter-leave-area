/*
 * Copyright (c) 2014 Rafal Kijewski <kijes@kijes.com>
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package com.kijes.ela.server;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Transaction;
import com.kijes.ela.common.ElaUser;
import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.Server;
import org.restlet.data.Protocol;
import org.restlet.routing.Router;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ElaServerApplication extends Application {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(ElaServerApplication.class);

    private static DatastoreService dataStore = DatastoreServiceFactory.getDatastoreService();

	public ElaServerApplication() {
	}

    public void createUser(ElaUser user) throws DataStoreException {
        Transaction txn = dataStore.beginTransaction();
        try {
            if (getUserByUserId(user.getUserId()) == null && getUserByCardId(user.getCardId()) == null) {
                Entity userEntity = userToEntity(user);
                dataStore.put(userEntity);
                txn.commit();
            } else {
                throw new DataStoreException("Duplicate "+ ElaUser.PROPERTY_USER_ID +"/"+ ElaUser.PROPERTY_CARD_ID+" not allowed");
            }
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    public void updateUser(ElaUser user) {
        Transaction txn = dataStore.beginTransaction();
        try {
            Entity userEntity = getEntityByProperty(ElaUser.PROPERTY_USER_ID, user.getUserId());
            userEntity.setProperty(ElaUser.PROPERTY_USER_ID, user.getUserId());
            userEntity.setProperty(ElaUser.PROPERTY_CARD_ID, user.getCardId());
            userEntity.setProperty(ElaUser.PROPERTY_ENTERED_AREA, user.getEnteredArea());
            dataStore.put(userEntity);
            txn.commit();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    public void deleteUser(String userId) {
        Transaction txn = dataStore.beginTransaction();
        try {
            Entity userEntity = getEntityByProperty(ElaUser.PROPERTY_USER_ID, userId);
            dataStore.delete(userEntity.getKey());
            txn.commit();
        } finally {
            if (txn.isActive()) {
                txn.rollback();
            }
        }
    }

    private ElaUser entityToUser(Entity userEntity) {
        ElaUser elaUser = new ElaUser((String)userEntity.getProperty(ElaUser.PROPERTY_USER_ID),
                (String)userEntity.getProperty(ElaUser.PROPERTY_CARD_ID));
        elaUser.setEnteredArea(Boolean.valueOf((Boolean)userEntity.getProperty(ElaUser.PROPERTY_ENTERED_AREA)));
        return elaUser;
    }

    private Entity userToEntity(ElaUser user) {
        Entity userEntity = new Entity("ElaUser");
        userEntity.setProperty(ElaUser.PROPERTY_USER_ID, user.getUserId());
        userEntity.setProperty(ElaUser.PROPERTY_CARD_ID, user.getCardId());
        userEntity.setProperty(ElaUser.PROPERTY_ENTERED_AREA, user.getEnteredArea());
        return userEntity;
    }

    public Collection<ElaUser> getUsers() {
        Query query = new Query("ElaUser");
        List<Entity> userEntities = dataStore.prepare(query).asList(FetchOptions.Builder.withLimit(50));
        Collection<ElaUser> elaUsers = new ArrayList<>();
        for (Entity userEntity : userEntities) {
            elaUsers.add(entityToUser(userEntity));
        }
        return elaUsers;
    }

    public ElaUser getUserByUserId(String userId) {
        Entity userEntity = getEntityByProperty(ElaUser.PROPERTY_USER_ID, userId);
        if (userEntity != null) {
            return entityToUser(userEntity);
        }
        return null;
    }

    public ElaUser getUserByCardId(String cardId) {
        Entity userEntity = getEntityByProperty(ElaUser.PROPERTY_CARD_ID, cardId);
        if (userEntity != null) {
            return entityToUser(userEntity);
        }
        return null;
    }

    private Entity getEntityByProperty(String propertyName, String propertyValue) {
        Query query = new Query("ElaUser").addFilter(propertyName, Query.FilterOperator.EQUAL, propertyValue);
        return dataStore.prepare(query).asSingleEntity();
    }

    public static void main(String[] args) throws Exception {

        LOGGER.info("Starting server");

		Server server = new Server(Protocol.HTTP, 8080);
		server.setNext(new ElaServerApplication());
		server.start();
	}

	@Override
	public Restlet createInboundRoot() {		
		Router router = new Router(getContext());
		router.attach("/enter/{cardId}", EnterLeaveAreaResource.class);
		router.attach("/leave/{cardId}", EnterLeaveAreaResource.class);
		router.attach("/user/", UsersResource.class);
		router.attach("/user/{userId}", UserResource.class);
		return router;
	}
}
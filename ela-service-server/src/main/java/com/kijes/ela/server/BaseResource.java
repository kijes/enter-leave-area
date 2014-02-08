/*
 * Copyright (c) 2014 Rafal Kijewski <kijes@kijes.com>
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package com.kijes.ela.server;

import com.kijes.ela.common.ElaUser;
import org.restlet.resource.ServerResource;

import java.util.Collection;

public abstract class BaseResource extends ServerResource {

    protected Collection<ElaUser> getUsers() {
        return ((ElaServerApplication) getApplication()).getUsers();
    }

    protected ElaUser getUserByUserId(String userId) {
        return ((ElaServerApplication) getApplication()).getUserByUserId(userId);
    }

    protected ElaUser getUserByCardId(String cardId) {
        return ((ElaServerApplication) getApplication()).getUserByCardId(cardId);
    }

    protected void createUser(ElaUser user) throws DataStoreException {
        ((ElaServerApplication) getApplication()).createUser(user);
    }

    protected void updateUser(ElaUser user) {
        ((ElaServerApplication) getApplication()).updateUser(user);
    }

    protected void deleteUser(String userId) {
        ((ElaServerApplication) getApplication()).deleteUser(userId);
    }
}
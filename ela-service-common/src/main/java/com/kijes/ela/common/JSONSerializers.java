/*
 * Copyright (c) 2014 Rafal Kijewski <kijes@kijes.com>
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package com.kijes.ela.common;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;

public class JSONSerializers {

    private static final String USER_LIST_PROPERTY = "user_list";

    private JSONSerializers() {
    }

    public static Collection<ElaUser> usersFromJson(JSONObject jsonUsers) throws InvalidDataException {
        Collection<ElaUser> elaUsers = new ArrayList<>();
        JSONArray jsonUserList = jsonUsers.getJSONArray(USER_LIST_PROPERTY);
        for (int i = 0; i < jsonUserList.length(); i++) {
            JSONObject jsonUser = jsonUserList.getJSONObject(i);
            elaUsers.add(userFromJson(jsonUser));
        }
        return elaUsers;
    }

    public static JSONObject usersToJson(Collection<ElaUser> users) {
        JSONArray jsonUserList = new JSONArray();
        for (ElaUser user : users) {
            JSONObject jsonUser = userToJson(user);
            jsonUserList.put(jsonUser);
        }
        JSONObject jsonUsers = new JSONObject();
        jsonUsers.put(USER_LIST_PROPERTY, jsonUserList);
        return jsonUsers;
    }

    public static ElaUser userFromJson(JSONObject jsonUser) throws InvalidDataException {
        String userId = jsonUser.getString(ElaUser.PROPERTY_USER_ID);
        String cardId = jsonUser.getString(ElaUser.PROPERTY_CARD_ID);
        String enteredArea = jsonUser.getString(ElaUser.PROPERTY_ENTERED_AREA);
        if (userId == null) {
            throw new InvalidDataException("Invalid "+ ElaUser.PROPERTY_USER_ID);
        }
        if (cardId == null) {
            throw new InvalidDataException("Invalid "+ ElaUser.PROPERTY_CARD_ID);
        }
        if (enteredArea != null) {
            return new ElaUser(userId, cardId, Boolean.parseBoolean(enteredArea));
        }
        return new ElaUser(userId, cardId);
    }

    public static JSONObject userToJson(ElaUser elaUser) {
        JSONObject jsonUser = new JSONObject();
        jsonUser.put(ElaUser.PROPERTY_USER_ID, elaUser.getUserId());
        jsonUser.put(ElaUser.PROPERTY_CARD_ID, elaUser.getCardId());
        jsonUser.put(ElaUser.PROPERTY_ENTERED_AREA, elaUser.getEnteredArea().toString());
        return jsonUser;
    }

    public static JSONObject enterLeaveAreaToJson(EnterLeaveArea enterLeaveArea) {
        JSONObject jsonEnterLeaveArea = new JSONObject();
        jsonEnterLeaveArea.put(ElaUser.PROPERTY_ENTERED_AREA, enterLeaveArea.isEnteredArea().toString());
        return jsonEnterLeaveArea;
    }

    public static EnterLeaveArea enterLeaveAreaFromJson(JSONObject jsonEnterLeaveArea) throws InvalidDataException {
        String enterLeaveAreaStr = jsonEnterLeaveArea.getString(ElaUser.PROPERTY_ENTERED_AREA);
        if (enterLeaveAreaStr == null) {
            throw new InvalidDataException("Invalid "+ ElaUser.PROPERTY_ENTERED_AREA);
        }
        return new EnterLeaveArea(Boolean.parseBoolean(enterLeaveAreaStr));
    }

    public static JSONObject statusToJson(ElaStatus status) {
        JSONObject jsonStatus = new JSONObject();
        jsonStatus.put(ElaStatus.PROPERTY_STATUS_CODE, status.getCode().toString());
        jsonStatus.put(ElaStatus.PROPERTY_STATUS_MESSAGE, status.getMessage());
        return jsonStatus;
    }

    public static ElaStatus statusFromJson(JSONObject jsonStatus) {
        String statusCode = jsonStatus.getString(ElaStatus.PROPERTY_STATUS_CODE);
        String statusMessage = jsonStatus.getString(ElaStatus.PROPERTY_STATUS_MESSAGE);
        return new ElaStatus(ElaStatusCode.valueOf(statusCode), statusMessage);
    }
}

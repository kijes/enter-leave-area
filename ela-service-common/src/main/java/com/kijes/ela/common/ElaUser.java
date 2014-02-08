/*
 * Copyright (c) 2014 Rafal Kijewski <kijes@kijes.com>
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package com.kijes.ela.common;

public class ElaUser {
    public static final String PROPERTY_USER_ID = "user_id";
    public static final String PROPERTY_CARD_ID = "card_id";
    public static final String PROPERTY_ENTERED_AREA = "entered_area";
    private String userId;
    private String cardId;
    private Boolean enteredArea = Boolean.FALSE;

    public ElaUser(String userId, String cardId) {
        this.userId = userId;
        this.cardId = cardId;
    }

    public ElaUser(String userId, String cardId, Boolean enteredArea) {
        this(userId, cardId);
        this.enteredArea = enteredArea;
    }

    public Boolean getEnteredArea() {
        return enteredArea;
    }

    public void setEnteredArea(Boolean enteredArea) {
        this.enteredArea = enteredArea;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getUserId() {
        return userId;
    }

    public String getCardId() {
        return cardId;
    }

    public ElaStatusCode enterArea() {
        if (!enteredArea) {
            enteredArea = Boolean.TRUE;
            return ElaStatusCode.PASS;
        }
        return ElaStatusCode.WARN;
    }

    public ElaStatusCode leaveArea() {
        if (enteredArea) {
            enteredArea = Boolean.FALSE;
            return ElaStatusCode.PASS;
        }
        return ElaStatusCode.WARN;
    }

    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("UserID [");
        str.append(userId);
        str.append("], CardID [");
        str.append(cardId);
        str.append("], EnteredArea [");
        str.append(enteredArea);
        str.append("]");
        return str.toString();
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ElaUser other = (ElaUser) obj;
        if (userId == null) {
            if (other.userId != null)
                return false;
        } else if (!userId.equals(other.userId))
            return false;
        return true;
    }
}

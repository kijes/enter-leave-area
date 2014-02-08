/*
 * Copyright (c) 2014 Rafal Kijewski <kijes@kijes.com>
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package com.kijes.ela.common;

public class EnterLeaveArea {
    private Boolean enteredArea = Boolean.FALSE;
    public EnterLeaveArea(Boolean enteredArea) {
        this.enteredArea = enteredArea;
    }

    public Boolean isEnteredArea() {
        return enteredArea;
    }
}

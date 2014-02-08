/*
 * Copyright (c) 2014 Rafal Kijewski <kijes@kijes.com>
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package com.kijes.ela.serial.command;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Command {

    public static final String CMD_ENTER = "ENTER";
    public static final String CMD_LEAVE = "LEAVE";
    public static final String CMD_STATUS = "STATUS";

    public static final char CMD_BEGIN = '<';
    public static final char CMD_END = '>';
    public static final int CMD_MAX_LENGTH = 20;

    private String action;
    private String value;
    private static final Pattern p = Pattern.compile("([A-Z_]+):([A-F0-9]{10})");

    public static Command createFromString(String cmdStr) throws CommandParseException {
        Matcher m = p.matcher(cmdStr);
        if (!m.matches()) {
            throw new CommandParseException("Invalid command [" + cmdStr + "]");
        }
        return new Command(m.group(1), m.group(2));
    }

    public Command(String action, String value) {
        this.action = action;
        this.value = value;
    }

    public String getAction() {
        return action;
    }

    public String getValue() {
        return value;
    }

    public String toString() {
        return action + ":" + value;
    }

    public String toSerialFormat() {
        StringBuilder str = new StringBuilder();
        str.append(CMD_BEGIN);
        str.append(action);
        str.append(":");
        str.append(value);
        str.append(CMD_END);
        return str.toString();
    }
}
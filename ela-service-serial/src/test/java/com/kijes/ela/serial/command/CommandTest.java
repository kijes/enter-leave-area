/*
 * Copyright (c) 2014 Rafal Kijewski <kijes@kijes.com>
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package com.kijes.ela.serial.command;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.regex.Matcher;

public class CommandTest {
    
    @Test(expected = CommandParseException.class)
    public void testInvalidCommand1() throws Exception {
        Command.createFromString("ENTER:");
    }

    @Test(expected = CommandParseException.class)
    public void testInvalidCommand2() throws Exception {
        Command.createFromString("ENTER");
    }

    @Test(expected = CommandParseException.class)
    public void testInvalidCommand3() throws Exception {
        Command.createFromString("ENTER:AAA");
    }

    @Test(expected = CommandParseException.class)
    public void testInvalidCommand4() throws Exception {
        Command.createFromString("ENTER:1");
    }

    @Test
    public void testValidCommand() throws Exception {
        Command cmd = Command.createFromString("ENTER:0123456789");
        Assert.assertEquals("ENTER", cmd.getAction());
        Assert.assertEquals("0123456789", cmd.getValue());
    }

    @Test
    public void testSerialFormat() throws Exception {
        Command cmd = Command.createFromString("ENTER:0123456789");
        Assert.assertEquals("<ENTER:0123456789>", cmd.toSerialFormat());
    }
}

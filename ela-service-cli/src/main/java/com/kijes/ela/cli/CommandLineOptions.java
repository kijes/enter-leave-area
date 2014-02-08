/*
 * Copyright (c) 2014 Rafal Kijewski <kijes@kijes.com>
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package com.kijes.ela.cli;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandLineOptions {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(CommandLineOptions.class);

    private static final String VERBOSE = "verbose";
    private static final String USER_LIST = "userlist";
    private static final String USER_INFO = "userinfo";
    private static final String USER_ENTER = "userenter";
    private static final String USER_LEAVE = "userleave";
    private static final String USER_ID = "userid";
    private static final String SERVICE_URL = "serviceurl";
    private static final String CARD_ID = "cardid";
    private static final String USER_CREATE = "usercreate";
    private static final String USER_DELETE = "userdelete";
    private static final String HTTP_PROXY = "httpproxy";
    private static final String DEFAULT_SERVICE_URL = "http://localhost:8080";

    private CommandLine commandLine;

    public boolean isUserList() {
        return commandLine.hasOption(USER_LIST);
    }

    public boolean isUserInfo() {
        return commandLine.hasOption(USER_INFO);
    }

    public boolean isUserEnter() {
        return commandLine.hasOption(USER_ENTER);
    }
    
    public boolean isUserLeave() {
        return commandLine.hasOption(USER_LEAVE);
    }

    public boolean isUserCreate() {
        return commandLine.hasOption(USER_CREATE);
    }

    public boolean isUserDelete() {
        return commandLine.hasOption(USER_DELETE);
    }

    public String getHttpProxy() {
        return commandLine.getOptionValue(HTTP_PROXY);
    }

    public String getUserId() {
        String value = commandLine.getOptionValue(USER_ID);
        if (value == null) {
        	throw new RuntimeException("Invalid User Id");
        }
        return value;
    }

    public String getCardId() {
        String value = commandLine.getOptionValue(CARD_ID);
        if (value == null) {
        	throw new RuntimeException("Invalid Card Id");
        }
        return value;
    }

    public String getServiceUrl() {
        String value = commandLine.getOptionValue(SERVICE_URL);
        if (value == null) {
            value = DEFAULT_SERVICE_URL;
            LOGGER.info("Using service [{}]", value);
        }
        return value;
    }

    public boolean hasAnyOptions() {
        return commandLine.getOptions().length > 0;
    }

    public boolean isVerboseEnabled() {
        return commandLine.hasOption(VERBOSE);
    }

    public void parse(String[] args) throws ParseException {
        commandLine = new GnuParser().parse(getProgramOptions(), args);
    }

    private Options getProgramOptions() {
        Options options = new Options();
        options.addOption(createOption(USER_LIST, false, false, "Show all users"));
        options.addOption(createOption(USER_INFO, false, false, "Show status for user"));
        options.addOption(createOption(USER_ENTER, false, false, "User entered the area"));
        options.addOption(createOption(USER_LEAVE, false, false, "User left the area"));
        options.addOption(createOption(USER_ID, true, false, "User ID"));
        options.addOption(createOption(SERVICE_URL, true, false, "Service URL"));
        options.addOption(createOption(VERBOSE, false, false, "Verbose mode"));
        options.addOption(createOption(CARD_ID, true, false, "Card ID"));
        options.addOption(createOption(USER_CREATE, false, false, "Create new user"));
        options.addOption(createOption(USER_DELETE, false, false, "Delete user"));
        options.addOption(createOption(HTTP_PROXY, true, false, "Http proxy"));
        return options;
    }

    private Option createOption(String optionName, boolean hasArg, boolean isRequired, String description) {
        return OptionBuilder.withArgName(optionName).hasArg(hasArg).isRequired(isRequired).withDescription(description).
                create(optionName);
    }

    public void printHelp() {
        new HelpFormatter().printHelp("Enter Leave Area CLI", getProgramOptions());
    }
}

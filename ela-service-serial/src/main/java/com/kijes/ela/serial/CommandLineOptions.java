/*
 * Copyright (c) 2014 Rafal Kijewski <kijes@kijes.com>
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package com.kijes.ela.serial;

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
    private static final String SERVICE_URL = "serviceurl";
    private static final String HTTP_PROXY = "httpproxy";

    private CommandLine commandLine;

    public boolean isVerboseEnabled() {
        return commandLine.hasOption(VERBOSE);
    }

    public String getServiceUrl() {
        String value = commandLine.getOptionValue(SERVICE_URL);
        if (value == null) {
            throw new RuntimeException("Invalid service URL");
        }
        return value;
    }

    public String getHttpProxy() {
        return commandLine.getOptionValue(HTTP_PROXY);
    }

    public void parse(String[] args) throws ParseException {
        LOGGER.info("Parsing command line parameters");
        commandLine = new GnuParser().parse(getProgramOptions(), args);
    }

    private Options getProgramOptions() {
        Options options = new Options();
        options.addOption(createOption(SERVICE_URL, true, false, "Service URL"));
        options.addOption(createOption(VERBOSE, false, false, "Verbose mode"));
        options.addOption(createOption(HTTP_PROXY, true, false, "Http proxy"));
        return options;
    }

    private Option createOption(String optionName, boolean hasArg, boolean isRequired, String description) {
        return OptionBuilder.withArgName(optionName).hasArg(hasArg).isRequired(isRequired).withDescription(description).
                create(optionName);
    }

    public void printHelp() {
        new HelpFormatter().printHelp("ElaSerialApplication", getProgramOptions());
    }
}

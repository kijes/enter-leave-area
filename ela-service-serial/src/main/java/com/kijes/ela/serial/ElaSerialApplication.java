/*
 * Copyright (c) 2014 Rafal Kijewski <kijes@kijes.com>
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package com.kijes.ela.serial;

import com.kijes.ela.serial.command.CommandProcessor;
import com.kijes.ela.serial.command.SerialCommandReaderWriter;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ElaSerialApplication {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(ElaSerialApplication.class);

	private CommandLineOptions options = new CommandLineOptions();
    private ElaSerialProperties props = new ElaSerialProperties();

	public ElaSerialApplication() {
	}

	public static void main(String[] args) {
		System.exit(new ElaSerialApplication().run(args));
	}

	private int run(String[] args) {
        int exitCode = 0;
		try {
            LOGGER.info("Started serial command processor...");
			options.parse(args);
            props.load();

            String serviceUrl = props.getServiceUrl();
            if (serviceUrl == null) {
                serviceUrl = options.getServiceUrl();
            }

            SerialCommandReaderWriter cmdReaderWriter = new SerialCommandReaderWriter(props.getSerialPort(), props.getBaudRate());
            CommandProcessor processor = new CommandProcessor(serviceUrl, options.getHttpProxy(), cmdReaderWriter);
            processor.process();
		} catch (ParseException e1) {
			LOGGER.error(e1.getMessage());
			options.printHelp();
            exitCode = 1;
        } catch (Exception e2) {
			LOGGER.error(e2.getMessage(), e2);
            exitCode = 1;
		}
        LOGGER.info("Finished serial command processor...");
        return exitCode;
	}
}

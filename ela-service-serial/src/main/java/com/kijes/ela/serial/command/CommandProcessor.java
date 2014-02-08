/*
 * Copyright (c) 2014 Rafal Kijewski <kijes@kijes.com>
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package com.kijes.ela.serial.command;

import com.kijes.ela.client.ElaServiceClient;
import com.kijes.ela.common.ElaStatus;
import com.kijes.ela.common.ElaStatusCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class CommandProcessor {
    private static final Logger LOGGER = LoggerFactory
            .getLogger(CommandProcessor.class);

    private SerialCommandReaderWriter cmdReaderWriter;
    private String serviceUrl;
    private String httpProxy;
    private ElaServiceClient service = new ElaServiceClient();

    public CommandProcessor(String serviceUrl, String httpProxy, SerialCommandReaderWriter cmdReaderWriter) {
        this.serviceUrl = serviceUrl;
        this.httpProxy = httpProxy;
        this.cmdReaderWriter = cmdReaderWriter;
    }

    public void process() throws IOException {
        LOGGER.info("Processing commands from serial port and sending to ELA Service");
        try {
            cmdReaderWriter.init();
            service.connect(serviceUrl, httpProxy);

            while (true) {
                Command cmd = cmdReaderWriter.getCommand();

                ElaStatus status = null;
                if (Command.CMD_ENTER.equals(cmd.getAction())) {
                    status = processEnterCommand(cmd);
                } else if (Command.CMD_LEAVE.equals(cmd.getAction())) {
                    status = processLeaveCommand(cmd);
                } else {
                    LOGGER.warn("Command [{}] not supported ... ignoring", cmd.toString());
                }
                if (status != null) {
                    cmdReaderWriter.putCommand(new Command(Command.CMD_STATUS, ""+status.getCode().ordinal()));
                }
            }
        } finally {
            cmdReaderWriter.fini();
        }
    }

    private ElaStatus processLeaveCommand(Command cmd) {
        LOGGER.info("Processing LEAVE command [{}]", cmd);
        ElaStatus status = null;
        try {
            status = service.userLeave(cmd.getValue());
        } catch (Exception e) {
            status = new ElaStatus(ElaStatusCode.FAIL, "");
        }
        return status;
    }

    private ElaStatus processEnterCommand(Command cmd) {
        LOGGER.info("Processing ENTER command [{}]", cmd);
        ElaStatus status = null;
        try {
            status = service.userEnter(cmd.getValue());
        } catch (Exception e) {
            status = new ElaStatus(ElaStatusCode.FAIL, "");
        }
        return status;
    }
}

/*
 * Copyright (c) 2014 Rafal Kijewski <kijes@kijes.com>
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package com.kijes.ela.serial.command;

import gnu.io.NRSerialPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class SerialCommandReaderWriter {

    private static final Logger LOGGER = LoggerFactory
            .getLogger(SerialCommandReaderWriter.class);

    private NRSerialPort serial;
    private DataInputStream ins;
    private DataOutputStream outs;
    private String serialPort;

    public SerialCommandReaderWriter(String serialPort, int baudRate) {
        this.serialPort = serialPort;
        this.serial = new NRSerialPort(serialPort, baudRate);
    }

    public void init() {
        LOGGER.info("Connecting to serial port [{}], speed [{}]", serialPort, serial.getBaud());
        serial.connect();
        ins = new DataInputStream(serial.getInputStream());
        outs = new DataOutputStream(serial.getOutputStream());
    }

    public void fini() {
        LOGGER.info("Disconnecting from serial port [{}]", serialPort);
        if (serial != null) {
            serial.disconnect();
        }
    }

    private static byte[] stringToBytesUTF(String str) {
        char[] buffer = str.toCharArray();
        byte[] b = new byte[buffer.length];
        for(int i = 0; i < buffer.length; i++) {
            b[i] = (byte) (buffer[i]&0x00FF);
        }
        return b;
    }

    public void putCommand(Command cmd) throws IOException {
        LOGGER.info("Writing data [{}] to serial port [{}]", cmd.toSerialFormat(), serialPort);
        outs.write(stringToBytesUTF(cmd.toSerialFormat()));
        outs.flush();
    }

    private char getByte() throws IOException {
        while (true) {
            if (ins.available() > 0) {
                byte b = (byte)ins.read();
                return (char)(b&0xFF);
            } else {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    private void moveToCommandStart() throws IOException {
        char ch = '\0';
        do {
            ch = getByte();
       } while (ch != Command.CMD_BEGIN);
    }

    private String readCommand() throws IOException {
        LOGGER.info("Reading data from serial port [{}]", serialPort);
        StringBuilder cmdBuffer = new StringBuilder();
        moveToCommandStart();
        char ch = getByte();
        do {
            if (ch != Command.CMD_END) {
                cmdBuffer.append(ch);
            }
            ch = getByte();
        } while ((ch != Command.CMD_END)&&(cmdBuffer.length() < Command.CMD_MAX_LENGTH));
        return cmdBuffer.toString();
    }

    public Command getCommand() throws IOException {
        LOGGER.info("Retrieving next command");
        while (true) {
            String cmd = readCommand();
            LOGGER.info("Retrieved [{}]", cmd);
            try {
                return Command.createFromString(cmd);
            } catch (CommandParseException e) {
                LOGGER.warn("Invalid command [{}] ... ignoring", cmd);
            }
        }
    }
}

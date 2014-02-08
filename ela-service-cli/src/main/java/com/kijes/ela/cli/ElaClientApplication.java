/*
 * Copyright (c) 2014 Rafal Kijewski <kijes@kijes.com>
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package com.kijes.ela.cli;

import com.kijes.ela.client.ElaServiceClient;
import com.kijes.ela.common.ElaUser;
import org.apache.commons.cli.ParseException;
import org.restlet.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

public class ElaClientApplication extends Application {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(ElaClientApplication.class);
	private CommandLineOptions options = new CommandLineOptions();
	private ElaServiceClient service = new ElaServiceClient();

	public ElaClientApplication() {
	}

    private static void printUsers(Collection<ElaUser> users) {
        LOGGER.info("User list:");
        for (ElaUser user : users) {
            printUser(user);
        }
    }

    private static void printUser(ElaUser elaUser) {
        LOGGER.info("User ["+ elaUser.toString() + "]");
    }

	public static void main(String[] args) throws Exception {
		System.exit(new ElaClientApplication().run(args));
/*
		ElaClientApplication app = new ElaClientApplication();
		app.service.connect("http://localhost:8080/");

        Collection<ElaUser> elaUsers = app.service.getUsers();
        printUsers(elaUsers);

        app.service.createUser("111", "AAA");
		app.service.createUser("222", "BBB");

        elaUsers = app.service.getUsers();
        printUsers(elaUsers);

		app.service.userEnter("AAA");
        ElaUser elaUser = app.service.getUser("111");
        printUser(elaUser);

        app.service.userLeave("AAA");
        elaUser = app.service.getUser("111");
        printUser(elaUser);
		
		app.service.deleteUser("111");

		elaUsers = app.service.getUsers();
        printUsers(elaUsers);
        */
	}

    private void handleOptions(CommandLineOptions options) {
        service.connect(options.getServiceUrl(), options.getHttpProxy());
        if (options.isUserList()) {
            Collection<ElaUser> elaUsers = service.getUsers();
            printUsers(elaUsers);
        } else if (options.isUserInfo()) {
            ElaUser elaUser = service.getUser(options.getUserId());
            printUser(elaUser);
        } else if (options.isUserEnter()) {
            service.userEnter(options.getCardId());
        } else if (options.isUserLeave()) {
            service.userLeave(options.getCardId());
        } else if (options.isUserCreate()) {
            service.createUser(options.getUserId(), options.getCardId());
        } else if (options.isUserDelete()) {
            service.deleteUser(options.getUserId());
        }
    }

	private int run(String[] args) {
        int exitCode = 0;
		try {
            LOGGER.info("Started ELA CLI");
			options.parse(args);
            if (!options.hasAnyOptions()) {
                options.printHelp();
            } else {
                handleOptions(options);
            }
		} catch (ParseException e1) {
			LOGGER.error(e1.getMessage());
			options.printHelp();
            exitCode = 2;
        } catch (Exception e4) {
			LOGGER.error(e4.getMessage());
            exitCode = 1;
		}
        LOGGER.info("Finished ELA CLI");
        return exitCode;
	}
}

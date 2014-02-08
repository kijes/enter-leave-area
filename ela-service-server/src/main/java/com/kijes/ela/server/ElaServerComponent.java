/*
 * Copyright (c) 2014 Rafal Kijewski <kijes@kijes.com>
 * This code is licensed under MIT license (see LICENSE.txt for details)
 */

package com.kijes.ela.server;

import org.restlet.Component;

public class ElaServerComponent extends Component {

	public ElaServerComponent() throws Exception {
		setName("Enter/Leave Area Server Component");
		setDescription("Enter/Leave Area Service");
		setOwner("www.kijes.com");
		getDefaultHost().attachDefault(new ElaServerApplication());
	}
}
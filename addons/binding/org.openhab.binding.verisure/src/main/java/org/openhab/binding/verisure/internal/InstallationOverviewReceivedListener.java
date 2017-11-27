/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.verisure.internal;

import org.openhab.binding.verisure.internal.json.InstallationOverview;

/**
 * The {@link InstallationOverviewReceivedListener} is used to implement the Observer pattern for all sensors under
 * an alarm bridge. This way is possible to update the whole system of sensors with a single call to the Verisure
 * backend
 *
 * @author Jonas Gabriel - Initial contribution
 */
public interface InstallationOverviewReceivedListener {
    void onInstallationOverviewReceived(InstallationOverview installationOverview);
}

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
 * The {@link InstallationOverviewReceivedListener} is responsible for creating things and thing
 * handlers.
 *
 * @author Jonas Gabriel - Initial contribution
 */
public interface InstallationOverviewReceivedListener {
    void onInstallationOverviewReceived(InstallationOverview installationOverview);
}

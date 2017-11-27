/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.verisure.handler;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.openhab.binding.verisure.internal.InstallationOverviewReceivedListener;

/**
 * The {@link VerisureThingHandler} is an abstract class used for handlers under an alarm bridge. It registers itself to
 * receive {@link org.openhab.binding.verisure.internal.json.InstallationOverview} updates.
 *
 * @author Jonas Gabriel - Initial contribution
 */
public abstract class VerisureThingHandler extends BaseThingHandler implements InstallationOverviewReceivedListener {

    public static String DEVICE_LABEL_PARAM = "device-label";

    protected String deviceLabel;

    public VerisureThingHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {

        Configuration config = getThing().getConfiguration();
        this.deviceLabel = (String) config.get(DEVICE_LABEL_PARAM);

        if (this.getBridge() != null) {
            AlarmBridgeHandler alarmBridgeHandler = (AlarmBridgeHandler) this.getBridge().getHandler();
            alarmBridgeHandler.registerInstallationOverviewReceivedListener(this);
        } else {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge could be found");
        }
    }

    @Override
    public void dispose() {
        if (this.getBridge() != null) {
            AlarmBridgeHandler alarmBridgeHandler = (AlarmBridgeHandler) this.getBridge().getHandler();
            alarmBridgeHandler.unregisterInstallationOverviewReceivedListener(this);
        }
    }
}

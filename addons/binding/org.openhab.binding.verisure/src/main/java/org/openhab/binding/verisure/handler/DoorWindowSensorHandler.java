/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.verisure.handler;

import java.time.format.DateTimeFormatter;
import java.util.Optional;

import static org.openhab.binding.verisure.VerisureBindingConstants.*;

import org.eclipse.smarthome.core.library.types.OpenClosedType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.verisure.internal.json.DoorWindow;
import org.openhab.binding.verisure.internal.json.DoorWindowDevice;
import org.openhab.binding.verisure.internal.json.InstallationOverview;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DoorWindowSensorHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jonas Gabriel - Initial contribution
 */
public class DoorWindowSensorHandler extends VerisureThingHandler {

    private final Logger logger = LoggerFactory.getLogger(DoorWindowSensorHandler.class);

    public DoorWindowSensorHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

    }


    @Override
    public void onInstallationOverviewReceived(InstallationOverview installationOverview) {
        DoorWindow doorWindow = installationOverview.getDoorWindow();
        if (doorWindow != null && doorWindow.getDoorWindowDevice() != null) {
            Optional<DoorWindowDevice> doorWindowDevice = doorWindow.getDoorWindowDevice().stream()
                    .filter(candidate -> candidate.getDeviceLabel().equals(this.deviceLabel))
                    .findAny();

            doorWindowDevice.ifPresent(this::updateDoorWindowSensorState);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR);
        }
    }

    public void updateDoorWindowSensorState(DoorWindowDevice doorWindowDevice) {
        this.updateStatus(ThingStatus.ONLINE);

        logger.debug("Door/Window sensor update received [{}]", doorWindowDevice);

        ChannelUID channelUID = new ChannelUID(getThing().getUID(), DOOR_WINDOW_CHANNEL);
        String state = doorWindowDevice.getState();
        if (state.equalsIgnoreCase("OPEN") || state.equalsIgnoreCase("OPENED")) {
            updateState(channelUID, OpenClosedType.OPEN);
        } else if (state.equalsIgnoreCase("CLOSE") || state.equalsIgnoreCase("CLOSED")) {
            updateState(channelUID, OpenClosedType.CLOSED);
        } else {
            updateState(channelUID, UnDefType.UNDEF);
        }

        channelUID = new ChannelUID(getThing().getUID(), LAST_UPDATE_CHANNEL);
        if (doorWindowDevice.getReportTime() != null) {
            StringType timestamp = new StringType(doorWindowDevice.getReportTime().format(DateTimeFormatter.ISO_OFFSET_TIME));
            updateState(channelUID, timestamp);
        } else {
            updateState(channelUID, UnDefType.UNDEF);
        }

        channelUID = new ChannelUID(getThing().getUID(), LOCATION_CHANNEL);
        if (doorWindowDevice.getArea() != null && doorWindowDevice.getDeviceLabel() != null) {
            String sensorArea = doorWindowDevice.getArea();
            StringType location = new StringType(sensorArea);
            updateState(channelUID, location);
        } else {
            updateState(channelUID, UnDefType.UNDEF);
        }
    }
}

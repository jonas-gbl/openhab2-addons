/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.verisure.handler;

import java.time.OffsetDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import static org.openhab.binding.verisure.VerisureBindingConstants.*;

import org.eclipse.smarthome.core.library.types.DateTimeType;
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
 * The {@link DoorWindowSensorHandler} is responsible for Door/Window sensors
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
        OffsetDateTime reportTime = doorWindowDevice.getReportTime();
        if (reportTime != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(Date.from(reportTime.toInstant()));
            DateTimeType timestamp = new DateTimeType(calendar);
            updateState(channelUID, timestamp);
            updateState(channelUID, timestamp);
        } else {
            updateState(channelUID, UnDefType.UNDEF);
        }

        channelUID = new ChannelUID(getThing().getUID(), LOCATION_CHANNEL);
        String sensorArea = doorWindowDevice.getArea();
        if (sensorArea != null) {
            StringType location = new StringType(sensorArea);
            updateState(channelUID, location);
        } else {
            updateState(channelUID, UnDefType.UNDEF);
        }
    }
}

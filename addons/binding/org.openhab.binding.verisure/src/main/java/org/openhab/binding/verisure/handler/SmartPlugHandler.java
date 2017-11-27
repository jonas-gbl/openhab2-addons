/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.verisure.handler;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.openhab.binding.verisure.VerisureBindingConstants.LOCATION_CHANNEL;
import static org.openhab.binding.verisure.VerisureBindingConstants.ON_OFF_CHANNEL;

import org.eclipse.smarthome.core.library.types.OnOffType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.verisure.internal.VerisureSession;
import org.openhab.binding.verisure.internal.json.InstallationOverview;
import org.openhab.binding.verisure.internal.json.SmartPlug;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link SmartPlugHandler} is responsible for handling Smart Plug devices
 *
 * @author Jonas Gabriel - Initial contribution
 */
public class SmartPlugHandler extends VerisureThingHandler {

    private final Logger logger = LoggerFactory.getLogger(SmartPlugHandler.class);

    public SmartPlugHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        logger.debug("Received command of type [{}] and  content [{}]",
                     command.getClass().getCanonicalName(), command);

        if (channelUID.getId().equals(ON_OFF_CHANNEL) && command instanceof OnOffType) {
            OnOffType receivedCommand = (OnOffType) command;
            if (getBridge() != null) {
                AlarmBridgeHandler alarmBridgeHandler = (AlarmBridgeHandler) getBridge().getHandler();
                VerisureSession verisureSession = alarmBridgeHandler.getVerisureSession();
                try {
                    verisureSession.setSmartPlug(alarmBridgeHandler.getGiid(), deviceLabel, receivedCommand == OnOffType.ON);
                } catch (IOException ioe) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR);
                }

                logger.debug("Scheduling one time update after receiving command of type [{}] and  content [{}]",
                             command.getClass().getCanonicalName(), command);
                scheduler.schedule(alarmBridgeHandler::updateAlarmArmState, 1, TimeUnit.SECONDS);
            }
        }
    }

    @Override
    public void onInstallationOverviewReceived(InstallationOverview installationOverview) {
        List<SmartPlug> smartPlugs = installationOverview.getSmartPlugs();
        if (smartPlugs != null) {
            Optional<SmartPlug> doorWindowDevice = smartPlugs.stream()
                    .filter(candidate -> candidate.getDeviceLabel().equals(this.deviceLabel))
                    .findAny();

            doorWindowDevice.ifPresent(this::updateSmartPlugState);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR);
        }
    }

    public void updateSmartPlugState(SmartPlug smartPlug) {
        this.updateStatus(ThingStatus.ONLINE);

        logger.debug("SmartPlug state update received [{}]", smartPlug);

        ChannelUID channelUID = new ChannelUID(getThing().getUID(), ON_OFF_CHANNEL);
        OnOffType currentState = smartPlug.getCurrentState();

        if (currentState != null) {
            updateState(channelUID, currentState);
        } else {
            updateState(channelUID, UnDefType.UNDEF);
        }


        channelUID = new ChannelUID(getThing().getUID(), LOCATION_CHANNEL);
        if (smartPlug.getArea() != null) {
            String sensorArea = smartPlug.getArea();
            StringType location = new StringType(sensorArea);
            updateState(channelUID, location);
        } else {
            updateState(channelUID, UnDefType.UNDEF);
        }
    }
}

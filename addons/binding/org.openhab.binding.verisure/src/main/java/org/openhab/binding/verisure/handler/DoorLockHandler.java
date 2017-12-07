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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.openhab.binding.verisure.VerisureBindingConstants.LOCATION_CHANNEL;
import static org.openhab.binding.verisure.VerisureBindingConstants.LOCK_UNLOCK_CHANNEL;

import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.verisure.LockStatus;
import org.openhab.binding.verisure.internal.VerisureSession;
import org.openhab.binding.verisure.internal.json.DoorLock;
import org.openhab.binding.verisure.internal.json.InstallationOverview;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link DoorLockHandler} is responsible for handling Door Lock devices
 *
 * @author Jonas Gabriel - Initial contribution
 */
public class DoorLockHandler extends VerisureThingHandler {
    private static final Pattern SET_LOCK_STATE_PATTERN = Pattern.compile("(LOCKED|UNLOCKED)(?:_([0-9]{4,}))?");
    private final Logger logger = LoggerFactory.getLogger(DoorLockHandler.class);

    public DoorLockHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (isArmStateCommand(channelUID, command)) {

            String payload = command.toFullString();
            Matcher lockStateMatcher = SET_LOCK_STATE_PATTERN.matcher(payload);
            if (lockStateMatcher.matches()) {
                LockStatus requestedStatus = LockStatus.retrieveById(lockStateMatcher.group(1));
                String receivedPin = lockStateMatcher.group(2);

                AlarmBridgeHandler alarmBridgeHandler = (AlarmBridgeHandler) getBridge().getHandler();
                VerisureSession verisureSession = alarmBridgeHandler.getVerisureSession();
                try {
                    receivedPin = receivedPin != null ? receivedPin : alarmBridgeHandler.getPin();
                    verisureSession.setDoorLock(alarmBridgeHandler.getGiid(), deviceLabel, receivedPin, requestedStatus);
                } catch (IOException ioe) {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR);
                }

                logger.debug("Scheduling one time update after receiving command of type [{}] and  content [{}]",
                             command.getClass().getCanonicalName(), command);
                scheduler.schedule(alarmBridgeHandler::updateAlarmArmState, 3, TimeUnit.SECONDS);
            }


        }

    }

    @Override
    public void onInstallationOverviewReceived(InstallationOverview installationOverview) {
        List<DoorLock> DoorLocks = installationOverview.getDoorLockStatusList();
        if (DoorLocks != null) {
            Optional<DoorLock> doorWindowDevice = DoorLocks.stream()
                    .filter(candidate -> candidate.getDeviceLabel().equals(this.deviceLabel))
                    .findAny();

            doorWindowDevice.ifPresent(this::updateDoorLockState);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR);
        }
    }

    public void updateDoorLockState(DoorLock doorLock) {
        this.updateStatus(ThingStatus.ONLINE);

        logger.debug("Door lock state update received [{}]", doorLock);

        ChannelUID channelUID = new ChannelUID(getThing().getUID(), LOCK_UNLOCK_CHANNEL);
        LockStatus lockStatus = doorLock.getCurrentLockState();

        if (lockStatus != null) {
            updateState(channelUID, new StringType(lockStatus.id));
        } else {
            updateState(channelUID, UnDefType.UNDEF);
        }


        channelUID = new ChannelUID(getThing().getUID(), LOCATION_CHANNEL);
        String smartPlugArea = doorLock.getArea();
        if (smartPlugArea != null) {
            StringType location = new StringType(smartPlugArea);
            updateState(channelUID, location);
        } else {
            updateState(channelUID, UnDefType.UNDEF);
        }
    }

    private boolean isArmStateCommand(ChannelUID channelUID, Command command) {

        boolean result = false;
        if (command instanceof StringType) {
            String payload = command.toFullString();

            Matcher burstMatcher = SET_LOCK_STATE_PATTERN.matcher(payload);
            result = burstMatcher.matches();
        }

        result &= channelUID.getId().equals(LOCK_UNLOCK_CHANNEL);

        return result;
    }
}

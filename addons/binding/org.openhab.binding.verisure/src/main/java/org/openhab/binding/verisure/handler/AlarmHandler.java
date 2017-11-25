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
import java.math.BigDecimal;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.openhab.binding.verisure.VerisureBindingConstants.ALARM_STATUS_CHANNEL;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.*;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.verisure.VerisureBindingConstants;
import org.openhab.binding.verisure.internal.ArmStatus;
import org.openhab.binding.verisure.internal.VerisureSession;
import org.openhab.binding.verisure.internal.VerisureUrls;
import org.openhab.binding.verisure.internal.json.ArmState;
import org.openhab.binding.verisure.internal.json.InstallationOverview;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AlarmHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jonas Gabriel - Initial contribution
 */
public class AlarmHandler extends BaseBridgeHandler {


    private final Logger logger = LoggerFactory.getLogger(AlarmHandler.class);

    private String giid;
    private String pin;
    private boolean allowStateUpdate = false;
    private BigDecimal refresh;

    private VerisureSession verisureSession;

    private ScheduledFuture<?> refreshJob, loginJob;

    public AlarmHandler(Bridge bridge) {
        super(bridge);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Verisure handler.");

        Configuration config = getThing().getConfiguration();

        giid = (String) config.get(VerisureBindingConstants.GIID_PARAM);
        String username = (String) config.get(VerisureBindingConstants.USERNAME_PARAM);
        String password = (String) config.get(VerisureBindingConstants.PASSWORD_PARAM);
        refresh = (BigDecimal) config.get(VerisureBindingConstants.REFRESH_PARAM);

        pin = (String) config.get(VerisureBindingConstants.PIN_PARAM);
        allowStateUpdate = (pin != null && !pin.isEmpty());

        String baseUrl = (String) config.get(VerisureBindingConstants.BASEURL_PARAM);
        VerisureUrls verisureUrls = VerisureUrls.withBaseUrl(baseUrl);

        try {
            verisureSession = new VerisureSession(verisureUrls, username, password);
            startAutomaticRefresh();
        } catch (IllegalArgumentException ilae) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, ilae.getMessage());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(ALARM_STATUS_CHANNEL) && command instanceof StringType) {
            StringType receivedCommand = (StringType) command;
            if (allowStateUpdate) {
                logger.debug("Requested state is [{}]", receivedCommand);
                ArmStatus requestedState = ArmStatus.retrieveById(receivedCommand.toString());
                this.setArmState(requestedState);
            }
            logger.debug("Scheduling one time update after receiving update command [{}]", command);
            scheduler.schedule(this::updateAlarmArmState, 1, TimeUnit.SECONDS);
        }
    }


    private void startAutomaticRefresh() {
        refreshJob = scheduler.scheduleWithFixedDelay(this::updateAlarmArmState, 0, refresh.intValue(), TimeUnit.SECONDS);
        loginJob = scheduler.scheduleWithFixedDelay(this::updateVerisureCookie, 1, 12, TimeUnit.HOURS);
    }

    private synchronized void updateAlarmArmState() {
        try {
            if (verisureSession.isLoggedIn() || verisureSession.login()) {
                InstallationOverview data = verisureSession.retrieveInstallationOverview(giid);
                updateStatus(ThingStatus.ONLINE);
                ArmState armState = data.getArmState();
                ArmStatus status = armState != null ? armState.getStatusType() : null;
                if (status != null) {
                    updateStatus(ThingStatus.ONLINE);
                    ChannelUID channelUID = new ChannelUID(getThing().getUID(), ALARM_STATUS_CHANNEL);
                    StringType state = new StringType(status.id);
                    updateState(channelUID, state);
                } else {
                    updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, "Failed to retrieve a valid ArmStatus");
                }
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    private synchronized void setArmState(ArmStatus requestedState) {
        try {
            if (verisureSession.isLoggedIn() || verisureSession.login()) {
                ArmStatus updatedState = verisureSession.setArmState(giid, pin, requestedState);
                updateStatus(ThingStatus.ONLINE);
                ChannelUID channelUID = new ChannelUID(getThing().getUID(), ALARM_STATUS_CHANNEL);
                StringType state = new StringType(updatedState.id);
                updateState(channelUID, state);
            }
        } catch (IOException ioe) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, ioe.getMessage());
        }
    }

    private synchronized void updateVerisureCookie() {
        try {
            verisureSession.login();
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    @Override
    public void dispose() {
        refreshJob.cancel(true);
        loginJob.cancel(true);
        try {
            verisureSession.logout();
        } catch (IOException e) {
            logger.debug("Failed to logout", e);
        }
    }
}

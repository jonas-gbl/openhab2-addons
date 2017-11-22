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
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.verisure.internal.VerisureSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VerisureHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jonas Gabriel - Initial contribution
 */
public class VerisureHandler extends BaseThingHandler {


    private static final String GIID_PARAM = "giid";
    private static final String USERNAME_PARAM = "username";
    private static final String PASSWORD_PARAM = "password";
    private static final String REFRESH_PARAM = "refresh";

    private final Logger logger = LoggerFactory.getLogger(VerisureHandler.class);

    private String giid;

    private BigDecimal refresh;

    private VerisureSession verisureSession;

    private ScheduledFuture<?> refreshJob;

    public VerisureHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        logger.debug("Initializing Verisure handler.");

        Configuration config = getThing().getConfiguration();

        giid = (String) config.get(GIID_PARAM);
        String username = (String) config.get(USERNAME_PARAM);
        String password = (String) config.get(PASSWORD_PARAM);
        refresh = (BigDecimal) config.get(REFRESH_PARAM);
        verisureSession = new VerisureSession(username, password);

        ChannelUID channelUID = new ChannelUID(getThing().getUID(), ALARM_STATUS_CHANNEL);
        StringType state = new StringType(VerisureSession.ArmState.ARMED_AWAY.id);
        updateState(channelUID, state);

        startAutomaticRefresh();
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(ALARM_STATUS_CHANNEL)) {
            // TODO: handle command

            // Note: if communication with thing fails for some reason,
            // indicate that by setting the status with detail information
            // updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.COMMUNICATION_ERROR,
            // "Could not control device at IP address x.x.x.x");
        }
    }

    private void startAutomaticRefresh() {
        refreshJob = scheduler.scheduleWithFixedDelay(this::updateAlarmArmState, 0, refresh.intValue(), TimeUnit.SECONDS);
    }

    private void updateAlarmArmState() {

        try {
            if (verisureSession.login()) {
                VerisureSession.ArmState data = verisureSession.getArmState(giid);
                updateStatus(ThingStatus.ONLINE);
                ChannelUID channelUID = new ChannelUID(getThing().getUID(), ALARM_STATUS_CHANNEL);
                StringType state = new StringType(data.id);
                updateState(channelUID, state);
            }
        } catch (IOException e) {
            logger.debug("Exception occurred during execution: {}", e.getMessage(), e);
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        }

    }

    @Override
    public void dispose() {
        refreshJob.cancel(true);
        try {
            verisureSession.logout();
        } catch (IOException e) {
            logger.debug("Failed to logout", e);
        }
    }
}

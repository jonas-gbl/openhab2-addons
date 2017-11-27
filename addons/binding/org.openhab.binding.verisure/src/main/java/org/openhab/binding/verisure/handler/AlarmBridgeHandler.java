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
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static org.openhab.binding.verisure.VerisureBindingConstants.ALARM_STATUS_CHANNEL;

import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseBridgeHandler;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.RefreshType;
import org.openhab.binding.verisure.internal.InstallationOverviewReceivedListener;
import org.openhab.binding.verisure.internal.VerisureSession;
import org.openhab.binding.verisure.internal.VerisureUrls;
import org.openhab.binding.verisure.internal.json.ArmState;
import org.openhab.binding.verisure.internal.json.ArmStatus;
import org.openhab.binding.verisure.internal.json.InstallationOverview;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link AlarmBridgeHandler} a bridge handler for handing a Verisure Alarm Installation
 *
 * @author Jonas Gabriel - Initial contribution
 */
public class AlarmBridgeHandler extends BaseBridgeHandler {


    // Configuration Parameters;
    public static final String GIID_PARAM = "giid";
    public static final String USERNAME_PARAM = "username";
    public static final String PASSWORD_PARAM = "password";
    public static final String REFRESH_PARAM = "refresh";
    public static final String BASEURL_PARAM = "baseurl";
    public static final String PIN_PARAM = "pin";

    private final Logger logger = LoggerFactory.getLogger(AlarmBridgeHandler.class);
    private final CopyOnWriteArrayList<InstallationOverviewReceivedListener> installationOverviewReceivedListeners = new CopyOnWriteArrayList<>();


    private String giid;
    private String pin;
    private boolean allowStateUpdate = false;
    private BigDecimal refresh;

    private VerisureSession verisureSession;
    private ScheduledFuture<?> refreshJob, loginJob;

    public AlarmBridgeHandler(Bridge bridge) {
        super(bridge);
    }

    public VerisureSession getVerisureSession() {
        return verisureSession;
    }

    public String getGiid() {
        return giid;
    }

    @Override
    public void initialize() {
        try {
            logger.debug("Initializing AlarmBridgeHandler.");

            Configuration config = getThing().getConfiguration();
            String baseUrl = (String) config.get(BASEURL_PARAM);
            giid = (String) config.get(GIID_PARAM);
            String username = (String) config.get(USERNAME_PARAM);
            String password = (String) config.get(PASSWORD_PARAM);
            refresh = (BigDecimal) config.get(REFRESH_PARAM);
            pin = (String) config.get(PIN_PARAM);

            Preconditions.checkState(StringUtils.isNotEmpty(baseUrl), "base URL is empty");
            Preconditions.checkState(StringUtils.isNotEmpty(giid), "GIID misconfigured");
            Preconditions.checkState(StringUtils.isNotEmpty(username), "Username is empty");
            Preconditions.checkState(StringUtils.isNotEmpty(password), "Password is empty");
            Preconditions.checkState(Objects.nonNull(refresh), "Refresh is null");
            Preconditions.checkState(refresh.intValue() > 0, "Refresh is not positive");


            allowStateUpdate = StringUtils.isNotBlank(pin);
            VerisureUrls verisureUrls = VerisureUrls.withBaseUrl(baseUrl);


            verisureSession = new VerisureSession(verisureUrls, username, password);
            startAutomaticRefresh();
        } catch (IllegalArgumentException | IllegalStateException exception) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR, exception.getMessage());
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (channelUID.getId().equals(ALARM_STATUS_CHANNEL) && command instanceof StringType) {
            StringType receivedCommand = (StringType) command;
            if (allowStateUpdate) {
                logger.debug("Requested arm state is [{}]", receivedCommand);
                ArmStatus requestedState = ArmStatus.retrieveById(receivedCommand.toString());
                this.setArmState(requestedState);
            }
            logger.debug("Scheduling one time update after receiving update command [{}]", command);
            scheduler.schedule(this::updateAlarmArmState, 1, TimeUnit.SECONDS);
        } else if (command instanceof RefreshType) {
            logger.debug("Scheduling one time update after receiving refresh command [{}]", command);
            scheduler.schedule(this::updateAlarmArmState, 1, TimeUnit.SECONDS);
        }
    }


    private void startAutomaticRefresh() {
        refreshJob = scheduler.scheduleWithFixedDelay(this::updateAlarmArmState, 0, refresh.intValue(), TimeUnit.SECONDS);
        loginJob = scheduler.scheduleWithFixedDelay(this::updateVerisureCookie, 1, 12, TimeUnit.HOURS);
    }

    public synchronized void updateAlarmArmState() {
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

                notifyInstallationOverviewReceivedListeners(data);
            }
        } catch (IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR, e.getMessage());
        }
    }

    public void registerInstallationOverviewReceivedListener(InstallationOverviewReceivedListener listener) {
        this.installationOverviewReceivedListeners.add(listener);
        logger.debug("Scheduling one time update after registration of listener");
        scheduler.schedule(this::updateAlarmArmState, 1, TimeUnit.SECONDS);
    }

    public void unregisterInstallationOverviewReceivedListener(InstallationOverviewReceivedListener listener) {
        this.installationOverviewReceivedListeners.remove(listener);
    }

    private void notifyInstallationOverviewReceivedListeners(InstallationOverview installationOverview) {
        logger.debug("Notifying [{}] listener(s)", installationOverviewReceivedListeners.size());
        this.installationOverviewReceivedListeners.forEach(listener -> listener.onInstallationOverviewReceived(installationOverview));
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

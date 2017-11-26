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
import java.util.List;
import java.util.Optional;

import static org.openhab.binding.verisure.VerisureBindingConstants.*;

import org.eclipse.smarthome.config.core.Configuration;
import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.openhab.binding.verisure.internal.InstallationOverviewReceivedListener;
import org.openhab.binding.verisure.internal.json.ClimateValue;
import org.openhab.binding.verisure.internal.json.InstallationOverview;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ClimateSensorHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jonas Gabriel - Initial contribution
 */
public class ClimateSensorHandler extends BaseThingHandler implements InstallationOverviewReceivedListener {


    private static String DEVICE_LABEL_PARAM = "device-label";

    private final Logger logger = LoggerFactory.getLogger(ClimateSensorHandler.class);

    private String deviceLabel;

    public ClimateSensorHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {

        Configuration config = getThing().getConfiguration();
        this.deviceLabel = (String) config.get(DEVICE_LABEL_PARAM);

        this.updateStatus(ThingStatus.ONLINE);

        AlarmBridgeHandler alarmBridgeHandler = (AlarmBridgeHandler) this.getBridge().getHandler();
        if (alarmBridgeHandler != null) {
            alarmBridgeHandler.registerInstallationOverviewReceivedListener(this);
        } else {
            this.updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, "No bridge could be found");
        }
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

    }

    @Override
    public void onInstallationOverviewReceived(InstallationOverview installationOverview) {
        List<ClimateValue> climateValues = installationOverview.getClimateValues();
        if (climateValues != null) {
            Optional<ClimateValue> climateValue = climateValues.stream()
                    .filter(candidate -> candidate.getDeviceLabel().equals(this.deviceLabel))
                    .findAny();

            climateValue.ifPresent(this::updateClimateSensorState);
        } else {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.CONFIGURATION_ERROR);
        }
    }

    public void updateClimateSensorState(ClimateValue climateValue) {
        this.updateStatus(ThingStatus.ONLINE);

        logger.debug("Climate sensor update received [{}]", climateValue);

        ChannelUID channelUID;
        if (climateValue.getTemperature() != null) {
            channelUID = new ChannelUID(getThing().getUID(), TEMPERATURE_CHANNEL);
            DecimalType temperature = new DecimalType(climateValue.getTemperature());
            updateState(channelUID, temperature);
        }


        if (climateValue.getHumidity() != null) {
            channelUID = new ChannelUID(getThing().getUID(), HUMIDITY_CHANNEL);
            DecimalType humidity = new DecimalType(climateValue.getHumidity());
            updateState(channelUID, humidity);
        }

        if (climateValue.getTime() != null) {
            channelUID = new ChannelUID(getThing().getUID(), LAST_UPDATE_CHANNEL);
            StringType timestamp = new StringType(climateValue.getTime().format(DateTimeFormatter.ISO_OFFSET_TIME));
            updateState(channelUID, timestamp);
        }

        if (climateValue.getDeviceArea() != null && climateValue.getDeviceLabel() != null) {
            channelUID = new ChannelUID(getThing().getUID(), LOCATION_CHANNEL);
            String sensorLocation = climateValue.getDeviceArea() + " (" + climateValue.getDeviceType() + ")";
            StringType location = new StringType(sensorLocation);
            updateState(channelUID, location);
        }
    }

    @Override
    public void dispose() {
        AlarmBridgeHandler alarmBridgeHandler = (AlarmBridgeHandler) this.getBridge().getHandler();
        alarmBridgeHandler.unregisterInstallationOverviewReceivedListener(this);
    }
}

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

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingStatus;
import org.eclipse.smarthome.core.thing.ThingStatusDetail;
import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.UnDefType;
import org.openhab.binding.verisure.internal.InstallationOverviewReceivedListener;
import org.openhab.binding.verisure.internal.json.ClimateValue;
import org.openhab.binding.verisure.internal.json.InstallationOverview;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ClimateSensorHandler} is responsible for handling climate sensors
 *
 *
 * @author Jonas Gabriel - Initial contribution
 */
public class ClimateSensorHandler extends VerisureThingHandler implements InstallationOverviewReceivedListener {


    private final Logger logger = LoggerFactory.getLogger(ClimateSensorHandler.class);


    public ClimateSensorHandler(Thing thing) {
        super(thing);
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
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.OFFLINE.COMMUNICATION_ERROR);
        }
    }

    public void updateClimateSensorState(ClimateValue climateValue) {
        this.updateStatus(ThingStatus.ONLINE);

        logger.debug("Climate sensor update received [{}]", climateValue);

        ChannelUID channelUID = new ChannelUID(getThing().getUID(), TEMPERATURE_CHANNEL);
        if (climateValue.getTemperature() != null) {
            DecimalType temperature = new DecimalType(climateValue.getTemperature());
            updateState(channelUID, temperature);
        } else {
            updateState(channelUID, UnDefType.UNDEF);
        }


        channelUID = new ChannelUID(getThing().getUID(), HUMIDITY_CHANNEL);
        if (climateValue.getHumidity() != null) {
            DecimalType humidity = new DecimalType(climateValue.getHumidity());
            updateState(channelUID, humidity);
        } else {
            updateState(channelUID, UnDefType.UNDEF);
        }

        channelUID = new ChannelUID(getThing().getUID(), LAST_UPDATE_CHANNEL);
        if (climateValue.getTime() != null) {
            StringType timestamp = new StringType(climateValue.getTime().format(DateTimeFormatter.ISO_OFFSET_TIME));
            updateState(channelUID, timestamp);
        } else {
            updateState(channelUID, UnDefType.UNDEF);
        }

        channelUID = new ChannelUID(getThing().getUID(), LOCATION_CHANNEL);
        if (climateValue.getDeviceArea() != null && climateValue.getDeviceLabel() != null) {
            String sensorLocation = climateValue.getDeviceArea() + " (" + climateValue.getDeviceType() + ")";
            StringType location = new StringType(sensorLocation);
            updateState(channelUID, location);
        } else {
            updateState(channelUID, UnDefType.UNDEF);
        }
    }
}

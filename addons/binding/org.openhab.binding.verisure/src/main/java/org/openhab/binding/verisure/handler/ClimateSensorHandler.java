/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.verisure.handler;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.openhab.binding.verisure.VerisureBindingConstants.*;

import org.eclipse.smarthome.core.library.types.DecimalType;
import org.eclipse.smarthome.core.library.types.StringType;
import org.eclipse.smarthome.core.thing.ChannelUID;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandler;
import org.eclipse.smarthome.core.types.Command;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link ClimateSensorHandler} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jonas Gabriel - Initial contribution
 */
public class ClimateSensorHandler extends BaseThingHandler {


    private final Logger logger = LoggerFactory.getLogger(ClimateSensorHandler.class);

    public ClimateSensorHandler(Thing thing) {
        super(thing);
    }

    @Override
    public void initialize() {
        super.initialize();
        ChannelUID channelUID = new ChannelUID(getThing().getUID(), TEMPERATURE_CHANNEL);
        DecimalType temperature = new DecimalType(34.6d);
        updateState(channelUID, temperature);

        channelUID = new ChannelUID(getThing().getUID(), HUMIDITY_CHANNEL);
        DecimalType humidity = new DecimalType(80);
        updateState(channelUID, humidity);

        channelUID = new ChannelUID(getThing().getUID(), LAST_UPDATE_CHANNEL);
        StringType timestamp = new StringType(ZonedDateTime.now().format(DateTimeFormatter.ISO_LOCAL_TIME));
        updateState(channelUID, timestamp);

        channelUID = new ChannelUID(getThing().getUID(), LOCATION_CHANNEL);
        StringType location = new StringType("Somewhere");
        updateState(channelUID, location);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {

    }
}

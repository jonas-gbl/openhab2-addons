/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.verisure.internal;

import java.util.Set;

import static org.openhab.binding.verisure.VerisureBindingConstants.THING_TYPE_CLIMATE_SENSOR;
import static org.openhab.binding.verisure.VerisureBindingConstants.THING_TYPE_VERISURE_ALARM;

import com.google.common.collect.Sets;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.verisure.handler.AlarmHandler;
import org.openhab.binding.verisure.handler.ClimateSensorHandler;
import org.osgi.service.component.annotations.Component;

/**
 * The {@link VerisureHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Jonas Gabriel - Initial contribution
 */
@Component(service = ThingHandlerFactory.class, immediate = true, configurationPid = "binding.verisure")
@NonNullByDefault
public class VerisureHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Sets.newHashSet(THING_TYPE_VERISURE_ALARM, THING_TYPE_CLIMATE_SENSOR);

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_VERISURE_ALARM)) {
            return new AlarmHandler((Bridge) thing);
        } else if (thingTypeUID.equals(THING_TYPE_CLIMATE_SENSOR)) {
            return new ClimateSensorHandler(thing);
        }

        return null;
    }
}

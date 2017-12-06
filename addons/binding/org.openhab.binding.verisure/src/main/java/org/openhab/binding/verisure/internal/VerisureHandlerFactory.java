/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.verisure.internal;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

import static org.openhab.binding.verisure.VerisureBindingConstants.*;

import com.google.common.collect.Sets;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.Bridge;
import org.eclipse.smarthome.core.thing.Thing;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.eclipse.smarthome.core.thing.binding.BaseThingHandlerFactory;
import org.eclipse.smarthome.core.thing.binding.ThingHandler;
import org.eclipse.smarthome.core.thing.binding.ThingHandlerFactory;
import org.openhab.binding.verisure.discovery.VerisureSensorDiscoveryService;
import org.openhab.binding.verisure.handler.*;
import org.osgi.framework.ServiceRegistration;
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

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS =
            Sets.newHashSet(THING_TYPE_VERISURE_ALARM,
                            THING_TYPE_CLIMATE_SENSOR,
                            THING_TYPE_WINDOW_DOOR_SENSOR,
                            THING_TYPE_SMARTPLUG,
                            THING_TYPE_DOORLOCK);

    private Map<ThingUID, ServiceRegistration<?>> discoveryServiceRegs = new HashMap<>();

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (thingTypeUID.equals(THING_TYPE_VERISURE_ALARM)) {
            AlarmBridgeHandler handler = new AlarmBridgeHandler((Bridge) thing);
            registerApplianceDiscoveryService(handler);
            return handler;
        } else if (thingTypeUID.equals(THING_TYPE_CLIMATE_SENSOR)) {
            return new ClimateSensorHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_WINDOW_DOOR_SENSOR)) {
            return new DoorWindowSensorHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_SMARTPLUG)) {
            return new SmartPlugHandler(thing);
        } else if (thingTypeUID.equals(THING_TYPE_DOORLOCK)) {
            return new DoorLockHandler(thing);
        }

        return null;
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof AlarmBridgeHandler) {
            ServiceRegistration<?> serviceReg = this.discoveryServiceRegs.get(thingHandler.getThing().getUID());
            if (serviceReg != null) {
                // remove discovery service, if bridge handler is removed
                VerisureSensorDiscoveryService service = (VerisureSensorDiscoveryService) bundleContext
                        .getService(serviceReg.getReference());
                service.deactivate();
                serviceReg.unregister();
                discoveryServiceRegs.remove(thingHandler.getThing().getUID());
            }
        }
    }

    private synchronized void registerApplianceDiscoveryService(AlarmBridgeHandler bridgeHandler) {
        VerisureSensorDiscoveryService discoveryService = new VerisureSensorDiscoveryService(bridgeHandler);
        discoveryService.activate();
        this.discoveryServiceRegs.put(bridgeHandler.getThing().getUID(), bundleContext
                .registerService(DiscoveryService.class.getName(), discoveryService, new Hashtable<String, Object>()));
    }
}

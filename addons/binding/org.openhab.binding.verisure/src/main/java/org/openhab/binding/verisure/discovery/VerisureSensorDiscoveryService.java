/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.verisure.discovery;

import java.io.IOException;
import java.util.*;

import static org.openhab.binding.verisure.VerisureBindingConstants.*;

import com.google.common.collect.Sets;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.verisure.handler.AlarmBridgeHandler;
import org.openhab.binding.verisure.handler.VerisureThingHandler;
import org.openhab.binding.verisure.internal.InstallationOverviewReceivedListener;
import org.openhab.binding.verisure.internal.json.ClimateValue;
import org.openhab.binding.verisure.internal.json.DoorWindowDevice;
import org.openhab.binding.verisure.internal.json.InstallationOverview;
import org.openhab.binding.verisure.internal.json.SmartPlug;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VerisureSensorDiscoveryService} is used to connect to the Verisure services.
 *
 * @author Jonas Gabriel - Initial contribution
 */
public class VerisureSensorDiscoveryService extends AbstractDiscoveryService implements InstallationOverviewReceivedListener {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS =
            Sets.newHashSet(THING_TYPE_CLIMATE_SENSOR,
                            THING_TYPE_WINDOW_DOOR_SENSOR,
                            THING_TYPE_SMARTPLUG);

    private static final int SEARCH_TIME = 60;
    private final Logger logger = LoggerFactory.getLogger(VerisureSensorDiscoveryService.class);
    private AlarmBridgeHandler alarmBridgeHandler;
    private String giid;

    public VerisureSensorDiscoveryService(AlarmBridgeHandler alarmBridgeHandler) throws IllegalArgumentException {
        super(SUPPORTED_THING_TYPES_UIDS, SEARCH_TIME, false);
        this.alarmBridgeHandler = alarmBridgeHandler;
        this.giid = alarmBridgeHandler.getGiid();
    }

    public void activate() {
        alarmBridgeHandler.registerInstallationOverviewReceivedListener(this);
    }

    @Override
    public void deactivate() {
        removeOlderResults(new Date().getTime());
        alarmBridgeHandler.unregisterInstallationOverviewReceivedListener(this);
    }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        return SUPPORTED_THING_TYPES_UIDS;
    }


    @Override
    protected void startScan() {
        try {
            InstallationOverview installationOverview =
                    alarmBridgeHandler.getVerisureSession().retrieveInstallationOverview(giid);

            processInstallationOverview(installationOverview);

        } catch (IOException e) {
            logger.debug("Failed to retrieve installation overview");
        }

    }

    private void processInstallationOverview(InstallationOverview installationOverview) {
        ThingUID bridgeUID = alarmBridgeHandler.getThing().getUID();

        for (ClimateValue climateValue : installationOverview.getClimateValues()) {
            String deviceLabel = climateValue.getDeviceLabel();
            String normalizedDeviceLabel = StringUtils.lowerCase(deviceLabel.replaceAll("[^a-zA-Z0-9_]", "_"));
            ThingUID thingUID = new ThingUID(THING_TYPE_CLIMATE_SENSOR, bridgeUID, normalizedDeviceLabel);

            Map<String, Object> properties = new HashMap<>(1);
            properties.put(VerisureThingHandler.DEVICE_LABEL_PARAM, deviceLabel);

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                    .withProperties(properties)
                    .withBridge(bridgeUID)
                    .withLabel("Climate sensor " + deviceLabel)
                    .build();

            thingDiscovered(discoveryResult);
        }

        List<DoorWindowDevice> doorWindowDevices = installationOverview.getDoorWindow().getDoorWindowDevice();
        for (DoorWindowDevice doorWindowDevice : doorWindowDevices) {
            String deviceLabel = doorWindowDevice.getDeviceLabel();
            String normalizedDeviceLabel = StringUtils.lowerCase(deviceLabel.replaceAll("[^a-zA-Z0-9_]", "_"));
            ThingUID thingUID = new ThingUID(THING_TYPE_WINDOW_DOOR_SENSOR, bridgeUID, normalizedDeviceLabel);

            Map<String, Object> properties = new HashMap<>(1);
            properties.put(VerisureThingHandler.DEVICE_LABEL_PARAM, deviceLabel);

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                    .withProperties(properties)
                    .withBridge(bridgeUID)
                    .withLabel("Door/Window sensor " + deviceLabel)
                    .build();

            thingDiscovered(discoveryResult);

        }

        List<SmartPlug> smartPlugs = installationOverview.getSmartPlugs();
        for (SmartPlug smartPlug : smartPlugs) {
            String deviceLabel = smartPlug.getDeviceLabel();
            String normalizedDeviceLabel = StringUtils.lowerCase(deviceLabel.replaceAll("[^a-zA-Z0-9_]", "_"));
            ThingUID thingUID = new ThingUID(THING_TYPE_SMARTPLUG, bridgeUID, normalizedDeviceLabel);

            Map<String, Object> properties = new HashMap<>(1);
            properties.put(VerisureThingHandler.DEVICE_LABEL_PARAM, deviceLabel);

            DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                    .withProperties(properties)
                    .withBridge(bridgeUID)
                    .withLabel("Smart Plug " + deviceLabel)
                    .build();

            thingDiscovered(discoveryResult);
        }
    }


    @Override
    protected synchronized void stopScan() {
        super.stopScan();
        removeOlderResults(getTimestampOfLastScan());
    }

    @Override
    public void onInstallationOverviewReceived(InstallationOverview installationOverview) {
//        long timestamp = new Date().getTime();
//        removeOlderResults(timestamp);
        processInstallationOverview(installationOverview);
    }

    private ThingUID getThingUID(InstallationOverview appliance) {
        return null;
    }
}

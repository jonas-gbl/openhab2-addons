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
import java.math.BigDecimal;
import java.util.*;

import static org.openhab.binding.verisure.VerisureBindingConstants.THING_TYPE_VERISURE_ALARM;
import static org.openhab.binding.verisure.handler.AlarmBridgeHandler.*;

import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringUtils;
import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.config.discovery.DiscoveryService;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.verisure.internal.VerisureSession;
import org.openhab.binding.verisure.internal.VerisureUrls;
import org.openhab.binding.verisure.internal.json.Installation;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VerisureSensorDiscoveryService} is used to connect to the Verisure services.
 *
 * @author Jonas Gabriel - Initial contribution
 */
@Component(service = DiscoveryService.class, immediate = true, configurationPid = "discovery.verisure")
public class VerisureAlarmDiscoveryService extends AbstractDiscoveryService {
    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Collections.singleton(THING_TYPE_VERISURE_ALARM);
    private static final int SEARCH_TIME = 5;
    private static final int DEFAULT_REFRESH_INTERVAL = 60;
    private final Logger logger = LoggerFactory.getLogger(VerisureAlarmDiscoveryService.class);
    private String baseUrl;
    private String username;
    private String password;
    private String refresh;
    private String pin;

    public VerisureAlarmDiscoveryService() {
        super(SUPPORTED_THING_TYPES_UIDS, SEARCH_TIME, false);
    }


    @Override
    protected void activate(Map<String, Object> configProperties) {
        super.activate(configProperties);
        if (configProperties != null) {
            Object property = configProperties.get(BASEURL_PARAM);
            if (property != null) {
                baseUrl = (String) property;
                logger.debug("Found baseurl property [{}]", baseUrl);
            }
            property = configProperties.get(USERNAME_PARAM);
            if (property != null) {
                username = (String) property;
                logger.debug("Found username property [{}]", username);
            }
            property = configProperties.get(PASSWORD_PARAM);
            if (property != null) {
                password = (String) property;
                logger.debug("Found password property [{}]", password);
            }
            property = configProperties.get(PIN_PARAM);
            if (property != null) {
                pin = (String) property;
                logger.debug("Found pin property [{}] as [{}]", property, property.getClass().getCanonicalName());
            }
            property = configProperties.get(REFRESH_PARAM);
            if (property != null) {
                refresh = (String) property;
                logger.debug("Found refresh property [{}] as [{}]", property, property.getClass().getCanonicalName());
            }
        }

    }

    @Override
    protected void startScan() {

        try {
            Preconditions.checkState(StringUtils.isNotEmpty(baseUrl), "No base URL configured");
            Preconditions.checkState(StringUtils.isNotEmpty(username), "No username configured");
            Preconditions.checkState(StringUtils.isNotEmpty(password), "No password configured");

            VerisureSession verisureSession = new VerisureSession(
                    VerisureUrls.withBaseUrl(baseUrl),
                    username, password);

            verisureSession.login();
            List<Installation> installations = verisureSession.retrieveInstallations();
            for (Installation installation : installations) {
                String giid = installation.getGiid();

                ThingUID thingUID = new ThingUID(THING_TYPE_VERISURE_ALARM, "verisure-" + giid);

                Map<String, Object> properties = new HashMap<>(1);
                properties.put(GIID_PARAM, giid);
                properties.put(USERNAME_PARAM, username);
                properties.put(PASSWORD_PARAM, password);

                BigDecimal refreshValue;
                try {
                    refreshValue = new BigDecimal(refresh);
                } catch (NumberFormatException nfe) {
                    refreshValue = new BigDecimal(DEFAULT_REFRESH_INTERVAL);
                }
                properties.put(REFRESH_PARAM, refreshValue);

                properties.put(PIN_PARAM, pin);

                String alias = installation.getAlias();
                String label = StringUtils.isNotBlank(alias) ? "Alarm (" + alias + ")" : "Alarm (giid=" + giid + ")";

                DiscoveryResult discoveryResult = DiscoveryResultBuilder.create(thingUID)
                        .withProperties(properties)
                        .withLabel(label)
                        .build();

                thingDiscovered(discoveryResult);

                verisureSession.logout();
            }
        } catch (IOException e) {
            logger.debug("Problems discovering alarms", e);
        } catch (IllegalStateException iae) {
            logger.error("The discovery service is mis-configured", iae);
        }

    }
}

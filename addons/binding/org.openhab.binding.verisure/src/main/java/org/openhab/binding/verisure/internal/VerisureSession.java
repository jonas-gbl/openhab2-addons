/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.verisure.internal;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.fatboyindustrial.gsonjavatime.Converters;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jetty.util.B64Code;
import org.openhab.binding.verisure.ArmStatus;
import org.openhab.binding.verisure.internal.http.HttpResponse;
import org.openhab.binding.verisure.internal.http.HttpUtils;
import org.openhab.binding.verisure.internal.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VerisureSession} is a port of https://github.com/persandstrom/python-verisure
 *
 * @author Jonas Gabriel - Initial contribution
 */
public class VerisureSession {

    private static final String EMPTY = "";
    private final Logger logger = LoggerFactory.getLogger(VerisureSession.class);
    private final String username;
    private final String password;
    private final Gson gson;
    private final VerisureUrls verisureUrls;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private String vid;

    public VerisureSession(VerisureUrls verisureUrls, String username, String password) {
        this.verisureUrls = verisureUrls;
        this.username = username;
        this.password = password;
        this.gson = Converters.registerOffsetDateTime(new GsonBuilder()).create();
        setCookie(EMPTY);
    }

    public boolean login() throws IOException {
        String basicAuthentication = "Basic " + B64Code.encode("CPE/" + username + ":" + password, "utf-8");
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", basicAuthentication);
        headers.put("Accept", "application/json,text/javascript, */*; q=0.01");

        CookieInfo cookieInfo;

        HttpResponse response = HttpUtils.post(verisureUrls.login(), headers, null);
        String responseBody = response.getBody();
        if (response.getStatus() == 200) {
            try {
                cookieInfo = gson.fromJson(responseBody, CookieInfo.class);
            } catch (JsonSyntaxException | IllegalArgumentException exception) {
                logger.debug("Failed to parse cookie response [{}]", responseBody);
                setCookie(EMPTY);
                throw new IOException("Failed to parse cookie response");
            }
            if (cookieInfo == null || cookieInfo.getCookie() == null) {
                setCookie(EMPTY);
                throw new IOException("Failed to parse cookie response");
            } else {
                setCookie(cookieInfo.getCookie());
            }
        } else {
            logger.debug("Failed to login. Response status was [{}]", response.getStatus());
            handleErrorResponse(response);
            throw new IOException("Failed to login. Response status was [" + response.getStatus() + "]");
        }

        return isLoggedIn();
    }

    public boolean isLoggedIn() {
        return !getCookie().isEmpty();
    }

    public boolean logout() throws IOException {

        Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", "vid=" + getCookie());
        headers.put("Accept", "application/json,text/javascript, */*; q=0.01");

        boolean success;
        try {
            HttpResponse response = HttpUtils.delete(verisureUrls.login(), headers);
            success = (response.getStatus() == 200);
        } finally {
            setCookie(EMPTY);
        }

        return success;
    }

    public ArmStatus getArmState(String giid) throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", "vid=" + getCookie());
        headers.put("Accept", "application/json,text/javascript, */*; q=0.01");

        ArmStatus result;

        HttpResponse response = HttpUtils.get(verisureUrls.armState(giid), headers);
        if (response.getStatus() == 200) {
            String responseBody = response.getBody();
            try {
                ArmState armState = gson.fromJson(responseBody, ArmState.class);
                result = armState.getStatusType();
            } catch (JsonSyntaxException | IllegalArgumentException exception) {
                logger.debug("Fail to parse arm state response [{}]", responseBody);
                throw new IOException("Failed to parse arm state for guid [" + giid + "]");
            }
            if (result == null) {
                throw new IOException("Failed to parse arm state for guid [" + giid + "]");
            }
        } else {
            logger.debug("Failed to retrieve arm state for giid [{}]. Response status was [{}]", giid, response.getStatus());
            handleErrorResponse(response);
            throw new IOException("Could not retrieve arm state for gid [" + giid + "]. Response status was [" + response.getStatus() + "]");
        }
        return result;
    }


    public ArmStatus setArmState(String giid, String pin, ArmStatus state) throws IOException {

        Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", "vid=" + getCookie());
        headers.put("Accept", "application/json,text/javascript, */*; q=0.01");

        ArmPayload armPayload = new ArmPayload(pin, state);

        String json = gson.toJson(Collections.singleton(armPayload));

        HttpResponse response = HttpUtils.post(verisureUrls.armStateCode(giid), headers, json);

        if (response.getStatus() != 200) {
            logger.debug("Failed to retrieve arm state for giid [{}]. Response status was [{}]", giid, response.getStatus());
            handleErrorResponse(response);
            throw new IOException("Could not retrieve arm state for gid [" + giid + "]. Response status was [" + response.getStatus() + "]");
        }
        return getArmState(giid);
    }

    public void setSmartPlug(String giid, String deviceLabel, boolean active) throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", "vid=" + getCookie());
        headers.put("Accept", "application/json");
        headers.put("Content-Type", "application/json; charset=UTF-8");

        SmartPlugPayload smartPlugPayload = new SmartPlugPayload(deviceLabel, active);

        String json = gson.toJson(Collections.singletonList(smartPlugPayload));

        logger.debug("Sending SmartplugCommand json={}", json);

        HttpResponse response = HttpUtils.post(verisureUrls.smartPlug(giid), headers, json);

        if (response.getStatus() != 200) {
            logger.debug("Failed to set Smart Plug [{}] on giid [{}] to state [{}]. Response status was [{}]. Response body was [{}]",
                         deviceLabel, giid, active,
                         response.getStatus(), response.getBody());
            handleErrorResponse(response);
            throw new IOException("Could not retrieve arm state for gid [" + giid + "]. Response status was [" + response.getStatus() + "]");
        }
        logger.debug("Successfully set Smart Plug [{}] on giid [{}] to state [{}]",
                     deviceLabel, giid, active);
    }

    public List<Installation> retrieveInstallations() throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", "vid=" + getCookie());
        headers.put("Accept", "application/json,text/javascript, */*; q=0.01");
        List<Installation> installations;

        HttpResponse response = HttpUtils.get(verisureUrls.installations(this.username), headers);
        if (response.getStatus() == 200) {
            String responseBody = response.getBody();
            try {

                installations = gson.fromJson(responseBody,
                                              new TypeToken<List<Installation>>() {
                                              }.getType());

            } catch (JsonSyntaxException exception) {
                logger.debug("Failed to parse installations [{}]", responseBody);
                throw new IOException("Response could not be parsed [" + responseBody + "]");
            }

            for (Installation installation : installations) {
                if (StringUtils.isBlank(installation.getGiid())) {
                    throw new IOException("Response was invalid. No GIIDs were found.");
                }
            }
        } else {
            logger.debug("Failed to retrieve installations. Response status was [{}]", response.getStatus());
            handleErrorResponse(response);
            throw new IOException("Could not retrieve installations. Response status was [" + response.getStatus() + "]");
        }


        return installations;
    }

    public InstallationOverview retrieveInstallationOverview(String giid) throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", "vid=" + getCookie());
        headers.put("Accept", "application/json,text/javascript, */*; q=0.01");
        InstallationOverview installationOverview;

        HttpResponse response = HttpUtils.get(verisureUrls.overview(giid), headers);
        if (response.getStatus() == 200) {
            String responseBody = response.getBody();
            try {
                installationOverview = gson.fromJson(responseBody, InstallationOverview.class);
            } catch (JsonSyntaxException exception) {
                logger.debug("Failed to parse installations [{}]", responseBody);
                throw new IOException("Response could not be parsed [" + responseBody + "]");
            }
        } else {
            logger.debug("Failed to retrieve installations. Response status was [{}]", response.getStatus());
            handleErrorResponse(response);
            throw new IOException("Could not retrieve installations. Response status was [" + response.getStatus() + "]");
        }

        return installationOverview;
    }

    private void handleErrorResponse(HttpResponse response) {
        int status = response.getStatus();
        if (status == 401 || status == 403) {
            try {
                logout();
            } catch (IOException e) {
                logger.error("Failed to logout on remote server", e);
            }
        }
    }

    private String getCookie() {
        String result;
        try {
            lock.readLock().lock();
            result = this.vid;
        } finally {
            lock.readLock().unlock();
        }

        return result;
    }

    private void setCookie(String vid) {
        try {
            lock.writeLock().lock();
            this.vid = vid;
        } finally {
            lock.writeLock().unlock();
        }
    }
}

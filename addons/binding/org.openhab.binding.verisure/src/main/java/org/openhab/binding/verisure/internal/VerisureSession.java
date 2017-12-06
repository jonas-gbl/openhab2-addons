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
import java.util.*;
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
import org.openhab.binding.verisure.LockStatus;
import org.openhab.binding.verisure.internal.http.HttpResponse;
import org.openhab.binding.verisure.internal.http.HttpUtils;
import org.openhab.binding.verisure.internal.json.*;
import org.openhab.binding.verisure.internal.json.Error;
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

        Map<String, String> headers = HeadersWithBasicAuthentication();

        HttpResponse response = HttpUtils.post(verisureUrls.login(), headers, EMPTY);
        String responseBody = response.getBody();
        int responseStatus = response.getStatus();

        String cookieData = EMPTY;
        try {
            if (responseStatus == 200) {
                CookieInfo cookieInfo = gson.fromJson(responseBody, CookieInfo.class);
                cookieData = cookieInfo.getCookie();
            }
        } finally {
            setCookie(StringUtils.isNotBlank(cookieData) ? cookieData : EMPTY);
        }

        if (StringUtils.isBlank(cookieData)) {
            throw new VerisureResponseException(response);
        } else {
            return isLoggedIn();
        }
    }

    public boolean isLoggedIn() {
        return !getCookie().isEmpty();
    }

    public boolean logout() throws IOException {

        Map<String, String> headers = HeadersWithCookieAuthentication();

        try {
            HttpResponse response = HttpUtils.delete(verisureUrls.login(), headers);
            int responseStatus = response.getStatus();
            String responseBody = response.getBody();
            logger.debug("Logged out. Response status [{}]. Response body was [{}]", responseStatus, responseBody);
            return (responseStatus == 200);
        } finally {
            setCookie(EMPTY);
        }
    }

    public void setArmState(String giid, String pin, ArmStatus state) throws IOException {

        Map<String, String> headers = HeadersWithCookieAuthentication();
        headers.put("Content-Type", "application/json; charset=UTF-8");

        ArmPayload armPayload = new ArmPayload(pin, state);
        String json = gson.toJson(armPayload);

        HttpResponse response = HttpUtils.put(verisureUrls.armStateCode(giid), headers, json);
        int responseStatus = response.getStatus();
        String responseBody = response.getBody();

        if (responseStatus == 200) {
            logger.debug("Set arm state for giid [{}] succeeded. Response status was [{}]. Actual response was [{}]",
                         giid, responseStatus, responseBody);
        } else if (handleErrorResponse(response)) {
            throw new VerisureResponseException(response);
        }
    }

    public void setDoorLock(String giid, String deviceLabel, String pin, LockStatus lockStatus) throws IOException {

        Map<String, String> headers = HeadersWithCookieAuthentication();
        headers.put("Content-Type", "application/json; charset=UTF-8");

        DoorLockPayload doorLockPayload = new DoorLockPayload(pin);
        String json = gson.toJson(doorLockPayload);

        HttpResponse response = HttpUtils.put(verisureUrls.doorLock(giid, deviceLabel, lockStatus), headers, json);
        int responseStatus = response.getStatus();

        if (responseStatus == 200) {
            logger.debug("Successfully submitted request to set device [{}] on installation [{}] for state [{}]",
                         deviceLabel, giid, lockStatus);
        } else if (handleErrorResponse(response)) {
            throw new VerisureResponseException(response);
        }
    }

    public void setSmartPlug(String giid, String deviceLabel, boolean active) throws IOException {
        Map<String, String> headers = HeadersWithCookieAuthentication();
        headers.put("Content-Type", "application/json; charset=UTF-8");

        SmartPlugPayload smartPlugPayload = new SmartPlugPayload(deviceLabel, active);
        String json = gson.toJson(Collections.singletonList(smartPlugPayload));

        HttpResponse response = HttpUtils.post(verisureUrls.smartPlug(giid), headers, json);
        int responseStatus = response.getStatus();

        if (responseStatus == 200) {
            logger.debug("Successfully set Smart Plug [{}] on giid [{}] to state [{}]", deviceLabel, giid, active);
        } else if (handleErrorResponse(response)) {
            throw new VerisureResponseException(response);
        }
    }

    public List<Installation> retrieveInstallations() throws IOException {
        Map<String, String> headers = HeadersWithCookieAuthentication();

        List<Installation> installations = new ArrayList<>();

        HttpResponse response = HttpUtils.get(verisureUrls.installations(this.username), headers);
        int responseStatus = response.getStatus();
        String responseBody = response.getBody();

        boolean success = false;
        if (responseStatus == 200) {
            try {
                installations = gson.fromJson(responseBody, new TypeToken<List<Installation>>() {}.getType());
                success = installations.stream().noneMatch(installation -> StringUtils.isBlank(installation.getGiid()));
            } catch (JsonSyntaxException ignored) { }
        } else {
            handleErrorResponse(response);
        }

        if (success) {
            return installations;
        } else {
            throw new VerisureResponseException(response);
        }
    }

    public InstallationOverview retrieveInstallationOverview(String giid) throws IOException {
        Map<String, String> headers = HeadersWithCookieAuthentication();

        InstallationOverview installationOverview = null;

        HttpResponse response = HttpUtils.get(verisureUrls.overview(giid), headers);
        int responseStatus = response.getStatus();
        String responseBody = response.getBody();

        boolean success = false;
        if (responseStatus == 200) {
            try {
                installationOverview = gson.fromJson(responseBody, InstallationOverview.class);
                success = (installationOverview != null);
            } catch (JsonSyntaxException ignored) { }
        } else {
            handleErrorResponse(response);
        }

        if (success) {
            return installationOverview;
        } else {
            throw new VerisureResponseException(response);
        }
    }

    private boolean handleErrorResponse(HttpResponse response) {
        boolean isError = true;
        int responseStatus = response.getStatus();
        String responseBody = response.getBody();

        String errorCode;
        try {
            Error error = gson.fromJson(responseBody, Error.class);
            errorCode = error != null ? error.getErrorCode() : "Unknown";
        } catch (JsonSyntaxException e) {
            errorCode = responseBody;
        }

        if (responseStatus == 401 || responseStatus == 403) {
            try {
                logout();
            } catch (IOException e) {
                logger.error("Failed to logout on remote server", e);
            }
        } else if (responseStatus == 400) {

            if (errorCode.equalsIgnoreCase("VAL_00818") || errorCode.equalsIgnoreCase("VAL_00819")) {
                isError = false;
                logger.debug("Requested state is the same");
            }
        } else {
            logger.debug("Failed to request state update. Response status was [{}]. Actual response was [{}]", responseStatus, responseBody);
        }

        return isError;
    }

    private Map<String, String> HeadersWithBasicAuthentication() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", "Basic " + B64Code.encode("CPE/" + username + ":" + password, "utf-8"));
        headers.put("Accept", "application/json,text/javascript, */*; q=0.01");
        return headers;
    }

    private Map<String, String> HeadersWithCookieAuthentication() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", "vid=" + getCookie());
        headers.put("Accept", "application/json,text/javascript, */*; q=0.01");
        return headers;
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

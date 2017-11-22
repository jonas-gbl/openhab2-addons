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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.eclipse.jetty.util.B64Code;
import org.openhab.binding.verisure.internal.http.HttpResponse;
import org.openhab.binding.verisure.internal.http.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link VerisureSession} is used to connect to the Verisure services.
 *
 * @author Jonas Gabriel - Initial contribution
 */
public class VerisureSession {

    private static final String EMPTY = "";
    private final Logger logger = LoggerFactory.getLogger(VerisureSession.class);
    private final String username;
    private final String password;
    private final VerisureUrls verisureUrls;
    private String vid;

    public VerisureSession(VerisureUrls verisureUrls, String username, String password) {
        this.verisureUrls = verisureUrls;
        this.username = username;
        this.password = password;
        this.vid = EMPTY;
    }

    public boolean login() throws IOException {
        String basicAuthentication = "Basic " + B64Code.encode("CPE/" + username + ":" + password, "utf-8");
        Map<String, String> headers = new HashMap<>();
        headers.put("Authorization", basicAuthentication);
        headers.put("Accept", "application/json,text/javascript, */*; q=0.01");


        HttpResponse response = HttpUtils.post(verisureUrls.login(), headers, null);
        if (response.getStatus() == 200) {
            JsonParser parser = new JsonParser();
            JsonObject jsonObject = parser.parse(response.getBody()).getAsJsonObject();
            vid = jsonObject.get("cookie").getAsString();
        }

        return isLoggedIn();
    }

    public boolean isLoggedIn() {
        return !vid.isEmpty();
    }

    public boolean logout() throws IOException {

        Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", "vid=" + this.vid);
        headers.put("Accept", "application/json,text/javascript, */*; q=0.01");

        boolean success;
        try {
            HttpResponse response = HttpUtils.delete(verisureUrls.login(), headers);
            success = (response.getStatus() == 200);
        } finally {
            this.vid = EMPTY;
        }

        return success;
    }

    public ArmState getArmState(String giid) throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", "vid=" + this.vid);
        headers.put("Accept", "application/json,text/javascript, */*; q=0.01");

        ArmState result;

        HttpResponse response = HttpUtils.get(verisureUrls.armState(giid), headers);
        if (response.getStatus() == 200) {
            String responseBody = response.getBody();

            JsonParser parser = new JsonParser();

            JsonObject jsonObject = parser.parse(responseBody).getAsJsonObject();

            JsonElement statusType = jsonObject.get("statusType");
            result = ArmState.retrieveById(statusType.getAsString());
        } else {
            handleErrorResponse(response);
            throw new IOException("Could not retrieve arm state for guid [" + giid + "]");
        }

        return result;
    }

    private List<String> populateInstallations() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", "vid=" + this.vid);
        headers.put("Accept", "application/json,text/javascript, */*; q=0.01");
        List<String> installations = new ArrayList<>();
        try {
            HttpResponse response = HttpUtils.get(verisureUrls.installations(this.username), headers);
            if (response.getStatus() == 200) {
                JsonParser parser = new JsonParser();
                String responseBody = response.getBody();
                JsonArray jArray = parser.parse(responseBody).getAsJsonArray();

                for (JsonElement obj : jArray) {
                    JsonElement giid = obj.getAsJsonObject().get("giid");
                    installations.add(giid.getAsString());
                }
            }
        } catch (IOException e) {
            logger.error("Failed to populate installations", e);
        }

        return installations;
    }

    private void handleErrorResponse(HttpResponse response) {
        int status = response.getStatus();
        if (status == 401 || status == 403) {
            try {
                logout();
            } catch (IOException e) {
                logger.error("Failed to logout", e);
            }
        }
    }

    public enum ArmState {
        ARMED_HOME("ARMED_HOME", "Armed (Home)"), ARMED_AWAY("ARMED_AWAY", "Armed (Away)"), DISARMED("DISARMED", "Disarmed");

        public final String id;
        public final String text;

        public static ArmState retrieveById(String value) {
            for (ArmState candidate : ArmState.values()) {
                if (candidate.id.equalsIgnoreCase(value)) {
                    return candidate;
                }
            }
            throw new IllegalArgumentException(value);
        }

        ArmState(String id, String text) {
            this.id = id;
            this.text = text;
        }
    }
}

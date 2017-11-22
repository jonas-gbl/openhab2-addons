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
    private List<String> installations;
    private String vid;

    public VerisureSession(String username, String password) {
        this.username = username;
        this.password = password;
        this.vid = EMPTY;
        this.installations = new ArrayList<>();


    }

    public boolean login() throws IOException {

        if (!isLoggedIn()) {
            String basicAuthentication = "Basic " + B64Code.encode("CPE/" + username + ":" + password, "utf-8");
            Map<String, String> headers = new HashMap<>();
            headers.put("Authorization", basicAuthentication);
            headers.put("Accept", "application/json");


            HttpResponse response = HttpUtils.post(VerisureUrls.login(), headers, null);
            if (response.getStatus() == 200) {
                JsonParser parser = new JsonParser();
                JsonObject jsonObject = parser.parse(response.getBody()).getAsJsonObject();
                vid = jsonObject.get("cookie").getAsString();
                populateInstallations();
            }
        }
        return isLoggedIn();
    }

    public boolean isLoggedIn() {
        return !vid.isEmpty();
    }

    public void logout() throws IOException {
        if (isLoggedIn()) {
            Map<String, String> headers = new HashMap<>();
            headers.put("Cookie", "vid=" + this.vid);
            headers.put("Accept", "application/json,text/javascript, */*; q=0.01");

            HttpResponse response = HttpUtils.delete(VerisureUrls.login(), headers);
            if (response.getStatus() == 200) {
                this.vid = EMPTY;
            }
        }
    }

    public ArmState getArmState(String giid) throws IOException {
        Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", "vid=" + this.vid);
        headers.put("Accept", "application/json,text/javascript, */*; q=0.01");

        ArmState result;

        HttpResponse response = HttpUtils.get(VerisureUrls.armState(giid), headers);
        if (response.getStatus() == 200) {
            String responseBody = response.getBody();

            JsonParser parser = new JsonParser();

            JsonObject jsonObject = parser.parse(responseBody).getAsJsonObject();

            JsonElement statusType = jsonObject.get("statusType");
            result = ArmState.retrieveById(statusType.getAsString());
        } else {
            throw new IOException("Could not retrieve arm state for guid [" + giid + "]");
        }

        return result;
    }

    private void populateInstallations() {
        Map<String, String> headers = new HashMap<>();
        headers.put("Cookie", "vid=" + this.vid);
        headers.put("Accept", "application/json,text/javascript, */*; q=0.01");
        try {
            HttpResponse response = HttpUtils.get(VerisureUrls.installations(this.username), headers);
            if (response.getStatus() == 200) {
                JsonParser parser = new JsonParser();
                String responseBody = response.getBody();
                JsonArray jArray = parser.parse(responseBody).getAsJsonArray();

                installations.clear();
                for (JsonElement obj : jArray) {
                    JsonElement giid = obj.getAsJsonObject().get("giid");
                    installations.add(giid.getAsString());
                }
            }
        } catch (IOException e) {
            logger.error("Failed to populate installations", e);
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

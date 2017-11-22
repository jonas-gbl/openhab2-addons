/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.verisure.internal;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * The {@link VerisureUrls} is responsible for handling commands, which are
 * sent to one of the channels.
 *
 * @author Jonas Gabriel - Initial contribution
 */
public class VerisureUrls {
    private final String baseUrl;

    public VerisureUrls(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public URL login() {
        try {
            return new URL(baseUrl + "/cookie");
        } catch (MalformedURLException e) {
            throw new IllegalStateException("login URL is misconfigured");
        }
    }

    public URL installations(String username) {
        try {
            return new URL(baseUrl + "/installation/search?email=" + username);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("installations URL is misconfigured");
        }

    }

    public URL installation(String guid) {
        try {
            return new URL(baseUrl + "/installation/" + guid + "/");
        } catch (MalformedURLException e) {
            throw new IllegalStateException("installation URL is misconfigured");
        }
    }

    public URL armStateCode(String guid) {
        try {
            return new URL(baseUrl + "/installation/" + guid + "/" + "armstate/code");
        } catch (MalformedURLException e) {
            throw new IllegalStateException("armStateCode URL is misconfigured");
        }
    }

    public URL armState(String guid) {
        try {
            return new URL(baseUrl + "/installation/" + guid + "/" + "armstate");
        } catch (MalformedURLException e) {
            throw new IllegalStateException("armState URL is misconfigured");
        }
    }
}

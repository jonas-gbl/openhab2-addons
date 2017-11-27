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

import com.google.common.base.Preconditions;

/**
 * The {@link VerisureUrls} is a small utility class for generating URLs to the Verisure backend.
 * It is a port from https://github.com/persandstrom/python-verisure.
 *
 * @author Jonas Gabriel - Initial contribution
 */
public class VerisureUrls {
    private final String protocol;
    private final String host;
    private final int port;
    private final String basePath;


    public static VerisureUrls withBaseUrl(String baseUrl) {
        Builder builder = new Builder();
        builder.withBaseUrl(baseUrl);
        return builder.build();
    }

    public URL login() {
        try {
            String path = basePath + "/cookie";
            return new URL(protocol, host, port, path);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("login URL is misconfigured");
        }
    }

    public URL installations(String username) {
        try {
            String path = basePath + "/installation/search?email=" + username;
            return new URL(protocol, host, port, path);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("installations URL is misconfigured");
        }

    }

    public URL installation(String guid) {
        try {
            String path = basePath + "/installation/" + guid + "/";
            return new URL(protocol, host, port, path);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("installation URL is misconfigured");
        }
    }

    public URL armStateCode(String guid) {
        try {
            String path = basePath + "/installation/" + guid + "/" + "armstate/code";
            return new URL(protocol, host, port, path);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("armStateCode URL is misconfigured");
        }
    }

    public URL armState(String giid) {
        try {
            String path = basePath + "/installation/" + giid + "/" + "armstate";
            return new URL(protocol, host, port, path);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("armState URL is misconfigured");
        }
    }

    public URL smartPlug(String giid) {
        try {
            String path = basePath + "/installation/" + giid + "/" + "smartplug/state";
            return new URL(protocol, host, port, path);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("armState URL is misconfigured");
        }
    }

    public URL overview(String giid) {
        try {
            String path = basePath + "/installation/" + giid + "/" + "overview";
            return new URL(protocol, host, port, path);
        } catch (MalformedURLException e) {
            throw new IllegalStateException("armState URL is misconfigured");
        }
    }

    private VerisureUrls(Builder builder) {
        protocol = builder.protocol;
        host = builder.host;
        port = builder.port;
        basePath = builder.basePath;
    }

    public static final class Builder {
        private String protocol;
        private String host;
        private int port;
        private String basePath;

        public Builder withBaseUrl(String baseUrl) {
            try {
                URL endpoint = new URL(baseUrl);
                Preconditions.checkArgument(endpoint.getQuery() == null, "Base Url should not contain query parameters");
                Preconditions.checkArgument(endpoint.getRef() == null, "Base Url should not contain refs");
                protocol = endpoint.getProtocol();
                port = endpoint.getPort();

                host = endpoint.getHost();
                basePath = endpoint.getPath();
            } catch (MalformedURLException e) {
                throw new IllegalArgumentException("Base Url [" + baseUrl + "] is invalid");
            }
            return this;
        }

        public VerisureUrls build() {

            if (basePath.length() > 0 && basePath.charAt(basePath.length() - 1) == '/') {
                basePath = basePath.substring(0, basePath.length() - 1);
            }
            return new VerisureUrls(this);
        }

        private Builder() {
        }
    }
}

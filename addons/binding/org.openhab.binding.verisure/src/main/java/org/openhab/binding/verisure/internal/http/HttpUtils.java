/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.verisure.internal.http;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

/**
 * The {@link HttpUtils} is responsible for creating things and thing
 * handlers.
 *
 * @author Jonas Gabriel - Initial contribution
 */
public class HttpUtils {
    public static HttpResponse post(URL url, Map<String, String> headers, String data) throws IOException {

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");

        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }

        if (data != null && !data.isEmpty()) {
            connection.setDoOutput(true);
        }


        if (data != null && !data.isEmpty()) {
            OutputStream out = null;
            try {
                out = connection.getOutputStream();
                out.write(data.getBytes("UTF-8"));
                out.flush();
            } finally {
                if (out != null) {
                    out.close();
                }
            }
        }

        connection.disconnect();
        return retrieveResponse(connection);
    }

    public static HttpResponse get(URL url, Map<String, String> headers) throws IOException {

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }


        connection.connect();
        return retrieveResponse(connection);
    }

    public static HttpResponse delete(URL url, Map<String, String> headers) throws IOException {

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("DELETE");

        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                connection.addRequestProperty(entry.getKey(), entry.getValue());
            }
        }

        return retrieveResponse(connection);
    }

    private static HttpResponse retrieveResponse(HttpURLConnection connection) throws IOException {
        String result;
        int statusCode = connection.getResponseCode();
        if (statusCode >= HttpURLConnection.HTTP_BAD_REQUEST) {
            InputStream error = connection.getErrorStream();
            result = getInputStreamAsString(error);
        } else {
            InputStream in = connection.getInputStream();
            result = getInputStreamAsString(in);
        }
        return new HttpResponse(statusCode, result);
    }

    private static String getInputStreamAsString(InputStream in) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        try {

            in = new BufferedInputStream(in);

            byte[] buff = new byte[1024];
            int n;
            while ((n = in.read(buff)) > 0) {
                bos.write(buff, 0, n);
            }
        } finally {
            bos.flush();
            bos.close();

            if (in != null) {
                in.close();
            }
        }
        return bos.toString();
    }
}

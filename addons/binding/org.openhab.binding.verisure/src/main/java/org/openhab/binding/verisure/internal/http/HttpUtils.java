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

import com.google.common.base.Preconditions;
import org.apache.commons.lang.StringUtils;

/**
 * The {@link HttpUtils} provides basic helper methods to perform HTTP actions. On error a  {@link IOException} is thrown.
 *
 * @author Jonas Gabriel - Initial contribution
 */
public class HttpUtils {

    public static HttpResponse put(URL url, Map<String, String> headers, String data) throws IOException {

        Preconditions.checkArgument(url != null);
        Preconditions.checkArgument(StringUtils.isEmpty(data) || contentTypeHeaderExists(headers));

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("PUT");

        writeHeaders(headers, connection);
        writeRequestData(data, connection);

        connection.disconnect();
        return retrieveResponse(connection);
    }

    public static HttpResponse post(URL url, Map<String, String> headers, String data) throws IOException {

        Preconditions.checkArgument(url != null);
        Preconditions.checkArgument(StringUtils.isEmpty(data) || contentTypeHeaderExists(headers));

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");

        writeHeaders(headers, connection);
        writeRequestData(data, connection);

        connection.disconnect();
        return retrieveResponse(connection);
    }

    public static HttpResponse get(URL url, Map<String, String> headers) throws IOException {

        Preconditions.checkArgument(url != null);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        writeHeaders(headers, connection);


        connection.connect();
        return retrieveResponse(connection);
    }

    public static HttpResponse delete(URL url, Map<String, String> headers) throws IOException {
        Preconditions.checkArgument(url != null);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("DELETE");
        writeHeaders(headers, connection);
        return retrieveResponse(connection);
    }

    private static void writeHeaders(Map<String, String> headers, HttpURLConnection connection) {

        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                connection.setRequestProperty(entry.getKey(), entry.getValue());
            }
        }
    }

    private static void writeRequestData(String data, HttpURLConnection connection) throws IOException {

        if (StringUtils.isNotBlank(data)) {
            connection.setDoOutput(true);
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
            if (in != null) {
                in = new BufferedInputStream(in);

                byte[] buff = new byte[1024];
                int n;
                while ((n = in.read(buff)) > 0) {
                    bos.write(buff, 0, n);
                }
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

    private static boolean contentTypeHeaderExists(Map<String, String> headers) {

        return (headers != null)
                && (headers.entrySet().stream()
                .anyMatch(entry ->
                                  entry.getKey().equalsIgnoreCase("Content-Type")
                                          && StringUtils.isNotBlank(entry.getValue())));
    }

}

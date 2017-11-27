/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.verisure.internal.http;

import java.util.Objects;

/**
 * The {@link HttpResponse} is a utility class that encapsulates a response code and body.
 *
 * @author Jonas Gabriel - Initial contribution
 */
public class HttpResponse {
    private final int status;
    private final String body;

    HttpResponse(int status, String body) {
        this.status = status;
        this.body = body;
    }

    public int getStatus() {
        return status;
    }

    public String getBody() {
        return body;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof HttpResponse)) {
            return false;
        }
        HttpResponse that = (HttpResponse) o;
        return status == that.status &&
                Objects.equals(body, that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(body, status);
    }
}

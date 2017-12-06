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

import org.openhab.binding.verisure.internal.http.HttpResponse;

/**
 * The {@link VerisureResponseException} is a  {@link IOException} marking an Error response from the Verisure backend.
 *
 * @author Jonas Gabriel - Initial contribution
 */
public class VerisureResponseException extends IOException {
    private HttpResponse httpResponse;

    public VerisureResponseException(HttpResponse httpResponse) {
        this.httpResponse = httpResponse;
    }

    @Override
    public String getMessage() {
        return "Response status was " + httpResponse.getStatus() + ". Actual response was: " + httpResponse.getBody();
    }
}

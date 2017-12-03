/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.verisure.internal.json;

import java.util.Objects;

/**
 * @author Jonas Gabriel - Initial contribution
 */
public class Error {
    private String errorGroup;
    private String errorCode;
    private String errorMessage;

    public String getErrorGroup() {
        return errorGroup;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("errorGroup", errorGroup)
                .add("errorCode", errorCode)
                .add("errorMessage", errorMessage)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Error)) {
            return false;
        }
        Error error = (Error) o;
        return Objects.equals(errorGroup, error.errorGroup) &&
                Objects.equals(errorCode, error.errorCode) &&
                Objects.equals(errorMessage, error.errorMessage);
    }

    @Override
    public int hashCode() {
        return Objects.hash(errorGroup, errorCode, errorMessage);
    }
}

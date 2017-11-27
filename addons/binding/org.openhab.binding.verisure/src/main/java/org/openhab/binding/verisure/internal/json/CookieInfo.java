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
public class CookieInfo {
    private String cookie;

    public CookieInfo() {
        this.cookie = "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CookieInfo)) {
            return false;
        }
        CookieInfo cookieInfo = (CookieInfo) o;
        return Objects.equals(cookie, cookieInfo.cookie);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cookie);
    }

    public String getCookie() {

        return cookie;
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("cookie", cookie)
                .toString();
    }
}

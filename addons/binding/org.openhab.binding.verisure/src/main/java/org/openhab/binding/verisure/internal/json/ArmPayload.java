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

import org.openhab.binding.verisure.ArmStatus;

/**
 * The {@link ArmPayload} is used to update the state of the alarm
 *
 * @author Jonas Gabriel - Initial contribution
 */
public class ArmPayload {
    private String code;
    private ArmStatus state;

    public ArmPayload(String code, ArmStatus state) {
        this.code = code;
        this.state = state;
    }

    public String getCode() {
        return code;
    }

    public ArmStatus getState() {
        return state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ArmPayload)) {
            return false;
        }
        ArmPayload that = (ArmPayload) o;
        return Objects.equals(code, that.code) &&
                state == that.state;
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, state);
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("code", code)
                .add("state", state)
                .toString();
    }
}

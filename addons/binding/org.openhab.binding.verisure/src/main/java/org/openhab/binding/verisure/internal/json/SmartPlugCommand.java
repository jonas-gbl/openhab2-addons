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
 * The {@link SmartPlugCommand} is used to update the status of a Smart Plug
 *
 * @author Jonas Gabriel - Initial contribution
 */
public class SmartPlugCommand {
    private String deviceLabel;
    private boolean state;

    public SmartPlugCommand(String deviceLabel, boolean state) {
        this.deviceLabel = deviceLabel;
        this.state = state;
    }

    public String getDeviceLabel() {
        return deviceLabel;
    }

    public boolean isState() {
        return state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SmartPlugCommand)) {
            return false;
        }
        SmartPlugCommand that = (SmartPlugCommand) o;
        return state == that.state &&
                Objects.equals(deviceLabel, that.deviceLabel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deviceLabel, state);
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("deviceLabel", deviceLabel)
                .add("state", state)
                .toString();
    }
}

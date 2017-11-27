/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.verisure.internal.json;

import java.util.List;
import java.util.Objects;

/**
 * @author Jonas Gabriel - Initial contribution
 */
public class DoorWindow {
    private List<DoorWindowDevice> doorWindowDevice;
    private boolean reportState;

    public DoorWindow() {
    }

    public List<DoorWindowDevice> getDoorWindowDevice() {
        return doorWindowDevice;
    }

    public boolean isReportState() {
        return reportState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DoorWindow)) {
            return false;
        }
        DoorWindow that = (DoorWindow) o;
        return reportState == that.reportState &&
                Objects.equals(doorWindowDevice, that.doorWindowDevice);
    }

    @Override
    public int hashCode() {
        return Objects.hash(doorWindowDevice, reportState);
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("doorWindowDevice", doorWindowDevice)
                .add("reportState", reportState)
                .toString();
    }
}

/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.verisure.internal.json;

import java.time.OffsetDateTime;
import java.util.Objects;

/**
 * @author Jonas Gabriel - Initial contribution
 */
public class DoorWindowDevice {

    private OffsetDateTime reportTime;
    private String state;
    private String deviceLabel;
    private boolean wired;
    private String area;

    public DoorWindowDevice() {
    }

    public OffsetDateTime getReportTime() {

        return reportTime;
    }

    public String getState() {
        return state;
    }

    public String getDeviceLabel() {
        return deviceLabel;
    }

    public boolean isWired() {
        return wired;
    }

    public String getArea() {
        return area;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DoorWindowDevice)) {
            return false;
        }
        DoorWindowDevice that = (DoorWindowDevice) o;
        return wired == that.wired &&
                Objects.equals(reportTime, that.reportTime) &&
                Objects.equals(state, that.state) &&
                Objects.equals(deviceLabel, that.deviceLabel) &&
                Objects.equals(area, that.area);
    }

    @Override
    public int hashCode() {
        return Objects.hash(reportTime, state, deviceLabel, wired, area);
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("reportTime", reportTime)
                .add("state", state)
                .add("deviceLabel", deviceLabel)
                .add("wired", wired)
                .add("area", area)
                .toString();
    }
}

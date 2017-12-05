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

import org.openhab.binding.verisure.LockStatus;

/**
 * @author Jonas Gabriel - Initial contribution
 */
public class DoorLock {

    private OffsetDateTime eventTime;
    private String area;
    private String deviceLabel;
    private LockStatus currentLockState;
    private boolean motorJam;
    private boolean secureModeActive;
    private boolean paired;
    private LockStatus lockedState;
    private String pendingLockState;
    private String method;

    public OffsetDateTime getEventTime() {
        return eventTime;
    }

    public String getArea() {
        return area;
    }

    public String getDeviceLabel() {
        return deviceLabel;
    }

    public LockStatus getCurrentLockState() {
        return currentLockState;
    }

    public boolean isMotorJam() {
        return motorJam;
    }

    public boolean isSecureModeActive() {
        return secureModeActive;
    }

    public boolean isPaired() {
        return paired;
    }

    public LockStatus getLockedState() {
        return lockedState;
    }

    public String getPendingLockState() {
        return pendingLockState;
    }

    public String getMethod() {
        return method;
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("eventTime", eventTime)
                .add("area", area)
                .add("deviceLabel", deviceLabel)
                .add("currentLockState", currentLockState)
                .add("motorJam", motorJam)
                .add("secureModeActive", secureModeActive)
                .add("paired", paired)
                .add("lockedState", lockedState)
                .add("pendingLockState", pendingLockState)
                .add("method", method)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DoorLock)) {
            return false;
        }
        DoorLock that = (DoorLock) o;
        return motorJam == that.motorJam &&
                secureModeActive == that.secureModeActive &&
                paired == that.paired &&
                Objects.equals(eventTime, that.eventTime) &&
                Objects.equals(area, that.area) &&
                Objects.equals(deviceLabel, that.deviceLabel) &&
                currentLockState == that.currentLockState &&
                lockedState == that.lockedState &&
                Objects.equals(pendingLockState, that.pendingLockState) &&
                Objects.equals(method, that.method);
    }

    @Override
    public int hashCode() {
        return Objects
                .hash(eventTime, area, deviceLabel, currentLockState, motorJam, secureModeActive, paired, lockedState, pendingLockState, method);
    }
}

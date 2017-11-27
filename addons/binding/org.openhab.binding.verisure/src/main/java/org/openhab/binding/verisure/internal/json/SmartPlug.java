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

import com.google.gson.annotations.SerializedName;
import org.eclipse.smarthome.core.library.types.OnOffType;

/**
 * @author Jonas Gabriel - Initial contribution
 */
public class SmartPlug {

    @SerializedName("isHazardous")
    private boolean hazardous;
    private String area;
    private String deviceLabel;
    private OnOffType currentState;
    private String icon;

    public boolean isHazardous() {
        return hazardous;
    }

    public String getArea() {
        return area;
    }

    public String getDeviceLabel() {
        return deviceLabel;
    }

    public OnOffType getCurrentState() {
        return currentState;
    }

    public String getIcon() {
        return icon;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SmartPlug)) {
            return false;
        }
        SmartPlug smartPlug = (SmartPlug) o;
        return hazardous == smartPlug.hazardous &&
                Objects.equals(area, smartPlug.area) &&
                Objects.equals(deviceLabel, smartPlug.deviceLabel) &&
                currentState == smartPlug.currentState &&
                Objects.equals(icon, smartPlug.icon);
    }

    @Override
    public int hashCode() {
        return Objects.hash(hazardous, area, deviceLabel, currentState, icon);
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("hazardous", hazardous)
                .add("area", area)
                .add("deviceLabel", deviceLabel)
                .add("currentState", currentState)
                .add("icon", icon)
                .toString();
    }
}

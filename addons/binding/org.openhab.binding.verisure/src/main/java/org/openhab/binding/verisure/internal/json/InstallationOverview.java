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
 * The {@link InstallationOverview} is used to connect to the Verisure services.
 *
 * @author Jonas Gabriel - Initial contribution
 */
public class InstallationOverview {

    private DoorWindow doorWindow;
    private List<ClimateValue> climateValues;
    private List<SmartPlug> smartPlugs;
    private ArmState armState;

    public InstallationOverview() {

    }

    public List<SmartPlug> getSmartPlugs() {
        return smartPlugs;
    }

    public DoorWindow getDoorWindow() {

        return doorWindow;
    }

    public List<ClimateValue> getClimateValues() {
        return climateValues;
    }

    public ArmState getArmState() {
        return armState;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof InstallationOverview)) {
            return false;
        }
        InstallationOverview that = (InstallationOverview) o;
        return Objects.equals(doorWindow, that.doorWindow) &&
                Objects.equals(climateValues, that.climateValues) &&
                Objects.equals(smartPlugs, that.smartPlugs) &&
                Objects.equals(armState, that.armState);
    }

    @Override
    public int hashCode() {
        return Objects.hash(doorWindow, climateValues, smartPlugs, armState);
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("doorWindow", doorWindow)
                .add("climateValues", climateValues)
                .add("smartPlugs", smartPlugs)
                .add("armState", armState)
                .toString();
    }
}

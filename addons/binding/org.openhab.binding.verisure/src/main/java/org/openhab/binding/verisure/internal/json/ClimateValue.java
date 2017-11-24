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
 * The {@link ClimateValue} is used to connect to the Verisure services.
 *
 * @author Jonas Gabriel - Initial contribution
 */
public class ClimateValue {

    private float temperature;
    private String deviceLabel;
    private float humidity;
    private String deviceType;
    private OffsetDateTime time;
    private String deviceAreal;

    public ClimateValue() {
    }

    public float getTemperature() {
        return temperature;
    }

    public String getDeviceLabel() {
        return deviceLabel;
    }

    public float getHumidity() {
        return humidity;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public OffsetDateTime getTime() {
        return time;
    }

    public String getDeviceAreal() {
        return deviceAreal;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ClimateValue)) {
            return false;
        }
        ClimateValue that = (ClimateValue) o;
        return Float.compare(that.temperature, temperature) == 0 &&
                Float.compare(that.humidity, humidity) == 0 &&
                Objects.equals(deviceLabel, that.deviceLabel) &&
                Objects.equals(deviceType, that.deviceType) &&
                Objects.equals(time, that.time) &&
                Objects.equals(deviceAreal, that.deviceAreal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(temperature, deviceLabel, humidity, deviceType, time, deviceAreal);
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("temperature", temperature)
                .add("deviceLabel", deviceLabel)
                .add("humidity", humidity)
                .add("deviceType", deviceType)
                .add("time", time)
                .add("deviceAreal", deviceAreal)
                .toString();
    }
}

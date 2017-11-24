/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.verisure.internal;

/**
 * The {@link ArmStatus} is used to connect to the Verisure services.
 *
 * @author Jonas Gabriel - Initial contribution
 */
public enum ArmStatus {
    ARMED_HOME("ARMED_HOME", "Armed (Home)"), ARMED_AWAY("ARMED_AWAY", "Armed (Away)"), DISARMED("DISARMED", "Disarmed");

    public final String id;
    public final String text;

    public static ArmStatus retrieveById(String value) {
        for (ArmStatus candidate : ArmStatus.values()) {
            if (candidate.id.equalsIgnoreCase(value)) {
                return candidate;
            }
        }
        throw new IllegalArgumentException(value);
    }

    ArmStatus(String id, String text) {
        this.id = id;
        this.text = text;
    }
}

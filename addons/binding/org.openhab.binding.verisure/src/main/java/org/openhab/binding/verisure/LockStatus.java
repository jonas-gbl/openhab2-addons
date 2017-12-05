/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.verisure;

/**
 * @author Jonas Gabriel - Initial contribution
 */
public enum LockStatus {

    LOCKED("LOCKED", "Locked"), UNLOCKED("UNLOCKED", "Unlocked");

    public final String id;
    public final String text;

    public static LockStatus retrieveById(String value) {
        for (LockStatus candidate : LockStatus.values()) {
            if (candidate.id.equalsIgnoreCase(value)) {
                return candidate;
            }
        }
        throw new IllegalArgumentException(value);
    }

    LockStatus(String id, String text) {
        this.id = id;
        this.text = text;
    }
}

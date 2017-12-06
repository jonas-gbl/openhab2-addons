/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.verisure;

import com.google.common.base.Objects;
import org.eclipse.smarthome.core.types.Command;

/**
 * {@link ArmCommand} can be used to set the arm state
 *
 * @author Jonas Gabriel - Initial contribution
 */
public class ArmCommand implements Command {

    private String pin;
    private ArmStatus status;

    public ArmCommand(String pin, ArmStatus status) {
        this(null, null, pin, status);
    }


    public ArmCommand(String username, String password, String pin, String status) {
        this(username, password, pin, ArmStatus.retrieveById(status));
    }

    public ArmCommand(String username, String password, String pin, ArmStatus status) {
        this.pin = pin;
        this.status = status;
    }

    public String getPin() {
        return pin;
    }

    public ArmStatus getStatus() {
        return status;
    }

    @Override
    public String format(String patten) {
        return String.format(patten, status);
    }

    @Override
    public String toFullString() {
        return toString();
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("status", status)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ArmCommand)) {
            return false;
        }
        ArmCommand that = (ArmCommand) o;
        return java.util.Objects.equals(pin, that.pin)
                && status == that.status;
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(pin, status);
    }
}

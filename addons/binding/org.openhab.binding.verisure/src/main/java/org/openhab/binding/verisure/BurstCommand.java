/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.verisure;

import java.util.Objects;

import org.eclipse.smarthome.core.types.Command;
import org.eclipse.smarthome.core.types.PrimitiveType;
import org.eclipse.smarthome.core.types.State;


/**
 * {@link BurstCommand} can be used to set the alarm on burst mode, checking frequently the
 * Verisure backend
 *
 * @author Jonas Gabriel - Initial contribution
 */
public class BurstCommand implements PrimitiveType, State, Command {


    private long count;
    private long millisecondInterval;

    public BurstCommand(long count, long millisecondInterval) {
        this.count = count;
        this.millisecondInterval = millisecondInterval;
    }

    public long getCount() {
        return count;
    }

    public long getIntervalInMilliseconds() {
        return millisecondInterval;
    }

    @Override
    public String format(String patten) {
        return String.format(patten, count, millisecondInterval);
    }

    @Override
    public String toFullString() {
        return toString();
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("count", count)
                .add("millisecondInterval", millisecondInterval)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof BurstCommand)) {
            return false;
        }
        BurstCommand that = (BurstCommand) o;
        return count == that.count &&
                millisecondInterval == that.millisecondInterval;
    }

    @Override
    public int hashCode() {
        return Objects.hash(count, millisecondInterval);
    }
}

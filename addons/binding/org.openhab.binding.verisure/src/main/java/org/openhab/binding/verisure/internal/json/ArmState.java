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
public class ArmState {

    private OffsetDateTime date;
    private ArmStatus statusType;
    private String name;
    private String cid;
    private String changedVia;
    private Boolean state;

    public ArmState() {
    }

    public String getCid() {
        return cid;
    }

    public OffsetDateTime getDate() {
        return date;
    }

    public Boolean getState() {
        return state;
    }

    public ArmStatus getStatusType() {
        return statusType;
    }

    public String getName() {
        return name;
    }

    public String getChangedVia() {
        return changedVia;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ArmState)) {
            return false;
        }
        ArmState armState = (ArmState) o;
        return Objects.equals(date, armState.date) &&
                statusType == armState.statusType &&
                Objects.equals(name, armState.name) &&
                Objects.equals(cid, armState.cid) &&
                Objects.equals(changedVia, armState.changedVia) &&
                Objects.equals(state, armState.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, statusType, name, cid, changedVia, state);
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("date", date)
                .add("statusType", statusType)
                .add("name", name)
                .add("cid", cid)
                .add("changedVia", changedVia)
                .add("state", state)
                .toString();
    }
}

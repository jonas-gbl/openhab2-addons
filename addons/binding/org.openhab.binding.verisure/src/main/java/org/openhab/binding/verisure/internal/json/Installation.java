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

/**
 * @author Jonas Gabriel - Initial contribution
 */
public class Installation {

    private String alias;
    private String cid;
    private String locale;
    private String giid;
    private String streetNo1;
    private int shard;
    private String routingGroup;
    private boolean deleted;
    private String street;
    private int signalFilterId;
    private String getStreetNo2;
    private int firmwareVersion;

    public String getAlias() {
        return alias;
    }

    public String getCid() {
        return cid;
    }

    public String getLocale() {
        return locale;
    }

    public String getGiid() {
        return giid;
    }

    public String getStreetNo1() {
        return streetNo1;
    }

    public int getShard() {
        return shard;
    }

    public String getRoutingGroup() {
        return routingGroup;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public String getStreet() {
        return street;
    }

    public int getSignalFilterId() {
        return signalFilterId;
    }

    public String getGetStreetNo2() {
        return getStreetNo2;
    }

    public int getFirmwareVersion() {
        return firmwareVersion;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Installation)) {
            return false;
        }
        Installation that = (Installation) o;
        return shard == that.shard &&
                deleted == that.deleted &&
                signalFilterId == that.signalFilterId &&
                firmwareVersion == that.firmwareVersion &&
                Objects.equals(alias, that.alias) &&
                Objects.equals(cid, that.cid) &&
                Objects.equals(locale, that.locale) &&
                Objects.equals(giid, that.giid) &&
                Objects.equals(streetNo1, that.streetNo1) &&
                Objects.equals(routingGroup, that.routingGroup) &&
                Objects.equals(street, that.street) &&
                Objects.equals(getStreetNo2, that.getStreetNo2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alias, cid, locale, giid, streetNo1, shard, routingGroup, deleted, street, signalFilterId, getStreetNo2, firmwareVersion);
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
                .add("alias", alias)
                .add("cid", cid)
                .add("locale", locale)
                .add("giid", giid)
                .add("streetNo1", streetNo1)
                .add("shard", shard)
                .add("routingGroup", routingGroup)
                .add("deleted", deleted)
                .add("street", street)
                .add("signalFilterId", signalFilterId)
                .add("getStreetNo2", getStreetNo2)
                .add("firmwareVersion", firmwareVersion)
                .toString();
    }
}

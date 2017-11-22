/**
 * Copyright (c) 2010-2017 by the respective copyright holders.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.binding.verisure;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.smarthome.core.thing.ThingTypeUID;

/**
 * The {@link VerisureBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Jonas Gabriel - Initial contribution
 */
@NonNullByDefault
public class VerisureBindingConstants {

    public static final String BINDING_ID = "verisure";
    // List of all Thing Type UIDs
    public static final ThingTypeUID THING_TYPE_VERISURE_ALARM = new ThingTypeUID(BINDING_ID, "verisure-alarm");

    // List of all Channel ids
    public static final String ALARM_STATUS_CHANNEL = "alarm-status-channel";
}

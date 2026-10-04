/*
 * This file is part of HuskClaims, licensed under the Apache License 2.0.
 *
 *  Copyright (c) William278 <will27528@gmail.com>
 *  Copyright (c) contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.william278.huskclaims.claim;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.william278.cloplib.operation.OperationType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WildernessFlagsTests {

    private static OperationType register(String key) {
        return OperationType.isRegistered(key)
                ? OperationType.getOrCreate(key) : OperationType.register(OperationType.getOrCreate(key));
    }

    private static ClaimWorld load(String json) {
        final JsonObject object = JsonParser.parseString(json).getAsJsonObject();
        return new ClaimWorldSerializer(null).deserialize(object, ClaimWorld.class, null);
    }

    @Test
    void typesAddedAfterTheWorldWasSavedAreAllowedInTheWilderness() {
        final OperationType added = register("test:added_later");
        final ClaimWorld world = load("{\"claims\":[],\"wilderness_flags\":[]}");

        assertTrue(world.getWildernessFlags().contains(added));
    }

    @Test
    void typesSwitchedOffStayOffOnceTheyWereKnown() {
        final OperationType off = register("test:switched_off");
        final ClaimWorld world = load("{\"claims\":[],\"wilderness_flags\":[],\"known_operation_types\":[\"test:switched_off\"]}");

        assertFalse(world.getWildernessFlags().contains(off));
    }
}

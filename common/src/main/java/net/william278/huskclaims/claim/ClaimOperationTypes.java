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

import net.william278.cloplib.operation.OperationType;
import net.william278.cloplib.operation.OperationTypeRegistry;
import org.jetbrains.annotations.NotNull;

public final class ClaimOperationTypes {

    public static final OperationType DISPLAY_ENTITY_EDIT =
            OperationType.getOrCreate("huskclaims:display_entity_edit");
    public static final OperationType ITEM_PICKUP =
            OperationType.getOrCreate("huskclaims:item_pickup");

    private ClaimOperationTypes() {
    }

    public static void register(@NotNull OperationTypeRegistry registry) {
        register(registry, DISPLAY_ENTITY_EDIT);
        register(registry, ITEM_PICKUP);
    }

    private static void register(@NotNull OperationTypeRegistry registry, @NotNull OperationType type) {
        if (!registry.isRegisteredOperationType(type.getKey())) {
            registry.registerOperationType(type);
        }
    }

}

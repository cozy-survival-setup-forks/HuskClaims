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

package net.william278.huskclaims.config;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class OperationGroupTests {

    private static final List<String> MENU_IDS = List.of(
            "explosions", "pvp", "monsters", "animals", "fire", "pearls", "raids", "spawn_eggs");

    private static List<Settings.OperationGroup> defaults() {
        try {
            final Constructor<Settings> constructor = Settings.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance().getOperationGroups();
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Test
    void defaultGroupsCoverTheSettingsMenu() {
        final List<Settings.OperationGroup> groups = defaults();

        assertEquals(MENU_IDS, groups.stream().map(Settings.OperationGroup::getPlaceholderId).toList());
        for (Settings.OperationGroup group : groups) {
            assertFalse(group.getToggleCommandAliases().isEmpty(), group.getName());
            assertFalse(group.getAllowedOperations().isEmpty(), group.getName());
        }
    }

    @Test
    void commandsAndIdsAreUnique() {
        final List<Settings.OperationGroup> groups = defaults();
        final Set<String> commands = new HashSet<>();

        groups.forEach(group -> commands.addAll(group.getToggleCommandAliases()));
        assertEquals(groups.size(), commands.size());
        assertEquals(groups.size(), groups.stream().map(Settings.OperationGroup::getPlaceholderId).distinct().count());
        assertTrue(commands.containsAll(List.of("claimexplosions", "claimpvp", "claimmonsters", "claimanimals",
                "claimfire", "claimpearls", "claimraids", "claimeggs")));
    }
}

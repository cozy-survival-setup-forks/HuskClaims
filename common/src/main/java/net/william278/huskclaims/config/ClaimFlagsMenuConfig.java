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

import com.google.common.collect.Lists;
import de.exlll.configlib.Configuration;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * Config for the /claimsettings menu, where claim managers switch operation groups on and off.
 *
 * @since 1.5.11
 */
@SuppressWarnings("FieldMayBeFinal")
@Getter
@Configuration
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClaimFlagsMenuConfig {

    protected static final String CONFIG_HEADER = """
            ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
            ┃ HuskClaims - Claim Settings Menu ┃
            ┃      Developed by William278     ┃
            ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
            ┣╸ Customise the /claimsettings menu, where players switch operation groups on and off
            ┣╸ Supports MiniMessage (https://docs.advntr.dev/minimessage/format.html)
            ┣╸ and legacy codes, including &#RRGGBB hex codes and &a / &l style codes
            ┣╸ Slots count across each row, 0 is the top left corner
            ┣╸ Empty slots are filled with filler_material; set it to AIR to leave them empty
            ┣╸ Every entry in `flags` is one operation group from `operation_groups` in config.yml, by its id.
            ┃  Add an entry to show another group, remove one to hide it. `enabled` is the item shown while the
            ┃  group is on in the claim, `disabled` the item shown while it is off.
            ┣╸ Placeholders in flag items: %group_name%, %group_description%
            ┗╸ Groups can only be changed by players with the manage operation groups trust privilege in the claim
            """;

    private String title = "<black>| Claims - Settings";

    private int rows = 5;

    private String fillerMaterial = "BLACK_STAINED_GLASS_PANE";

    private ItemDef infoItem = ItemDef.of("BOOK", "<b><#74C7FF>CLAIM SETTINGS",
            "<#B6C1C8>Claims",
            "",
            "<#FF9558>Description:",
            "<white>Manage the <#74C7FF>claim <white>you are",
            "<white>currently standing inside.",
            "",
            "<#FF9558>How does it work?",
            "<white>Click a setting to <#FFE05A>enable",
            "<white>or <#FF7490>disable <white>it for this claim.",
            "",
            "<#B6C1C8>You must be allowed to manage",
            "<#B6C1C8>operation groups in this claim.");

    private int infoSlot = 4;

    private ItemDef noClaimItem = ItemDef.of("BARRIER", "<b><#FF4361>NO CLAIM FOUND",
            "<#B6C1C8>Claims",
            "",
            "<#FF9558>Description:",
            "<white>Stand inside the <#74C7FF>claim <white>you",
            "<white>want to configure, then reopen this menu.",
            "",
            "<#FF4361>✘ You are not inside a claim.");

    private int noClaimSlot = 22;

    private ItemDef closeItem = ItemDef.of("GLOBE_BANNER_PATTERN", "<b><#FF4361>CLOSE",
            "<#B6C1C8>Claims",
            "",
            "<#FF9558>Description:",
            "<white>Click this button to",
            "<white>close this menu.",
            "",
            "<#FF9558>⏵ <b><u>CLICK</u></b> to Close");

    private int closeSlot = 40;

    private List<FlagItem> flags = Lists.newArrayList(
            flag("explosions", 10, "TNT", "TNT", "CLAIM EXPLOSIONS",
                    "<white>Allow <#FFE05A>explosions <white>to damage", "<white>terrain inside this claim."),
            flag("pvp", 11, "DIAMOND_SWORD", "WOODEN_SWORD", "CLAIM PVP",
                    "<white>Allow <#74C7FF>players <white>to fight", "<white>each other inside this claim."),
            flag("monsters", 12, "ZOMBIE_HEAD", "SKELETON_SKULL", "MONSTER SPAWNING",
                    "<white>Allow <#FFE05A>hostile mobs <white>to", "<white>spawn naturally in this claim."),
            flag("animals", 13, "COW_SPAWN_EGG", "COW_SPAWN_EGG", "ANIMAL SPAWNING",
                    "<white>Allow <#B0FF83>passive mobs <white>to", "<white>spawn naturally in this claim."),
            flag("fire", 14, "FLINT_AND_STEEL", "FIRE_CHARGE", "FIRE SPREAD",
                    "<white>Allow <#FF7490>fire <white>to spread", "<white>and burn blocks in this claim."),
            flag("pearls", 15, "ENDER_PEARL", "ENDER_PEARL", "ENDER PEARLS",
                    "<white>Allow <#74C7FF>ender pearls <white>to", "<white>teleport players into this claim."),
            flag("raids", 16, "OMINOUS_BOTTLE", "GLASS_BOTTLE", "RAIDS",
                    "<white>Allow <#FFE05A>raids <white>to start", "<white>inside the borders of this claim."),
            flag("spawn_eggs", 22, "CREEPER_SPAWN_EGG", "CREEPER_SPAWN_EGG", "SPAWN EGGS",
                    "<white>Allow <#FF7490>spawn eggs <white>to be", "<white>used inside this claim.")
    );

    private static FlagItem flag(String group, int slot, String enabledMaterial, String disabledMaterial, String name,
                                 String... description) {
        final List<String> enabledLore = new ArrayList<>(List.of("<#B6C1C8>Claims", "", "<#FF9558>Description:"));
        enabledLore.addAll(List.of(description));
        final List<String> disabledLore = new ArrayList<>(enabledLore);
        enabledLore.addAll(List.of("", "<#95FF7E>✔ Enabled", "<gray>✘ Disabled", "",
                "<#FF9558>⏵ <b><u>CLICK</u></b> to Disable"));
        disabledLore.addAll(List.of("", "<gray>✔ Enabled", "<#FF4361>✘ Disabled", "",
                "<#FF9558>⏵ <b><u>CLICK</u></b> to Enable"));
        return FlagItem.builder()
                .group(group)
                .slot(slot)
                .enabled(new ItemDef(enabledMaterial, "<b><#95FF7E>" + name, enabledLore))
                .disabled(new ItemDef(disabledMaterial, "<b><#FF4361>" + name, disabledLore))
                .build();
    }

    @Getter
    @Builder
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PUBLIC)
    public static class ItemDef {
        private String material;
        private String name;
        @Builder.Default
        private List<String> lore = List.of();

        static ItemDef of(String material, String name, String... lore) {
            return new ItemDef(material, name, List.of(lore));
        }
    }

    @Getter
    @Builder
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class FlagItem {
        private String group;
        private int slot;
        private ItemDef enabled;
        private ItemDef disabled;
    }

}

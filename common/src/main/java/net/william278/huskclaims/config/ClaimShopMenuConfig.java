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

import java.util.List;

/**
 * Config for the /claimblocks shop GUI, letting server owners re-skin it without touching code.
 *
 * @since 1.5.11
 */
@SuppressWarnings("FieldMayBeFinal")
@Getter
@Configuration
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ClaimShopMenuConfig {

    protected static final String CONFIG_HEADER = """
            ┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓
            ┃ HuskClaims - Claim Shop Menu ┃
            ┃    Developed by William278   ┃
            ┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛
            ┣╸ Customise the appearance of the /claimblocks shop
            ┣╸ Formatted in MineDown: https://github.com/Phoenix616/MineDown
            ┣╸ Note: a &#RRGGBB hex code must end with an & before the next
            ┣╸ code or plain text, e.g. &#66D9E8&Text, or &l&#66D9E8&Text
            ┗╸ Bundle placeholders: %amount%, %amount_x10%
            ┗╸ Info item placeholders: %quantity%, %unit_price%, %total_cost%
            """;

    private String title = "&8&l« &l&#9B8CFF&Claim Block Shop &8&l»";

    private String borderMaterial = "BLACK_STAINED_GLASS_PANE";

    private String accentBorderMaterial = "PURPLE_STAINED_GLASS_PANE";

    private List<BundleItem> bundles = Lists.newArrayList(
            BundleItem.builder()
                    .material("PRISMARINE_CRYSTALS")
                    .name("&l&#66D9E8&SMALL BUNDLE")
                    .amount(100)
                    .lore(List.of(
                            "&7Claim Block Shop",
                            "",
                            "&fA quick top-up of &l&#66D9E8&claim blocks&r&f.",
                            "",
                            "&l&#FFB86B&LEFT&r &8→ &7Add &f%amount%",
                            "&l&#FFB86B&RIGHT&r &8→ &7Remove &f%amount%",
                            "&l&#FFB86B&SHIFT&r &8→ &7Apply &f%amount_x10%"
                    ))
                    .build(),
            BundleItem.builder()
                    .material("SUNFLOWER")
                    .name("&l&#FFD166&MEDIUM BUNDLE")
                    .amount(500)
                    .lore(List.of(
                            "&7Claim Block Shop",
                            "",
                            "&fA solid stack of &l&#FFD166&claim blocks&r&f.",
                            "",
                            "&l&#FFB86B&LEFT&r &8→ &7Add &f%amount%",
                            "&l&#FFB86B&RIGHT&r &8→ &7Remove &f%amount%",
                            "&l&#FFB86B&SHIFT&r &8→ &7Apply &f%amount_x10%"
                    ))
                    .build(),
            BundleItem.builder()
                    .material("AMETHYST_SHARD")
                    .name("&l&#C8A6FF&LARGE BUNDLE")
                    .amount(1000)
                    .lore(List.of(
                            "&7Claim Block Shop",
                            "",
                            "&fGo big with &l&#C8A6FF&a thousand&r&f blocks.",
                            "",
                            "&l&#FFB86B&LEFT&r &8→ &7Add &f%amount%",
                            "&l&#FFB86B&RIGHT&r &8→ &7Remove &f%amount%",
                            "&l&#FFB86B&SHIFT&r &8→ &7Apply &f%amount_x10%"
                    ))
                    .build()
    );

    private ItemDef infoItem = ItemDef.builder()
            .material("NETHER_STAR")
            .name("&l&#C8A6FF&ORDER SUMMARY")
            .lore(List.of(
                    "&7Claim Block Shop",
                    "",
                    "&#66D9E8&Quantity &8→ &f%quantity%",
                    "&#FFD166&Unit Price &8→ &f%unit_price%",
                    "&#8BF0A6&Total Cost &8→ &f%total_cost%"
            ))
            .build();

    private ItemDef resetItem = ItemDef.builder()
            .material("RECOVERY_COMPASS")
            .name("&l&#FF6B7A&RESET")
            .lore(List.of(
                    "&7Claim Block Shop",
                    "",
                    "&fPut the &#66D9E8&selection &fback to",
                    "&fits &#8BF0A6&default &famount.",
                    "",
                    "&l&#FFB86B&LEFT&r &8→ &7Reset selection"
            ))
            .build();

    private ItemDef customAmountItem = ItemDef.builder()
            .material("WRITABLE_BOOK")
            .name("&l&#FFD166&CUSTOM AMOUNT")
            .lore(List.of(
                    "&7Claim Block Shop",
                    "",
                    "&fType a &#66D9E8&custom &famount of",
                    "&#8BF0A6&claim blocks &fin chat.",
                    "",
                    "&l&#FFB86B&LEFT&r &8→ &7Enter amount"
            ))
            .build();

    private ItemDef confirmItem = ItemDef.builder()
            .material("LIME_CONCRETE")
            .name("&l&#8BF0A6&CONFIRM PURCHASE")
            .lore(List.of(
                    "&7Claim Block Shop",
                    "",
                    "&fBuy the &#66D9E8&selected &famount for",
                    "&fthe &#FF6B7A&total &fshown above.",
                    "",
                    "&l&#FFB86B&LEFT&r &8→ &7Confirm purchase"
            ))
            .build();

    private ItemDef closeItem = ItemDef.builder()
            .material("BARRIER")
            .name("&l&#FF6B7A&CLOSE")
            .lore(List.of(
                    "&7Claim Block Shop",
                    "",
                    "&l&#FFB86B&LEFT&r &8→ &7Close this menu"
            ))
            .build();

    @Getter
    @Builder
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class BundleItem {
        private String material;
        private String name;
        private long amount;
        @Builder.Default
        private List<String> lore = List.of();
    }

    @Getter
    @Builder
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class ItemDef {
        private String material;
        private String name;
        @Builder.Default
        private List<String> lore = List.of();
    }

}

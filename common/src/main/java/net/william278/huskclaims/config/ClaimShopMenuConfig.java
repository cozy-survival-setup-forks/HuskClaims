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
            ┣╸ Supports MiniMessage (https://docs.advntr.dev/minimessage/format.html)
            ┣╸ and legacy codes, including &#RRGGBB hex codes and &a / &l style codes
            ┣╸ Every item has a slot (0 is the top left corner, counting across each row)
            ┣╸ Empty slots are filled with filler_material; set it to AIR to leave them empty
            ┣╸ Bundle placeholders: %amount%, %amount_x10%
            ┗╸ Info item placeholders: %quantity%, %unit_price%, %total_cost%
            """;

    private String title = "<black>| Claim Block Shop";

    private int rows = 6;

    private String fillerMaterial = "BLACK_STAINED_GLASS_PANE";

    private List<BundleItem> bundles = Lists.newArrayList(
            BundleItem.builder()
                    .material("PRISMARINE_CRYSTALS")
                    .slot(11)
                    .name("<b><#66D9E8>SMALL BUNDLE")
                    .amount(100)
                    .lore(List.of(
                            "<gray>Claim Block Shop",
                            "",
                            "<white>A quick top-up of <b><#66D9E8>claim blocks</b><white>.",
                            "",
                            "<#EBCB8B>▶ <b><u>LEFT CLICK</u></b> to add %amount%",
                            "<#EBCB8B>▶ <b><u>RIGHT CLICK</u></b> to remove %amount%",
                            "<#EBCB8B>▶ <b><u>SHIFT CLICK</u></b> to change by %amount_x10%"
                    ))
                    .build(),
            BundleItem.builder()
                    .material("SUNFLOWER")
                    .slot(13)
                    .name("<b><#FFD166>MEDIUM BUNDLE")
                    .amount(500)
                    .lore(List.of(
                            "<gray>Claim Block Shop",
                            "",
                            "<white>A solid stack of <b><#FFD166>claim blocks</b><white>.",
                            "",
                            "<#EBCB8B>▶ <b><u>LEFT CLICK</u></b> to add %amount%",
                            "<#EBCB8B>▶ <b><u>RIGHT CLICK</u></b> to remove %amount%",
                            "<#EBCB8B>▶ <b><u>SHIFT CLICK</u></b> to change by %amount_x10%"
                    ))
                    .build(),
            BundleItem.builder()
                    .material("AMETHYST_SHARD")
                    .slot(15)
                    .name("<b><#C8A6FF>LARGE BUNDLE")
                    .amount(1000)
                    .lore(List.of(
                            "<gray>Claim Block Shop",
                            "",
                            "<white>Go big with <b><#C8A6FF>a thousand</b><white> blocks.",
                            "",
                            "<#EBCB8B>▶ <b><u>LEFT CLICK</u></b> to add %amount%",
                            "<#EBCB8B>▶ <b><u>RIGHT CLICK</u></b> to remove %amount%",
                            "<#EBCB8B>▶ <b><u>SHIFT CLICK</u></b> to change by %amount_x10%"
                    ))
                    .build()
    );

    private ItemDef infoItem = ItemDef.builder()
            .material("NETHER_STAR")
            .slot(22)
            .name("<b><#C8A6FF>ORDER SUMMARY")
            .lore(List.of(
                    "<gray>Claim Block Shop",
                    "",
                    "<#66D9E8>Quantity <dark_gray>→ <white>%quantity%",
                    "<#FFD166>Unit Price <dark_gray>→ <white>%unit_price%",
                    "<#8BF0A6>Total Cost <dark_gray>→ <white>%total_cost%"
            ))
            .build();

    private ItemDef resetItem = ItemDef.builder()
            .material("RECOVERY_COMPASS")
            .slot(29)
            .name("<b><#FF6B7A>RESET")
            .lore(List.of(
                    "<gray>Claim Block Shop",
                    "",
                    "<white>Put the <#66D9E8>selection <white>back to",
                    "<white>its <#8BF0A6>default <white>amount.",
                    "",
                    "<#EBCB8B>▶ <b><u>CLICK</u></b> to reset"
            ))
            .build();

    private ItemDef customAmountItem = ItemDef.builder()
            .material("WRITABLE_BOOK")
            .slot(31)
            .name("<b><#FFD166>CUSTOM AMOUNT")
            .lore(List.of(
                    "<gray>Claim Block Shop",
                    "",
                    "<white>Type a <#66D9E8>custom <white>amount of",
                    "<#8BF0A6>claim blocks <white>in chat.",
                    "",
                    "<#EBCB8B>▶ <b><u>CLICK</u></b> to enter an amount"
            ))
            .build();

    private ItemDef confirmItem = ItemDef.builder()
            .material("LIME_CONCRETE")
            .slot(33)
            .name("<b><#8BF0A6>CONFIRM PURCHASE")
            .lore(List.of(
                    "<gray>Claim Block Shop",
                    "",
                    "<white>Buy the <#66D9E8>selected <white>amount for",
                    "<white>the <#FF6B7A>total <white>shown above.",
                    "",
                    "<#EBCB8B>▶ <b><u>CLICK</u></b> to buy"
            ))
            .build();

    private ItemDef closeItem = ItemDef.builder()
            .material("BARRIER")
            .slot(49)
            .name("<b><#FF6B7A>CLOSE")
            .lore(List.of(
                    "<gray>Claim Block Shop",
                    "",
                    "<#EBCB8B>▶ <b><u>CLICK</u></b> to close"
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
        private int slot = -1;
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
        private int slot = -1;
        @Builder.Default
        private List<String> lore = List.of();
    }

}

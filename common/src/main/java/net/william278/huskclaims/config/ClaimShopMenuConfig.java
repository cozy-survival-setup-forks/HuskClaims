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

    /**
     * Bumped when the default layout changes; a file written by an older layout is kept as claim_shop_menu.yml.old
     * and replaced by the defaults.
     */
    public static final int LAYOUT_VERSION = 2;

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
            ┣╸ Bundles change the selected amount by their amount when clicked; a negative amount removes
            ┣╸ Bundle placeholders: %amount% (the size of the change, without the sign)
            ┗╸ Info item placeholders: %quantity%, %unit_price%, %total_cost%
            """;

    private int layoutVersion = LAYOUT_VERSION;

    private String title = "&0| Claim Blocks Shop";

    private int rows = 4;

    private String fillerMaterial = "BLACK_STAINED_GLASS_PANE";

    private List<BundleItem> bundles = Lists.newArrayList(
            bundle("RED_STAINED_GLASS_PANE", 9, "&#EF305F&lREMOVE 1000", -1000),
            bundle("RED_STAINED_GLASS_PANE", 10, "&#EF305F&lREMOVE 500", -500),
            bundle("RED_STAINED_GLASS_PANE", 11, "&#EF305F&lREMOVE 100", -100),
            bundle("LIME_STAINED_GLASS_PANE", 15, "&#63F68B&lADD 100", 100),
            bundle("LIME_STAINED_GLASS_PANE", 16, "&#63F68B&lADD 500", 500),
            bundle("LIME_STAINED_GLASS_PANE", 17, "&#63F68B&lADD 1000", 1000)
    );

    private ItemDef infoItem = ItemDef.builder()
            .material("GOLDEN_SHOVEL")
            .slot(13)
            .name("&#FF6C00&lCLAIM BLOCKS")
            .lore(List.of(
                    "&#9FA3B8Claim Block Shop",
                    "",
                    "&#FAE156Description:",
                    "&#FD6DEE⚑ &fQuantity: &#FD6DEE%quantity%",
                    "&#63EAF6☄ &fUnit Price: &#63EAF6%unit_price%",
                    "&#63F68B&l$ &fTotal Cost: &#63F68B%total_cost%"
            ))
            .build();

    private ItemDef customAmountItem = ItemDef.builder()
            .material("YELLOW_STAINED_GLASS_PANE")
            .slot(31)
            .name("&#FAE156&lCUSTOM AMOUNT")
            .lore(List.of(
                    "&#9FA3B8Claim Block Shop",
                    "",
                    "&#FAE156Description:",
                    "&fType a &#63F68Bcustom &fnumber",
                    "&fof claim &#FAE156blocks &fto &#EF305Fbuy.",
                    "",
                    "&#FAE156▶ &l&nCLICK&#FAE156 to Continue"
            ))
            .build();

    private ItemDef cancelItem = ItemDef.builder()
            .material("RED_SHULKER_BOX")
            .slot(30)
            .name("&#ff0000&lCANCEL")
            .lore(List.of(
                    "&#9FA3B8Claim Block Shop",
                    "",
                    "&#FAE156Description:",
                    "&fClick &#63F68Bhere &fto go &#FAE156back",
                    "&fto the &#EF305Fmain menu",
                    "",
                    "&#FAE156▶ &l&nCLICK&#FAE156 to Return"
            ))
            .build();

    private ItemDef confirmItem = ItemDef.builder()
            .material("LIME_SHULKER_BOX")
            .slot(32)
            .name("&a&lCONFIRM")
            .lore(List.of(
                    "&#9FA3B8Claim Block Shop",
                    "",
                    "&#FAE156Description:",
                    "&fClick &#63F68Bhere &fto &#FAE156buy",
                    "&fthe &#EF305Fitem!",
                    "",
                    "&#FAE156▶ &l&nCLICK&#FAE156 to Buy"
            ))
            .build();

    private static BundleItem bundle(String material, int slot, String name, long amount) {
        return BundleItem.builder()
                .material(material)
                .slot(slot)
                .name(name)
                .amount(amount)
                .lore(List.of(
                        "&#9FA3B8Claim Block Shop",
                        "",
                        "&#FAE156▶ &l&nCLICK&#FAE156 to Continue"
                ))
                .build();
    }

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

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

package net.william278.huskclaims.menu;

import de.themoep.minedown.adventure.MineDown;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.kyori.adventure.text.Component;
import net.william278.huskclaims.PaperHuskClaims;
import net.william278.huskclaims.hook.EconomyHook;
import net.william278.huskclaims.user.ClaimBlocksManager;
import net.william278.huskclaims.user.OnlineUser;
import net.william278.huskclaims.user.SavedUser;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ClaimShopMenu {

    private static final int SIZE = 45;
    private static final int DEFAULT_AMOUNT = 100;
    private static final Map<UUID, ClaimShopMenu> ACTIVE_INPUTS = new ConcurrentHashMap<>();

    private final PaperHuskClaims plugin;
    private final Player player;
    private final Gui gui;
    private long selectedAmount;

    public ClaimShopMenu(@NotNull PaperHuskClaims plugin, @NotNull Player player) {
        this.plugin = plugin;
        this.player = player;
        this.selectedAmount = DEFAULT_AMOUNT;
        this.gui = Gui.gui()
                .title(text("&#C8A6FF&lClaim Block Exchange"))
                .rows(SIZE / 9)
                .disableAllInteractions()
                .create();
        draw();
    }

    public void open() {
        gui.open(player);
    }

    private void handleClick(@NotNull InventoryClickEvent event) {
        if (event.getRawSlot() < 0 || event.getRawSlot() >= SIZE) {
            return;
        }

        final long multiplier = event.isShiftClick() ? 10L : 1L;
        final long bundle = switch (event.getRawSlot()) {
            case 11 -> 100L;
            case 13 -> 500L;
            case 15 -> 1000L;
            default -> 0L;
        };
        if (bundle > 0) {
            final long change = Math.multiplyExact(bundle, multiplier);
            selectedAmount = event.isRightClick()
                    ? Math.max(1, selectedAmount - change)
                    : Math.min(SavedUser.MAX_CLAIM_BLOCKS, selectedAmount + change);
            draw();
            return;
        }

        switch (event.getRawSlot()) {
            case 29 -> {
                selectedAmount = DEFAULT_AMOUNT;
                draw();
            }
            case 31 -> beginCustomInput();
            case 33 -> confirmPurchase();
            case 40 -> player.closeInventory();
            default -> {
            }
        }
    }

    public void acceptInput(@NotNull String input) {
        if (input.equalsIgnoreCase("cancel")) {
            ACTIVE_INPUTS.remove(player.getUniqueId(), this);
            locale("claim_shop_input_cancelled");
            return;
        }
        try {
            final long amount = Long.parseLong(input.trim());
            if (amount < 1 || amount > SavedUser.MAX_CLAIM_BLOCKS) {
                throw new NumberFormatException("Out of range");
            }
            selectedAmount = amount;
            ACTIVE_INPUTS.remove(player.getUniqueId(), this);
            draw();
            open();
        } catch (NumberFormatException e) {
            locale("claim_shop_input_invalid");
        }
    }

    public static ClaimShopMenu getActiveInput(@NotNull UUID uuid) {
        return ACTIVE_INPUTS.get(uuid);
    }

    public static void clearActiveInput(@NotNull UUID uuid) {
        ACTIVE_INPUTS.remove(uuid);
    }

    private void draw() {
        gui.clearItems();
        final ItemStack border = item(Material.GRAY_STAINED_GLASS_PANE, " ", List.of());
        for (int slot = 0; slot < SIZE; slot++) {
            if (slot < 9 || slot >= 36 || slot % 9 == 0 || slot % 9 == 8) {
                gui.setItem(slot, new GuiItem(border));
            }
        }

        setActionItem(11, bundle(Material.PRISMARINE_CRYSTALS, "&#66D9E8&lQUICK", 100));
        setActionItem(13, bundle(Material.SUNFLOWER, "&#FFD166&lSTANDARD", 500));
        setActionItem(15, bundle(Material.AMETHYST_SHARD, "&#C8A6FF&lEXPANDED", 1000));
        gui.setItem(22, new GuiItem(infoItem()));
        setActionItem(29, item(
                Material.REDSTONE,
                "&#FF6B7A&lRESET",
                List.of(
                        "&#A7B6C4Claim Block Exchange",
                        "",
                        "&#FF9F7ADescription:",
                        "&fRestore the &#66D9E8selection &fto its",
                        "&#8BF0A6default &famount.",
                        "",
                        "&#FFB86B⏵ &#FFB86B&lLEFT CLICK&r &#A7B6C4• &fReset selection"
                )
        ));
        setActionItem(31, item(
                Material.NAME_TAG,
                "&#FFD166&lCUSTOM AMOUNT",
                List.of(
                        "&#A7B6C4Claim Block Exchange",
                        "",
                        "&#FF9F7ADescription:",
                        "&fEnter a &#66D9E8custom &fnumber of",
                        "&#8BF0A6claim &fblocks through chat.",
                        "",
                        "&#FFB86B⏵ &#FFB86B&lLEFT CLICK&r &#A7B6C4• &fEnter amount"
                )
        ));
        setActionItem(33, item(
                Material.EMERALD,
                "&#8BF0A6&lCONFIRM PURCHASE",
                List.of(
                        "&#A7B6C4Claim Block Exchange",
                        "",
                        "&#FF9F7ADescription:",
                        "&fPurchase the &#66D9E8selected &fclaim",
                        "&#8BF0A6blocks &ffor the shown &#FF6B7Atotal&f.",
                        "",
                        "&#FFB86B⏵ &#FFB86B&lLEFT CLICK&r &#A7B6C4• &fConfirm purchase"
                )
        ));
        setActionItem(40, item(
                Material.BARRIER,
                "&#FF6B7A&lCLOSE",
                List.of(
                        "&#A7B6C4Claim Block Exchange",
                        "",
                        "&#FFB86B⏵ &#FFB86B&lLEFT CLICK&r &#A7B6C4• &fClose menu"
                )
        ));
        gui.update();
    }

    private void setActionItem(int slot, @NotNull ItemStack item) {
        gui.setItem(slot, new GuiItem(item, this::handleClick));
    }

    private ItemStack bundle(@NotNull Material material, @NotNull String name, long amount) {
        return item(material, name, List.of(
                "&#A7B6C4Claim Block Exchange",
                "",
                "&#FF9F7ADescription:",
                "&fChoose a &#66D9E8bundle &fto &#FFD166adjust",
                "&fyour &#FF6B7Aselection &finstantly.",
                "",
                "&#FFB86B⏵ &#FFB86B&lLEFT CLICK&r &#A7B6C4• &fAdd &#FFB86B" + amount,
                "&#FFB86B⏵ &#FFB86B&lRIGHT CLICK&r &#A7B6C4• &fRemove &#FFB86B" + amount,
                "&#FFB86B⏵ &#FFB86B&lSHIFT CLICK&r &#A7B6C4• &fApply &#FFB86B" + (amount * 10)
        ));
    }

    private ItemStack infoItem() {
        final double unitPrice = Math.max(0, plugin.getSettings().getHooks().getEconomy().getCostPerBlock());
        final double total = selectedAmount * unitPrice;
        final Optional<EconomyHook> economy = plugin.getHook(EconomyHook.class);
        final String formattedUnit = economy.map(hook -> hook.format(unitPrice)).orElse(Double.toString(unitPrice));
        final String formattedTotal = economy.map(hook -> hook.format(total)).orElse(Double.toString(total));
        return item(Material.GOLDEN_SHOVEL, "&#C8A6FF&lCLAIM BLOCKS", List.of(
                "&#A7B6C4Claim Block Exchange",
                "",
                "&#FF9F7ADescription:",
                "&fReview the &#FFD166quantity&f, &#66D9E8price&f,",
                "&fand final &#8BF0A6cost &fbefore &#FF6B7Apurchase&f.",
                "",
                "&#66D9E8Quantity: &f" + selectedAmount,
                "&#FFD166Unit Price: &f" + formattedUnit,
                "&#8BF0A6Total Cost: &f" + formattedTotal
        ));
    }

    private void beginCustomInput() {
        player.closeInventory();
        ACTIVE_INPUTS.put(player.getUniqueId(), this);
        locale("claim_shop_input_prompt");
        player.getScheduler().runDelayed(plugin, task -> {
            if (ACTIVE_INPUTS.remove(player.getUniqueId(), this)) {
                locale("claim_shop_input_timeout");
            }
        }, null, 600L);
    }

    private void confirmPurchase() {
        final Optional<EconomyHook> economy = plugin.getHook(EconomyHook.class);
        if (economy.isEmpty()) {
            locale("error_economy_not_found");
            return;
        }
        final EconomyHook hook = economy.get();
        final double cost = selectedAmount * Math.max(0, plugin.getSettings().getHooks().getEconomy().getCostPerBlock());
        final OnlineUser user = plugin.getOnlineUser(player);
        if (!hook.takeMoney(user, cost, EconomyHook.EconomyReason.BUY_CLAIM_BLOCKS)) {
            locale("error_insufficient_funds");
            return;
        }

        final long amount = selectedAmount;
        plugin.editClaimBlocks(
                user,
                ClaimBlocksManager.ClaimBlockSource.PURCHASE,
                blocks -> blocks > Long.MAX_VALUE - amount ? Long.MAX_VALUE : blocks + amount,
                newBalance -> plugin.getLocales().getLocale(
                        "claim_blocks_purchased",
                        Long.toString(amount), hook.format(cost), Long.toString(newBalance)
                ).ifPresent(user::sendMessage)
        );
        player.closeInventory();
    }

    private void locale(@NotNull String key) {
        plugin.getLocales().getLocale(key).ifPresent(plugin.getOnlineUser(player)::sendMessage);
    }

    private static ItemStack item(@NotNull Material material, @NotNull String name,
                                  @NotNull List<String> lore) {
        final ItemStack stack = new ItemStack(material);
        final ItemMeta meta = stack.getItemMeta();
        meta.displayName(text(name));
        meta.lore(lore.stream().map(ClaimShopMenu::text).toList());
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        stack.setItemMeta(meta);
        return stack;
    }

    private static Component text(@NotNull String input) {
        return new MineDown(input).toComponent();
    }
}

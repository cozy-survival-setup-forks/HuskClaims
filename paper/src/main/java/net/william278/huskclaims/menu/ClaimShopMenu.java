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
import net.william278.huskclaims.config.ClaimShopMenuConfig;
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

    private static final int SIZE = 54;
    private static final int DEFAULT_AMOUNT = 100;
    private static final int[] BUNDLE_SLOTS = {11, 13, 15};
    private static final int RESET_SLOT = 29;
    private static final int CUSTOM_AMOUNT_SLOT = 31;
    private static final int CONFIRM_SLOT = 33;
    private static final int INFO_SLOT = 22;
    private static final int CLOSE_SLOT = 49;
    private static final Map<UUID, ClaimShopMenu> ACTIVE_INPUTS = new ConcurrentHashMap<>();

    private final PaperHuskClaims plugin;
    private final ClaimShopMenuConfig config;
    private final Player player;
    private final Gui gui;
    private long selectedAmount;

    public ClaimShopMenu(@NotNull PaperHuskClaims plugin, @NotNull Player player) {
        this.plugin = plugin;
        this.config = plugin.getClaimShopMenuConfig();
        this.player = player;
        this.selectedAmount = DEFAULT_AMOUNT;
        this.gui = Gui.gui()
                .title(text(config.getTitle()))
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
        final long bundle = bundleAmountAt(event.getRawSlot());
        if (bundle > 0) {
            final long change = Math.multiplyExact(bundle, multiplier);
            selectedAmount = event.isRightClick()
                    ? Math.max(1, selectedAmount - change)
                    : Math.min(SavedUser.MAX_CLAIM_BLOCKS, selectedAmount + change);
            draw();
            return;
        }

        if (event.getRawSlot() == RESET_SLOT) {
            selectedAmount = DEFAULT_AMOUNT;
            draw();
        } else if (event.getRawSlot() == CUSTOM_AMOUNT_SLOT) {
            beginCustomInput();
        } else if (event.getRawSlot() == CONFIRM_SLOT) {
            confirmPurchase();
        } else if (event.getRawSlot() == CLOSE_SLOT) {
            player.closeInventory();
        }
    }

    private long bundleAmountAt(int slot) {
        final List<ClaimShopMenuConfig.BundleItem> bundles = config.getBundles();
        for (int i = 0; i < BUNDLE_SLOTS.length && i < bundles.size(); i++) {
            if (BUNDLE_SLOTS[i] == slot) {
                return bundles.get(i).getAmount();
            }
        }
        return 0L;
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
        final ItemStack border = item(resolveMaterial(config.getBorderMaterial(), Material.BLACK_STAINED_GLASS_PANE), " ", List.of());
        final ItemStack accentBorder = item(resolveMaterial(config.getAccentBorderMaterial(), Material.PURPLE_STAINED_GLASS_PANE), " ", List.of());
        for (int slot = 0; slot < SIZE; slot++) {
            final boolean topOrBottomRow = slot < 9 || slot >= 45;
            final boolean sideColumn = slot % 9 == 0 || slot % 9 == 8;
            if (topOrBottomRow) {
                gui.setItem(slot, new GuiItem(accentBorder));
            } else if (sideColumn) {
                gui.setItem(slot, new GuiItem(border));
            }
        }

        final List<ClaimShopMenuConfig.BundleItem> bundles = config.getBundles();
        for (int i = 0; i < BUNDLE_SLOTS.length && i < bundles.size(); i++) {
            setActionItem(BUNDLE_SLOTS[i], bundleItemStack(bundles.get(i)));
        }

        gui.setItem(INFO_SLOT, new GuiItem(infoItemStack()));
        setActionItem(RESET_SLOT, defItemStack(config.getResetItem()));
        setActionItem(CUSTOM_AMOUNT_SLOT, defItemStack(config.getCustomAmountItem()));
        setActionItem(CONFIRM_SLOT, defItemStack(config.getConfirmItem()));
        setActionItem(CLOSE_SLOT, defItemStack(config.getCloseItem()));
        gui.update();
    }

    private void setActionItem(int slot, @NotNull ItemStack item) {
        gui.setItem(slot, new GuiItem(item, this::handleClick));
    }

    private ItemStack bundleItemStack(@NotNull ClaimShopMenuConfig.BundleItem bundle) {
        final List<String> lore = bundle.getLore().stream()
                .map(line -> line
                        .replace("%amount%", Long.toString(bundle.getAmount()))
                        .replace("%amount_x10%", Long.toString(bundle.getAmount() * 10)))
                .toList();
        return item(resolveMaterial(bundle.getMaterial(), Material.PAPER), bundle.getName(), lore);
    }

    private ItemStack infoItemStack() {
        final double unitPrice = Math.max(0, plugin.getSettings().getHooks().getEconomy().getCostPerBlock());
        final double total = selectedAmount * unitPrice;
        final Optional<EconomyHook> economy = plugin.getHook(EconomyHook.class);
        final String formattedUnit = economy.map(hook -> hook.format(unitPrice)).orElse(Double.toString(unitPrice));
        final String formattedTotal = economy.map(hook -> hook.format(total)).orElse(Double.toString(total));

        final ClaimShopMenuConfig.ItemDef def = config.getInfoItem();
        final List<String> lore = def.getLore().stream()
                .map(line -> line
                        .replace("%quantity%", Long.toString(selectedAmount))
                        .replace("%unit_price%", formattedUnit)
                        .replace("%total_cost%", formattedTotal))
                .toList();
        return item(resolveMaterial(def.getMaterial(), Material.NETHER_STAR), def.getName(), lore);
    }

    private ItemStack defItemStack(@NotNull ClaimShopMenuConfig.ItemDef def) {
        return item(resolveMaterial(def.getMaterial(), Material.BARRIER), def.getName(), def.getLore());
    }

    @NotNull
    private Material resolveMaterial(@NotNull String name, @NotNull Material fallback) {
        final Material material = Material.matchMaterial(name);
        return material != null ? material : fallback;
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

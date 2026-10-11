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
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
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

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ClaimShopMenu {

    private static final long DEFAULT_AMOUNT = 1L;
    private static final int INFO_SLOT = 13;
    private static final int CANCEL_SLOT = 30;
    private static final int CUSTOM_AMOUNT_SLOT = 31;
    private static final int CONFIRM_SLOT = 32;
    private static final int INPUT_SECONDS = 30;
    private static final Map<UUID, ClaimShopMenu> ACTIVE_INPUTS = new ConcurrentHashMap<>();
    private static final Pattern LEGACY_CODE = Pattern.compile("[&§]x((?:[&§][0-9a-fA-F]){6})|[&§]#([0-9a-fA-F]{6})|[&§]([0-9a-fA-Fk-orK-OR])");
    private static final Pattern SECTION_CODES = Pattern.compile("\u00a7x(?:\u00a7[0-9a-fA-F]){6}|\u00a7[0-9a-fA-Fk-orK-OR]");
    private static final Map<Character, String> LEGACY_TAGS = Map.ofEntries(
            Map.entry('0', "<black>"), Map.entry('1', "<dark_blue>"), Map.entry('2', "<dark_green>"),
            Map.entry('3', "<dark_aqua>"), Map.entry('4', "<dark_red>"), Map.entry('5', "<dark_purple>"),
            Map.entry('6', "<gold>"), Map.entry('7', "<gray>"), Map.entry('8', "<dark_gray>"),
            Map.entry('9', "<blue>"), Map.entry('a', "<green>"), Map.entry('b', "<aqua>"),
            Map.entry('c', "<red>"), Map.entry('d', "<light_purple>"), Map.entry('e', "<yellow>"),
            Map.entry('f', "<white>"), Map.entry('k', "<obfuscated>"), Map.entry('l', "<bold>"),
            Map.entry('m', "<strikethrough>"), Map.entry('n', "<underlined>"), Map.entry('o', "<italic>"),
            Map.entry('r', "<reset>")
    );

    private final PaperHuskClaims plugin;
    private final ClaimShopMenuConfig config;
    private final Player player;
    private final Gui gui;
    private final int size;
    private long selectedAmount;

    public ClaimShopMenu(@NotNull PaperHuskClaims plugin, @NotNull Player player) {
        this.plugin = plugin;
        this.config = plugin.getClaimShopMenuConfig();
        this.player = player;
        this.selectedAmount = DEFAULT_AMOUNT;
        this.size = Math.max(1, Math.min(6, config.getRows())) * 9;
        this.gui = Gui.gui()
                .title(text(config.getTitle()))
                .rows(size / 9)
                .disableAllInteractions()
                .create();
        draw();
    }

    public void open() {
        gui.open(player);
    }

    private void handleClick(@NotNull InventoryClickEvent event) {
        final int slot = event.getRawSlot();
        if (slot < 0 || slot >= size) {
            return;
        }

        final Long change = bundleChangeAt(slot);
        if (change != null) {
            selectedAmount = Math.max(1L, Math.min(SavedUser.MAX_CLAIM_BLOCKS, selectedAmount + change));
            draw();
        } else if (slot == slotOf(config.getCustomAmountItem(), CUSTOM_AMOUNT_SLOT)) {
            beginCustomInput();
        } else if (slot == slotOf(config.getConfirmItem(), CONFIRM_SLOT)) {
            confirmPurchase();
        } else if (slot == slotOf(config.getCancelItem(), CANCEL_SLOT)) {
            player.closeInventory();
        }
    }

    // the change a bundle makes, kept inside what a single click can sensibly move
    private Long bundleChangeAt(int slot) {
        for (ClaimShopMenuConfig.BundleItem bundle : config.getBundles()) {
            if (bundle.getSlot() == slot) {
                return Math.max(-SavedUser.MAX_CLAIM_BLOCKS, Math.min(SavedUser.MAX_CLAIM_BLOCKS, bundle.getAmount()));
            }
        }
        return null;
    }

    private static int slotOf(@NotNull ClaimShopMenuConfig.ItemDef def, int fallback) {
        return def.getSlot() >= 0 ? def.getSlot() : fallback;
    }

    public void acceptInput(@NotNull String input) {
        if (input.strip().equalsIgnoreCase("cancel")) {
            ACTIVE_INPUTS.remove(player.getUniqueId(), this);
            player.clearTitle();
            locale("claim_shop_input_cancelled");
            return;
        }

        final long amount;
        try {
            // "1,000" and "1 000" are fine, "1k" is not
            amount = Long.parseLong(input.replaceAll("[,_\\s]", ""));
        } catch (NumberFormatException e) {
            locale("claim_shop_input_invalid");
            return;
        }
        if (amount < 1 || amount > SavedUser.MAX_CLAIM_BLOCKS) {
            locale("claim_shop_input_amount");
            return;
        }

        selectedAmount = amount;
        ACTIVE_INPUTS.remove(player.getUniqueId(), this);
        player.clearTitle();
        draw();
        open();
    }

    public static ClaimShopMenu getActiveInput(@NotNull UUID uuid) {
        return ACTIVE_INPUTS.get(uuid);
    }

    public static void clearActiveInput(@NotNull UUID uuid) {
        ACTIVE_INPUTS.remove(uuid);
    }

    private void draw() {
        gui.clearItems();
        final Material fillerMaterial = resolveMaterial(config.getFillerMaterial(), Material.BLACK_STAINED_GLASS_PANE);
        if (!fillerMaterial.isAir()) {
            final ItemStack filler = item(fillerMaterial, " ", List.of());
            for (int slot = 0; slot < size; slot++) {
                gui.setItem(slot, new GuiItem(filler));
            }
        }

        for (ClaimShopMenuConfig.BundleItem bundle : config.getBundles()) {
            setActionItem(bundle.getSlot(), bundleItemStack(bundle));
        }

        setItemAt(slotOf(config.getInfoItem(), INFO_SLOT), new GuiItem(infoItemStack()));
        setActionItem(slotOf(config.getCustomAmountItem(), CUSTOM_AMOUNT_SLOT), defItemStack(config.getCustomAmountItem()));
        setActionItem(slotOf(config.getConfirmItem(), CONFIRM_SLOT), defItemStack(config.getConfirmItem()));
        setActionItem(slotOf(config.getCancelItem(), CANCEL_SLOT), defItemStack(config.getCancelItem()));
        gui.update();
    }

    private void setItemAt(int slot, @NotNull GuiItem item) {
        if (slot >= 0 && slot < size) {
            gui.setItem(slot, item);
        }
    }

    private void setActionItem(int slot, @NotNull ItemStack item) {
        setItemAt(slot, new GuiItem(item, this::handleClick));
    }

    private ItemStack bundleItemStack(@NotNull ClaimShopMenuConfig.BundleItem bundle) {
        final List<String> lore = bundle.getLore().stream()
                .map(line -> line.replace("%amount%", Long.toString(Math.abs(bundle.getAmount()))))
                .toList();
        return item(resolveMaterial(bundle.getMaterial(), Material.PAPER), bundle.getName(), lore);
    }

    private ItemStack infoItemStack() {
        final double unitPrice = Math.max(0, plugin.getSettings().getHooks().getEconomy().getCostPerBlock());
        final double total = selectedAmount * unitPrice;
        final Optional<EconomyHook> economy = plugin.getHook(EconomyHook.class);
        final String formattedUnit = plainText(economy.map(hook -> hook.format(unitPrice)).orElse(Double.toString(unitPrice)));
        final String formattedTotal = plainText(economy.map(hook -> hook.format(total)).orElse(Double.toString(total)));

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

        // counts down in the action bar and title while the player types
        final int[] remaining = {INPUT_SECONDS};
        player.getScheduler().runAtFixedRate(plugin, task -> {
            if (ACTIVE_INPUTS.get(player.getUniqueId()) != this) {
                task.cancel();
                return;
            }
            if (remaining[0] <= 0) {
                ACTIVE_INPUTS.remove(player.getUniqueId(), this);
                task.cancel();
                locale("claim_shop_input_timeout");
                showTitle("claim_shop_input_timeout_title", "claim_shop_input_timeout_subtitle");
                return;
            }
            final String seconds = Integer.toString(remaining[0]--);
            plugin.getLocales().getLocale("claim_shop_input_actionbar", seconds)
                    .ifPresent(message -> player.sendActionBar(message.toComponent()));
            showTitle("claim_shop_input_title", "claim_shop_input_subtitle", seconds);
        }, null, 1L, 20L);
    }

    private void showTitle(@NotNull String titleKey, @NotNull String subtitleKey, @NotNull String... replacements) {
        final Component title = plugin.getLocales().getLocale(titleKey, replacements)
                .map(MineDown::toComponent).orElse(Component.empty());
        final Component subtitle = plugin.getLocales().getLocale(subtitleKey, replacements)
                .map(MineDown::toComponent).orElse(Component.empty());
        player.showTitle(Title.title(title, subtitle, Title.Times.times(
                Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500))));
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

    static Component text(@NotNull String input) {
        return MiniMessage.miniMessage().deserialize(legacyToMiniMessage(input))
                .decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE);
    }

    // economy plugins colour and embolden their own prices; the menu line decides how the value looks
    private static String plainText(@NotNull String formatted) {
        return SECTION_CODES.matcher(formatted).replaceAll("");
    }

    private static String legacyToMiniMessage(@NotNull String input) {
        final Matcher matcher = LEGACY_CODE.matcher(input);
        final StringBuilder out = new StringBuilder();
        while (matcher.find()) {
            // placeholders such as the unit price arrive with section-sign codes already applied
            final String spread = matcher.group(1);
            final String hex = spread != null ? spread.replaceAll("[&§]", "") : matcher.group(2);
            final String tag;
            if (hex != null) {
                tag = "<reset><#" + hex + ">";
            } else {
                final char code = Character.toLowerCase(matcher.group(3).charAt(0));
                final String named = LEGACY_TAGS.get(code);
                // a colour code clears bold, underline and the rest, a format code adds to them
                tag = named == null ? matcher.group()
                        : (Character.digit(code, 16) >= 0 ? "<reset>" : "") + named;
            }
            matcher.appendReplacement(out, Matcher.quoteReplacement(tag));
        }
        matcher.appendTail(out);
        return out.toString();
    }
}

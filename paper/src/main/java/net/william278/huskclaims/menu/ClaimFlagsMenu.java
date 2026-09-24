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

import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import net.william278.huskclaims.PaperHuskClaims;
import net.william278.huskclaims.claim.Claim;
import net.william278.huskclaims.claim.ClaimWorld;
import net.william278.huskclaims.config.ClaimFlagsMenuConfig;
import net.william278.huskclaims.config.Settings;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * The /claimsettings menu: one item per operation group, showing whether it is on in the claim the player is
 * standing in. A click runs the group's own toggle command, so trust privileges and messages work as usual.
 */
public final class ClaimFlagsMenu {

    private final PaperHuskClaims plugin;
    private final ClaimFlagsMenuConfig config;
    private final Player player;
    private final Gui gui;
    private final int size;
    @Nullable
    private final Claim claim;

    public ClaimFlagsMenu(@NotNull PaperHuskClaims plugin, @NotNull Player player) {
        this.plugin = plugin;
        this.config = plugin.getClaimFlagsMenuConfig();
        this.player = player;
        this.size = Math.max(1, Math.min(6, config.getRows())) * 9;
        this.claim = findClaim();
        this.gui = Gui.gui()
                .title(ClaimShopMenu.text(config.getTitle()))
                .rows(size / 9)
                .disableAllInteractions()
                .create();
        draw();
    }

    public void open() {
        gui.open(player);
    }

    @Nullable
    private Claim findClaim() {
        final var user = plugin.getOnlineUser(player);
        final Optional<ClaimWorld> world = plugin.getClaimWorld(user.getWorld());
        return world.flatMap(w -> w.getClaimAt(user.getPosition())).orElse(null);
    }

    private void draw() {
        gui.clearItems();
        final Material fillerMaterial = material(config.getFillerMaterial(), Material.BLACK_STAINED_GLASS_PANE);
        if (!fillerMaterial.isAir()) {
            final ItemStack filler = item(fillerMaterial, " ", List.of());
            for (int slot = 0; slot < size; slot++) {
                gui.setItem(slot, new GuiItem(filler));
            }
        }

        set(config.getInfoSlot(), new GuiItem(item(config.getInfoItem(), "", "")));
        set(config.getCloseSlot(), new GuiItem(item(config.getCloseItem(), "", ""), event -> player.closeInventory()));

        if (claim == null) {
            set(config.getNoClaimSlot(), new GuiItem(item(config.getNoClaimItem(), "", "")));
        } else {
            for (ClaimFlagsMenuConfig.FlagItem flag : config.getFlags()) {
                drawFlag(flag);
            }
        }
        gui.update();
    }

    private void drawFlag(@NotNull ClaimFlagsMenuConfig.FlagItem flag) {
        final Optional<Settings.OperationGroup> group = findGroup(flag.getGroup());
        if (group.isEmpty() || claim == null || flag.getEnabled() == null || flag.getDisabled() == null) {
            return;
        }
        final Settings.OperationGroup g = group.get();
        final boolean on = claim.getDefaultFlags().containsAll(g.getAllowedOperations());
        final ItemStack stack = item(on ? flag.getEnabled() : flag.getDisabled(), g.getName(), g.getDescription());
        set(flag.getSlot(), new GuiItem(stack, event -> toggle(g, !on)));
    }

    private void toggle(@NotNull Settings.OperationGroup group, boolean enable) {
        if (group.getToggleCommandAliases().isEmpty()) {
            return;
        }
        player.performCommand(group.getToggleCommandAliases().get(0) + (enable ? " on" : " off"));
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        draw();
    }

    private Optional<Settings.OperationGroup> findGroup(String id) {
        if (id == null) {
            return Optional.empty();
        }
        return plugin.getSettings().getOperationGroups().stream()
                .filter(group -> group.getPlaceholderId().equalsIgnoreCase(id.trim()))
                .findFirst();
    }

    private void set(int slot, @NotNull GuiItem item) {
        if (slot >= 0 && slot < size) {
            gui.setItem(slot, item);
        }
    }

    private static Material material(@NotNull String name, @NotNull Material fallback) {
        final Material material = Material.matchMaterial(name);
        return material != null ? material : fallback;
    }

    private static ItemStack item(@NotNull ClaimFlagsMenuConfig.ItemDef def, @NotNull String groupName,
                                  @NotNull String groupDescription) {
        final List<String> lore = def.getLore().stream()
                .map(line -> line
                        .replace("%group_name%", groupName)
                        .replace("%group_description%", groupDescription))
                .toList();
        return item(material(def.getMaterial(), Material.BARRIER),
                def.getName().replace("%group_name%", groupName).replace("%group_description%", groupDescription),
                lore);
    }

    private static ItemStack item(@NotNull Material material, @NotNull String name, @NotNull List<String> lore) {
        final ItemStack stack = new ItemStack(material);
        final ItemMeta meta = stack.getItemMeta();
        meta.displayName(ClaimShopMenu.text(name));
        meta.lore(lore.stream().map(ClaimShopMenu::text).toList());
        meta.addItemFlags(ItemFlag.values());
        stack.setItemMeta(meta);
        return stack;
    }

}

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
import net.william278.huskclaims.trust.TrustLevel;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * The /claimflags menu: one item per operation group, showing whether it is on in the claim the player is
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
            drawFlags();
        }
        gui.update();
    }

    /** Every operation group gets an item: the one from the menu file, else the built-in one, else a plain one. */
    private void drawFlags() {
        final List<Settings.OperationGroup> groups = groups();
        final Set<Integer> taken = new HashSet<>();
        taken.add(config.getInfoSlot());
        taken.add(config.getCloseSlot());
        final List<ClaimFlagsMenuConfig.FlagItem> placed = new ArrayList<>();
        final List<Settings.OperationGroup> unplaced = new ArrayList<>();
        for (Settings.OperationGroup group : groups) {
            final ClaimFlagsMenuConfig.FlagItem flag = flagFor(group);
            if (flag.getSlot() >= 0 && flag.getSlot() < size && taken.add(flag.getSlot())) {
                placed.add(flag);
            } else {
                unplaced.add(group);
            }
        }
        for (ClaimFlagsMenuConfig.FlagItem flag : placed) {
            drawFlag(flag, findGroup(groups, flag.getGroup()));
        }
        int next = 0;
        for (Settings.OperationGroup group : unplaced) {
            // The first free slot inside the menu, not the first or last column
            while (next < size && (taken.contains(next) || next % 9 == 0 || next % 9 == 8)) {
                next++;
            }
            if (next >= size) {
                return;
            }
            taken.add(next);
            final ClaimFlagsMenuConfig.FlagItem flag = flagFor(group);
            drawFlag(ClaimFlagsMenuConfig.FlagItem.builder().group(flag.getGroup()).slot(next)
                    .enabled(flag.getEnabled()).disabled(flag.getDisabled()).build(), Optional.of(group));
        }
    }

    /** The groups from config.yml, plus built-in ones an older config.yml does not have. */
    private List<Settings.OperationGroup> groups() {
        final List<Settings.OperationGroup> groups = new ArrayList<>(plugin.getSettings().getOperationGroups());
        for (Settings.OperationGroup builtIn : Settings.defaultOperationGroups()) {
            if (findGroup(groups, builtIn.getPlaceholderId()).isEmpty()) {
                groups.add(builtIn);
            }
        }
        return groups;
    }

    @NotNull
    private ClaimFlagsMenuConfig.FlagItem flagFor(@NotNull Settings.OperationGroup group) {
        final String id = group.getPlaceholderId();
        for (ClaimFlagsMenuConfig.FlagItem flag : config.getFlags()) {
            if (flag.getGroup() != null && flag.getGroup().trim().equalsIgnoreCase(id)
                    && flag.getEnabled() != null && flag.getDisabled() != null) {
                return flag;
            }
        }
        for (ClaimFlagsMenuConfig.FlagItem flag : ClaimFlagsMenuConfig.defaultFlags()) {
            if (flag.getGroup().equals(id)) {
                return ClaimFlagsMenuConfig.FlagItem.builder().group(id).slot(-1)
                        .enabled(flag.getEnabled()).disabled(flag.getDisabled()).build();
            }
        }
        return ClaimFlagsMenuConfig.flag(id, -1, "LIME_DYE", "GRAY_DYE", "%group_name%", "<white>%group_description%");
    }

    private void drawFlag(@NotNull ClaimFlagsMenuConfig.FlagItem flag, @NotNull Optional<Settings.OperationGroup> group) {
        if (group.isEmpty() || claim == null || flag.getEnabled() == null || flag.getDisabled() == null) {
            return;
        }
        final Settings.OperationGroup g = group.get();
        final boolean on = claim.getDefaultFlags().containsAll(g.getAllowedOperations());
        final ItemStack stack = item(on ? flag.getEnabled() : flag.getDisabled(), g.getName(), g.getDescription());
        set(flag.getSlot(), new GuiItem(stack, event -> toggle(g, !on)));
    }

    /** Switches the group in the claim the menu was opened for, then redraws so the item shows the new state. */
    private void toggle(@NotNull Settings.OperationGroup group, boolean enable) {
        final var user = plugin.getOnlineUser(player);
        final Optional<ClaimWorld> world = plugin.getClaimWorld(user.getWorld());
        if (claim == null || world.isEmpty()) {
            return;
        }
        if (!claim.isPrivilegeAllowed(TrustLevel.Privilege.MANAGE_OPERATION_GROUPS, user, plugin)
                && !player.hasPermission("huskclaims.command.claimflags.other")) {
            plugin.getLocales().getLocale("no_claim_privilege").ifPresent(user::sendMessage);
            return;
        }
        if (enable) {
            claim.getDefaultFlags().addAll(group.getAllowedOperations());
        } else {
            group.getAllowedOperations().forEach(claim.getDefaultFlags()::remove);
        }
        plugin.getLocales().getLocale(enable ? "enabled_operation_group" : "disabled_operation_group", group.getName())
                .ifPresent(user::sendMessage);
        plugin.runQueued(() -> plugin.getDatabase().updateClaimWorld(world.get()));
        player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
        draw();
    }

    private static Optional<Settings.OperationGroup> findGroup(@NotNull List<Settings.OperationGroup> groups, String id) {
        if (id == null) {
            return Optional.empty();
        }
        return groups.stream()
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

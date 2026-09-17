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

package net.william278.huskclaims.listener;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.william278.huskclaims.BukkitHuskClaims;
import net.william278.huskclaims.menu.ClaimShopMenu;
import net.william278.huskclaims.moderation.SignWrite;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.sign.Side;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class PaperListener extends BukkitListener {

    public PaperListener(@NotNull BukkitHuskClaims plugin) {
        super(plugin);
    }

    @Override
    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        setInspectorCallbacks();
    }

    @EventHandler
    public void onSignEdit(@NotNull SignChangeEvent e) {
        filterSign(
                handleSignEdit(SignWrite.create(
                        getPlugin().getOnlineUser(e.getPlayer()),
                        BukkitHuskClaims.Adapter.adapt(e.getBlock().getLocation()),
                        e.getSide() == Side.FRONT ? SignWrite.Type.SIGN_EDIT_FRONT : SignWrite.Type.SIGN_EDIT_BACK,
                        e.lines(),
                        plugin.getServerName()
                )), e
        );
    }

    @EventHandler(ignoreCancelled = true)
    public void onClaimShopChat(@NotNull AsyncChatEvent e) {
        final ClaimShopMenu menu = ClaimShopMenu.getActiveInput(e.getPlayer().getUniqueId());
        if (menu == null) {
            return;
        }
        e.setCancelled(true);
        final String input = PlainTextComponentSerializer.plainText().serialize(e.message());
        e.getPlayer().getScheduler().run(plugin, task -> menu.acceptInput(input), null);
    }

    @EventHandler
    public void onClaimShopQuit(@NotNull PlayerQuitEvent e) {
        ClaimShopMenu.clearActiveInput(e.getPlayer().getUniqueId());
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onWindChargeHit(@NotNull ProjectileHitEvent e) {
        if (e.getEntityType() != EntityType.WIND_CHARGE) {
            return;
        }
        final Location origin = e.getEntity().getLocation();
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    final Block block = origin.getWorld().getBlockAt(
                            origin.getBlockX() + x, origin.getBlockY() + y, origin.getBlockZ() + z);
                    if (Tag.BUTTONS.isTagged(block.getType())
                            && plugin.getClaimAt(BukkitHuskClaims.Adapter.adapt(block.getLocation())).isPresent()) {
                        pressButton(block);
                    }
                }
            }
        }
    }

    private void pressButton(@NotNull Block block) {
        if (!(block.getBlockData() instanceof Powerable powerable) || powerable.isPowered()) {
            return;
        }
        powerable.setPowered(true);
        block.setBlockData(powerable, true);

        final long resetDelay = block.getType() == Material.STONE_BUTTON
                || block.getType() == Material.POLISHED_BLACKSTONE_BUTTON ? 20L : 30L;
        plugin.getServer().getRegionScheduler().runDelayed(plugin, block.getLocation(), task -> {
            final Block current = block.getWorld().getBlockAt(block.getX(), block.getY(), block.getZ());
            if (current.getBlockData() instanceof Powerable currentState && currentState.isPowered()) {
                currentState.setPowered(false);
                current.setBlockData(currentState, true);
            }
        }, resetDelay);
    }

    // Apply filter edits to a sign if needed
    private void filterSign(@NotNull SignWrite write, @NotNull SignChangeEvent e) {
        if (write.getFilteredText() == null) {
            return;
        }
        for (int l = 0; l < e.lines().size(); l++) {
            e.line(l, Component.text(
                    write.getFilteredText().get(l),
                    Objects.requireNonNull(e.line(l)).color()
            ));
        }
    }

}

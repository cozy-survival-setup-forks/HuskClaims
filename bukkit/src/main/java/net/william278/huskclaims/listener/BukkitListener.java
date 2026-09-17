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

import lombok.Getter;
import net.william278.cloplib.listener.BukkitOperationListener;
import net.william278.cloplib.operation.Operation;
import net.william278.cloplib.operation.OperationPosition;
import net.william278.cloplib.operation.OperationType;
import net.william278.cloplib.operation.OperationUser;
import net.william278.huskclaims.BukkitHuskClaims;
import net.william278.huskclaims.claim.ClaimOperationTypes;
import net.william278.huskclaims.moderation.SignListener;
import net.william278.huskclaims.position.Position;
import net.william278.huskclaims.position.World;
import net.william278.huskclaims.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Hanging;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Tameable;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockFromToEvent;
import org.bukkit.event.block.BlockIgniteEvent;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityInteractEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.vehicle.VehicleMoveEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

@Getter
public class BukkitListener extends BukkitOperationListener implements BukkitPetListener, BukkitDropsListener,
        ClaimsListener, UserListener, SignListener {

    protected final BukkitHuskClaims plugin;

    public BukkitListener(@NotNull BukkitHuskClaims plugin) {
        super(plugin, plugin);
        this.plugin = plugin;
    }

    @Override
    public void register() {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        setInspectorCallbacks();
    }

    @EventHandler
    public void onPlayerJoin(@NotNull PlayerJoinEvent e) {
        this.onUserJoin(plugin.getOnlineUser(e.getPlayer()));
    }

    @EventHandler
    public void onPlayerQuit(@NotNull PlayerQuitEvent e) {
        this.onUserQuit(plugin.getOnlineUser(e.getPlayer()));
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onPlayerSwitchHeldItem(@NotNull PlayerItemHeldEvent e) {
        final ItemStack mainHand = e.getPlayer().getInventory().getItem(e.getNewSlot());
        final ItemStack offHand = e.getPlayer().getInventory().getItemInOffHand();
        this.onUserSwitchHeldItem(
                plugin.getOnlineUser(e.getPlayer()),
                (mainHand != null ? mainHand.getType() : Material.AIR).getKey().toString(),
                offHand.getType().getKey().toString()
        );
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onUserSwapHands(@NotNull PlayerSwapHandItemsEvent e) {
        final ItemStack mainHand = e.getMainHandItem();
        final ItemStack offHand = e.getOffHandItem();
        this.onUserSwitchHeldItem(
                plugin.getOnlineUser(e.getPlayer()),
                (mainHand != null ? mainHand.getType() : Material.AIR).getKey().toString(),
                (offHand != null ? offHand.getType() : Material.AIR).getKey().toString()
        );
    }

    @EventHandler(ignoreCancelled = true)
    public void onUserTeleport(@NotNull PlayerTeleportEvent e) {
        if (e.getTo() != null && getPlugin().cancelMovement(
                plugin.getOnlineUser(e.getPlayer()),
                BukkitHuskClaims.Adapter.adapt(e.getFrom()),
                BukkitHuskClaims.Adapter.adapt(e.getTo())
        )) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onEndCrystalExplode(@NotNull EntityExplodeEvent e) {
        if (!e.getEntity().getType().getKey().getKey().equals("end_crystal")) {
            return;
        }
        e.blockList().removeIf(block -> plugin.cancelOperation(
                net.william278.cloplib.operation.Operation.of(
                        net.william278.cloplib.operation.OperationType.EXPLOSION_DAMAGE_TERRAIN,
                        getPosition(block.getLocation())
                )
        ));
    }

    @Override
    @EventHandler(ignoreCancelled = true)
    public void onVehicleMove(@NotNull VehicleMoveEvent e) {
        final Location from = e.getFrom();
        final Location to = e.getTo();
        if (from.getBlock().equals(to.getBlock()) && from.distance(to) < 0.1d) {
            return;
        }
        final Vehicle vehicle = e.getVehicle();
        final Optional<Player> blocked = vehicle.getPassengers().stream()
                .filter(Player.class::isInstance)
                .map(Player.class::cast)
                .filter(player -> !isPlayerNpc(player))
                .filter(player -> plugin.cancelMovement(
                        plugin.getOnlineUser(player), getPosition(from), getPosition(to)))
                .findFirst();
        if (blocked.isEmpty()) {
            return;
        }
        vehicle.getPassengers().stream()
                .filter(Player.class::isInstance)
                .map(Player.class::cast)
                .forEach(Player::leaveVehicle);
        vehicle.teleport(from);
    }

    @Override
    @EventHandler(ignoreCancelled = true)
    public void onPlayerInteractEntity(@NotNull PlayerInteractEntityEvent e) {
        if (!isDisplayEntity(e.getRightClicked())) {
            super.onPlayerInteractEntity(e);
            return;
        }
        if (cancelDisplayEdit(e.getPlayer(), e.getRightClicked(), OperationType.ENTITY_INTERACT)) {
            e.setCancelled(true);
        }
    }

    @Override
    @EventHandler(ignoreCancelled = true)
    public void onPlayerArmorStand(@NotNull PlayerArmorStandManipulateEvent e) {
        if (cancelDisplayEdit(e.getPlayer(), e.getRightClicked(), OperationType.ENTITY_INTERACT)) {
            e.setCancelled(true);
        }
    }

    @Override
    @EventHandler(ignoreCancelled = true)
    public void onEntityDamageEntity(@NotNull EntityDamageByEntityEvent e) {
        final Entity target = e.getEntity();
        final Entity damager = e.getDamager();
        final Entity source = damager instanceof Projectile projectile
                && projectile.getShooter() instanceof Entity shooter ? shooter : damager;
        if (source instanceof Mob && target instanceof Mob) {
            return;
        }
        final Optional<Player> player = getPlayerSource(damager);
        if (player.isPresent() && isDisplayEntity(target)) {
            if (cancelDisplayEdit(player.get(), target, OperationType.PLAYER_DAMAGE_PERSISTENT_ENTITY)) {
                e.setCancelled(true);
            }
            return;
        }
        super.onEntityDamageEntity(e);
    }

    @Override
    @EventHandler(ignoreCancelled = true)
    public void onPlayerBreakHangingEntity(@NotNull HangingBreakByEntityEvent e) {
        final Optional<Player> player = getPlayerSource(e.getRemover());
        if (player.isEmpty()) {
            super.onPlayerBreakHangingEntity(e);
            return;
        }
        if (cancelDisplayEdit(player.get(), e.getEntity(), OperationType.BREAK_HANGING_ENTITY)) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerPickupItem(@NotNull EntityPickupItemEvent e) {
        if (!(e.getEntity() instanceof Player player)) {
            return;
        }
        final Item item = e.getItem();
        if (player.getUniqueId().equals(item.getOwner()) || player.getUniqueId().equals(item.getThrower())) {
            return;
        }
        final Position position = BukkitHuskClaims.Adapter.adapt(item.getLocation());
        final boolean customDenied = plugin.cancelOperation(Operation.of(
                plugin.getOnlineUser(player), ClaimOperationTypes.ITEM_PICKUP, position));
        final boolean legacyDenied = plugin.cancelOperation(Operation.of(
                plugin.getOnlineUser(player), OperationType.ENTITY_INTERACT, position));
        if (customDenied && legacyDenied) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onClaimedArmorStandExplosion(@NotNull EntityDamageEvent e) {
        if (!(e.getEntity() instanceof ArmorStand)
                || (e.getCause() != EntityDamageEvent.DamageCause.ENTITY_EXPLOSION
                && e.getCause() != EntityDamageEvent.DamageCause.BLOCK_EXPLOSION)) {
            return;
        }
        if (plugin.getClaimAt(BukkitHuskClaims.Adapter.adapt(e.getEntity().getLocation())).isPresent()) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onClaimedPressurePlate(@NotNull EntityInteractEvent e) {
        if (!(e.getEntity() instanceof Item item) || !Tag.PRESSURE_PLATES.isTagged(e.getBlock().getType())) {
            return;
        }
        final Position position = BukkitHuskClaims.Adapter.adapt(e.getBlock().getLocation());
        if (plugin.getClaimAt(position).isEmpty()) {
            return;
        }
        final Player thrower = Optional.ofNullable(item.getThrower()).map(Bukkit::getPlayer).orElse(null);
        if (thrower == null || plugin.cancelOperation(Operation.of(
                plugin.getOnlineUser(thrower), OperationType.BLOCK_INTERACT, position))) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClaimedDragonEggInteract(@NotNull PlayerInteractEvent e) {
        if ((e.getAction() != Action.RIGHT_CLICK_BLOCK && e.getAction() != Action.LEFT_CLICK_BLOCK)
                || e.getClickedBlock() == null || e.getClickedBlock().getType() != Material.DRAGON_EGG) {
            return;
        }
        final Position position = BukkitHuskClaims.Adapter.adapt(e.getClickedBlock().getLocation());
        if (plugin.getClaimAt(position).isPresent() && plugin.cancelOperation(Operation.of(
                plugin.getOnlineUser(e.getPlayer()), OperationType.BLOCK_INTERACT, position))) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClaimedDragonEggTeleport(@NotNull EntityChangeBlockEvent e) {
        if (e.getBlock().getType() == Material.DRAGON_EGG && isClaimed(e.getBlock())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onClaimedDragonEggFlow(@NotNull BlockFromToEvent e) {
        if (e.getBlock().getType() == Material.DRAGON_EGG && isClaimed(e.getBlock())) {
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onClaimedCampfireIgnite(@NotNull BlockIgniteEvent e) {
        final Block block = e.getBlock();
        if (!Tag.CAMPFIRES.isTagged(block.getType()) || !isClaimed(block)) {
            return;
        }
        final Entity igniter = e.getIgnitingEntity();
        final Player player = igniter instanceof Player direct ? direct
                : igniter instanceof Projectile projectile && projectile.getShooter() instanceof Player shooter
                ? shooter : null;
        final Position position = BukkitHuskClaims.Adapter.adapt(block.getLocation());
        if (player == null || plugin.cancelOperation(Operation.of(
                plugin.getOnlineUser(player), OperationType.BLOCK_PLACE, position))) {
            e.setCancelled(true);
        }
    }

    private boolean isClaimed(@NotNull Block block) {
        return plugin.getClaimAt(BukkitHuskClaims.Adapter.adapt(block.getLocation())).isPresent();
    }

    private boolean cancelDisplayEdit(@NotNull Player player, @NotNull Entity entity,
                                      @NotNull OperationType fallback) {
        final Position position = BukkitHuskClaims.Adapter.adapt(entity.getLocation());
        final boolean customDenied = plugin.cancelOperation(Operation.of(
                plugin.getOnlineUser(player), ClaimOperationTypes.DISPLAY_ENTITY_EDIT, position));
        final boolean fallbackDenied = plugin.cancelOperation(Operation.of(
                plugin.getOnlineUser(player), fallback, position));
        return customDenied && fallbackDenied;
    }

    private boolean isDisplayEntity(@NotNull Entity entity) {
        return entity instanceof ArmorStand || entity instanceof Hanging;
    }
        
    @EventHandler(ignoreCancelled = true)
    public void onWorldLoad(@NotNull WorldLoadEvent e) {
        plugin.runAsync(() -> {
            final World world = BukkitHuskClaims.Adapter.adapt(e.getWorld());
            plugin.loadClaimWorld(world);
            plugin.getClaimWorld(world).ifPresent(loaded -> plugin.getMapHooks().forEach(
                    hook -> hook.markClaims(loaded.getClaims(), loaded))
            );
        });
    }

    @Override
    public void onUserTamedEntityAction(@NotNull Cancellable event, @Nullable Entity player, @NotNull Entity entity) {
        // If pets are enabled, check if the entity is tamed
        if (player == null || !getPlugin().getSettings().getPets().isEnabled() || !(entity instanceof Tameable tamed)) {
            return;
        }

        // Check it was damaged by a player
        final Optional<Player> source = getPlayerSource(player);
        final Optional<User> owner = getPlugin().getPetOwner(tamed);
        if (source.isEmpty() || owner.isEmpty()) {
            return;
        }

        // Don't cancel the event if there's no mismatch
        if (getPlugin().cancelPetOperation(plugin.getOnlineUser(source.get()), owner.get())) {
            event.setCancelled(true);
        }
    }

    @Override
    @NotNull
    public OperationPosition getPosition(@NotNull Location location) {
        return BukkitHuskClaims.Adapter.adapt(location);
    }

    @Override
    @NotNull
    public OperationUser getUser(@NotNull Player player) {
        return plugin.getOnlineUser(player);
    }

    @Override
    public void setInspectionDistance(int i) {
        throw new UnsupportedOperationException("Cannot change inspection distance");
    }

}

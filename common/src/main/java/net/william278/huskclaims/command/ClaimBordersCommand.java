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

package net.william278.huskclaims.command;

import com.google.common.collect.Lists;
import net.william278.huskclaims.HuskClaims;
import net.william278.huskclaims.claim.Claim;
import net.william278.huskclaims.claim.ClaimWorld;
import net.william278.huskclaims.claim.Region;
import net.william278.huskclaims.user.OnlineUser;
import net.william278.huskclaims.util.Task;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Shows the borders of the claims around a player until they turn it off, using the same highlights as the
 * inspection tool. It follows the player as they walk and changes world.
 */
public class ClaimBordersCommand extends OnlineUserCommand implements ToggleTabCompletable {

    private final Set<UUID> viewers = ConcurrentHashMap.newKeySet();
    private Task.Repeating task;

    public ClaimBordersCommand(@NotNull HuskClaims plugin) {
        super(List.of("claimborders", "claimborder"), "[on|off]", plugin);
    }

    @Override
    public void execute(@NotNull OnlineUser user, @NotNull String[] args) {
        final boolean show = parseBooleanArg(args, 0).orElse(!viewers.contains(user.getUuid()));
        if (!show) {
            viewers.remove(user.getUuid());
            plugin.getHighlighter(user).stopHighlighting(user);
            plugin.getLocales().getLocale("claim_borders_off").ifPresent(user::sendMessage);
            return;
        }

        if (plugin.getClaimWorld(user.getWorld()).isEmpty()) {
            plugin.getLocales().getLocale("world_not_claimable").ifPresent(user::sendMessage);
            return;
        }
        viewers.add(user.getUuid());
        plugin.getLocales().getLocale("claim_borders_on", Integer.toString(distance())).ifPresent(user::sendMessage);
        refresh(user);
        start();
    }

    private int distance() {
        return plugin.getSettings().getClaims().getBorderViewDistance();
    }

    private synchronized void start() {
        if (task != null) {
            return;
        }
        final Duration every = Duration.ofSeconds(Math.max(1, plugin.getSettings().getClaims().getBorderViewRefreshSeconds()));
        task = plugin.getRepeatingTask(this::refreshAll, every, every);
        task.run();
    }

    private synchronized void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    private void refreshAll() {
        if (viewers.isEmpty()) {
            stop();
            return;
        }
        final Set<UUID> online = ConcurrentHashMap.newKeySet();
        for (OnlineUser user : plugin.getOnlineUsers()) {
            if (!viewers.contains(user.getUuid())) {
                continue;
            }
            online.add(user.getUuid());
            try {
                refresh(user);
            } catch (RuntimeException e) {
                plugin.log(java.util.logging.Level.WARNING, "Could not show claim borders to " + user.getName() + ": " + e);
            }
        }
        viewers.retainAll(online);
    }

    // The highlighter does nothing when what it would show has not changed since the last time
    private void refresh(@NotNull OnlineUser user) {
        final Optional<ClaimWorld> world = plugin.getClaimWorld(user.getWorld());
        if (world.isEmpty()) {
            plugin.getHighlighter(user).stopHighlighting(user);
            return;
        }

        final List<Claim> claims = Lists.newArrayList(
                world.get().getParentClaimsOverlapping(Region.around(user.getPosition(), distance())));
        if (claims.isEmpty()) {
            plugin.getHighlighter(user).stopHighlighting(user);
            return;
        }
        for (Claim claim : List.copyOf(claims)) {
            claims.addAll(claim.getChildren());
        }
        plugin.getHighlighter(user).startHighlighting(user, user.getWorld(), claims);
    }

}

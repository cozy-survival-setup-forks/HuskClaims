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

import net.william278.huskclaims.HuskClaims;
import net.william278.huskclaims.claim.Claim;
import net.william278.huskclaims.claim.ClaimWorld;
import net.william278.huskclaims.trust.TrustLevel;
import net.william278.huskclaims.user.CommandUser;
import net.william278.huskclaims.user.OnlineUser;
import net.william278.huskclaims.user.User;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimedTrustCommand extends InClaimCommand implements UserListTabCompletable {

    private static final Pattern DURATION = Pattern.compile("^(\\d+)([smhdw])$", Pattern.CASE_INSENSITIVE);
    private static final Duration MAX_DURATION = Duration.ofDays(30);

    protected TimedTrustCommand(@NotNull HuskClaims plugin) {
        super(
                List.of("timedtrust", "temptrust"),
                "<player> <duration>",
                TrustLevel.Privilege.MANAGE_TRUSTEES,
                plugin
        );
    }

    @Override
    public void execute(@NotNull OnlineUser executor, @NotNull ClaimWorld world,
                        @NotNull Claim claim, @NotNull String[] args) {
        if (args.length != 2) {
            plugin.getLocales().getLocale("error_invalid_syntax", getUsage()).ifPresent(executor::sendMessage);
            return;
        }
        final Optional<Duration> duration = parseDuration(args[1]);
        if (duration.isEmpty()) {
            plugin.getLocales().getLocale("timed_trust_invalid_duration").ifPresent(executor::sendMessage);
            return;
        }
        resolveUser(executor, args[0]).ifPresent(user -> setTrust(executor, user, duration.get(), world, claim));
    }

    private void setTrust(@NotNull OnlineUser executor, @NotNull User user, @NotNull Duration duration,
                          @NotNull ClaimWorld world, @NotNull Claim claim) {
        if (user.getUuid().equals(executor.getUuid())) {
            plugin.getLocales().getLocale("timed_trust_self").ifPresent(executor::sendMessage);
            return;
        }
        if (!checkUserHasAccess(executor, user, claim)) {
            return;
        }
        if (claim.getTrustedUsers().containsKey(user.getUuid())) {
            plugin.getLocales().getLocale("timed_trust_already_permanent", user.getName())
                    .ifPresent(executor::sendMessage);
            return;
        }
        final Optional<TrustLevel> build = plugin.getTrustLevel("build");
        if (build.isEmpty()) {
            plugin.getLocales().getLocale("timed_trust_unavailable").ifPresent(executor::sendMessage);
            return;
        }
        plugin.fireTrustEvent(executor, build.get(), user, claim, world, event -> {
            claim.setTimedTrust(user.getUuid(), build.get(), Instant.now().plus(duration));
            world.cacheUser(user);
            plugin.runQueued(() -> plugin.getDatabase().updateClaimWorld(world));
            plugin.invalidateClaimListCache(claim.getOwner().orElse(null));
            plugin.getLocales().getLocale("timed_trust_set", user.getName(), formatDuration(duration))
                    .ifPresent(executor::sendMessage);
        });
    }

    @NotNull
    private Optional<Duration> parseDuration(@NotNull String input) {
        final Matcher matcher = DURATION.matcher(input.trim());
        if (!matcher.matches()) {
            return Optional.empty();
        }
        try {
            final long amount = Long.parseLong(matcher.group(1));
            final Duration duration = switch (matcher.group(2).toLowerCase(Locale.ENGLISH)) {
                case "s" -> Duration.ofSeconds(amount);
                case "m" -> Duration.ofMinutes(amount);
                case "h" -> Duration.ofHours(amount);
                case "d" -> Duration.ofDays(amount);
                case "w" -> Duration.ofDays(Math.multiplyExact(amount, 7));
                default -> Duration.ZERO;
            };
            return duration.isZero() || duration.isNegative() || duration.compareTo(MAX_DURATION) > 0
                    ? Optional.empty() : Optional.of(duration);
        } catch (ArithmeticException e) {
            return Optional.empty();
        }
    }

    @NotNull
    private String formatDuration(@NotNull Duration duration) {
        if (duration.toDaysPart() > 0 && duration.toHoursPart() == 0) {
            return duration.toDays() + "d";
        }
        if (duration.toHours() > 0 && duration.toMinutesPart() == 0) {
            return duration.toHours() + "h";
        }
        if (duration.toMinutes() > 0 && duration.toSecondsPart() == 0) {
            return duration.toMinutes() + "m";
        }
        return duration.toSeconds() + "s";
    }

    @Nullable
    @Override
    public List<String> suggest(@NotNull CommandUser user, @NotNull String[] args) {
        return args.length <= 1 ? UserListTabCompletable.super.suggest(user, args)
                : List.of("15m", "1h", "6h", "1d", "7d");
    }

}

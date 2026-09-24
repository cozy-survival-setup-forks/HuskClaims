package net.william278.huskclaims.config;

import de.exlll.configlib.YamlConfigurationProperties;
import de.exlll.configlib.YamlConfigurations;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ClaimFlagsMenuConfigTests {

    @Test
    void defaultMenuIsWrittenAndReadBack(@TempDir Path dir) throws Exception {
        final Path file = dir.resolve("claim_flags_menu.yml");
        final YamlConfigurationProperties properties = YamlConfigurationProperties.newBuilder().build();

        final ClaimFlagsMenuConfig written = YamlConfigurations.update(file, ClaimFlagsMenuConfig.class, properties);
        assertTrue(Files.exists(file));

        final ClaimFlagsMenuConfig read = YamlConfigurations.load(file, ClaimFlagsMenuConfig.class, properties);
        assertEquals(written.getFlags().size(), read.getFlags().size());
        assertEquals(List.of("explosions", "pvp", "monsters", "animals", "fire", "pearls", "raids", "spawn_eggs"),
                read.getFlags().stream().map(ClaimFlagsMenuConfig.FlagItem::getGroup).toList());

        final ClaimFlagsMenuConfig.FlagItem pvp = read.getFlags().get(1);
        assertEquals("DIAMOND_SWORD", pvp.getEnabled().getMaterial());
        assertEquals("WOODEN_SWORD", pvp.getDisabled().getMaterial());
        assertTrue(pvp.getEnabled().getLore().contains("<#95FF7E>✔ Enabled"));
        assertTrue(pvp.getDisabled().getLore().contains("<#FF4361>✘ Disabled"));
    }
}

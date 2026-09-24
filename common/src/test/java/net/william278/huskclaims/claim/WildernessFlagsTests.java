package net.william278.huskclaims.claim;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.william278.cloplib.operation.OperationType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class WildernessFlagsTests {

    private static OperationType register(String key) {
        return OperationType.isRegistered(key)
                ? OperationType.getOrCreate(key) : OperationType.register(OperationType.getOrCreate(key));
    }

    private static ClaimWorld load(String json) {
        final JsonObject object = JsonParser.parseString(json).getAsJsonObject();
        return new ClaimWorldSerializer(null).deserialize(object, ClaimWorld.class, null);
    }

    @Test
    void typesAddedAfterTheWorldWasSavedAreAllowedInTheWilderness() {
        final OperationType added = register("test:added_later");
        final ClaimWorld world = load("{\"claims\":[],\"wilderness_flags\":[]}");

        assertTrue(world.getWildernessFlags().contains(added));
    }

    @Test
    void typesSwitchedOffStayOffOnceTheyWereKnown() {
        final OperationType off = register("test:switched_off");
        final ClaimWorld world = load("{\"claims\":[],\"wilderness_flags\":[],\"known_operation_types\":[\"test:switched_off\"]}");

        assertFalse(world.getWildernessFlags().contains(off));
    }
}

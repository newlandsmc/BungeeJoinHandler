package tv.tirco.bungeejoin.util;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TitleTracker {
    private static List<UUID> justConnected = new ArrayList<>();
    private static List<UUID> justConnectedNew = new ArrayList<>();

    public static List<UUID> getJustConnected() {
        return justConnected;
    }

    public static List<UUID> getJustConnectedNew() {
        return justConnectedNew;
    }
    public static boolean isJustConnected(UUID uuid) {
        return justConnected.contains(uuid);
    }
    public static boolean isJustConnectedNew(UUID uuid) {
        return justConnectedNew.contains(uuid);
    }

}

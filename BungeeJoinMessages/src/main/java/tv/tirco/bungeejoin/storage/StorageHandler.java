package tv.tirco.bungeejoin.storage;

import tv.tirco.bungeejoin.Main;

import java.util.UUID;

public interface StorageHandler {
    void init(Main plugin);

    void close(Main plugin);

    boolean doesPlayerExist(UUID uuid);

    void createPlayer(UUID uuid, String name);

    void setLastJoin(UUID uuid, long time);
}

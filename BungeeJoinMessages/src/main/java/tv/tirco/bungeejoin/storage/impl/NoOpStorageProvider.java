package tv.tirco.bungeejoin.storage.impl;

import tv.tirco.bungeejoin.Main;
import tv.tirco.bungeejoin.storage.StorageHandler;

import java.util.UUID;

public class NoOpStorageProvider implements StorageHandler {
    @Override
    public void init(Main plugin) {

    }

    @Override
    public void close(Main plugin) {

    }

    @Override
    public boolean doesPlayerExist(UUID uuid) {
        return false;
    }

    @Override
    public void createPlayer(UUID uuid, String name) {

    }

    @Override
    public void setLastJoin(UUID uuid, long time) {

    }
}

package com.ble.BleDemo;

import java.util.List;
import java.util.UUID;

/**
 * Created by Tao on 2015/10/26 0026.
 */
public class BleAdvertisedData {
    private List<UUID> mUuids;
    private String mName;

    public BleAdvertisedData(List<UUID> uuids, String name) {
        mUuids = uuids;
        mName = name;
    }

    public List<UUID> getUuids() {
        return mUuids;
    }

    public String getName() {
        return mName;
    }
}

package io;

import process.PCB;

public class DiskDevice extends IODevice{
    public DiskDevice(String name) {
        super(name);
    }

    @Override
    public void startOperation(IORequest request) {
        super.busy = true;

    }
}

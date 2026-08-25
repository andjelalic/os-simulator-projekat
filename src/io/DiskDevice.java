package io;

import process.PCB;

public class DiskDevice extends IODevice{
    public DiskDevice(String name) {
        super(name);
    }

    @Override
    public void startOperation(IOOperation op, PCB p) {
        super.busy = true;

    }
}

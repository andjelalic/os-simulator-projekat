package io;

import process.PCB;

public abstract class IODevice {
    protected String name;
    protected boolean busy;

    public IODevice(String name) {
        this.name = name;
        this.busy = false;
    }

    public String getName(){
        return name;
    }

    public boolean isBusy(){
        return busy;
    }

    public abstract void startOperation(IOOperation op, PCB p);
}

package io;

import process.PCB;

import java.util.Queue;

public abstract class IODevice {
    protected String name;
    protected boolean busy;
    protected Queue<IORequest> queue;

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

    public abstract void startOperation(IORequest request);
}

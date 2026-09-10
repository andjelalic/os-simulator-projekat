package io;

import process.PCB;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public abstract class IODevice {
    private final String name;
    protected boolean busy;

    protected List<IORequest> requests;

    protected IORequest currentRequest;

    public IODevice(String name) {
        this.name = name;
        this.busy = false;
        this.requests = new ArrayList<>();
        this.currentRequest = null;
    }

    public String getName() {
        return name;
    }

    public boolean isBusy() {
        return busy;
    }

    public void startOperation(IORequest request) {
        currentRequest = request;
        busy = true;
    }

    public void finishOperation() {
        currentRequest = null;
        busy = false;
    }

    public void addRequest(IORequest request) {
        requests.add(request);
    }

    public abstract IORequest getNextRequest();

    public IORequest getCurrentRequest() {
        return currentRequest;
    }

    public void setBusy(boolean b) {
        this.busy = b;
    }
}

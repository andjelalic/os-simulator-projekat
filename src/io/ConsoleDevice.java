package io;

import process.PCB;

public class ConsoleDevice extends IODevice{
    public ConsoleDevice(String name) {
        super(name);
    }

    @Override
    public IORequest getNextRequest() {
        return requests.getFirst();
    }
}
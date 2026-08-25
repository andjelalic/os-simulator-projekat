package io;

import process.PCB;

public class ConsoleDevice extends IODevice{
    public ConsoleDevice(String name) {
        super(name);
    }

    @Override
    public void startOperation(IOOperation op, PCB p) {

    }
}

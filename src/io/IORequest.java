package io;

import process.PCB;

public class IORequest {
    private PCB process;
    private IOOperation operation;
    private IODevice device;

    public IORequest(PCB process, IOOperation operation, IODevice device){
        this.operation = operation;
        this.process = process;
        this.device = device;
    }

    public PCB getProcess() {return process;}
    public IOOperation getOperation() {return operation;}
    public IODevice getDevice() {return device;}

}

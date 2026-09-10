package io;

import process.PCB;

public class IORequest {
    private PCB process;
    private IOOperation operation;
    private IODevice device;
    private int position;

    public IORequest(PCB process, IOOperation operation, IODevice device, int position){
        this.operation = operation;
        this.process = process;
        this.device = device;
        this.position = position;
    }

    public IORequest(PCB process, IOOperation operation, IODevice device){
        this(process, operation, device, -1);
    }

    public PCB getProcess() {return process;}
    public IOOperation getOperation() {return operation;}
    public IODevice getDevice() {return device;}
    public int getPosition() {return position;}

}

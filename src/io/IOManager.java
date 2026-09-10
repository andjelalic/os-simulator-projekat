package io;

import process.PCB;
import process.ProcessState;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class IOManager {
    private List<IODevice> devices;

    public IOManager(){
        devices = new ArrayList<>();
    }

    public void addDevice(IODevice d){
        devices.add(d);
    }

    public void removeDevice(IODevice d){
        devices.remove(d);
    }

    public void requestIO(PCB p, String deviceName, IOOperation op){
        IODevice device = null;
        for(IODevice d: devices){
            if(Objects.equals(deviceName, d.getName())){
                device = d;
                break;
            }
        }
        if(device == null)
            throw new IllegalArgumentException("Device '" + deviceName + "' does not exist.");

        IORequest request = new IORequest(p,op, device);
        p.setState(ProcessState.WAITING);

        if(!device.isBusy()){
            device.startOperation(request);
        }else{
            device.addRequest(request);
        }
    }

    public void completeIO(IODevice device){
        IORequest completed = device.getCurrentRequest();

        if (completed == null)
            return;

        PCB process = completed.getProcess();
        process.setWaitingDevice(null);
        process.setState(ProcessState.READY);

        IORequest next = device.getNextRequest();

        if (next != null) {
            device.startOperation(next);
        } else {
            device.finishOperation();
        }
    }
}

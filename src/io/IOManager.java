package io;

import process.PCB;

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

        if(!device.isBusy()){
            device.startOperation(request);
        }else{
            device.queue.add(request);
        }
    }

    public void completeIO(IODevice device){

    }
}

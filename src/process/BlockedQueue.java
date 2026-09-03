package process;

import io.IODevice;

import java.util.ArrayList;
import java.util.List;

public class BlockedQueue {

    private List<PCB> list;

    public BlockedQueue() {
        list = new ArrayList<>();
    }

    public void block(PCB p) {
        p.setState(ProcessState.WAITING);
        list.add(p);
    }

    public void unblock(PCB p) {
        // samo mijenja state procesa, ne dodaje ga nazad u ReadyQueue
        // to je odgovornost kernela kasnije
        list.remove(p);
        p.setState(ProcessState.READY);
    }

    public PCB findByDevice(IODevice d) {
        for (PCB p : list) {
            if (p.getWaitingDevice() == d) {
                return p;
            }
        }
        return null;
    }
}

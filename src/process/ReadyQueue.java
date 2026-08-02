package process;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class ReadyQueue {

    private Queue<PCB> queue;

    public ReadyQueue() {
        queue = new LinkedList<>();
    }

    public void add(PCB p) {
        queue.add(p);
    }

    public PCB removeNext() {
        return queue.poll();
    }

    public boolean isEmpty() {
        return queue.isEmpty();
    }

    public List<PCB> snapshot() {
        return new LinkedList<>(queue);
    }

    public boolean remove(PCB p) {
        return queue.remove(p);
    }
}

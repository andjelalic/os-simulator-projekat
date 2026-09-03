package process;

public class CPU {

    private PCB current;
    private long cycleCount;
    private int stepsInBurst;
    private int quantum;

    public CPU(int quantum) {
        this.quantum = quantum;
        this.cycleCount = 0;
        this.stepsInBurst = 0;
        this.current = null;
    }

    public void executeOneStep() {
        if (isIdle()) {
            return;
        }

        current.setState(ProcessState.RUNNING);
        current.setRemainingTime(current.getRemainingTime() - 1);
        cycleCount++;
        stepsInBurst++;

        if (current.getRemainingTime() <= 0) {
            current.setState(ProcessState.TERMINATED);
        }
    }

    public void contextSwitch(PCB next) {
        current = next;
        stepsInBurst = 0;
    }

    public PCB getCurrent() {
        return current;
    }

    public boolean isQuantumExpired() {
        return stepsInBurst >= quantum;
    }

    public boolean isIdle() {
        return current == null || current.getState() == ProcessState.TERMINATED;
    }
}

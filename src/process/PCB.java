package process;

import filesystem.OpenFileHandle;
import io.IODevice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PCB {

    private int pid;
    private ProcessState state;
    private int priority;
    private int programCounter;
    private Map<String, Integer> registers;
    private int baseAddress;
    private int limit;
    private List<OpenFileHandle> openFiles;
    private int remainingTime;
    private IODevice waitingDevice;
    private boolean isSystemProcess;

    public PCB(int pid, ProcessState state, int priority, int programCounter,
               Map<String, Integer> registers, int baseAddress, int limit,
               List<OpenFileHandle> openFiles, int remainingTime) {
        this.pid = pid;
        this.state = state;
        this.priority = priority;
        this.programCounter = programCounter;
        this.registers = registers;
        this.baseAddress = baseAddress;
        this.limit = limit;
        this.openFiles = openFiles;
        this.remainingTime = remainingTime;
        this.isSystemProcess = false;
    }

    // static factory metoda za kreiranje sistemskih procesa, npr. procesi jezgra osa
    // sistemski proces se pravi sa praznim pcetnim stanjem (new, prioritet 0, prgramCounter 0,
    // prazni registri, bez memorije, bez otvorenih fajlova) i zadatim trajanjem ivzrsavanja
    public static PCB createSystemProcess(int pid, int duration) {
        PCB pcb = new PCB(pid, ProcessState.NEW, 0, 0,
                new HashMap<>(), 0, 0, new ArrayList<>(), duration);
        pcb.setSystemProcess(true);
        return pcb;
    }

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public ProcessState getState() {
        return state;
    }

    public void setState(ProcessState state) {
        this.state = state;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public int getProgramCounter() {
        return programCounter;
    }

    public void setProgramCounter(int programCounter) {
        this.programCounter = programCounter;
    }

    public Map<String, Integer> getRegisters() {
        return registers;
    }

    public void setRegisters(Map<String, Integer> registers) {
        this.registers = registers;
    }

    public int getBaseAddress() {
        return baseAddress;
    }

    public void setBaseAddress(int baseAddress) {
        this.baseAddress = baseAddress;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public List<OpenFileHandle> getOpenFiles() {
        return openFiles;
    }

    public void setOpenFiles(List<OpenFileHandle> openFiles) {
        this.openFiles = openFiles;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(int remainingTime) {
        this.remainingTime = remainingTime;
    }

    public IODevice getWaitingDevice() {
        return waitingDevice;
    }

    public void setWaitingDevice(IODevice waitingDevice) {
        this.waitingDevice = waitingDevice;
    }

    public boolean isSystemProcess() {
        return isSystemProcess;
    }

    public void setSystemProcess(boolean systemProcess) {
        isSystemProcess = systemProcess;
    }

    @Override
    public String toString() {
        return "PCB{" +
                "pid=" + pid +
                ", state=" + state +
                ", priority=" + priority +
                ", programCounter=" + programCounter +
                ", registers=" + registers +
                ", baseAddress=" + baseAddress +
                ", limit=" + limit +
                ", openFiles=" + openFiles +
                ", remainingTime=" + remainingTime +
                ", waitingDevice=" + waitingDevice +
                ", isSystemProcess=" + isSystemProcess +
                '}';
    }
}

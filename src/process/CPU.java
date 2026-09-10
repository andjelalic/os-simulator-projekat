package process;

import assembler.Instruction;

import java.util.List;

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
        cycleCount++;
        stepsInBurst++;

        if (current.getProgram() == null) {
            // staro ponasanje: proces bez ucitanog programa npr. sistemski proces
            // ili postojeci testovi se izvrsava samo odbrojavanjem preostalog vremena
            current.setRemainingTime(current.getRemainingTime() - 1);

            if (current.getRemainingTime() <= 0) {
                current.setState(ProcessState.TERMINATED);
            }
            return;
        }

        List<Instruction> program = current.getProgram();
        int pc = current.getProgramCounter();

        if (pc >= program.size()) {
            current.setState(ProcessState.TERMINATED);
            return;
        }

        Instruction instruction = program.get(pc);
        int acc = current.getRegisters().getOrDefault("ACC", 0);
        int newAcc = acc;
        boolean halted = false;

        switch (instruction.getOpcode()) {
            case LOAD:
                newAcc = instruction.getOperand();
                break;
            case ADD:
                newAcc = acc + instruction.getOperand();
                break;
            case SUB:
                newAcc = acc - instruction.getOperand();
                break;
            case STORE:
                current.storeToLocalMemory(instruction.getOperand(), acc);
                break;
            case PRINT:
                System.out.println("[PID=" + current.getPid() + "] PRINT: " + acc);
                break;
            case HALT:
                current.setState(ProcessState.TERMINATED);
                halted = true;
                break;
        }

        if (!halted) {
            current.getRegisters().put("ACC", newAcc);
        }

        pc++;
        current.setProgramCounter(pc);
        current.setRemainingTime(program.size() - pc);

        if (pc >= program.size() && current.getState() != ProcessState.TERMINATED) {
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

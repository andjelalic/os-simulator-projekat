import process.CPU;
import process.PCB;
import process.ProcessState;
import process.ReadyQueue;
import process.SRTScheduler;

import java.util.ArrayList;
import java.util.HashMap;

public class Main {
    public static void main(String[] args) {
        // Demo test za SRTScheduler

        PCB p1 = new PCB(1, ProcessState.READY, 0, 0,
                new HashMap<>(), 0, 0, new ArrayList<>(), 10);
        PCB p2 = new PCB(2, ProcessState.READY, 0, 0,
                new HashMap<>(), 0, 0, new ArrayList<>(), 3);
        PCB p3 = new PCB(3, ProcessState.READY, 0, 0,
                new HashMap<>(), 0, 0, new ArrayList<>(), 7);

        ReadyQueue readyQueue = new ReadyQueue();
        readyQueue.add(p1);
        readyQueue.add(p2);
        readyQueue.add(p3);

        SRTScheduler scheduler = new SRTScheduler();
        PCB chosen = scheduler.chooseNext(readyQueue);

        System.out.println("Izabran proces: pid=" + chosen.getPid() +
                ", remainingTime=" + chosen.getRemainingTime());

        boolean testPassed = chosen.getRemainingTime() == 3;
        System.out.println(testPassed
                ? "Test PROŠAO: izabran je proces sa najmanjim remainingTime."
                : "Test NIJE PROŠAO: nije izabran proces sa najmanjim remainingTime.");

        // demo test za cpu (quantum, executeOneStep, contextSwitch)
        System.out.println();
        System.out.println("Demo test za CPU");

        CPU cpu = new CPU(3);
        PCB p4 = new PCB(4, ProcessState.READY, 0, 0,
                new HashMap<>(), 0, 0, new ArrayList<>(), 5);

        cpu.contextSwitch(p4);

        for (int i = 1; i <= 4; i++) {
            cpu.executeOneStep();
            System.out.println("Korak " + i +
                    ": cycleCount=" + i +
                    ", remainingTime=" + p4.getRemainingTime() +
                    ", state=" + p4.getState() +
                    ", quantumExpired=" + cpu.isQuantumExpired());
        }
    }
}
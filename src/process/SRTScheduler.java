package process;

import java.util.List;

public class SRTScheduler implements Scheduler {

    public SRTScheduler() {
    }

    // SRT (Shortest Remaining Time): pretražuje sve procese trenutno u ready redu
    // i bira onaj sa najmanjim preostalim vremenom izvršavanja (remainingTime).
    // Izabrani proces se uklanja iz reda i vraća pozivaocu na izvršavanje.
    @Override
    public PCB chooseNext(ReadyQueue ready) {
        List<PCB> processes = ready.snapshot();
        if (processes.isEmpty()) {
            return null;
        }

        PCB shortest = processes.get(0);
        for (PCB p : processes) {
            if (p.getRemainingTime() < shortest.getRemainingTime()) {
                shortest = p;
            }
        }

        ready.remove(shortest);
        return shortest;
    }
}

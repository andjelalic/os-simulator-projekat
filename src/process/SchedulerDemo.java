package process;

import java.util.List;

/**
 * privremena klasa napravljena iskljucivo radi testiranja integracije
 * spu + scheduler + ReadyQueue
 * ovu ulogu, simulaciju, biranje procesa,izvrsavanje ciklusa, stanje procesa preuzece klasa OSKernel, a SchedulerDemo
 * prezuece klasa OSKernel, a SchedulerDemo uklaanjam
 **/
public class SchedulerDemo {

    public static void runSimulation(List<PCB> processes, int quantum) {
        ReadyQueue readyQueue = new ReadyQueue();
        for (PCB p : processes) {
            p.setState(ProcessState.READY);
            readyQueue.add(p);
        }

        SRTScheduler scheduler = new SRTScheduler();
        CPU cpu = new CPU(quantum);

        long cycle = 0;

        // dok god ima posla, procesa u ready redu, ili procesa trenutno na cpu-u koji jos nije zavrsio
        while (!readyQueue.isEmpty()
                || (cpu.getCurrent() != null && cpu.getCurrent().getState() != ProcessState.TERMINATED)) {

            // ako je cpu slobodan, izaberi sljedeci proces po srt
            if (cpu.isIdle()) {
                PCB next = scheduler.chooseNext(readyQueue);
                if (next == null) {
                    // nema vise procesa ni u redu ni na cpu-u kraj simulacije
                    break;
                }
                cpu.contextSwitch(next);
            }

            // izvrsi jedan ciklus, tick,trenutnog procesa
            cpu.executeOneStep();
            cycle++;

            PCB current = cpu.getCurrent();

            // log jednog reda, ciklus, pid, da li je sistemski proces, preostalo vrijeme, stanje
            System.out.println("cycle=" + cycle +
                    " | pid=" + current.getPid() +
                    " | system=" + current.isSystemProcess() +
                    " | remainingTime=" + current.getRemainingTime() +
                    " | state=" + current.getState());

            if (current.getState() == ProcessState.TERMINATED) {
                // proces je zavrsio izvrsavanje
                System.out.println("   -> Proces pid=" + current.getPid() + " je ZAVRŠEN.");
            } else if (cpu.isQuantumExpired()) {
                // kvantum je istekao, a proces nije zavrsio,  vracamo ga u ready red
                // i oslobadjamo CPU (contextSwitch(null)) da sljedeca iteracija ponovo bira proces po srt
                System.out.println("   -> Kvantum istekao za pid=" + current.getPid() + ", vraćam u ready red.");
                current.setState(ProcessState.READY);
                readyQueue.add(current);
                cpu.contextSwitch(null);
            }
        }
    }
}

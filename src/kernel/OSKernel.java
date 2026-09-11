package kernel;

import assembler.Assembler;
import assembler.Instruction;
import filesystem.FileSystem;
import io.IOManager;
import memory.MemoryManager;
import process.BlockedQueue;
import process.CPU;
import process.PCB;
import process.ProcessState;
import process.ReadyQueue;
import process.SRTScheduler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// centralna klasa koja povezuje sve podsisteme (procese, memoriju, fajlove, IO)
// u jednu cjelinu; svi objekti (readyQueue, cpu, memoryManager itd.) se prave
// spolja i prosljeduju kroz konstruktor, OSKernel ih samo koordinira
public class OSKernel {

    private ReadyQueue readyQueue;
    private BlockedQueue blockedQueue;
    private CPU cpu;
    private SRTScheduler scheduler;
    private MemoryManager memoryManager;
    private FileSystem fileSystem;
    private IOManager ioManager;
    private List<PCB> allProcesses;
    private int nextPid;

    private volatile boolean running = false;
    private Thread schedulerThread;

    public OSKernel(ReadyQueue readyQueue, BlockedQueue blockedQueue, CPU cpu,
                     SRTScheduler scheduler, MemoryManager memoryManager,
                     FileSystem fileSystem, IOManager ioManager) {
        this.readyQueue = readyQueue;
        this.blockedQueue = blockedQueue;
        this.cpu = cpu;
        this.scheduler = scheduler;
        this.memoryManager = memoryManager;
        this.fileSystem = fileSystem;
        this.ioManager = ioManager;
        this.allProcesses = new ArrayList<>();
        this.nextPid = 1;
    }

    // asemblira dati kod, alocira memoriju i pravi novi proces spreman za izvrsavanje
    public String createProcess(String programCode, int memorySize) {
        List<Instruction> instructions;
        try {
            instructions = Assembler.parse(programCode);
        } catch (IllegalArgumentException e) {
            return "Greska pri asembliranju programa: " + e.getMessage();
        }

        PCB pcb = new PCB(nextPid++, ProcessState.NEW, 0, 0,
                new HashMap<>(), 0, 0, new ArrayList<>(), 0);

        if (!memoryManager.allocate(pcb, memorySize)) {
            return "Nema dovoljno memorije za proces";
        }

        pcb.loadProgram(instructions);
        pcb.setState(ProcessState.READY);

        readyQueue.add(pcb);
        allProcesses.add(pcb);

        return "Proces uspjesno kreiran, pid=" + pcb.getPid();
    }

    // jedan otkucaj sistema, ako je CPU slobodan bira se sljedeci proces iz
    // readyQueue-a SRT seduler, izvrsi se jedan korak, pa se provjerava
    // da li je proces zavrsio (oslobadja se memorija) ili mu je istekao kvantum
    // (vraca se u readyQueue da ceka na red ponovo)
    public void tick() {
        if (cpu.isIdle()) {
            if (readyQueue.isEmpty()) {
                return; // nema procesa za izvrsavanje ovaj tick
            }
            PCB next = scheduler.chooseNext(readyQueue);
            cpu.contextSwitch(next);
        }

        cpu.executeOneStep();

        PCB current = cpu.getCurrent();
        if (current == null) {
            return;
        }

        if (current.getState() == ProcessState.TERMINATED) {
            memoryManager.free(current);
        } else if (cpu.isQuantumExpired()) {
            readyQueue.add(current);
            cpu.contextSwitch(null);
        }
    }

    // simulira ucitavanje osnovnih sistemskih procesa (servisa) pri pokretanju
    // sistema (boot), kao spomenuto u uputstvu - kreira nekoliko sistemskih
    // procesa (PCB.createSystemProcess, bez programa, samo se odbrojava
    // remainingTime) i stavlja ih u readyQueue kao da su vec pokrenuti
    public void bootSystemProcesses() {
        PCB system1 = PCB.createSystemProcess(900, 5);
        PCB system2 = PCB.createSystemProcess(901, 8);

        readyQueue.add(system1);
        readyQueue.add(system2);
        allProcesses.add(system1);
        allProcesses.add(system2);
    }

    // pokrece pozadinsku nit (daemon) koja kontinuirano poziva tick() - svakih
    // 50ms izvrsi po jedan korak simulacije, zahvaljujuci tome createProcess/run
    // (pozivi preko CommandInterpreter-a) ODMAH vracaju kontrolu pozivaocu
    // (korisniku), a stvarno izvrsavanje procesa se desava paralelno u pozadini,
    // bez potrebe da se rucno zove tick() u petlji
    public void start() {
        if (running) {
            return; // vec pokrenuto, izbjegavamo duplo pokretanje niti
        }

        running = true;
        schedulerThread = new Thread(() -> {
            while (running) {
                tick();
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        });
        schedulerThread.setDaemon(true); // ne blokira izlazak iz programa
        schedulerThread.start();
    }

    // zaustavlja pozadinsku nit pokrenutu preko start(), postavlja running na
    // false (petlja u niti ce se zavrsiti nakon trenutnog sleep-a/tick-a) i
    // odmah prekida nit (interrupt) da ne cekamo istek trenutnog sleep-a
    public void stop() {
        running = false;
        if (schedulerThread != null) {
            schedulerThread.interrupt();
        }
    }

    // pregled svih ikad kreiranih procesa (pid/state/remainingTime), jedan po redu
    public String listProcesses() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < allProcesses.size(); i++) {
            PCB p = allProcesses.get(i);
            sb.append("pid=").append(p.getPid())
                    .append(", state=").append(p.getState())
                    .append(", remainingTime=").append(p.getRemainingTime());
            if (i < allProcesses.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    // blokira proces sa datim pid-om (prebacuje ga u blockedQueue, stanje WAITING)
    public String blockProcess(int pid) {
        PCB pcb = findByPid(pid);
        if (pcb == null) {
            return "Greska: proces sa pid=" + pid + " ne postoji";
        }

        blockedQueue.block(pcb);
        return "Proces pid=" + pid + " blokiran";
    }

    // deblokira proces sa datim pid-om vraca ga u READY stanje (blockedQueue.unblock)
    // i dodaje ga nazad u readyQueue da bi ponovo mogao biti izabran za izvrsavanje
    public String unblockProcess(int pid) {
        PCB pcb = findByPid(pid);
        if (pcb == null || pcb.getState() != ProcessState.WAITING) {
            return "Greska: proces sa pid=" + pid + " nije blokiran";
        }

        blockedQueue.unblock(pcb);
        readyQueue.add(pcb);
        return "Proces pid=" + pid + " deblokiran";
    }

    private PCB findByPid(int pid) {
        for (PCB p : allProcesses) {
            if (p.getPid() == pid) {
                return p;
            }
        }
        return null;
    }

    public ReadyQueue getReadyQueue() {
        return readyQueue;
    }

    public BlockedQueue getBlockedQueue() {
        return blockedQueue;
    }

    public CPU getCpu() {
        return cpu;
    }

    public SRTScheduler getScheduler() {
        return scheduler;
    }

    public MemoryManager getMemoryManager() {
        return memoryManager;
    }

    public FileSystem getFileSystem() {
        return fileSystem;
    }

    public IOManager getIoManager() {
        return ioManager;
    }

    public List<PCB> getAllProcesses() {
        return allProcesses;
    }
}

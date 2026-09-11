import assembler.Assembler;
import assembler.Instruction;
import cli.CommandInterpreter;
import filesystem.Directory;
import filesystem.FileSystem;
import io.DiskDevice;
import io.IOManager;
import kernel.OSKernel;
import memory.MemoryManager;
import memory.RAM;
import process.BlockedQueue;
import process.CPU;
import process.PCB;
import process.ProcessState;
import process.ReadyQueue;
import process.SRTScheduler;
import process.SchedulerDemo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // test za SRTScheduler

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

        // demo test za SchedulerDemo (integracija CPU + SRTScheduler + ReadyQueue)
        System.out.println();
        System.out.println("Demo test za SchedulerDemo (kvant = 3)");

        PCB user1 = new PCB(1, ProcessState.READY, 0, 0,
                new HashMap<>(), 0, 0, new ArrayList<>(), 6);
        PCB user2 = new PCB(2, ProcessState.READY, 0, 0,
                new HashMap<>(), 0, 0, new ArrayList<>(), 4);
        PCB system1 = PCB.createSystemProcess(100, 2);
        PCB system2 = PCB.createSystemProcess(101, 3);

        List<PCB> allProcesses = Arrays.asList(user1, user2, system1, system2);

        SchedulerDemo.runSimulation(allProcesses, 3);

        // demo test za Assembler
        System.out.println();
        System.out.println("Demo test za Assembler");

        String program = "LOAD 5\nADD 3\nSUB 1\nPRINT\nHALT";
        List<Instruction> parsedInstructions = Assembler.parse(program);

        for (Instruction instruction : parsedInstructions) {
            System.out.println(instruction);
        }

        // test da parsiranje baca gresku na nevalidnom kodu, operand koji nije broj
        System.out.println();
        System.out.println("Test parsiranja nevalidnog koda ('LOAD abc')");

        try {
            Assembler.parse("LOAD abc");
            System.out.println("Test NIJE PROŠAO: očekivana je greška, a nije bačena.");
        } catch (IllegalArgumentException e) {
            System.out.println("Test PROŠAO, uhvaćena očekivana greška: " + e.getMessage());
        }

        // demo test za round trip (parse -> toBinary -> fromBinary)
        System.out.println();
        System.out.println("Demo test za round-trip (toBinary/fromBinary)");

        int[] binary = Assembler.toBinary(parsedInstructions);
        System.out.println("Binarni zapis: " + Arrays.toString(binary));

        List<Instruction> recompressedInstructions = Assembler.fromBinary(binary);
        System.out.println("Vraćene instrukcije:");
        for (Instruction instruction : recompressedInstructions) {
            System.out.println(instruction);
        }

        boolean roundTripPassed = parsedInstructions.size() == recompressedInstructions.size();
        if (roundTripPassed) {
            for (int i = 0; i < parsedInstructions.size(); i++) {
                Instruction original = parsedInstructions.get(i);
                Instruction recompressed = recompressedInstructions.get(i);
                if (original.getOpcode() != recompressed.getOpcode()
                        || original.getOperand() != recompressed.getOperand()) {
                    roundTripPassed = false;
                    break;
                }
            }
        }

        System.out.println(roundTripPassed
                ? "Test prosao"
                : "Test nije prosao");

        // demo test za integraciju asembelra i cpua, korisnicki proces izvrsava
        // asemblirani program preko CPU.executeOneStep()
        System.out.println();
        System.out.println("Demo test za integraciju Assembler + CPU");

        PCB userProcess = new PCB(5, ProcessState.READY, 0, 0,
                new HashMap<>(), 0, 0, new ArrayList<>(), 0);

        String userProgram = "LOAD 10\nADD 5\nSTORE 0\nSUB 3\nPRINT\nHALT";
        List<Instruction> userInstructions = Assembler.parse(userProgram);
        userProcess.loadProgram(userInstructions);

        CPU cpu = new CPU(10);
        cpu.contextSwitch(userProcess);

        while (!cpu.isIdle()) {
            cpu.executeOneStep();
        }

        System.out.println("Vrijednost u lokalnoj memoriji na adresi 0: "
                + userProcess.loadFromLocalMemory(0));

        // demo test za CommandInterpreter (cli paket), sada preko OSKernel-a
        System.out.println();
        System.out.println("Demo test za CommandInterpreter (preko OSKernel-a)");

        RAM cliRam = new RAM(128);
        MemoryManager cliMemoryManager = new MemoryManager(cliRam);
        Directory cliRoot = new Directory("", null);
        DiskDevice cliDisk = new DiskDevice("disk0", 128);
        FileSystem cliFileSystem = new FileSystem(cliRoot, cliDisk);
        ReadyQueue cliReadyQueue = new ReadyQueue();
        BlockedQueue cliBlockedQueue = new BlockedQueue();
        CPU cliCpu = new CPU(5);
        SRTScheduler cliScheduler = new SRTScheduler();
        IOManager cliIoManager = new IOManager();

        OSKernel kernel = new OSKernel(cliReadyQueue, cliBlockedQueue, cliCpu,
                cliScheduler, cliMemoryManager, cliFileSystem, cliIoManager);
        CommandInterpreter interpreter = new CommandInterpreter(kernel);

        // pokrece pozadinsku nit koja sama, kontinuirano poziva tick()  od
        // sada createProcess/run odmah vracaju kontrolu, izvrsavanje ide paralelno
        kernel.start();

        System.out.println(interpreter.execute("create /test1"));
        System.out.println(interpreter.execute("write /test1 LOAD 7\nADD 3\nPRINT\nHALT"));
        System.out.println(interpreter.execute("run /test1"));

        // dajemo pozadinskoj niti vremena da izvrsi proces (LOAD/ADD/PRINT/HALT)
        // prije nego sto provjerimo stanje preko ps
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println(interpreter.execute("ps"));

        kernel.stop();

        // demo test za MemoryManager
        System.out.println();
        System.out.println("Demo test za MemoryManager");

        RAM ram = new RAM(64);
        MemoryManager memoryManager = new MemoryManager(ram);

        PCB memProcess = new PCB(9, ProcessState.READY, 0, 0,
                new HashMap<>(), 0, 0, new ArrayList<>(), 0);

        boolean allocated = memoryManager.allocate(memProcess, 10);
        System.out.println("Alokacija uspjela: " + allocated);
        System.out.println("baseAddress=" + memProcess.getBaseAddress()
                + ", limit=" + memProcess.getLimit());

        memoryManager.write(memProcess, 2, 42);
        int readValue = memoryManager.read(memProcess, 2);
        System.out.println("Procitana vrijednost na adresi 2: " + readValue);

        try {
            memoryManager.write(memProcess, 999, 1);
            System.out.println("Test NIJE PROŠAO: očekivana je greška, a nije bačena.");
        } catch (IllegalArgumentException e) {
            System.out.println("Test PROŠAO, uhvaćena očekivana greška: " + e.getMessage());
        }

        memoryManager.free(memProcess);
        System.out.println("Memorija procesa pid=" + memProcess.getPid() + " je oslobođena.");
    }
}
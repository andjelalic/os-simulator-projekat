import assembler.Assembler;
import assembler.Instruction;
import cli.CommandInterpreter;
import filesystem.Directory;
import filesystem.FileSystem;
import io.DiskDevice;
import io.IOManager;
import io.IOOperation;
import io.IORequest;
import io.IOType;
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
import java.util.Scanner;

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

        // kompletan test Scenarija 1 potpuno nezavisan setp
        System.out.println();
        runScenario1();

        //  C-SCAN rasporedjivanje diska i rucno block/unblock
        System.out.println();
        runScenario2();


        runInteractiveConsole();
    }


    // prvo pokazem da DiskDevice opsluzuje IORequest-ove po
    // pozciji (rastuce od trenutne pozicije glave), a ne po redoslijedu
    // kojim su stigli u red
    // onda prikazem da blokiran proces (block komanda) ne napreduje dok
    // ne bude odblokiran , iako pozadinska nit kontinuirano radi tick()
    private static void runScenario2() {
        System.out.println("=== SCENARIO 2 ===");

        // c scan rasporedjivanje diska
        System.out.println("======C-SCAN raspoređivanje diska ===");

        DiskDevice disk = new DiskDevice("disk1", 200);

        //sam sadrzaj procesa nije bitan za prikaz rasporedjivanja
        PCB dummyPcb = PCB.createSystemProcess(999, 1);
        IOOperation dummyOp = new IOOperation(IOType.READ, 1); // READ ne smije imati data

        IORequest r1 = new IORequest(dummyPcb, dummyOp, disk, 50);
        IORequest r2 = new IORequest(dummyPcb, dummyOp, disk, 20);
        IORequest r3 = new IORequest(dummyPcb, dummyOp, disk, 90);
        IORequest r4 = new IORequest(dummyPcb, dummyOp, disk, 10);
        IORequest r5 = new IORequest(dummyPcb, dummyOp, disk, 60);

        disk.addRequest(r1);
        disk.addRequest(r2);
        disk.addRequest(r3);
        disk.addRequest(r4);
        disk.addRequest(r5);

        for (int i = 0; i < 5; i++) {
            IORequest next = disk.getNextRequest();
            System.out.println("Sljedeci zahtjev, pozicija=" + next.getPosition());
        }

        System.out.println("C-SCAN redoslijed pokazuje da glava diska ide u JEDNOM smjeru " +
                "(rastuce od trenutne pozicije), opsluzujuci zahtjeve po udaljenosti, ne po " +
                "redoslijedu dolaska - ocekivano: 10, 20, 50, 60, 90");

        // ===== Rucno blokiranje procesa =====
        System.out.println();
        System.out.println("=== Dio B: Ručno blokiranje procesa ===");

        RAM ram = new RAM(128);
        MemoryManager memoryManager = new MemoryManager(ram);
        Directory root = new Directory("", null);
        DiskDevice disk2 = new DiskDevice("disk2", 128);
        FileSystem fileSystem = new FileSystem(root, disk2);
        ReadyQueue readyQueue = new ReadyQueue();
        BlockedQueue blockedQueue = new BlockedQueue();
        CPU cpu = new CPU(5);
        SRTScheduler scheduler = new SRTScheduler();
        IOManager ioManager = new IOManager();

        OSKernel kernel = new OSKernel(readyQueue, blockedQueue, cpu,
                scheduler, memoryManager, fileSystem, ioManager);
        CommandInterpreter interpreter = new CommandInterpreter(kernel);

        kernel.start();

        System.out.println(interpreter.execute("create /block_test.asm"));
        System.out.println(interpreter.execute(
                "write /block_test.asm LOAD 1\nADD 1\nADD 1\nADD 1\nADD 1\nADD 1\nADD 1\nADD 1\nPRINT\nHALT"));

        String runResult = interpreter.execute("run /block_test.asm");
        System.out.println(runResult);


        int pid = Integer.parseInt(runResult.substring(runResult.indexOf("pid=") + 4).trim());

        // blokiramo proces odmah nakon sto je kreiran, prije nego sto stigne
        // da zavrsi (pozadinska nit tek kreće da ga izvrsava)
        System.out.println(interpreter.execute("block " + pid));
        System.out.println(interpreter.execute("ps"));

        try {
            Thread.sleep(400);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println(interpreter.execute("ps"));
        System.out.println("Proces je i dalje WAITING iako je proslo dovoljno vremena da zavrsi " +
                "- dokaz da blokiran proces ne napreduje");

        System.out.println(interpreter.execute("unblock " + pid));

        // 700ms umjesto 400ms, program ima 10 instrukcija, a pozadinska nit
        // izvrsava po jedan korak svakih 50ms (500ms samo za izvrsavanje),
        // pa je potrebna veca margina da proces sigurno stigne do terminayed
        try {
            Thread.sleep(700);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        System.out.println(interpreter.execute("ps"));

        kernel.stop();
    }


    private static void runInteractiveConsole() {
        RAM ram = new RAM(128);
        MemoryManager memoryManager = new MemoryManager(ram);
        Directory root = new Directory("", null);
        DiskDevice disk = new DiskDevice("disk0", 128);
        FileSystem fileSystem = new FileSystem(root, disk);
        ReadyQueue readyQueue = new ReadyQueue();
        BlockedQueue blockedQueue = new BlockedQueue();
        CPU cpu = new CPU(5);
        SRTScheduler scheduler = new SRTScheduler();
        IOManager ioManager = new IOManager();

        OSKernel kernel = new OSKernel(readyQueue, blockedQueue, cpu,
                scheduler, memoryManager, fileSystem, ioManager);
        CommandInterpreter interpreter = new CommandInterpreter(kernel);

        System.out.println("=== Interaktivna konzola OS simulacije ===");
        System.out.println("Dostupne komande: create <path>, mkdir <path>, write <path> <kod>, cat <path>, run <path>,");
        System.out.println(" ps, block <pid>, unblock <pid>, exit");
        System.out.println();

        // boot sistemskih procesa prije bilo cega drugog, ps odmah nakon
        // pokazuje da su tu od starta
        kernel.bootSystemProcesses();
        System.out.println(kernel.listProcesses());

        // pokrecemo pozadinsku nit koja stalono poziva tick(),
        // izvrsavanje korisnickih komandi (run, block, unblock iyd)
        //  procesi se izvrsavaju paralelno
        kernel.start();

        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("> ");
            String line = scanner.nextLine();
            String trimmed = line.trim();

            if (trimmed.equalsIgnoreCase("exit")) {
                kernel.stop();
                System.out.println("Gasim sistem...");
                break;
            }

            if (trimmed.isEmpty()) {
                continue;
            }

            if (trimmed.startsWith("write ")) {
                trimmed = trimmed.replace("\\n", "\n");
            }

            System.out.println(interpreter.execute(trimmed));
        }
    }

    // test Scenarija 1  boot sistemskih procesa, kreiranje
    // i pisanje fajla preko CommandInterpreter-a, pokretanje procesa preko
    // OSKernel.start() i provjera liste procesa na kraju
    //skroz nezavisan setup od ostatka Mejna, sopstveni RAM, FileSystem,
    // OSKernel itdyyy
    private static void runScenario1() {
        System.out.println("=========================================");
        System.out.println("SCENARIO 1");
        System.out.println("=========================================");

        RAM ram = new RAM(128);
        MemoryManager memoryManager = new MemoryManager(ram);
        Directory root = new Directory("", null);
        DiskDevice disk = new DiskDevice("disk0", 128);
        FileSystem fileSystem = new FileSystem(root, disk);
        ReadyQueue readyQueue = new ReadyQueue();
        BlockedQueue blockedQueue = new BlockedQueue();
        CPU cpu = new CPU(5);
        SRTScheduler scheduler = new SRTScheduler();
        IOManager ioManager = new IOManager();

        OSKernel kernel = new OSKernel(readyQueue, blockedQueue, cpu,
                scheduler, memoryManager, fileSystem, ioManager);
        CommandInterpreter interpreter = new CommandInterpreter(kernel);

        // boot: sistemski procesi (900, 901) se ucitavaju prije bilo cega
        // drugog, kao osnovni servisi sistema
        System.out.println("=== Boot: ucitavanje sistemskih procesa ===");
        kernel.bootSystemProcesses();
        System.out.println(kernel.listProcesses());

        // pokrecemo pozadinsku nit koja od sada sama, u paraleli, izvrsava
        // sistemske i buduce korisnicke procese
        kernel.start();


        System.out.println();
        System.out.println("=== Korak 1: Prikazati sadrzaj datoteke (prije nego sto postoji) ===");
        System.out.println(interpreter.execute("cat /program1.asm"));


        System.out.println();
        System.out.println("=== Korak 2-3: Kreirati novu datoteku, uci u nju ===");
        System.out.println(interpreter.execute("create /program1.asm"));


        System.out.println();
        System.out.println("=== Korak 4: Kreirati fajl (napomena: isto kao korak 2, ne radi se ponovo) ===");

        //otvaranje fajla za pisanje  to
        // je write, koji direktno upisuje sadrzaj
        System.out.println();
        System.out.println("=== Korak 5: Otvoriti fajl za pisanje ===");
        System.out.println(interpreter.execute("write /program1.asm LOAD 8\nADD 4\nSTORE 0\nPRINT\nHALT"));

        // cat pokazuje da je sadrzaj sad tekstualni asemblerski kod
        // prevod u binarni zapis se desava kasnije, kad run pozove
        // kernel.createProcess -> Assembler.parse -> pcb.loadProgram
        System.out.println();
        System.out.println("=== Korak 6: Upisati asemblerski kod (bice preveden u binarni zapis prije izvrsavanja) ===");
        System.out.println(interpreter.execute("cat /program1.asm"));

        // run asemblira kod i pravi novi proces, koji zatim
        // pozadinska nit (kernel.start()) izvrsava paralelno, bez cekanja
        System.out.println();
        System.out.println("=== Korak 7: Pokrenuti proces ===");
        System.out.println(interpreter.execute("run /program1.asm"));


        try {
            Thread.sleep(600);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        //ps sad treba da pokaze i sistemske procese (900, 901) i
        // korisnicki proces, tu ocekujemo terminated
        System.out.println();
        System.out.println("=== Korak 8: Prikazati listu procesa (ukljucujuci sistemske) ===");
        System.out.println(interpreter.execute("ps"));

        kernel.stop();
    }
}
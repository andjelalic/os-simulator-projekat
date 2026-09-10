package cli;

import assembler.Assembler;
import assembler.Instruction;
import process.BlockedQueue;
import process.CPU;
import process.PCB;
import process.ProcessState;
import process.ReadyQueue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// minimalni komandni interpreter: prima linije komandi kao stringove i njima
// upravlja fajlovima (privremena zamjena za pravi FileSystem), procesima i
// njihovim stanjem (ready/blocked)
public class CommandInterpreter {

    // privremena zamjena za pravi FileSystem: ime fajla -> sadrzaj (asemblerski kod)
    private Map<String, String> files;
    private ReadyQueue readyQueue;
    private CPU cpu;
    private int nextPid;
    // svi ikad kreirani procesi, cuvaju se ovdje radi komande ps
    private List<PCB> allProcesses;
    private BlockedQueue blockedQueue;

    public CommandInterpreter(ReadyQueue readyQueue, CPU cpu, BlockedQueue blockedQueue) {
        this.readyQueue = readyQueue;
        this.cpu = cpu;
        this.blockedQueue = blockedQueue;
        this.files = new HashMap<>();
        this.allProcesses = new ArrayList<>();
        this.nextPid = 1;
    }

    // parsira liniju komande: prvi dio je ime komande, ostatak (ako postoji)
    // se prosljedjuje odgovarajucoj privatnoj metodi na dalje parsiranje
    public String execute(String commandLine) {
        String[] tokens = commandLine.trim().split(" ", 2);
        String commandName = tokens[0];
        String arguments = tokens.length > 1 ? tokens[1] : "";

        switch (commandName) {
            case "create":
                return create(arguments);
            case "write":
                return write(arguments);
            case "run":
                return run(arguments);
            case "ps":
                return ps();
            case "block":
                return block(arguments);
            case "unblock":
                return unblock(arguments);
            default:
                return "Nepoznata komanda: " + commandName;
        }
    }

    // create <ime> pravi prazan fajl, greska ako fajl sa tim imenom vec postoji
    private String create(String name) {
        name = name.trim();
        if (files.containsKey(name)) {
            return "Greska: fajl '" + name + "' vec postoji";
        }
        files.put(name, "");
        return "Fajl '" + name + "' uspjesno kreiran";
    }

    // write <ime> <kod>, upisuje ostatak reda kao sadrzaj fajla , kreska ako
    // fajl ne postoji mora se prvo createovat
    private String write(String arguments) {
        String[] parts = arguments.split(" ", 2);
        String name = parts[0];
        String code = parts.length > 1 ? parts[1] : "";

        if (!files.containsKey(name)) {
            return "Greska: fajl '" + name + "' ne postoji";
        }
        files.put(name, code);
        return "Kod uspjesno upisan u fajl '" + name + "'";
    }

    // run <ime>,  asemblira sadrzaj fajla i pravi novi proces koji ga izvrsava,
    // greska ako fajl ne postoji ili je prazan
    private String run(String name) {
        name = name.trim();
        if (!files.containsKey(name) || files.get(name).isEmpty()) {
            return "Greska: fajl '" + name + "' ne postoji ili je prazan";
        }

        List<Instruction> instructions;
        try {
            instructions = Assembler.parse(files.get(name));
        } catch (IllegalArgumentException e) {
            return "Greska pri asembliranju fajla '" + name + "': " + e.getMessage();
        }

        PCB pcb = new PCB(nextPid++, ProcessState.NEW, 0, 0,
                new HashMap<>(), 0, 0, new ArrayList<>(), 0);
        pcb.loadProgram(instructions);
        pcb.setState(ProcessState.READY);

        readyQueue.add(pcb);
        allProcesses.add(pcb);

        return "Proces pokrenut iz fajla '" + name + "', pid=" + pcb.getPid();
    }

    // ps: ispisuje pregled svih ikad kreiranih procesa, po jedan red za svaki
    private String ps() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < allProcesses.size(); i++) {
            PCB p = allProcesses.get(i);
            sb.append("pid=").append(p.getPid())
                    .append(", state=").append(p.getState())
                    .append(", remainingTime=").append(p.getRemainingTime())
                    .append(", isSystemProcess=").append(p.isSystemProcess());
            if (i < allProcesses.size() - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    // block <pid>: prebacuje proces u blockedQueue (stanje WAITING).
    // Greska ako proces sa tim pid-om ne postoji
    private String block(String arguments) {
        Integer pid = parsePid(arguments);
        if (pid == null) {
            return "Greska: neispravan pid '" + arguments.trim() + "'";
        }

        PCB pcb = findByPid(pid);
        if (pcb == null) {
            return "Greska: proces sa pid=" + pid + " ne postoji";
        }

        blockedQueue.block(pcb);
        return "Proces pid=" + pid + " blokiran";
    }

    // unblock <pid>, vraca proces iz blockedQueue nazad u READY stanje i,
    // za razliku od BlockedQueue.unblock koja to namjerno ne radi, ovdje
    // ga i vraca u readyQueue da bi mogao ponovo biti izabran za izvrsavanje
    private String unblock(String arguments) {
        Integer pid = parsePid(arguments);
        if (pid == null) {
            return "Greska: neispravan pid '" + arguments.trim() + "'";
        }

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

    private Integer parsePid(String arguments) {
        try {
            return Integer.parseInt(arguments.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

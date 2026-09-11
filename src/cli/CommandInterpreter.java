package cli;

import filesystem.File;
import filesystem.FileMode;
import filesystem.OpenFileHandle;
import kernel.OSKernel;

// minimalni komandni interpreter: prima linije komandi kao stringove i preko
// OSKernela upravlja fajl sistemom i procesima kreiranje, izvrsavanje, ps,
// block unblock,, sam interpreter ne cuva nikakvo stanje osim referenci na kernel
public class CommandInterpreter {

    private OSKernel kernel;

    public CommandInterpreter(OSKernel kernel) {
        this.kernel = kernel;
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
            case "cat":
                return cat(arguments);
            case "mkdir":
                return mkdir(arguments);
            default:
                return "Nepoznata komanda: " + commandName;
        }
    }

    // create <path> pravi prazan fajl preko FileSystem-a, greska ako fajl vec
    // postoji ili roditeljski direktorijum ne postoji
    private String create(String path) {
        path = path.trim();
        try {
            kernel.getFileSystem().createFile(path);
        } catch (IllegalArgumentException e) {
            return "Greska: " + e.getMessage();
        }
        return "Fajl '" + path + "' uspjesno kreiran";
    }

    // write <path> <kod>, otvara fajl u WRITE modu i upisuje ostatak reda kao
    // njegov sadrzaj, greska ako fajl ne postoji
    private String write(String arguments) {
        String[] parts = arguments.split(" ", 2);
        String path = parts[0];
        String code = parts.length > 1 ? parts[1] : "";

        OpenFileHandle handle;
        try {
            handle = kernel.getFileSystem().open(path, FileMode.WRITE);
        } catch (IllegalArgumentException e) {
            return "Greska: " + e.getMessage();
        }

        handle.getFile().write(code);
        return "Kod uspjesno upisan u fajl '" + path + "'";
    }

    // run <path>, otvara fajl u READ modu, cita njegov sadrzaj i preko kernela
    // pravi novi proces koji ga izvrsava, greska ako fajl ne postoji ili je prazan
    private String run(String path) {
        path = path.trim();

        OpenFileHandle handle;
        try {
            handle = kernel.getFileSystem().open(path, FileMode.READ);
        } catch (IllegalArgumentException e) {
            return "Greska: " + e.getMessage();
        }

        File file = handle.getFile();
        String code = file.read();
        if (code.isEmpty()) {
            return "Greska: fajl '" + path + "' je prazan";
        }

        return kernel.createProcess(code, 16);
    }

    // ps: ispisuje pregled svih ikad kreiranih procesa preko kernela
    private String ps() {
        return kernel.listProcesses();
    }

    // block <pid>: prebacuje proces preko kernela u blokirano stanje
    private String block(String arguments) {
        Integer pid = parsePid(arguments);
        if (pid == null) {
            return "Greska: neispravan pid '" + arguments.trim() + "'";
        }
        return kernel.blockProcess(pid);
    }

    // unblock <pid>: vraca proces preko kernela iz blokiranog nazad u ready stanje
    private String unblock(String arguments) {
        Integer pid = parsePid(arguments);
        if (pid == null) {
            return "Greska: neispravan pid '" + arguments.trim() + "'";
        }
        return kernel.unblockProcess(pid);
    }

    // cat <path>, otvara fajl u READ modu i vraca njegov sadrzaj direktno (kao
    // prikaz sadrzaja u terminalu), greska ako fajl ne postoji; posebna poruka
    // ako je fajl prazan
    private String cat(String path) {
        path = path.trim();

        OpenFileHandle handle;
        try {
            handle = kernel.getFileSystem().open(path, FileMode.READ);
        } catch (IllegalArgumentException e) {
            return "Greska: " + e.getMessage();
        }

        String content = handle.getFile().read();
        if (content.isEmpty()) {
            return "Fajl je prazan";
        }
        return content;
    }

    // mkdir <path> pravi novi direktorijum preko FileSystem-a, greska ako vec
    // postoji ili roditeljski direktorijum ne postoji
    private String mkdir(String path) {
        path = path.trim();
        try {
            kernel.getFileSystem().createDirectory(path);
        } catch (IllegalArgumentException e) {
            return "Greska: " + e.getMessage();
        }
        return "Direktorijum '" + path + "' uspjesno kreiran";
    }

    private Integer parsePid(String arguments) {
        try {
            return Integer.parseInt(arguments.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

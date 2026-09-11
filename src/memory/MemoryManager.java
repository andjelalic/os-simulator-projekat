package memory;

import process.PCB;

import java.util.ArrayList;
import java.util.List;

public class MemoryManager {
    private RAM ram;
    private List<MemorySegment> segments;
    private boolean[] frameUsed; // pracenje zauzetosti frame-ova (okvira) u RAM-u

    public MemoryManager(RAM ram) {
        this.ram = ram;
        this.segments = new ArrayList<>();
        this.frameUsed = new boolean[ram.getSize() / MemorySegment.SEGMENT_SIZE];
    }

    public RAM getRam() {
        return ram;
    }

    public void setRam(RAM ram) {
        this.ram = ram;
    }

    public List<MemorySegment> getSegments() {
        return segments;
    }

    public void setSegments(List<MemorySegment> segments) {
        this.segments = segments;
    }

    public boolean allocate(PCB p, int size){
        int nosegments = (size + MemorySegment.SEGMENT_SIZE - 1) / MemorySegment.SEGMENT_SIZE; // rachuna koliko segmenata treba dati proces

        // trazimo dovoljno slobodnih frame-ova, redom od pocetka
        List<Integer> freeFrames = new ArrayList<>();
        for (int i = 0; i < frameUsed.length && freeFrames.size() < nosegments; i++) {
            if (!frameUsed[i]) {
                freeFrames.add(i);
            }
        }

        if (freeFrames.size() < nosegments) {
            return false; // nema dovoljno slobodne memorije
        }

        int firstBase = -1;
        for (int frame : freeFrames) {
            frameUsed[frame] = true;
            int base = frame * MemorySegment.SEGMENT_SIZE;
            if (firstBase == -1) {
                firstBase = base;
            }
            segments.add(new MemorySegment(p, base));
        }

        p.setBaseAddress(firstBase);
        p.setLimit(nosegments * MemorySegment.SEGMENT_SIZE);
        return true;
    }

    public void write(PCB p, int address, int value){
        checkBounds(p, address);
        ram.getCells()[p.getBaseAddress() + address] = value;
    }

    public int read(PCB p, int address){
        checkBounds(p, address);
        return ram.getCells()[p.getBaseAddress() + address];
    }

    private void checkBounds(PCB p, int address) {
        if (address < 0 || address >= p.getLimit()) {
            throw new IllegalArgumentException(
                    "Adresa " + address + " van dozvoljenog opsega za proces pid=" + p.getPid());
        }
    }

    public void free(PCB p){
        List<MemorySegment> toRemove = new ArrayList<>();
        for (MemorySegment segment : segments) {
            if (segment.getOwner() == p) {
                frameUsed[segment.getBase() / MemorySegment.SEGMENT_SIZE] = false;
                toRemove.add(segment);
            }
        }
        segments.removeAll(toRemove);
    }
}

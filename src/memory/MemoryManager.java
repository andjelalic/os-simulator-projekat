package memory;

import process.PCB;

import java.util.List;

public class MemoryManager {
    private RAM ram;
    private List<MemorySegment> segments;

    public MemoryManager(RAM ram, List<MemorySegment> segments) {
        this.ram = ram;
        this.segments = segments;
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
        return false;
    }
}

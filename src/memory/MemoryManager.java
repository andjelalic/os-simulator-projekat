package memory;

import process.PCB;

import java.util.ArrayList;
import java.util.List;

public class MemoryManager {
    private RAM ram;
    private List<MemorySegment> segments;

    public MemoryManager(RAM ram) {
        this.ram = ram;
        this.segments = new ArrayList<>();
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
        for(int i = 0; i < nosegments; i++){
            new MemorySegment(p, 0);
        }
        return false;
    }

    public void write(PCB p, int address, int value){
    // TODO
    }

    public int read(PCB p, int address){
        //TODO
        return 0;
    }

    public void free(PCB p){
        //TODO
    }
}

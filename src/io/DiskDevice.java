package io;

import java.util.Map;
import java.util.TreeMap;

public class DiskDevice extends IODevice{
    private TreeMap<Integer, Integer> freeSpace;
    private int position;
    public DiskDevice(String name, int diskSize) {
        super(name);

        this.freeSpace = new TreeMap<>();
        this.freeSpace.put(0, diskSize);

        this.position = 0;
    }

    @Override
    public IORequest getNextRequest() {
        if (requests.isEmpty()) {
            return null;
        }

        IORequest next = null;

        for (IORequest request : requests) {
            if (request.getPosition() >= position) {
                if (next == null ||
                        request.getPosition() < next.getPosition()) {
                    next = request;
                }
            }
        }

        if (next == null) {
            for (IORequest request : requests) {
                if (next == null ||
                        request.getPosition() < next.getPosition()) {
                    next = request;
                }
            }
        }

        requests.remove(next);
        assert next != null;
        position = next.getPosition();

        return next;
    }

    public int allocate(int blockCount) {
        for (Map.Entry<Integer, Integer> entry : freeSpace.entrySet()) {
            int start = entry.getKey();
            int length = entry.getValue();

            if (length >= blockCount) {

                freeSpace.remove(start);

                if (length > blockCount) {
                    freeSpace.put(
                            start + blockCount,
                            length - blockCount
                    );
                }

                return start;
            }
        }

        throw new IllegalStateException("Not enough contiguous free space.");
    }

    public void free(int start, int blockCount) {
        int end = start + blockCount;

        Map.Entry<Integer, Integer> lower = freeSpace.floorEntry(start);
        Map.Entry<Integer, Integer> higher = freeSpace.ceilingEntry(start);

        if (lower != null && lower.getKey() + lower.getValue() == start) {
            start = lower.getKey();
            freeSpace.remove(lower.getKey());
        }

        if (higher != null && end == higher.getKey()) {
            end = higher.getKey() + higher.getValue();
            freeSpace.remove(higher.getKey());
        }

        freeSpace.put(start, end - start);
    }
}

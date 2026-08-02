package process;

public interface Scheduler {

    PCB chooseNext(ReadyQueue ready);
}

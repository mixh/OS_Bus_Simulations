package pack;

//this class is created to save all the details related to process

import java.text.DecimalFormat;

public class Process implements Comparable<Process> {
    String processName;
    double startingTime;    // starting time of the process
    double totalExecutionTime; //  execution time generated at the time of initialization
    double completedExecutionTime;  // // execution time completed by a process
    double remainingExecutionTime;
    double waitingTimeStart;    // the time at which process starts waiting in the ready queue
    double readyQueuetotalWaitingTime;  // the total waiting time of the process spent in the ready queue
    double meanInterIOInterval; // the mean Inter I/O Interval given by the process
    double CPUBurst; // the CPU burst generated exponenetially for a process
    double ioburst;
    double iowait;  // the total waiting time process spent in the IO queue
    double ioWaitingTimeStart;
    double ioexecutionTime;
    double remainingCPUBurstTime;   // the remaining burst time in case of round robin
    double finishTime;  // time when process finishes

    String cpuStatus;
    boolean firstcycle = true;
    boolean finished = false;

    public Process(String processName, double totalExecutionTime, double meanInterIOInterval,
            double remainingExecutionTime) {
        this.processName = processName;
        this.startingTime = 0;
        this.totalExecutionTime = totalExecutionTime;
        this.completedExecutionTime = 0;
        this.remainingExecutionTime = remainingExecutionTime;
        this.waitingTimeStart = 0;
        this.meanInterIOInterval = meanInterIOInterval;
        this.readyQueuetotalWaitingTime = 0;
        this.CPUBurst = 0;
        this.ioburst = 0;
        this.iowait = 0;
        this.ioexecutionTime = 0;
        this.remainingCPUBurstTime = 0;
        this.finishTime = 0;
    }


    DecimalFormat df = new DecimalFormat("#.#####");
    @Override
    public String toString() {
        String returnString = " ";

        return String.format("Process No         " + processName + "%n" +
                "  Total Execution Time           " + df.format(totalExecutionTime) + "%n" +
                "  mean inter-I/O                 " + meanInterIOInterval + "%n" +
                "  Completed Execution Time       " + df.format(completedExecutionTime) + "%n" +
                "  Remaining Execution Time       " + df.format(remainingExecutionTime) + "%n" +
                "  Ready Queue Total Waiting Time " + df.format(readyQueuetotalWaitingTime) + "%n" +
                "  IO queue Waiting Time          " + df.format(iowait) + "%n" +
                "  IO Execution Time              " + df.format(ioexecutionTime) + "%n" +
                "  Finish Time                    " + df.format(finishTime) + "%n" +
                "  Turn Around Time               "
                + df.format((completedExecutionTime + readyQueuetotalWaitingTime + iowait + ioexecutionTime)) + "%n" +
                "                  " + returnString);
    }

    @Override
    public int compareTo(Process o) {

        return this.totalExecutionTime > o.totalExecutionTime ? 1
                : this.totalExecutionTime < o.totalExecutionTime ? -1 : 0;
    }
}
package pack;

import java.text.DecimalFormat;
import java.util.*;
import java.io.IOException;
import java.io.PrintStream;

public class SJF {
    ArrayList<PCB> PCB = new ArrayList<>();
    LinkedList<Queue> readyQueue = new LinkedList<>();
    LinkedList<Queue> IO_Queue = new LinkedList<>();
    LinkedList<Event> Event = new LinkedList<>();
    ArrayList<Process> Process = new ArrayList<>();
    ArrayList<GanttChart> snap = new ArrayList<>();
    Random rand = new Random();
    char CPU = 'N';
    char inputOutput = 'N';
    double IOburst;
    double meanInterIOInterval;
    int noOfProcess;
    DecimalFormat df = new DecimalFormat("#.#####");
    DecimalFormat df1 = new DecimalFormat("#.##");
    int counter = 1;
    int processCompletedNumber = 1;
    boolean Arrived_Processes = false;
    boolean processCompletedFlag = false;
    
    public SJF(int minExecutionTimeP, int maxExecutionTimeP, int noOfProcess, double IOburst,
            double meanInterIOInterval) {
        this.IOburst = IOburst;
        this.meanInterIOInterval = meanInterIOInterval;
        this.noOfProcess = noOfProcess;
        double meanInterArrival;
        int executionTime;

        for (int i = 0; i < noOfProcess; i++) {
            meanInterArrival = this.meanInterIOInterval + (5 * i);
            executionTime = UniformTime(minExecutionTimeP, maxExecutionTimeP);
            Process.add(new Process("P" + (i + 1), executionTime, meanInterArrival, executionTime));
        }
    
        // for the scheduling algorithm SJF, sorting the process details according to
        // execution time
            Collections.sort(Process);

        // Initializing the event details with the process details for CPU_load event.
        for (Process process : Process) {
            Event.add(new Event(0, process.processName, "CPU_load"));
        }

    }

    // method for uniformly distribution of execution time
    public int UniformTime(int min, int max) {
        return rand.nextInt((max - min) + 1) + min;
    }

    // method for exponentially generating the next IO cycle.
    public double exponentialTime(double meanInterInterval) {
        return (-meanInterInterval * (Math.log(((Math.random() * 65536 + 1) / 65536))));
    }

    // method for searching the index of process in Process control block
    public int search_Process(String processName) {
        PCB pcb;
        for (int i = 0; i < PCB.size(); i++) {
            pcb = PCB.get(i);
            if (pcb.processName.equals(processName))
                return i;

        }
        return -1;
    }

    // method for searching the index of process in Process Details
    public int searchProcess(String processName) {
        Process process;
        for (int i = 0; i < Process.size(); i++) {
            process = Process.get(i);
            if (process.processName.equals(processName))
                return i;

        }
        return -1;
    }

    // method for CPU_load event
    public void CPU_load(double clock, int processNo, String processName, PrintStream fileOut) {
        try {
            int PCBno;
            double cpuCompletionTime;
            Process procDetails = Process.get(processNo);

            if (counter <= noOfProcess) {
                PCB.add(new PCB(processName, procDetails.totalExecutionTime));
                counter++;
                PCBno = search_Process(processName);
                PCB.get(PCBno).state = "Ready";
                Process.get(processNo).waitingTimeStart = clock;
                cpuCompletionTime = exponentialTime(Process.get(processNo).meanInterIOInterval);
                Process.get(processNo).CPUBurst = cpuCompletionTime;
                fileOut.println("At clock --- " + clock);
                fileOut.println("Process added to ready queue --- " + processName);
                readyQueue.offer(new Queue(processName,
                        Process.get(processNo).totalExecutionTime,
                        Process.get(processNo).remainingExecutionTime,
                        cpuCompletionTime));
                fileOut.println("SJF Ready queue content====");
                for (Queue rq : readyQueue) {
                    fileOut.println(rq.toString());
                }
                if (readyQueue.size() == noOfProcess) {
                    // For SJF, sorting the ready queue according to CPU burst.
                        Collections.sort(readyQueue);
                    Queue newProcess = readyQueue.poll();
                    processName = newProcess.processName;
                    fileOut.println("At clock --- " + clock);
                    fileOut.println("Process picked from ready queue --- " + processName);
                    fileOut.println("Ready queue content=============================");
                    for (Queue rq : readyQueue) {
                        fileOut.println(rq.toString());
                    }
                    processNo = searchProcess((newProcess.processName));
                    Arrived_Processes = true;
                }
            }
            if (Arrived_Processes) {
                // checking whether CPU is busy or free
                if (CPU == 'N') {
                    PCBno = search_Process(processName);
                    Process.get(processNo).cpuStatus = "Y";
                    // Changing the CPU flag to Y to make it busy
                    CPU = 'Y';
                    // Changing the state of the process in PCB to running.
                    PCB.get(PCBno).state = "Running";

                    cpuCompletionTime = Process.get(processNo).CPUBurst;

                    if (cpuCompletionTime > Process.get(processNo).remainingExecutionTime) {
                        // 
                        cpuCompletionTime = Process.get(processNo).remainingExecutionTime;
                        Process.get(processNo).CPUBurst = cpuCompletionTime;
                    }
                    if (Process.get(processNo).firstcycle) {
                        snap.add(
                                new GanttChart(processName, Process.get(processNo).readyQueuetotalWaitingTime,
                                        Process.get(processNo).CPUBurst, false));
                    }


                    fileOut.println("At clock --- " + clock);
                    fileOut.println("Process executed by CPU --- " + processName + " CPU Burst = " + cpuCompletionTime);

                        Event.add(new Event(clock + cpuCompletionTime,
                                processName,
                                "CompletionCPU"));
                    

                }
                // if cpu is busy process will be queued to ready queue
                else {
                    fileOut.println("At clock --- " + clock);
                    fileOut.println("Process picked from ready queue --- " + processName);
                    PCBno = search_Process(processName);
                    PCB.get(PCBno).state = "Ready";
                    Process.get(processNo).waitingTimeStart = clock;
                    cpuCompletionTime = exponentialTime(
                            Process.get(processNo).meanInterIOInterval);
                    Process.get(processNo).CPUBurst = cpuCompletionTime;

                    readyQueue.offer(new Queue(processName,
                            Process.get(processNo).totalExecutionTime,
                            Process.get(processNo).remainingExecutionTime,
                            cpuCompletionTime));

                    fileOut.println("Ready queue content=============================");
                    for (Queue rq : readyQueue) {
                        fileOut.println(rq.toString());
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //  CompletionCPU event
    public void completionCPU(double clock, int processNo, String processName, PrintStream fileOut) {
        int PCBno;
        double waitingTime;
        CPU = 'N';
        Process.get(processNo).cpuStatus = "N";
        Process.get(processNo).firstcycle = false;
        Process.get(processNo).completedExecutionTime = Process.get(processNo).CPUBurst +
                Process.get(processNo).completedExecutionTime;

        Process.get(processNo).remainingExecutionTime = Process.get(processNo).totalExecutionTime -
                Process.get(processNo).completedExecutionTime;

        PCBno = search_Process(processName);
        PCB.get(PCBno).remainingTime = Process.get(processNo).remainingExecutionTime;

        Process procDetails = Process.get(processNo);

        if (procDetails.completedExecutionTime == procDetails.totalExecutionTime) {

            fileOut.println("At clock --- " + clock);
            fileOut.println("Process terminated--- " + processName);
            PCB.get(PCBno).state = "terminated";
            Process.get(processNo).finished = true;

            processCompletedFlag = true;

            Process.get(processNo).finishTime = clock;

        }
        else {
            if (inputOutput == 'N') {
                Event.add(new Event(clock,
                        processName,
                        "ArrivalI/O"));
            }
            else {
                fileOut.println("At clock --- " + clock);
                fileOut.println("Process added to I/O queue --- " + processName);
                PCBno = search_Process(processName);
                PCB.get(PCBno).state = "Waiting";
                Process.get(processNo).ioWaitingTimeStart = clock;
                IO_Queue.offer(new Queue(processName,
                        procDetails.totalExecutionTime,
                        procDetails.remainingExecutionTime,
                        procDetails.CPUBurst));
                fileOut.println("IO queue content***********************");
                for (Queue ioq : IO_Queue) {
                    fileOut.println(ioq.toString());
                }

            }
        }
        if (!readyQueue.isEmpty()) {
                Collections.sort(readyQueue);
            
            Queue newProcess = readyQueue.poll();
            fileOut.println("At clock --- " + clock);
            fileOut.println("Process picked from ready queue --- " + newProcess.processName);
            fileOut.println("Ready queue content=============================");
            for (Queue rq : readyQueue) {
                fileOut.println(rq.toString());
            }
            int processIndex = searchProcess(newProcess.processName);
            waitingTime = clock - Process.get(processIndex).waitingTimeStart;
            Process.get(processIndex).waitingTimeStart = 0;

            Process.get(processIndex).readyQueuetotalWaitingTime = Process
                    .get(processIndex).readyQueuetotalWaitingTime + waitingTime;
            Event.add(new Event(clock,
                    newProcess.processName,
                    "CPU_load"));

        }
        else {
            CPU = 'N';
        }

    }

    public void arrivalInputOutput(double clock, int processNo, String processName, PrintStream fileOut) {
        int PCBno;
        double iowaitingTime = 0;
        PCBno = search_Process(processName);
        PCB.get(PCBno).state = "Waiting";
        inputOutput = 'Y';
        fileOut.println("At clock --- " + clock);
        fileOut.println("Process executing I/O --- " + processName);
        Process.get(processNo).ioburst = IOburst;
        if (Process.get(processNo).ioWaitingTimeStart != 0) {
            iowaitingTime = clock - Process.get(processNo).ioWaitingTimeStart;
            Process.get(processNo).ioWaitingTimeStart = 0;
        }
        Process.get(processNo).iowait = Process.get(processNo).iowait + iowaitingTime;
        Event.add(new Event(clock + IOburst,
                processName,
                "CompletionI/O"));

    }

    public void completionInputOutput(double clock, int processNo, String processName, PrintStream fileOut) {
        double cpuCompletionTime;
        // int no;
        int PCBno;
        // marking the IO device free
        inputOutput = 'N';
        // updating the total IO execution time
        Process.get(processNo).ioexecutionTime = Process.get(processNo).ioexecutionTime +
                Process.get(processNo).ioburst;

        Process procDetails = Process.get(processNo);
        cpuCompletionTime = exponentialTime(Process.get(processNo).meanInterIOInterval);
        Process.get(processNo).CPUBurst = cpuCompletionTime;
        // if CPU is free
        if (CPU == 'N') {
            Event.add(new Event(clock,
                    processName,
                    "CPU_load"));

        }
        else {
            PCBno = search_Process(processName);

            PCB.get(PCBno).state = "Ready";
            Process.get(processNo).waitingTimeStart = clock;

            // logging ot the out file
            fileOut.println("At clock --- " + clock);
            fileOut.println("Process added to ready queue --- " + processName);

            readyQueue.offer(new Queue(processName,
                    procDetails.totalExecutionTime,
                    procDetails.remainingExecutionTime,
                    cpuCompletionTime));

            fileOut.println("Ready queue content=============================");
            for (Queue rq : readyQueue) {
                fileOut.println(rq.toString());
            }

        }
        if (!IO_Queue.isEmpty()) {

            Queue newProcess = IO_Queue.poll();

            fileOut.println("At clock --- " + clock);
            fileOut.println("Process picked from IO queue --- " + newProcess.processName);

            fileOut.println("IO queue content***********************");
            for (Queue ioq : IO_Queue) {
                fileOut.println(ioq.toString());
            }
            Event.add(new Event(clock,
                    newProcess.processName,
                    "ArrivalI/O"));

        } else {
            inputOutput = 'N';
        }
    }

    public void Simulation() {
        try {
            String eventType;
            int processNo;
            String processName;
            double clock;
            double avgWaitingTime = 0;
            double totalWaitingTime = 0;
            double avgTurnAroundTime = 0;
            double totalTurnAroundTime = 0;
            double cputotalUtilisationTime = 0;
            double cpuUtilisation = 0;
            Event eventRecord;
            int pcbSize;
            PrintStream fileOut = new PrintStream("./SJFout.txt");

            do {
                eventRecord = Event.poll();
                clock = eventRecord.time;
                eventType = eventRecord.eventType;
                processName = eventRecord.processName;
                processNo = searchProcess(processName);
                switch (eventType) {

                    case "CPU_load":
                        CPU_load(clock, processNo, processName, fileOut);
                        break;

                    case "CompletionCPU":
                        completionCPU(clock, processNo, processName, fileOut);
                        break;

                    case "ArrivalI/O":
                        arrivalInputOutput(clock, processNo, processName, fileOut);
                        break;

                    case "CompletionI/O":
                        completionInputOutput(clock, processNo, processName, fileOut);
                        break;
                }
                // sorting the event according to time
                Collections.sort(Event);
                if (processCompletedFlag) {
                    // logging
                    fileOut.println("Process Completed = " + processName);
                    for (PCB process : PCB) {
                        fileOut.println(process.toString());
                        fileOut.println("");
                        fileOut.println("");
                    }
                    pcbSize = search_Process(processName);
                    PCB.remove(pcbSize);
                    // Changing the process completed flag to false
                    processCompletedFlag = false;

                }
            } while (!PCB.isEmpty());

            // logging 
            for (Process process : Process) {
                System.out.println(process.toString());
                fileOut.println(process.toString());
                cputotalUtilisationTime = cputotalUtilisationTime + process.completedExecutionTime;
                totalWaitingTime = totalWaitingTime + process.readyQueuetotalWaitingTime;
                totalTurnAroundTime = totalTurnAroundTime + (process.completedExecutionTime
                        + process.readyQueuetotalWaitingTime + process.ioexecutionTime + process.iowait);
            }
            cpuUtilisation = cputotalUtilisationTime / clock;
            avgTurnAroundTime = totalTurnAroundTime / noOfProcess;
            avgWaitingTime = totalWaitingTime / noOfProcess;

            // logging
            System.out.println("Throughput =                " + df.format(noOfProcess / (clock / 1000)) + "/second");
            System.out.println("CPU Utilisation =           " + df.format(cpuUtilisation * 100));
            System.out.println("Average Turnaround Time =   " + df.format(avgTurnAroundTime));
            System.out.println("Average Waiting Time =      " + df.format(avgWaitingTime));

            // logging 
            fileOut.println("Gantt Chart for first CPU cycle");
            fileOut.println();
            for (GanttChart gc : snap) {
                System.out.print("|    " + gc.processName + "(" + df1.format(gc.executionTime) + ")" + " ");
                fileOut.print("|    " + gc.processName + "(" + df1.format(gc.executionTime) + ")" + " ");
            }
            System.out.println();
            fileOut.println();
            for (GanttChart gc : snap) {
                System.out.print(df1.format(gc.waitingTime) + "          ");
                fileOut.print(df1.format(gc.waitingTime) + "          ");
            }

            fileOut.println();
            fileOut.println();
            // logging
            fileOut.println("Throughput =                " + df.format(noOfProcess / (clock / 1000)) + "/second");
            fileOut.println("CPU Utilisation =           " + df.format(cpuUtilisation * 100));
            fileOut.println("Average Turnaround Time =   " + df.format(avgTurnAroundTime));
            fileOut.println("Average Waiting Time =      " + df.format(avgWaitingTime));

        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}

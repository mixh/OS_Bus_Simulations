package pack;

import java.text.DecimalFormat;
import java.util.*;
import java.io.IOException;
import java.io.PrintStream;

public class fcfs1 {
    ArrayList<PCB> PCB = new ArrayList<>();  // pcb list      
    LinkedList<Queue> readyQueue = new LinkedList<>();  //ready queue  
    LinkedList<Queue> IO_Queue = new LinkedList<>();    // io queue
    LinkedList<Event> Event = new LinkedList<>();       // event list
    ArrayList<Process> Process = new ArrayList<>();     // process list
    ArrayList<GanttChart> snap = new ArrayList<>();     // log file
    Random rand = new Random();
    char CPU = 'N';         // N-free Y-busy
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

    // constructor for initialization
    public fcfs1(int minExecutionTimeP, int maxExecutionTimeP, int noOfProcess, double IOburst,
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


        // Initializing the event details with the process details for CPU_load event.
        for (Process process : Process) {
            Event.add(new Event(0, process.processName, "CPU_load"));
        }

    }

    // UniformTime - generates random time between 2 and 4 mins
    public int UniformTime(int min, int max) {
        return rand.nextInt((max - min) + 1) + min;
    }

    // exponentialTime - generate exponential random time for given meanInterIOTime
    public double exponentialTime(double meanInterInterval) {
        return (-meanInterInterval * (Math.log(((Math.random() * 65536 + 1) / 65536))));
    }

    // searching in Process control block
    public int search_Process(String processName) {
        PCB pcb;
        for (int i = 0; i < PCB.size(); i++) {
            pcb = PCB.get(i);
            if (pcb.processName.equals(processName))
                return i;

        }
        return -1;
    }

    // searching the index of process in Process Details
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
            // boolean flag = false;
            // double remainingCPUBurstTime = 0;

            // retrieving the process details intor variable
            Process procDetails = Process.get(processNo);

            if (counter <= noOfProcess) {
                PCB.add(new PCB(processName, procDetails.totalExecutionTime));
                counter++;
                PCBno = search_Process(processName);
                PCB.get(PCBno).state = "Ready";
                Process.get(processNo).waitingTimeStart = clock;
                cpuCompletionTime = exponentialTime(Process.get(processNo).meanInterIOInterval);
                Process.get(processNo).CPUBurst = cpuCompletionTime;
                // Logging 
                fileOut.println("At clock   " + clock);
                fileOut.println("Process is added to Ready_queue   " + processName);
                readyQueue.offer(new Queue(processName,
                        Process.get(processNo).totalExecutionTime,
                        Process.get(processNo).remainingExecutionTime,
                        cpuCompletionTime));
                // Logging 
                fileOut.println("Ready queue content=>=");
                for (Queue rq : readyQueue) {
                    fileOut.println(rq.toString());
                }
                if (readyQueue.size() == noOfProcess) {
                    Queue newProcess = readyQueue.poll();
                    processName = newProcess.processName;
                    // logging 
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
            // if processes arrived true then CPU start executing them
            if (Arrived_Processes) {
                // cpu free or not if free
                if (CPU == 'N') {
                    PCBno = search_Process(processName);
                    Process.get(processNo).cpuStatus = "Y";
                    CPU = 'Y';
                    // Changing the state of the process in PCB to running.
                    PCB.get(PCBno).state = "Running";

                    cpuCompletionTime = Process.get(processNo).CPUBurst;
                    if (cpuCompletionTime > Process.get(processNo).remainingExecutionTime) {
                        // if yes CPU burst == remaining execution time
                        cpuCompletionTime = Process.get(processNo).remainingExecutionTime;
                        Process.get(processNo).CPUBurst = cpuCompletionTime;

                    }

                    // log for gantt chart if first cpu cycle
                    if (Process.get(processNo).firstcycle) {
                        snap.add(
                                new GanttChart(processName, Process.get(processNo).readyQueuetotalWaitingTime,
                                        Process.get(processNo).CPUBurst, false));
                    }

                    // logging
                    fileOut.println("At clock --- " + clock);
                    fileOut.println("Process executed by CPU --- " + processName + " CPU Burst = " + cpuCompletionTime);
                        Event.add(new Event(clock + cpuCompletionTime,
                                processName,
                                "CompletionCPU"));
                }
                // if cpu is busy process will be queued to ready queue
                else {
                    // logging
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

    // method for CompletionCPU event
    public void completionCPU(double clock, int processNo, String processName, PrintStream fileOut) {
        int PCBno;
        double waitingTime;
        // marking the CPU free
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

            // if completed logging to the file
            fileOut.println("ON time " + clock);
            fileOut.println("Process terminated " + processName);
            // Changing the state of the process to terminated
            PCB.get(PCBno).state = "terminated";
            // Changing the process finished flag to true
            Process.get(processNo).finished = true;
            // Changing the processCompleted flag to true, this flag will be used in
            // Simulation to remove the process from PCB
            processCompletedFlag = true;
            // Changing the finish time to clock in the process details
            Process.get(processNo).finishTime = clock;

        }
        // if process is not finsihed
        else {
            // checking if the IO device is free or not
            if (inputOutput == 'N') {
                // if the IO device is free generating the ArrivalI/O event at clock
                Event.add(new Event(clock,
                        processName,
                        "ArrivalI/O"));
            }
            // if IO device is not free
            else {
                // logging to the out file as process is adding to the IO queue
                fileOut.println("At clock --- " + clock);
                fileOut.println("Process added to I/O queue --- " + processName);
                PCBno = search_Process(processName);
                // Changing the state of the process to waiting
                PCB.get(PCBno).state = "Waiting";
                // Changing the starting io waiting time to clock
                Process.get(processNo).ioWaitingTimeStart = clock;
                // adding the process to io queue
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
        // checking if the ready queue is empty or not
        if (!readyQueue.isEmpty()) {
            // if not empty
            // if scheduling algorithm is SJF sorting the ready queue accoridng to CPU burst
            // picking from the ready queue
            Queue newProcess = readyQueue.poll();
            // logging to the out file
            fileOut.println("At clock --- " + clock);
            fileOut.println("Process picked from ready queue --- " + newProcess.processName);
            fileOut.println("Ready queue content=============================");
            for (Queue rq : readyQueue) {
                fileOut.println(rq.toString());
            }
            int processIndex = searchProcess(newProcess.processName);
            // calcualting the waiting time of the process which spent in the ready queue
            waitingTime = clock - Process.get(processIndex).waitingTimeStart;
            Process.get(processIndex).waitingTimeStart = 0;
            // updating the total waiting time spent in the ready queue by the process
            Process.get(processIndex).readyQueuetotalWaitingTime = Process
                    .get(processIndex).readyQueuetotalWaitingTime + waitingTime;
            // generating the "CPU_load" event at clcok
            Event.add(new Event(clock,
                    newProcess.processName,
                    "CPU_load"));

        }
        // if ready queue is empty Changing the CPU flag to N
        else {
            CPU = 'N';
        }

    }

    // method for "ArrivalI/O event
    public void arrivalInputOutput(double clock, int processNo, String processName, PrintStream fileOut) {
        int PCBno;
        double iowaitingTime = 0;
        PCBno = search_Process(processName);
        // Changing the state of the process to waiting in PCB
        PCB.get(PCBno).state = "Waiting";
        // marking the IO device busy
        inputOutput = 'Y';
        // logging to the out file
        fileOut.println("At clock --- " + clock);
        fileOut.println("Process executing I/O --- " + processName);
        // updating the IOburst which is provided by the user
        Process.get(processNo).ioburst = IOburst;
        // calcualting the IO waiting time which process spent in the IO queue
        if (Process.get(processNo).ioWaitingTimeStart != 0) {
            iowaitingTime = clock - Process.get(processNo).ioWaitingTimeStart;
            Process.get(processNo).ioWaitingTimeStart = 0;
        }
        // updating the total io wait time of the process
        Process.get(processNo).iowait = Process.get(processNo).iowait + iowaitingTime;
        // generating the CompletionI/O event at clock + IOburst time
        Event.add(new Event(clock + IOburst,
                processName,
                "CompletionI/O"));

    }

    // method for CompletionI/O event
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
        // generating the next IO cycle exponentially
        cpuCompletionTime = exponentialTime(Process.get(processNo).meanInterIOInterval);
        Process.get(processNo).CPUBurst = cpuCompletionTime;
        // if CPU is free
        if (CPU == 'N') {
            // generating new event CPU_load at clock
            Event.add(new Event(clock,
                    processName,
                    "CPU_load"));

        }
        // if CPU is not free
        else {
            PCBno = search_Process(processName);
            // Changing the state of the process to ready in process control block
            PCB.get(PCBno).state = "Ready";
            // Changing the start of the waiting time to clock
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

        // checking if IO queue is empty or not
        if (!IO_Queue.isEmpty()) {

            // if not empty
            Queue newProcess = IO_Queue.poll();
            // logging to the out file
            fileOut.println("At clock --- " + clock);
            fileOut.println("Process picked from IO queue --- " + newProcess.processName);

            fileOut.println("IO queue content***********************");
            for (Queue ioq : IO_Queue) {
                fileOut.println(ioq.toString());
            }
            // generating ArrivalI/O event at clock
            Event.add(new Event(clock,
                    newProcess.processName,
                    "ArrivalI/O"));

        } else {
            // if empty marking IO free
            inputOutput = 'N';
        }
    }

    // method for process simulation
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
            PrintStream fileOut = new PrintStream("./fcfsout.txt");

            do {
                // retrieving the first event record from the Event
                eventRecord = Event.poll();
                // retrieving the time of the event
                clock = eventRecord.time;
                // retrieving the event type
                eventType = eventRecord.eventType;
                // retrieving the process name
                processName = eventRecord.processName;
                // getting the index of the process in process details
                processNo = searchProcess(processName);

                // checking for the type of event, depend on the event type specific methods are
                // called
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
                // sorting the event according to time/clock
                Collections.sort(Event);

                // if the processCompletedFlag is true which was set in CompletionCPU event,
                // removing the process from PCB
                if (processCompletedFlag) {
                    // writing ot the log file
                    fileOut.println("Process Completed = " + processName);
                    for (PCB process : PCB) {
                        fileOut.println(process.toString());
                        fileOut.println("");
                        fileOut.println("");
                    }

                    // removing the process from PCB as the process is finished.
                    pcbSize = search_Process(processName);
                    PCB.remove(pcbSize);
                    // Changing the process completed flag to false
                    processCompletedFlag = false;

                }

                // loop condition to check if the processes are present in Process control block
            } while (!PCB.isEmpty());

            // logging the process details in the log file and excel file.
            for (Process process : Process) {
                System.out.println(process.toString());
                fileOut.println(process.toString());
                // calculating the total execution time by the CPU
                cputotalUtilisationTime = cputotalUtilisationTime + process.completedExecutionTime;
                // calculating the toal time spent by all the process in the ready queue
                totalWaitingTime = totalWaitingTime + process.readyQueuetotalWaitingTime;
                // calculating the total turn around time for all the process.
                totalTurnAroundTime = totalTurnAroundTime + (process.completedExecutionTime
                        + process.readyQueuetotalWaitingTime + process.ioexecutionTime + process.iowait);
            }
            cpuUtilisation = cputotalUtilisationTime / clock;
            avgTurnAroundTime = totalTurnAroundTime / noOfProcess;
            avgWaitingTime = totalWaitingTime / noOfProcess;

            // logging
            System.out.println("Throughput =                " + df.format(noOfProcess / (clock)));
            System.out.println("CPU Utilisation =           " + df.format(cpuUtilisation * 100));
            System.out.println("Average Turnaround Time =   " + df.format(avgTurnAroundTime));
            System.out.println("Average Waiting Time =      " + df.format(avgWaitingTime));

            // logging the gantt chart
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
        }
    }
}

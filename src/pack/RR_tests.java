package pack;

import java.text.DecimalFormat;
import java.util.*;
import java.io.IOException;
import java.io.PrintStream;

public class RR_tests {
    // Array list for process control block
    ArrayList<PCB> PCB = new ArrayList<>();
    // Queue for ready queue
    LinkedList<Queue> readyQueue = new LinkedList<>();
    // Queue for IO queue
    LinkedList<Queue> IO_Queue = new LinkedList<>();
    // Linked List for events
    LinkedList<Event> Event = new LinkedList<>();
    // Array List for process details
    ArrayList<Process> Process = new ArrayList<>();
    // Array List for gantt chart for first CPU cycle
    ArrayList<GanttChart> snap = new ArrayList<>();
    Random rand = new Random();
    // variable for CPU
    char CPU = 'N';
    // variable for input output device
    char inputOutput = 'N';
    // to store the IO burst provided by the user
    double IOburst;
    // to store the mean Inter IO Interval provided by the user
    double meanInterIOInterval;
    // to store the number of process
    int noOfProcess;
    DecimalFormat df = new DecimalFormat("#.#####");
    DecimalFormat df1 = new DecimalFormat("#.##");
    int counter = 1;
    int processCompletedNumber = 1;
    // to store the quantum provide by the user
    double quantum;
    boolean Arrived_Processes = false;
    boolean processCompletedFlag = false;

    // constructor for initialization
    public RR_tests(int minExecutionTimeP, int maxExecutionTimeP, int noOfProcess, double IOburst,
            double meanInterIOInterval, double quantum) {
        this.IOburst = IOburst;
        this.meanInterIOInterval = meanInterIOInterval;
        this.noOfProcess = noOfProcess;
        this.quantum = quantum;
        double meanInterArrival;
        int executionTime;

        // Initializing the process details with the no of process provided by the user.
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
            boolean flag = false;
            double remainingCPUBurstTime = 0;

            // retrieving the process details intor variable
            Process procDetails = Process.get(processNo);

            if (counter <= noOfProcess) {
                // if the process are ariving in the system then PCB entry will be created for
                // all the process
                PCB.add(new PCB(processName, procDetails.totalExecutionTime));
                counter++;

                // process are arriving so they will queued in the ready and status will be
                // ready at time 0
                PCBno = search_Process(processName);
                // Changing the status of the process to ready at time 0
                PCB.get(PCBno).state = "Ready";
                // Changing the starting waiting time of the process to clock
                Process.get(processNo).waitingTimeStart = clock;
                // generating the next IO cycle exponentially
                cpuCompletionTime = exponentialTime(Process.get(processNo).meanInterIOInterval);
                // Changing the CPU burst which is generated
                Process.get(processNo).CPUBurst = cpuCompletionTime;

                // Logging to the out file - as process is added to the ready queue
                fileOut.println("At clock --- " + clock);
                fileOut.println("Process added to ready queue --- " + processName);
                readyQueue.offer(new Queue(processName,
                        Process.get(processNo).totalExecutionTime,
                        Process.get(processNo).remainingExecutionTime,
                        cpuCompletionTime));

                // Logging to the out file - as process is added to the ready queue
                fileOut.println(" RR Ready queue content=============================");
                for (Queue rq : readyQueue) {
                    fileOut.println(rq.toString());
                }

                // when all process arrived in the system at time 0 the first process will be
                // picked up and processed by CPU if the CPU is free
                if (readyQueue.size() == noOfProcess) {
                    // process which is at first picked from the ready queue
                    Queue newProcess = readyQueue.poll();
                    processName = newProcess.processName;
                    // logging the process picked by ready queue to log file
                    fileOut.println("At clock --- " + clock);
                    fileOut.println("Process picked from ready queue --- " + processName);
                    fileOut.println("RR Ready queue content=============================");
                    for (Queue rq : readyQueue) {
                        fileOut.println(rq.toString());
                    }
                    processNo = searchProcess((newProcess.processName));
                    // Changing the flag to ture as all processes are arrived.
                    Arrived_Processes = true;
                }
            }
            // if all processes are arrived at time 0 then CPU will start executing them
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

                    // for Round Robin

                    // checking generated CPU burst time is greater than quantum(time slice)
                    if (cpuCompletionTime > quantum) {
                        // if yes then Changing flag to true
                        flag = true;
                        // computing the remaining burst time from the total generated CPU burst
                        remainingCPUBurstTime = cpuCompletionTime - quantum;
                        // updating CPU burst to quantum
                        cpuCompletionTime = quantum;
                        Process.get(processNo).CPUBurst = cpuCompletionTime;
                        // updating the remaining burst time of the process
                        Process.get(processNo).remainingCPUBurstTime = remainingCPUBurstTime;
                    }

                    // checking if the generated CPU burst is greater than the remaining execution
                    // time of the process
                    if (cpuCompletionTime > Process.get(processNo).remainingExecutionTime) {
                        // if yes Changing the CPU burst to remaining execution time
                        cpuCompletionTime = Process.get(processNo).remainingExecutionTime;
                        Process.get(processNo).CPUBurst = cpuCompletionTime;
                    }

                    // if this is for first CPU burst cycle collecting the statistics for the gantt
                    // chart
                    if (Process.get(processNo).firstcycle) {
                        snap.add(
                                new GanttChart(processName, Process.get(processNo).readyQueuetotalWaitingTime,
                                        Process.get(processNo).CPUBurst, false));
                    }

                    // logging to the out file that process is picked by CPU
                    fileOut.println("At clock --- " + clock);
                    fileOut.println("Process executed by CPU --- " + processName + " CPU Burst = " + cpuCompletionTime);

                    // for round robin algorithm checking the flag and CPU burst is less than
                    // remaining execution time generating InterruptTimer event at clock plus cpu
                    // burst time
                    if (flag && cpuCompletionTime < Process.get(processNo).remainingExecutionTime) {
                        Event.add(new Event(clock + cpuCompletionTime,
                                processName,
                                "InterruptTimer"));
                        flag = false;
                    }
                    // else generating CPU completion event at clock plus cpu burst time
                    else {
                        Event.add(new Event(clock + cpuCompletionTime,
                                processName,
                                "CompletionCPU"));
                    }

                }
                // if cpu is busy process will be queued to ready queue
                else {
                    // logging the process queue to ready queue to log file
                    fileOut.println("At clock --- " + clock);
                    fileOut.println("Process picked from ready queue --- " + processName);

                    PCBno = search_Process(processName);
                    // Changing the state of the process in PCB to ready
                    PCB.get(PCBno).state = "Ready";
                    // updating waiting time start , cpu burst and all cpu burst for the process.
                    Process.get(processNo).waitingTimeStart = clock;
                    cpuCompletionTime = exponentialTime(
                            Process.get(processNo).meanInterIOInterval);
                    Process.get(processNo).CPUBurst = cpuCompletionTime;

                    readyQueue.offer(new Queue(processName,
                            Process.get(processNo).totalExecutionTime,
                            Process.get(processNo).remainingExecutionTime,
                            cpuCompletionTime));

                    fileOut.println("RR Ready queue content=============================");
                    for (Queue rq : readyQueue) {
                        fileOut.println(rq.toString());
                    }

                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // method for InterruptTimer Event for Round Robin algorithm
    public void interruptTimer(double clock, int processNo, String processName, PrintStream fileOut) {
        int PCBno;
        PCBno = search_Process(processName);
        // updating the interrupt process status to ready
        PCB.get(PCBno).state = "Ready";
        double waitingTime;
        // updating the execution time, completed by the process
        Process.get(processNo).completedExecutionTime = Process.get(processNo).completedExecutionTime
                + Process.get(processNo).CPUBurst;
        // updating the remaining execution time
        Process.get(processNo).remainingExecutionTime = Process.get(processNo).totalExecutionTime -
                Process.get(processNo).completedExecutionTime;
        // updating the remaining execution time of the process in the PCB
        PCB.get(PCBno).remainingTime = Process.get(processNo).remainingExecutionTime;
        // Changing the waiting time start of the process to clock as it is entering
        // ready queue
        Process.get(processNo).waitingTimeStart = clock;
        // Changing the next CPU burst to the remaining CPU burst time which was left
        // after the quantum
        Process.get(processNo).CPUBurst = Process.get(processNo).remainingCPUBurstTime;
        // Changing the remaining CPU burst time to zero
        Process.get(processNo).remainingCPUBurstTime = 0;
        // marking the CPU free
        CPU = 'N';
        Process.get(processNo).cpuStatus = "N";

        // logginf to the out file as process is adding to the ready queue
        fileOut.println("At clock --- " + clock);
        fileOut.println("Process added to ready queue --- " + processName);
        readyQueue.offer(new Queue(processName,
                Process.get(processNo).completedExecutionTime,
                Process.get(processNo).remainingExecutionTime,
                Process.get(processNo).CPUBurst));
        fileOut.println("RR Ready queue content=============================");
        for (Queue rq : readyQueue) {
            fileOut.println(rq.toString());
        }

        Queue newProcess = readyQueue.poll();
        // logging to the out file as process is picked from the ready queue
        fileOut.println("At clock --- " + clock);
        fileOut.println("Process picked from ready queue --- " + newProcess.processName);
        fileOut.println("RR Ready queue content=============================");
        for (Queue rq : readyQueue) {
            fileOut.println(rq.toString());
        }
        int processIndex = searchProcess(newProcess.processName);
        // calculating the waiting time which process spent in the ready queue
        waitingTime = clock - Process.get(processIndex).waitingTimeStart;
        Process.get(processIndex).waitingTimeStart = 0;
        // updating the ready queue waiting time of the process
        Process.get(
                processIndex).readyQueuetotalWaitingTime = Process.get(processIndex).readyQueuetotalWaitingTime
                        + waitingTime;

        // generating CPU_load event at clock for the process picked from the ready
        // queue
        Event.add(new Event(clock,
                newProcess.processName,
                "CPU_load"));

    }

    // method for CompletionCPU event
    public void completionCPU(double clock, int processNo, String processName, PrintStream fileOut) {
        int PCBno;
        double waitingTime;
        // marking the CPU free
        CPU = 'N';
        Process.get(processNo).cpuStatus = "N";
        // first cycle is completed for the process so marking it to false
        Process.get(processNo).firstcycle = false;
        // updating the total time executed by the process
        Process.get(processNo).completedExecutionTime = Process.get(processNo).CPUBurst +
                Process.get(processNo).completedExecutionTime;

        // updatinng the total remaining exceution time of the process
        Process.get(processNo).remainingExecutionTime = Process.get(processNo).totalExecutionTime -
                Process.get(processNo).completedExecutionTime;

        PCBno = search_Process(processName);
        // updating the remaining execution time of the process in the PCB
        PCB.get(PCBno).remainingTime = Process.get(processNo).remainingExecutionTime;

        Process procDetails = Process.get(processNo);

        // checking if the process is completed
        if (procDetails.completedExecutionTime == procDetails.totalExecutionTime) {

            // if yes logging to the out file
            fileOut.println("At clock --- " + clock);
            fileOut.println("Process terminated--- " + processName);
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
            // picking from the ready queue
            Queue newProcess = readyQueue.poll();
            // logging to the out file
            fileOut.println("At clock --- " + clock);
            fileOut.println("Process picked from ready queue --- " + newProcess.processName);
            fileOut.println("RR Ready queue content=============================");
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
        Process.get(processNo).iowait = Process.get(processNo).iowait + iowaitingTime;
        Event.add(new Event(clock + IOburst,
                processName,
                "CompletionI/O"));

    }

    // method for CompletionI/O event
    public void completionInputOutput(double clock, int processNo, String processName, PrintStream fileOut) {
        double cpuCompletionTime;
        
        int PCBno;
        inputOutput = 'N';

        Process.get(processNo).ioexecutionTime = Process.get(processNo).ioexecutionTime +
                Process.get(processNo).ioburst;

        Process procDetails = Process.get(processNo);
    
        cpuCompletionTime = exponentialTime(Process.get(processNo).meanInterIOInterval);
        Process.get(processNo).CPUBurst = cpuCompletionTime;
    
        if (CPU == 'N') {

            Event.add(new Event(clock,
                    processName,
                    "CPU_load"));

        }

        else {
            PCBno = search_Process(processName);
            PCB.get(PCBno).state = "Ready";
            Process.get(processNo).waitingTimeStart = clock;

            fileOut.println("At clock --- " + clock);
            fileOut.println("Process added to ready queue --- " + processName);

            readyQueue.offer(new Queue(processName,
                    procDetails.totalExecutionTime,
                    procDetails.remainingExecutionTime,
                    cpuCompletionTime));

            fileOut.println("RR Ready queue content=============================");
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

    // method for process simulation
    public void Simulation(int a) {
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
            PrintStream fileOut = new PrintStream("./RR_test"+a+".txt");

            fileOut.println("Quantum = " + quantum);


            do {
                // retrieving the first event record from the Event
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

                    case "InterruptTimer":
                        interruptTimer(clock, processNo, processName, fileOut);
                        break;
                }
                Collections.sort(Event);
                if (processCompletedFlag) {
                    fileOut.println("Process Completed = " + processName);
                    for (PCB process : PCB) {
                        fileOut.println(process.toString());
                        fileOut.println("");
                        fileOut.println("");
                    }
                    pcbSize = search_Process(processName);
                    PCB.remove(pcbSize);
                    processCompletedFlag = false;

                }

                // loop condition to check if the processes are present in Process control block
            } while (!PCB.isEmpty());

            // logging.
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

            // logging the required 
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

            // logging the statistics to out file and excel file
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

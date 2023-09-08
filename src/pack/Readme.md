Event.java class to store event details implements compareTo method to sort the arrayList based on time.

App.java is the main class to implement simulations it is also the class where one could change the values for initialisation which are logically correct. All times are in in milliseconds unless specified seprately

- In App main class objects are created for simulation classes FCFS,SJF,RR,RR_tests with initial values passed to them and successively their runner methods are called. 
- quantum value is initalised to 30 for the simulation of round robin 
- RR_tests class is used for running the experiment 5 times with different quantum values initialised from 15 to 70 incrementing by 15. To run the tests again 5 times with different quantum values one has to change this q[] array.

PCB.java is a simulated process control block data object stores the name,state,execution time and remaining time for the processes.

- Initially remainingTime would be equal to executionTime as processes and is intiallised as same.

Process.java is the file that contains the details related to all the processes. it also prints the logs for the processes including all cpu burst generated for the processes.

Queue.java is the base structure for IO queue and ready queue implementation

GanttChart.java is the file created to save logs for the gantt chart stores process id, waiting time, execution time and if the process has finished or not


fcfs1.java
- initially data objects are created and described in the code file for the processes.
- constructor is used for initialisation taking minExecutionTime, maxExecutionTime, noOfProcesses and meanInterIOInterval from the app main class.
- processes are created and Initialised with the process details with provided by the user.
- then event list is appended with event details for each of processes and initially the event state is set as CPU_load

generateUniformDistribution - generates random time between 2 and 4 mins

generateExponentialDistribution - generate exponential random time for given meanInterIOTime 

search_Process searches for a given process id in the PCB

searchProcess searches for a given process id in the process info list class;

In CPU_load event 
- we retrieve the process details into a variable
- while process are ariving in the system then PCB entries are created 
- as process are arriving so they will be moved to ready queue
- starting time is set to clock 
- IO cycle is generated exponentially
- CPUBurst is set which is generated previously
- this generated CPU burst is added to the ArrayList of all CPU bursts which is also logged to our file
- when all process arrived in the system at time 0 the first process will be picked up and processed by CPU if the CPU is free
- process which is at first picked from the ready queue
- the log file is updated with the process picked by ready queue 

If all the processes are arrived cpu starts executing them

if cpu is free , if free the flag is changed and the state of process in cpu is set to running

- then we check if generated cpu burst is greater than remaining execution time if yes then cpu burst is set to remaining execution time.

- for the first cpu burst cycle we log for the gantt chart and calculate details for the processes

if cpu is busy process will be queued to ready queue
- changing state of process to ready
-  Waiting time = clock
- cpu completion time = exponential time for meanInterIOInterval
- cpu burst = cpu completionTime
- add this cpu burst to our all cpu burst list;

{SJF}
- As the scheduling algorithm is SJF, sorting the ready queue according to the CPU burst.

{RR}
{RR_tests} runs the file 5 times with the quantum array q[]s\
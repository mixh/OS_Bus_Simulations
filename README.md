# OS_Bus_Simulations
Create 10 processes of random execution time with lengths uniformly distributed between 2 and 4 minutes (use the uniform distribution method for random number generation).
• For each process, the times between I/O requests (i.e, CPU bursts) are distributed exponentially. The mean inter-I/O intervals for the processes are respectively 30ms, 35ms, 40ms, 45ms, 50ms, 55ms, 60ms, 65ms, 70ms, and 75ms.
• Each time an I/O is needed it takes precisely 60 ms
• A process, once it enters the system and before it exits it, can be either in the Ready Queue, or the I/O queue. (It is convenient to consider the process at the front of the Ready Queue to be serviced by the CPU, and the process at the front in the I/O Queue to be serviced by the channel that deals with all I/O devices; however, care should be taken to count the time of the process being serviced separately from waiting time.)

package pack;

public class App {
    public static void main(String[] args) {

        int min = 120000;         // min time = 2 mins
        int max = 240000;        // max time = 4 mins 
        int processes = 10;     // no of processes
        int IOburst = 60;
        int meanInterIOInterval = 30;

        // FCFS Simulation
        fcfs1 f = new fcfs1(min,max,processes,IOburst,meanInterIOInterval);
        f.Simulation();

        // SJF Simulation
        SJF s = new SJF(min, max, processes, IOburst, meanInterIOInterval);
        s.Simulation();

        // will run rr only once
        int quantum = 45;       // initialised to 45 can change.
        RR r = new RR(min, max, processes, IOburst, meanInterIOInterval,quantum);
        r.Simulation();

        // for round robin tests runs q array length times
        // decrease in quantum average waiting time can increase
        int q [] = {8,12,30,40,60};        // quantum values for testing qb
        // int q [] = {40,50,70,80,120}; 
            // quantum values for testing  qb //decrease in quantum waiting time can decrease
        for(int a = 0; a<q.length;a++ )
        {
            RR_tests rr_tests = new RR_tests(min, max, processes, IOburst, meanInterIOInterval,q[a]);
            rr_tests.Simulation(a+1);
        }
    }
} 

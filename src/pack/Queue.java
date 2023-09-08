package pack;

//base structure for io queue and ready queue implementation
public class Queue implements Comparable<Queue>
{
    String processName;
    double completionTime;
    double remainingTime;
    double CPUBurst;

    public Queue(String processName, double completionTime, double remainingTime, double CPUBurst) {
        this.processName = processName;
        this.completionTime = completionTime;
        this.remainingTime = remainingTime;
        this.CPUBurst = CPUBurst;
    }

    @Override
    public String toString() {
        return "processName='" + processName + '\'' +
                ", completionTime=" + completionTime +
                ", remainingTime=" + remainingTime +
                ", CPUBurst=" + CPUBurst;
    }

    // Overriding compareTo method to sort the ready queue based on cpu burst time.
    @Override
    public int compareTo(Queue o) {
        return this.CPUBurst > o.CPUBurst ? 1 : this.CPUBurst < o.CPUBurst ? -1 : 0;
    }

}

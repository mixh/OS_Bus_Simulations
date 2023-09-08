package pack;

public class PCB {
    String processName;
    String state;
    double executionTime;
    double remainingTime;


    // Initially remainingTime would be equal to executionTime as processes.
    public PCB(String processName, double executionTime) {
        this.processName = processName;
        this.state = "";
        this.executionTime = executionTime;
        this.remainingTime = executionTime;
    }

    @Override
    public String toString() {
        return "PCB{" +
                "processNo=" + processName +
                ", state='" + state +
                ", executionTime=" + executionTime +
                ", remainingTime=" + remainingTime +
                '}';
    }
}

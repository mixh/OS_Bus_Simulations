package pack;
public class GanttChart {
    String processName;
    double waitingTime;
    double executionTime;
    boolean finished;

    public GanttChart(String processName, double waitingTime, double executionTime, boolean finished) {
        this.processName = processName;
        this.waitingTime = waitingTime;
        this.executionTime = executionTime;
        this.finished = finished;

    }

    @Override
    public String toString() {
        return "GanttChart{" +
                "processName='" + processName + '\'' +
                ", waitingTime=" + waitingTime +
                ", executionTime=" + executionTime +
                '}';
    }

}

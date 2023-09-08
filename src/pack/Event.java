package pack;

// event class ; also implements compareTo method to sort the arrayList based on time.
public class Event implements Comparable<Event> {
    double time;            // the clock time
    String processName;     //  the process name
    String eventType;       // type of event

    public Event(double time, String processName, String eventType) {
        this.time = time;
        this.processName = processName;
        this.eventType = eventType;
    }

    @Override
    public String toString() {
        return "Event{" +
                "time=" + time +
                ", processName=" + processName +
                ", eventType='" + eventType + '\'' +
                '}';
    }

    @Override
    public int compareTo(Event Eventparam) {

        return this.time > Eventparam.time ? 1 : this.time < Eventparam.time ? -1 : 0;
    }
}

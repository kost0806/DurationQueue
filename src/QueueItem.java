/**
 * Created by Kostrian on 2016-06-08.
 */
public abstract class QueueItem {
    String id;
    private long duration;
    private long lastExecutedTime;

    public QueueItem(String id, long duration) {
        this.id = id;
        this.duration = duration;
        lastExecutedTime = System.currentTimeMillis();
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getDuration() {
        return duration;
    }

    public String getId() {
        return id;
    }

    public long getLastExecutedTime() {
        return lastExecutedTime;
    }

    public void execute() {
        recordTime();
        run();
    }

    private void recordTime() {
        lastExecutedTime = System.currentTimeMillis();
    }

    @Override
    public String toString() {
        return id;
    }

    abstract void onPreRun();
    abstract void run();
    abstract void onPostRun();
}

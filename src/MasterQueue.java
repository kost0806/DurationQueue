import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kostrian on 2016-06-08.
 */
public class MasterQueue {
    private static final Logger log = LogManager.getLogger();


    private List<QueueItem> queue;
    private List<QueueItem> items;
    private boolean isRunning;
    private boolean isItemRegistered;
    private Enqueuer enqueuer;
    Thread thread;

    public MasterQueue() {
        queue = new ArrayList<>();
        items = new ArrayList<>();
        enqueuer = new Enqueuer(this, items);
    }

    private boolean enqueue(QueueItem queueItem) {
        return queue.add(queueItem);
    }

    private void setRunning(boolean running) {
        isRunning = running;
    }

    public void stopExecute() {
        setRunning(false);
        thread.interrupt();
        thread = null;
    }

    public void registerItems(List<QueueItem> items) {
        isItemRegistered = true;
        this.items = items;
    }

    public boolean registerItem(QueueItem item) {
        isItemRegistered = true;
        return items.add(item);
    }

    public void run(OnRun onRun) {
        onRun.onPreExecute();
        if (!isItemRegistered) {
            log.error("MasterQueue is run with no items");
            return;
        }
        isRunning = true;
        thread = new Thread(new Enqueuer(this, items));
        thread.setName("Enqueuer");
        thread.start();
        execute();
        onRun.onPostExecute();
    }

    private void execute() {
        while (isRunning) {
            if (queue.size() > 0) {
                QueueItem item = queue.remove(0);
                log.info(String.format("%s Executed.", item.getId()));
                item.execute();
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                log.fatal(e.getMessage());
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (QueueItem item : queue) {
            sb.append(item.toString());
            sb.append(" ");
        }
        sb.append("]");
        return sb.toString();
    }

    abstract static class OnRun {
        abstract void onPreExecute();
        abstract void onPostExecute();
    }

    private class Enqueuer implements Runnable {
        private MasterQueue masterQueue;
        private List<QueueItem> items;

        public Enqueuer(MasterQueue masterQueue, List<QueueItem> items) {
            this.masterQueue = masterQueue;
            this.items = items;
        }

        @Override
        public void run() {
            try {
                while (true) {
                    doInThread();
                }
            } catch (InterruptedException e) {
                log.info("Enqueuer: Stoped.");
            } catch (Exception e) {
                log.error("Exception: " + e);
                e.printStackTrace();
            }
        }

        private void doInThread() throws InterruptedException {
            long currentTime = System.currentTimeMillis();
            for (QueueItem item : items) {
                Thread.sleep(10);
                long duration = item.getDuration();
                long lastExecutedTime = item.getLastExecutedTime();
                if (currentTime >= duration + lastExecutedTime) {
                    masterQueue.enqueue(item);
                    log.info("Enqueued: " + item.getId());
                }
            }
        }
    }
}

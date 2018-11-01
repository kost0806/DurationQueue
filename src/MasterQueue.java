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
    private Slave slave;
    Thread thread;

    public MasterQueue() {
        queue = new ArrayList<>();
        items = new ArrayList<>();
        slave = new Slave(this, items);
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

    public boolean registerItem(List<QueueItem> items) {
        if (isItemRegistered) {
            return this.items.addAll(items);
        }
        else {
            isItemRegistered = true;
            this.items = items;
            return true;
        }
    }

    public boolean registerItem(QueueItem item) {
        isItemRegistered = true;
        return items.add(item);
    }

    private void runPost() {
        log.info("Handle onPostRun...");
        for (QueueItem item : items) {
            item.onPostRun();
        }
    }

    private void runPre() {
        log.info("Handle onPreRun...");
        for (QueueItem item : items) {
            item.onPreRun();
        }
    }

    public void run(OnRun onRun) {
        onRun.onPreExecute();
        if (!isItemRegistered) {
            log.error("MasterQueue is run with no items");
            return;
        }
        isRunning = true;
        runPre();
        thread = new Thread(new Slave(this, items));
        thread.setName("Slave");
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

    private class Slave implements Runnable {
        private MasterQueue masterQueue;
        private List<QueueItem> items;

        public Slave(MasterQueue masterQueue, List<QueueItem> items) {
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
                runPost();
                log.info("Slave: Stoped.");
            } catch (Exception e) {
                runPost();
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

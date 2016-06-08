import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Timer;

/**
 * Created by Kostrian on 2016-06-08.
 */
public class Main {
    private static Logger log = LogManager.getLogger();

    public static void main(String[] args) {
        final MasterQueue mq = new MasterQueue();
        QueueItem[] items = new QueueItem[3];
        items[0] = new QueueItem("2000", 2000) {
            @Override
            void onPreRun() {
                System.out.println("====2000====");
            }

            @Override
            void onPostRun() {
                System.out.println("============");
            }

            @Override
            void run() {
                System.out.println("2000ms !!!");
            }
        };

        items[1] = new QueueItem("3000", 3000) {
            @Override
            void onPreRun() {
                System.out.println("====3000====");
            }

            @Override
            void onPostRun() {
                System.out.println("============");
            }

            @Override
            void run() {
                System.out.println("3000ms !!!");
            }
        };

        items[2] = new QueueItem("5000", 5000) {
            @Override
            void onPreRun() {
                System.out.println("====5000====");
            }

            @Override
            void onPostRun() {
                System.out.println("============");
            }

            @Override
            void run() {
                System.out.println("5000ms !!!");
            }
        };

        mq.registerItem(items[0]);
        mq.registerItem(items[1]);
        mq.registerItem(items[2]);

        mq.run(new MasterQueue.OnRun() {

            @Override
            void onPostExecute() {
                log.info("==========END!!!==========");
            }

            @Override
            void onPreExecute() {
                log.info("==========START!!!==========");
                final Timer timer = new Timer();
                timer.schedule(new java.util.TimerTask() {
                    @Override
                    public void run() {
                        System.out.println("Timer out");
                        mq.stopExecute();
                        timer.cancel();
                    }
                }, 20 * 1000);
            }
        });
    }
}

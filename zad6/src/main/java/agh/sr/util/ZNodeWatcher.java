package agh.sr.util;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import static agh.sr.ZooKeeperClient.TEST_NODE;

public class ZNodeWatcher implements Watcher, ZooKeeperErrorHandler {

    private final ZooKeeper zooKeeper;
    private final ZNodeChildrenWatcher ZNodeChildrenWatcher;
    private Process process = null;

    public ZNodeWatcher(final ZooKeeper zooKeeper) {
        addShutdownHookHandlingClosingProcess(zooKeeper);
        this.zooKeeper = zooKeeper;
        ZNodeChildrenWatcher = new ZNodeChildrenWatcher(zooKeeper);
    }

    private void addShutdownHookHandlingClosingProcess(final ZooKeeper zooKeeper) {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if(process!=null) process.destroy();
            try {
                zooKeeper.close();
            } catch (Exception e) {
                System.err.println("Cannot close ZooKeeper");
            }
        }));
    }

    public void process(WatchedEvent watchedEvent) {
        System.out.println(watchedEvent.toString());
        try {
            switch (watchedEvent.getType()) {
                case NodeCreated:
                    zooKeeper.getChildren(TEST_NODE, ZNodeChildrenWatcher);
                    process = Runtime.getRuntime().exec("cat");
                    break;
                case NodeDeleted:
                    if (process != null) process.destroy();
                    break;
                default:
                    break;
            }
            zooKeeper.exists(TEST_NODE, this);
        } catch (KeeperException e) {
            handleKeeperException(e,TEST_NODE);
        } catch (Exception e) {
            handleOtherException();
        }
    }

    public void handleExistingTestNode() {
        try{
            zooKeeper.getChildren(TEST_NODE, ZNodeChildrenWatcher);
            process = Runtime.getRuntime().exec("cat");
        } catch (KeeperException e) {
            handleKeeperException(e,TEST_NODE);
        } catch (Exception e) {
            handleOtherException();
        }
    }
}

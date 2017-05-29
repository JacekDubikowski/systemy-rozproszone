package agh.sr.util;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

import static agh.sr.ZooKeeperClient.TEST_NODE;

public class ZNodeWatcher implements Watcher, ZooKeeperErrorHandler {

    private final ZooKeeper zooKeeper;
    private final ZNodeChildrenWatcher ZNodeChildrenWatcher;
    private final String[] exec;
    private Process process = null;

    public ZNodeWatcher(final ZooKeeper zooKeeper, String[] exec) {
        addShutdownHookHandlingClosingProcess(zooKeeper);
        this.zooKeeper = zooKeeper;
        ZNodeChildrenWatcher = new ZNodeChildrenWatcher(zooKeeper);
        this.exec = exec;
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
        try {
            switch (watchedEvent.getType()) {
                case NodeCreated:
                    handleExistingTestNode();
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

    private void handleExistingTestNode() throws KeeperException, InterruptedException, IOException {
        zooKeeper.getChildren(TEST_NODE, ZNodeChildrenWatcher);
        process = Runtime.getRuntime().exec(exec);
    }

    public void handleExistingTestNodeBeforeStartingApp() {
        try{
            handleExistingTestNode();
        } catch (KeeperException e) {
            handleKeeperException(e,TEST_NODE);
        } catch (Exception e) {
            handleOtherException();
        }
    }
}

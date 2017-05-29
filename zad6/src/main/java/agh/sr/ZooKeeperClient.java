package agh.sr;

import agh.sr.util.ZNodeWatcher;
import agh.sr.util.ZooKeeperClientInputHandler;
import agh.sr.util.ZooKeeperErrorHandler;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

public class ZooKeeperClient implements Runnable, Watcher, ZooKeeperErrorHandler {

    private final static int TIMEOUT = 10000;
    public final static String TEST_NODE = "/test";
    private final String hostAndPort;
    private final String[] exec;

    public ZooKeeperClient(String hostAndPort, String[] exec) {
        this.hostAndPort = hostAndPort;
        this.exec = exec;
    }

    public void run() {
        try {
            ZooKeeper zooKeeper = new ZooKeeper(hostAndPort, TIMEOUT, this);
            ZNodeWatcher zNodeWatcher = new ZNodeWatcher(zooKeeper, exec);
            Stat stat = zooKeeper.exists(TEST_NODE,zNodeWatcher);
            if(stat!=null) zNodeWatcher.handleExistingTestNodeBeforeStartingApp();
            new Thread(new ZooKeeperClientInputHandler(zooKeeper)).start();
        } catch (KeeperException e) {
            handleKeeperException(e,TEST_NODE);
        } catch (Exception e) {
            handleOtherException();
        }
    }

    public void process(WatchedEvent watchedEvent) {
        System.out.println(watchedEvent.toString());
    }
}

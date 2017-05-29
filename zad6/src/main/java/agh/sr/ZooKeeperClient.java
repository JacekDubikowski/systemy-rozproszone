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

    public ZooKeeperClient(String hostAndPort) {
        this.hostAndPort = hostAndPort;
    }

    public void run() {
        try {
            ZooKeeper zooKeeper = new ZooKeeper(hostAndPort, TIMEOUT, this);
            ZNodeWatcher zNodeWatcher = new ZNodeWatcher(zooKeeper);
            Stat stat = zooKeeper.exists(TEST_NODE,zNodeWatcher);
            if(stat!=null) zNodeWatcher.handleExistingTestNode();
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

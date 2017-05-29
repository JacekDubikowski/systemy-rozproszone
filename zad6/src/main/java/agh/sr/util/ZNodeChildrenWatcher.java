package agh.sr.util;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;

import static agh.sr.ZooKeeperClient.TEST_NODE;

public class ZNodeChildrenWatcher implements Watcher, ZooKeeperErrorHandler  {

    private final ZooKeeper zooKeeper;

    public ZNodeChildrenWatcher(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    public void process(WatchedEvent watchedEvent) {
        System.out.println(watchedEvent);
        try {
            this.zooKeeper.getChildren(TEST_NODE, this);
            System.out.println(countDescendantsOfMainNode());
        } catch (KeeperException e) {
            handleKeeperException(e,TEST_NODE);
        } catch (InterruptedException e) {
            handleOtherException();
        }
    }

    private int countDescendantsOfMainNode() {
        return countDescendantsOfGivenNode(TEST_NODE);
    }

    private int countDescendantsOfGivenNode(String node) {
        try {
            List<String> children  = this.zooKeeper.getChildren(node, false);
            return children.size() + children.stream().mapToInt(child -> countDescendantsOfGivenNode(node.concat("/" + child))).sum();
        } catch (Exception e) {
            return 0;
        }
    }
}

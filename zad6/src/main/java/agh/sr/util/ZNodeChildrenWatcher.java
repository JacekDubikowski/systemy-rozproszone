package agh.sr.util;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;

import java.util.List;

import static agh.sr.ZooKeeperClient.TEST_NODE;

public class ZNodeChildrenWatcher implements Watcher, ZooKeeperErrorHandler  {

    private final ZooKeeper zooKeeper;

    public ZNodeChildrenWatcher(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    public void process(WatchedEvent watchedEvent) {
        try {
            this.zooKeeper.getChildren(TEST_NODE, this);
            handleCountingDescendants();
        } catch (KeeperException e) {
            handleKeeperException(e,TEST_NODE);
        } catch (InterruptedException e) {
            handleOtherException();
        }
    }

    private void handleCountingDescendants() throws KeeperException, InterruptedException {
        Stat stat = this.zooKeeper.exists(TEST_NODE, false);
        if(stat!=null){
            int descendants = countDescendantsOfMainNode();
            stat = this.zooKeeper.exists(TEST_NODE, false);
            if(stat!=null) System.out.println(descendants);
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

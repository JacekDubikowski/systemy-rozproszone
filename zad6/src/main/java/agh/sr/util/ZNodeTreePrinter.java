package agh.sr.util;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

import static agh.sr.ZooKeeperClient.TEST_NODE;

public class ZNodeTreePrinter implements ZooKeeperErrorHandler {

    private final static String POINTER = "'->";
    private final static int BEGINNING_LEVEL = 0;
    private final ZooKeeper zooKeeper;

    public ZNodeTreePrinter(ZooKeeper zooKeeper) {
        this.zooKeeper = zooKeeper;
    }

    public void startPrintingTree() {
        printLevelOfTheThree(TEST_NODE,BEGINNING_LEVEL);
    }

    private void printLevelOfTheThree(String zNodeName, int level) {
        System.out.println(prepareTreeStringForNode(zNodeName,level));
        try {
            zooKeeper.getChildren(zNodeName, false)
                    .forEach(child -> printLevelOfTheThree(zNodeName.concat("/" + child), level + 1));
        } catch (KeeperException e) {
            handleKeeperException(e,zNodeName);
        } catch (InterruptedException e) {
            handleOtherException();
        }
    }

    private String prepareTreeStringForNode(String zNodeName, int level) {
        StringBuffer st = new StringBuffer();
        for(int i = 0 ; i < level; i++) st.append('\t');
        st.append(POINTER);
        st.append(zNodeName);
        return new String(st);
    }
}

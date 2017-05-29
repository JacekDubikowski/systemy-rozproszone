package agh.sr.util;

import org.apache.zookeeper.KeeperException;

public interface ZooKeeperErrorHandler {

    default void handleKeeperException(KeeperException e, String node){
        if(e.code() == KeeperException.Code.NONODE) System.err.println("\"" + node  + "\" node has disappeared!");
        else {
            System.err.println("KeeperException of code: " + e.code() + "appeared!");
        }
    }

    default void handleOtherException(){
        System.err.println("Upss... Something went wrong!");
    }
}

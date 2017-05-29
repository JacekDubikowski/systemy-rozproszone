package agh.sr.util;

import org.apache.zookeeper.ZooKeeper;

import java.util.Scanner;

public class ZooKeeperClientInputHandler implements Runnable{

    private final Scanner scanner = new Scanner(System.in);
    private final ZNodeTreePrinter zNodeTreePrinter;

    public ZooKeeperClientInputHandler(ZooKeeper zooKeeper) {
        this.zNodeTreePrinter = new ZNodeTreePrinter(zooKeeper);
    }

    public void run() {
        handleUserInputInInfiniteLoop();
    }

    private void handleUserInputInInfiniteLoop() {
        printUsageInfo();
        while(true){
            String cmd = scanner.nextLine();
            if(cmd.trim().equalsIgnoreCase("quit")) break;
            else if(cmd.trim().equalsIgnoreCase("tree")){
                    zNodeTreePrinter.startPrintingTree();
            }
            else {
                printUsageInfo();
            }
        }
    }

    private static void printUsageInfo() {
        System.out.println("Possible command (case is ignored):");
        System.out.println("1. quit");
        System.out.println("2. tree");
    }
}

package agh.sr;

public class App{

    public static void main(String[] args) {
        if(checkIfThereAreNotEnoughArgs(args)) System.exit(1);
        try{
            new Thread(new ZooKeeperClient(args[0])).start();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private static boolean checkIfThereAreNotEnoughArgs(String[] args) {
        return args.length < 1;
    }

}

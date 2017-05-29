package agh.sr;

public class App{

    public static void main(String[] args) {
        if(checkIfThereAreNotEnoughArgs(args)) System.exit(1);
        try{
            String exec[] = new String[args.length - 1];
            System.arraycopy(args, 1, exec, 0, exec.length);
            new Thread(new ZooKeeperClient(args[0], exec)).start();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    private static boolean checkIfThereAreNotEnoughArgs(String[] args) {
        return args.length < 2;
    }

}

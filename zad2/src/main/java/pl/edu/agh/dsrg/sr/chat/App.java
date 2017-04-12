package pl.edu.agh.dsrg.sr.chat;

import java.util.Scanner;

public class App
{
    public static void main( String[] args ) throws Exception {
        System.setProperty("java.net.preferIPv4Stack","true");
        System.out.println("Provide your nickname:");
        Scanner scanner = new Scanner(System.in);
        new Chat(scanner.nextLine()).start();
    }
}

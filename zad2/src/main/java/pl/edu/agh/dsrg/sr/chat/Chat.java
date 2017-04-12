package pl.edu.agh.dsrg.sr.chat;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.protocols.*;
import org.jgroups.protocols.pbcast.*;
import org.jgroups.stack.Protocol;
import org.jgroups.stack.ProtocolStack;
import pl.edu.agh.dsrg.sr.chat.protos.ChatOperationProtos;
import pl.edu.agh.dsrg.sr.chat.receivers.ManagementChannelReceiver;
import pl.edu.agh.dsrg.sr.chat.receivers.UsualReceiver;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;

public class Chat {

    public final static String MANAGEMENT_CHANNEL_NAME = "ChatManagement321321";
    private final JChannel managementChannel;
    private final Map<String, JChannel> channels = new HashMap<>();
    private final Map<String, List<String>> usersOnChannels = new HashMap<>();
    private final String name;
    private JChannel current;

    public Chat(String name) throws Exception {
        this.name = name;
        managementChannel = new JChannel(false);
        managementChannel.setName(name);
        ProtocolStack stack = prepareNewStack(null);
        managementChannel.setProtocolStack(stack);
        managementChannel.setReceiver(new ManagementChannelReceiver(usersOnChannels));
    }

    public void start() throws Exception {
        current = managementChannel;
        channels.put(MANAGEMENT_CHANNEL_NAME, managementChannel);
        managementChannel.getProtocolStack().init();
        managementChannel.connect(MANAGEMENT_CHANNEL_NAME);
        managementChannel.getState(null, 10000);

        joinChannel("230.0.0.1");

        Scanner scanner = new Scanner(System.in);
        while(true){
            String newAction = scanner.nextLine();
            if(newAction.startsWith("-j")){
                String address = liftAddressOut(newAction);
                joinChannel(address);
            }
            else if(newAction.startsWith("-l")){
                String address = liftAddressOut(newAction);
                leaveChannel(address);
            }
            else if(newAction.startsWith("-c")){
                String address = liftAddressOut(newAction);
                changeChannel(address);
            }
            else if(newAction.startsWith("-a")){
                synchronized (usersOnChannels) {
                    usersOnChannels.forEach((c, list) -> {
                        System.out.print(c + ": [ ");
                        list.forEach(u -> System.out.print(u + " "));
                        System.out.println("]");
                    });
                }
            }
            else{
                sendMessage(newAction);
            }
        }

    }

    private void sendMessage(String toSend) throws Exception {
        ChatOperationProtos.ChatMessage msg = ChatOperationProtos.ChatMessage.newBuilder()
                .setMessage(toSend)
                .build();

        current.send(new Message(null, null, msg.toByteArray()));
    }

    private void changeChannel(String address) {
        joinChannel(address);
    }

    private String liftAddressOut(String s) {
        return s.split("\\w",2)[1].replace(" ","");
    }

    private void leaveChannel(String address) {
        try{
            isMulticastAddress(address);
            if (channels.containsKey(address)) {
                usersOnChannels.get(address).remove(name);
                updateManagementChannel(address, ChatOperationProtos.ChatAction.ActionType.LEAVE);
            }
            else System.out.println("No such channel");
        } catch (Exception e){
            System.out.println("Invalid address");
        }
    }

    private void joinChannel(String address) {
        try{
            isMulticastAddress(address);
            if(!channels.containsKey(address))createNewJChannel(address);
            else current = channels.get(address);
            updateManagementChannel(address, ChatOperationProtos.ChatAction.ActionType.JOIN);
        } catch (Exception e){
            System.out.println("Invalid address");
        }
    }

    private void updateManagementChannel(String address, ChatOperationProtos.ChatAction.ActionType action) throws Exception {
        ChatOperationProtos.ChatAction chatAction = ChatOperationProtos.ChatAction.newBuilder()
                .setAction(action)
                .setNickname(name)
                .setChannel(address)
                .build();

        Message msg = new Message(null, null, chatAction.toByteArray());
        managementChannel.send(msg);
    }

    private void createNewJChannel(String address) throws Exception {
        JChannel newChannel = new JChannel(false);
        newChannel.setName(name);
        newChannel.setProtocolStack(prepareNewStack(address));
        newChannel.setReceiver(new UsualReceiver(newChannel));
        newChannel.getProtocolStack().init();
        newChannel.connect(address);
        newChannel.getState();
        channels.put(address,newChannel);
        current = newChannel;
    }

    private ProtocolStack prepareNewStack(String address) throws Exception{
        Protocol udp = new UDP();
        if (address != null) {
            if (isMulticastAddress(address))
                udp.setValue("mcast_group_addr", InetAddress.getByName(address));
        }
        ProtocolStack stack=new ProtocolStack();
        stack.addProtocol(udp)
                .addProtocol(new PING())
                .addProtocol(new MERGE3())
                .addProtocol(new FD_SOCK())
                .addProtocol(new FD_ALL().setValue("timeout", 12000).setValue("interval", 3000))
                .addProtocol(new VERIFY_SUSPECT())
                .addProtocol(new BARRIER())
                .addProtocol(new NAKACK2())
                .addProtocol(new UNICAST3())
                .addProtocol(new STABLE())
                .addProtocol(new GMS())
                .addProtocol(new UFC())
                .addProtocol(new MFC())
                .addProtocol(new FRAG2())
                .addProtocol(new STATE_TRANSFER())
                .addProtocol(new FLUSH());
        return stack;
    }

    private boolean isMulticastAddress(String name) throws UnknownHostException {
        return InetAddress.getByName(name).isMulticastAddress();
    }
}

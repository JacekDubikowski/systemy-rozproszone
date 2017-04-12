package pl.edu.agh.dsrg.sr.chat.receivers;

import com.google.protobuf.InvalidProtocolBufferException;
import org.jgroups.Address;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import org.jgroups.View;
import pl.edu.agh.dsrg.sr.chat.Chat;
import pl.edu.agh.dsrg.sr.chat.protos.ChatOperationProtos;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ManagementChannelReceiver extends ReceiverAdapter {

    private final Map<String, List<String>> usersOnChannels;

    public ManagementChannelReceiver(Map<String, List<String>> usersOnChannels) {
        this.usersOnChannels = usersOnChannels;
    }

    @Override
    public void receive(Message msg) {
        synchronized(usersOnChannels) {
            ChatOperationProtos.ChatAction chatAction;
            try {
                chatAction = ChatOperationProtos.ChatAction.parseFrom(msg.getBuffer());
                String channelName = chatAction.getChannel();
                String nick = chatAction.getNickname();
                switch (chatAction.getAction()){
                    case JOIN:
                        if (!usersOnChannels.containsKey(channelName)){
                            List <String> newChannel = new LinkedList<>();
                            newChannel.add(nick);
                            usersOnChannels.put(channelName, newChannel);
                        }else usersOnChannels.get(channelName).add(nick);
                        break;
                    case LEAVE:
                        List<String> channelNicks = usersOnChannels.get(channelName);
                        channelNicks.remove(nick);
                        if (channelNicks.isEmpty()) usersOnChannels.remove(channelName);
                        break;
                }
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void getState(OutputStream output) throws Exception {
        synchronized (usersOnChannels) {
            ChatOperationProtos.ChatState.Builder builder = ChatOperationProtos.ChatState.newBuilder();

            for (Map.Entry<String, List<String>> entry : usersOnChannels.entrySet()) {
                List<String> users = entry.getValue();

                for (String user : users) {
                    builder.addStateBuilder()
                            .setAction(ChatOperationProtos.ChatAction.ActionType.JOIN)
                            .setChannel(entry.getKey())
                            .setNickname(user);
                }
            }
            ChatOperationProtos.ChatState state = builder.build();

            state.writeTo(output);
        }
    }

    @Override
    public void setState(InputStream input) throws Exception {
        synchronized(usersOnChannels) {
            ChatOperationProtos.ChatState state = ChatOperationProtos.ChatState.parseFrom(input);
            usersOnChannels.clear();
            for (ChatOperationProtos.ChatAction chatAction : state.getStateList()) {
                String channelName = chatAction.getChannel();
                String nick = chatAction.getNickname();
                if (!usersOnChannels.containsKey(channelName)) usersOnChannels.put(channelName, new LinkedList<>());
                usersOnChannels.get(channelName).add(nick);
            }
        }
    }

    @Override
    public void viewAccepted(View view) {
        synchronized (usersOnChannels) {
            List<String> currentUsers = view.getMembers().stream().map(Address::toString).collect(Collectors.toList());
            List<String> keysToRemove = new LinkedList<>();
            for (Map.Entry<String, List<String>> entry : usersOnChannels.entrySet()) {
                entry.getValue().retainAll(currentUsers);
                if(entry.getValue().isEmpty()) keysToRemove.add(entry.getKey());
            }
            keysToRemove.forEach(usersOnChannels::remove);
        }
    }
}

package pl.edu.agh.dsrg.sr.chat.receivers;

import com.google.protobuf.InvalidProtocolBufferException;
import org.jgroups.Address;
import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.ReceiverAdapter;
import pl.edu.agh.dsrg.sr.chat.protos.ChatOperationProtos;

public class UsualReceiver extends ReceiverAdapter {
    private final JChannel channel;

    public UsualReceiver(JChannel channel) {
        this.channel = channel;
    }

    @Override
    public void receive(Message msg) {
        try {
            Address address = msg.getSrc();
            ChatOperationProtos.ChatMessage message = ChatOperationProtos.ChatMessage.parseFrom(msg.getBuffer());

            String channelName = channel.getClusterName();
            String nick = channel.getName(address);
            String text = message.getMessage();

            if(!channel.getName().equals(nick))System.out.println(channelName + " --- " + nick + ": " + text);
        } catch (InvalidProtocolBufferException e) {
            e.printStackTrace();
        }
    }
}

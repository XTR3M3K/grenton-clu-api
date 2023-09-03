package pl.houseware.grenton.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import pl.houseware.grenton.message.GrentonCommand;
import pl.houseware.grenton.message.GrentonMessage;
import pl.houseware.grenton.message.GrentonMessageType;

import java.util.List;

public class GrentonMessageCodec extends MessageToMessageCodec<String, GrentonCommand> {
    @Override protected void encode(ChannelHandlerContext ctx, GrentonCommand msg, List<Object> out) {
        out.add(msg.serialize());
    }

    @Override protected void decode(ChannelHandlerContext ctx, String msg, List<Object> out) {
        var parts = msg.split(":");

        var type = parts[0];
        var ip = parts[1];
        var sessionId = parts[2];

        var command = msg.substring(type.length() + ip.length() + sessionId.length() + 3);

        out.add(new GrentonMessage(GrentonMessageType.fromString(type), ip, sessionId, command));
    }
}

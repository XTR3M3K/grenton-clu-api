package pl.houseware.grenton.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.AllArgsConstructor;
import pl.houseware.grenton.message.GrentonMessage;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@AllArgsConstructor
public class MessageHandler extends SimpleChannelInboundHandler<GrentonMessage> {
    private ConcurrentHashMap<String, CompletableFuture<Object>> futures;

    @Override protected void channelRead0(ChannelHandlerContext ctx, GrentonMessage msg) {
        if (!futures.containsKey(msg.getSessionId())) {
            return;
        }

        futures.get(msg.getSessionId()).complete(msg.getPayload());

        futures.remove(msg.getSessionId());
    }
}

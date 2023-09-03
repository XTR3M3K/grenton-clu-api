package pl.houseware.grenton;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.handler.codec.DatagramPacketDecoder;
import lombok.Data;
import pl.houseware.grenton.codec.GrentonEncryptionDecoder;
import pl.houseware.grenton.codec.GrentonEncryptionEncoder;
import pl.houseware.grenton.codec.GrentonMessageCodec;
import pl.houseware.grenton.encryption.GrentonEncryption;
import pl.houseware.grenton.message.GrentonCommand;
import pl.houseware.grenton.handler.MessageHandler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

@Data
public class GrentonCLU {
    private final static Logger logger = Logger.getLogger(GrentonCLU.class.getName());

    private final String host;
    private final int port;

    private final String secretKey;
    private final String iv;

    private Channel channel;

    private ConcurrentHashMap<String, CompletableFuture<Object>> futures = new ConcurrentHashMap<>();

    public GrentonCLU(String host, int port, String secretKey, String iv) {
        this.host = host;
        this.port = port;

        this.secretKey = secretKey;
        this.iv = iv;
    }

    public void connect() {
        var bootstrap = new Bootstrap();

        EventLoopGroup group = new NioEventLoopGroup();

        bootstrap
                .group(group)
                .remoteAddress(this.host, this.port)
                .channel(NioDatagramChannel.class);

        bootstrap.handler(new ChannelInitializer<DatagramChannel>() {
            @Override
            protected void initChannel(DatagramChannel ch) {
                ChannelPipeline pipeline = ch.pipeline();

                var encryption = new GrentonEncryption(secretKey, iv);

                pipeline.addLast("encryption-encoder", new GrentonEncryptionEncoder(encryption));
                pipeline.addLast("encryption-decoder", new DatagramPacketDecoder(new GrentonEncryptionDecoder(encryption)));

                pipeline.addLast("codec", new GrentonMessageCodec());

                pipeline.addLast("handler", new MessageHandler(futures));
            }
        });

        try {
            logger.log(Level.INFO, "Connecting to Grenton CLU at {0}:{1}", new Object[]{this.host, this.port});

            ChannelFuture future = bootstrap.connect().sync();

            this.channel = future.channel();

            if (future.isSuccess()) {
                logger.log(Level.INFO, "Connected to Grenton CLU at {0}:{1}", new Object[]{this.host, this.port});
            }
        } catch (InterruptedException e) {
            logger.log(Level.INFO, "Failed to connect to Grenton CLU at {0}:{1}", new Object[]{this.host, this.port});
            logger.log(Level.SEVERE, e.getMessage());
        }
    }

    public Future<Object> sendCommand(String command) {
        return sendCommand(new GrentonCommand(command));
    }

    public Future<Object> sendCommand(GrentonCommand command) {
        CompletableFuture<Object> future = new CompletableFuture<>();

        this.futures.put(command.getSessionId(), future);

        try {
            this.channel.writeAndFlush(command).sync();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        return future;
    }
}

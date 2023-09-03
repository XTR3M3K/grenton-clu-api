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

import java.util.concurrent.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Data
public class GrentonCLU {
    private final static Logger logger = Logger.getLogger(GrentonCLU.class.getName());

    private final String host;
    private final int port;
    private final int timeout;

    private final String secretKey;
    private final String iv;

    private Channel channel;

    private ConcurrentHashMap<String, CompletableFuture<Object>> futures = new ConcurrentHashMap<>();
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    public GrentonCLU(String host, int port, String secretKey, String iv) {
        this(host, port, secretKey, iv, 1000);
    }

    public GrentonCLU(String host, int port, String secretKey, String iv, int timeout) {
        this.host = host;
        this.port = port;

        this.secretKey = secretKey;
        this.iv = iv;

        this.timeout = timeout;
    }

    public void connect() {
        var bootstrap = new Bootstrap();

        EventLoopGroup group = new NioEventLoopGroup();

        bootstrap
                .group(group)
                .remoteAddress(this.host, this.port)
                .channel(NioDatagramChannel.class);

        bootstrap.handler(new ChannelInitializer<NioDatagramChannel>() {
            @Override
            protected void initChannel(NioDatagramChannel ch) {
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

        executor.schedule(() -> {
            if (futures.containsKey(command.getSessionId())) {
                futures.get(command.getSessionId()).completeExceptionally(new TimeoutException());

                futures.remove(command.getSessionId());
            }
        }, this.timeout, TimeUnit.MILLISECONDS);

        return future;
    }
}

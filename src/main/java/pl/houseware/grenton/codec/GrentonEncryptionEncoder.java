package pl.houseware.grenton.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import pl.houseware.grenton.encryption.GrentonEncryption;

public class GrentonEncryptionEncoder extends MessageToByteEncoder<String> {
    private final GrentonEncryption encryption;

    public GrentonEncryptionEncoder(GrentonEncryption encryption) {
        this.encryption = encryption;
    }

    @Override protected void encode(ChannelHandlerContext ctx, String msg, ByteBuf out) {
        out.writeBytes(encryption.encode(msg));
    }
}

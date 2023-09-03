package pl.houseware.grenton.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import pl.houseware.grenton.encryption.GrentonEncryption;

import java.util.List;

public class GrentonEncryptionDecoder extends MessageToMessageDecoder<ByteBuf> {
    private final GrentonEncryption encryption;

    public GrentonEncryptionDecoder(GrentonEncryption encryption) {
        this.encryption = encryption;
    }

    @Override protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) {
        byte[] arr;

        if (buf.hasArray()) {
            arr = buf.array();
        } else {
            arr = new byte[buf.readableBytes()];

            buf.getBytes(buf.readerIndex(), arr);
        }

        out.add(encryption.decode(arr));
    }
}

package pl.houseware.grenton.message;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.UUID;

public class GrentonCommand extends GrentonMessage {
    public GrentonCommand(String command) {
        super(GrentonMessageType.REQUEST, "127.0.0.1", UUID.randomUUID().toString().substring(0, 8), command);

        try {
            this.setIp(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException ignored) {}
    }
}

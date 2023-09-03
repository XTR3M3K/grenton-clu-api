package pl.houseware.grenton.message;

import lombok.Data;

@Data
public class GrentonMessage {
    private GrentonMessageType type;
    private String ip;
    private String sessionId;
    private String payload;

    public GrentonMessage(GrentonMessageType type, String ip, String sessionId, String payload) {
        this.type = type;
        this.ip = ip;
        this.sessionId = sessionId;
        this.payload = payload;
    }

    public String serialize() {
        return this.type + ":" + this.ip + ":" + this.sessionId + ":" + this.payload + "\r\n";
    }
}

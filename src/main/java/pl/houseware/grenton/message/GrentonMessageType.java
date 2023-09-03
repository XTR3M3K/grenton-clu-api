package pl.houseware.grenton.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum GrentonMessageType {
    REQUEST("req"),
    RESPONSE("resp");

    private final String name;

    @Override public String toString() {
        return this.name;
    }

    public static GrentonMessageType fromString(String name) {
        for (GrentonMessageType type : GrentonMessageType.values()) {
            if (type.name.equals(name)) {
                return type;
            }
        }

        return null;
    }
}

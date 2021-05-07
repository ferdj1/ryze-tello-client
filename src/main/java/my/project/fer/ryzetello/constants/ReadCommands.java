package my.project.fer.ryzetello.constants;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public enum ReadCommands {
    SPEED("speed"),
    BATTERY("battery"),
    TIME("time"),
    WIFI("wifi"),
    SDK("sdk"),
    SN("sn");

    private final String value;

    ReadCommands(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static List<String> valuesList() {
        return Arrays.stream(ReadCommands.values()).map(x -> x.value).collect(Collectors.toList());
    }

}

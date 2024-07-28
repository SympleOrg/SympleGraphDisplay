package top.symple.symplegraphdisplay.packets;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class UpdateGraphDataPacket implements Packet {
    private final HashMap<Integer, List<Double>> data;

    public UpdateGraphDataPacket(HashMap<Integer, List<Double>> data) {
        this.data = data;
    }

    @Override
    public String getId() {
        return getClass().getSimpleName();
    }

    @Override
    public String encodePacket() {
        String allSets = this.data.entrySet().stream().map((entry) -> encodeDataset(entry.getKey(), entry.getValue())).collect(Collectors.joining(","));

        return String.format("{\"data\": [%s]}", allSets);
    }

    private String encodeDataset(int id, List<Double> data) {
        return String.format("{\"id\":\"%s\",\"data\":[", id) +
                data.stream().map(Object::toString).collect(Collectors.joining(",")) +
                "]}";
    }
}

package top.symple.symplegraphdisplay.packets;


import top.symple.symplegraphdisplay.util.ColorRGBA;

public class InitGraphDataPacket implements Packet {
    private final int id;
    private final String label;
    private final ColorRGBA color;
    private final ColorRGBA fillColor;

    public InitGraphDataPacket(int id, String label, ColorRGBA color, ColorRGBA fillColor) {
        this.id = id;
        this.label = label;
        this.color = color;
        this.fillColor = fillColor;

    }

    @Override
    public String getId() {
        return getClass().getSimpleName();
    }

    @Override
    public String encodePacket() {
        return String.format("{\"id\": \"%s\", \"label\": \"%s\", \"color\": %s, \"fillColor\": %s}", id, label, color == null ? "null" : "\""+ color +"\"", fillColor == null ? "null" : "\""+ fillColor +"\"");
    }
}

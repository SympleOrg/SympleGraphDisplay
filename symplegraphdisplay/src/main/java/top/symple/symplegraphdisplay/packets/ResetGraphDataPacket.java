package top.symple.symplegraphdisplay.packets;

public class ResetGraphDataPacket implements Packet {
    public ResetGraphDataPacket() { }

    @Override
    public String getId() {
        return getClass().getSimpleName();
    }

    @Override
    public String encodePacket() {
        return null;
    }
}

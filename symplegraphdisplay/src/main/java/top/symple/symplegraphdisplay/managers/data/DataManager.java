package top.symple.symplegraphdisplay.managers.data;

import org.java_websocket.WebSocket;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import top.symple.symplegraphdisplay.SympleGraphDisplay;
import top.symple.symplegraphdisplay.packets.InitGraphDataPacket;
import top.symple.symplegraphdisplay.packets.UpdateGraphDataPacket;
import top.symple.symplegraphdisplay.util.ColorRGBA;

public class DataManager {
    private final HashMap<Integer, DataField> fieldsMap = new HashMap<>();

    public DataManager() { }

    public void registerClass(DataListenerGroup dataListenerGroup) {
        for(Field field : dataListenerGroup.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if(!field.isAnnotationPresent(DataListener.class)) continue;

            DataListener annotation = field.getAnnotation(DataListener.class);

            String label = annotation.label();
            if (label == null || label.trim().isEmpty()) label = field.getName();

            Color color = annotation.color();
            if(color == null || color.alpha() < 0) color = null;

            Color fillColor = annotation.fillColor();
            if(fillColor == null || fillColor.alpha() < 0) fillColor = null;

            DataField dataField = new DataField(label, ColorRGBA.fromColor(color), ColorRGBA.fromColor(fillColor), field, dataListenerGroup);

            fieldsMap.put(field.hashCode(), dataField);

            this.sendGraphInitPacket(field.hashCode(), dataField.getLabel(), dataField.getColor(), dataField.getFillColor());
        }
    }

    public void reset() {
        this.fieldsMap.clear();
    }

    public void sendAllGraphInitData(Collection<WebSocket> clients) {
        for(Map.Entry<Integer, DataField> entry : fieldsMap.entrySet()) {
            sendGraphInitPacket(clients, entry.getKey(), entry.getValue().getLabel(), entry.getValue().getColor(), entry.getValue().getFillColor());
        }
    }

    public void sendGraphInitPacket(Collection<WebSocket> clients, int id, String label, ColorRGBA color, ColorRGBA fillColor) {
        SympleGraphDisplay.getInstance().sendPacket(clients, new InitGraphDataPacket(id, label, color, fillColor));
    }

    public void sendGraphInitPacket(int id, String label, ColorRGBA color, ColorRGBA fillColor) {
        SympleGraphDisplay.getInstance().sendPacket(new InitGraphDataPacket(id, label, color, fillColor));
    }

    public void sendGraphUpdateDataPacket(HashMap<Integer, List<Double>> newData) {
        SympleGraphDisplay.getInstance().sendPacket(new UpdateGraphDataPacket(newData));
    }

    public void sendGraphUpdateDataPacket(Collection<WebSocket> clients, HashMap<Integer, List<Double>> newData) {
        SympleGraphDisplay.getInstance().sendPacket(clients, new UpdateGraphDataPacket(newData));
    }


    public void sendAllPreviousGraphData(Collection<WebSocket> clients) {
        HashMap<Integer, List<Double>> newValues = new HashMap<>();
        for(Map.Entry<Integer, DataField> entry : fieldsMap.entrySet()) {
            newValues.put(entry.getKey(), entry.getValue().getData());
        }

        sendGraphUpdateDataPacket(clients, newValues);
    }

    public void addAllNewData() {
        HashMap<Integer, List<Double>> newValues = new HashMap<>();
        for(Map.Entry<Integer, DataField> entry : fieldsMap.entrySet()) {
            double value = 0;
            try {
                value = entry.getValue().getField().getDouble(entry.getValue().getDataListenerGroup());
            } catch (IllegalAccessException e) {
                System.out.println(e);
            }

            entry.getValue().addData(value);

            final double finalValue = value;
            newValues.put(entry.getKey(), new ArrayList<Double>(){{ add(finalValue); }});
        }

        sendGraphUpdateDataPacket(newValues);
    }

    private static class DataField {
        private final List<Double> data;
        private final String label;
        private final ColorRGBA color;
        private final ColorRGBA fillColor;
        private final Field field;
        private final DataListenerGroup dataListenerGroup;


        private DataField(String label, ColorRGBA color, ColorRGBA fillColor, Field field, DataListenerGroup dataListenerGroup) {
            this.label = label;
            this.color = color;
            this.fillColor = fillColor;
            this.field = field;
            this.dataListenerGroup = dataListenerGroup;

            this.data = new ArrayList<>();
        }

        public String getLabel() {
            return label;
        }

        public ColorRGBA getColor() {
            return color;
        }

        public ColorRGBA getFillColor() {
            return fillColor;
        }

        public Field getField() {
            return field;
        }

        public DataListenerGroup getDataListenerGroup() {
            return dataListenerGroup;
        }

        public List<Double> getData() {
            return data;
        }

        public void addData(double... data) {
            for (double d : data) {
                this.data.add(d);
            }
        }
    }
}

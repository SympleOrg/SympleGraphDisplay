package top.symple.symplegraphdisplay;

import org.java_websocket.WebSocket;

import java.util.Collection;

import top.symple.symplegraphdisplay.apps.WebsocketServer;
import top.symple.symplegraphdisplay.managers.data.DataListenerGroup;
import top.symple.symplegraphdisplay.managers.data.DataManager;
import top.symple.symplegraphdisplay.packets.Packet;
import top.symple.symplegraphdisplay.packets.ResetGraphDataPacket;
import top.symple.symplegraphdisplay.util.Timer;

public class SympleGraphDisplayCore {
    private static final double DEFAULT_UPDATE_TIME = 0.1;

    private final WebsocketServer websocketServer;
    private final DataManager dataManager;

    private double updateTime = DEFAULT_UPDATE_TIME;
    private final Timer timer = new Timer();

    protected SympleGraphDisplayCore() {
        this.websocketServer = new WebsocketServer(3334, this);
        this.websocketServer.start();

        this.dataManager = new DataManager(this);
    }

    public void init() {
        reset();
    }

    public void reset() {
        this.timer.reset();
        this.dataManager.reset();
        this.updateTime = DEFAULT_UPDATE_TIME;
        this.sendPacket(new ResetGraphDataPacket());
    }

    public void run() {
        timer.run();
        if(timer.getCurrentTime() >= updateTime) {
            update();
            timer.reset();
        }
    }

    public void update() {
        this.dataManager.addAllNewData();
    }

    public void registerDataListenerGroup(DataListenerGroup... dataListenerGroup) {
        for (DataListenerGroup dlg : dataListenerGroup) {
            this.dataManager.registerClass(dlg);
        }
    }

    public void sendPacket(Collection<WebSocket> clients, Packet packet) {
        this.websocketServer.broadcast(String.format("{\"type\": \"%s\", \"data\": %s}", packet.getId(), packet.encodePacket()), clients);
    }

    public void sendPacket(Packet packet) {
        this.sendPacket(this.websocketServer.getConnections(), packet);
    }

    public DataManager getDataManager() {
        return dataManager;
    }

    public WebsocketServer getWebsocketServer() {
        return websocketServer;
    }

    public double getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(double updateTime) {
        this.updateTime = updateTime;
    }
}

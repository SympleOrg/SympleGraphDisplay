package top.symple.symplegraphdisplay.apps;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import top.symple.symplegraphdisplay.SympleGraphDisplay;
import top.symple.symplegraphdisplay.SympleGraphDisplayCore;
import top.symple.symplegraphdisplay.packets.ResetGraphDataPacket;

public class WebsocketServer extends WebSocketServer {
    private final SympleGraphDisplayCore core;

    public WebsocketServer(int port, SympleGraphDisplayCore core) {
        super(new InetSocketAddress(port));
        this.core = core;
    }

    @Override
    public void onOpen(WebSocket webSocket, ClientHandshake clientHandshake) {
        List<WebSocket> socketList = new ArrayList<WebSocket>(){{ add(webSocket); }};

        this.core.sendPacket(new ResetGraphDataPacket());
        this.core.getDataManager().sendAllGraphInitData(socketList);
        this.core.getDataManager().sendAllPreviousGraphData(socketList);
    }

    @Override
    public void onClose(WebSocket webSocket, int i, String s, boolean b) {
    }

    @Override
    public void onMessage(WebSocket webSocket, String s) {

    }

    @Override
    public void onError(WebSocket webSocket, Exception e) {

    }

    @Override
    public void onStart() {

    }
}
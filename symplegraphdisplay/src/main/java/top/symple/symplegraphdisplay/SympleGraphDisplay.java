package top.symple.symplegraphdisplay;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.util.Log;

import com.qualcomm.ftccommon.FtcEventLoop;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpModeManager;
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl;
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerNotifier;
import com.qualcomm.robotcore.eventloop.opmode.OpModeRegistrar;
import com.qualcomm.robotcore.util.WebHandlerManager;
import com.qualcomm.robotcore.util.WebServer;

import org.firstinspires.ftc.ftccommon.external.OnCreate;
import org.firstinspires.ftc.ftccommon.external.OnCreateEventLoop;
import org.firstinspires.ftc.ftccommon.external.OnDestroy;
import org.firstinspires.ftc.ftccommon.external.WebHandlerRegistrar;
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.robotcore.internal.system.Misc;
import org.firstinspires.ftc.robotcore.internal.webserver.WebHandler;
import org.firstinspires.ftc.robotserver.internal.webserver.MimeTypesUtil;
import org.java_websocket.WebSocket;

import java.io.IOException;
import java.util.Collection;

import fi.iki.elonen.NanoHTTPD;
import top.symple.symplegraphdisplay.apps.WebsocketServer;
import top.symple.symplegraphdisplay.managers.data.DataListenerGroup;
import top.symple.symplegraphdisplay.managers.data.DataManager;
import top.symple.symplegraphdisplay.packets.Packet;
import top.symple.symplegraphdisplay.packets.ResetGraphDataPacket;
import top.symple.symplegraphdisplay.util.Timer;

public class SympleGraphDisplay implements OpModeManagerNotifier.Notifications {
    private static final String PREFS_NAME = "SympleGraphDisplay";
    private static final String PREFS_AUTO_ENABLE_KEY = "autoEnable";
    private SharedPreferences prefs;

    private static SympleGraphDisplay instance;

    private final WebsocketServer websocketServer;
    private final DataManager dataManager;

    private OpModeManagerImpl opModeManager;

    private double updateTime = 0.05;
    private final Timer timer = new Timer();

    private boolean isEnabled = false;

    private static boolean suppressOpMode = false;

    @OpModeRegistrar
    public static void registerOpMode(OpModeManager manager) {
        if(instance != null && !suppressOpMode) {
            instance.internalRegisterOpMode(manager);
        }
    }

    public static void suppressOpMode() {
        suppressOpMode = true;
    }

    @OnCreate
    public static void onCreate(Context context) {
        if (instance == null) {
            instance = new SympleGraphDisplay();
        }
    }

    @WebHandlerRegistrar
    public static void attachWebServer(Context context, WebHandlerManager manager) {
        if (instance != null) {
            instance.internalAttachWebServer(manager.getWebServer());

        }
    }

    @OnCreateEventLoop
    public static void attachEventLoop(Context context, FtcEventLoop eventLoop) {
        if (instance != null) {
            instance.internalAttachEventLoop(eventLoop);
        }
    }

    @OnDestroy
    public static void destroy(Context context) throws IOException, InterruptedException {
        if(instance != null) {
            instance.close();
            instance = null;
        }
    }

    private boolean getAutoEnable() {
        return prefs.getBoolean(PREFS_AUTO_ENABLE_KEY, true);
    }

    private void setAutoEnable(boolean autoEnable) {
        prefs.edit()
            .putBoolean(PREFS_AUTO_ENABLE_KEY, autoEnable)
            .apply();
    }

    private void enable() {
        if(isEnabled) return;

        setAutoEnable(true);

        isEnabled = true;
    }

    private void disable() {
        if(!isEnabled) return;

        setAutoEnable(false);
        isEnabled = false;
    }

    private void close() throws IOException, InterruptedException {
        websocketServer.stop();

        if (opModeManager != null) {
            opModeManager.unregisterListener(this);
        }
        disable();
    }

    private void internalRegisterOpMode(OpModeManager manager) {
        manager.register(
            new OpModeMeta.Builder()
                .setName("Enable/Disable Graph Display")
                .setFlavor(OpModeMeta.Flavor.TELEOP)
                .setGroup("graph-display")
                .build(),
            new LinearOpMode() {
                @Override
                public void runOpMode() throws InterruptedException {
                    telemetry.log().add(Misc.formatInvariant("Status: %s, Start to %s it.", isEnabled ? "enabled" : "disabled", isEnabled ? "disable" : "enable"));
                    telemetry.update();

                    waitForStart();

                    if(isStopRequested()) return;

                    if(isEnabled) {
                        disable();
                    } else {
                        enable();
                    }
                }
            }
        );
    }


    private void internalAttachWebServer(WebServer webServer) {
        if (webServer == null) return;

        Activity activity = AppUtil.getInstance().getActivity();
        if (activity == null) return;

        WebHandlerManager webHandlerManager = webServer.getWebHandlerManager();
        AssetManager assetManager = activity.getAssets();
        webHandlerManager.register("/graph",
                newStaticAssetHandler(assetManager, "graph/html/index.html"));
        webHandlerManager.register("/graph/",
                newStaticAssetHandler(assetManager, "graph/html/index.html"));

        webHandlerManager.register("/graph/assets/chartjs-plugin-zoom.min.js",
                newStaticAssetHandler(assetManager, "graph/assets/chartjs-plugin-zoom.min.js"));
        webHandlerManager.register("/graph/assets/index.js",
                newStaticAssetHandler(assetManager, "graph/assets/index.js"));
        webHandlerManager.register("/graph/assets/style.css",
                newStaticAssetHandler(assetManager, "graph/assets/style.css"));
    }

    private void internalAttachEventLoop(FtcEventLoop eventLoop) {
        // this could be called multiple times within the lifecycle of the dashboard
        if (opModeManager != null) {
            opModeManager.unregisterListener(this);
        }

        opModeManager = eventLoop.getOpModeManager();
        if (opModeManager != null) {
            opModeManager.registerListener(this);
        }
    }

    private WebHandler newStaticAssetHandler(final AssetManager assetManager, final String file) {
        return new WebHandler() {
            @Override
            public NanoHTTPD.Response getResponse(NanoHTTPD.IHTTPSession session)
                    throws IOException {
                if (session.getMethod() == NanoHTTPD.Method.GET) {
                    String mimeType = MimeTypesUtil.determineMimeType(file);
                    return NanoHTTPD.newChunkedResponse(NanoHTTPD.Response.Status.OK,
                            mimeType, assetManager.open(file));
                } else {
                    return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.NOT_FOUND,
                            NanoHTTPD.MIME_PLAINTEXT, "");
                }
            }
        };
    }


    private SympleGraphDisplay() {
        this.websocketServer = new WebsocketServer(3334);
        this.websocketServer.start();

        Activity activity = AppUtil.getInstance().getActivity();
        prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if(getAutoEnable()) {
            enable();
        }

        this.dataManager = new DataManager();
    }

    public static SympleGraphDisplay getInstance() {
        return instance;
    }

    public void init() {
        reset();
    }

    public void reset() {
        this.timer.reset();
        this.dataManager.reset();
        this.updateTime = 1;
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

    public double getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(double updateTime) {
        this.updateTime = updateTime;
    }

    @Override
    public void onOpModePreInit(OpMode opMode) {

    }

    @Override
    public void onOpModePreStart(OpMode opMode) {

    }

    @Override
    public void onOpModePostStop(OpMode opMode) {

    }
}

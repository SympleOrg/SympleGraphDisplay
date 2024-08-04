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

public class SympleGraphDisplay {
    private static final String PREFS_NAME = "SympleGraphDisplay";
    private static final String PREFS_AUTO_ENABLE_KEY = "autoEnable";
    private SharedPreferences prefs;

    private static SympleGraphDisplay instance;
    private SympleGraphDisplayCore core;

    private boolean isEnabled = false;

    // register the op mode
    @OpModeRegistrar
    public static void registerOpMode(OpModeManager manager) {
        if(instance != null) {
            instance.internalRegisterOpMode(manager);
        }
    }

    // create new instance when the robot is turned on
    @OnCreate
    public static void onCreate(Context context) {
        if (instance == null) {
            instance = new SympleGraphDisplay();
        }
    }

    // register the web pages
    @WebHandlerRegistrar
    public static void attachWebServer(Context context, WebHandlerManager manager) {
        if (instance != null) {
            instance.internalAttachWebServer(manager.getWebServer());
        }
    }

    // close everything when the robot is turned off
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
        this.core.getWebsocketServer().stop();

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

        String[] staticAssets = new String[] {
                "chart.min.js",
                "chartjs-plugin-autocolors.js",
                "hammerjs.js",
                "chartjs-plugin-zoom.min.js",
                "index.js",
                "style.css"
        };

        for (String path : staticAssets) {
            webHandlerManager.register("/graph/assets/"+path,
                    newStaticAssetHandler(assetManager, "graph/assets/"+path));
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
        this.core = new SympleGraphDisplayCore();

        Activity activity = AppUtil.getInstance().getActivity();
        prefs = activity.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        if(getAutoEnable()) {
            enable();
        }
    }

    public static SympleGraphDisplay getInstance() {
        return instance;
    }

    /**
     * reset the current graph data
     */
    public void reset() {
        this.core.reset();
    }

    /**
     *  use this to update the graph
     */
    public void run() {
        this.core.run();
    }

    /**
     * all all new points to the graph
     * please use {@link SympleGraphDisplay#run()} instead
     */
    public void update() {
        this.core.update();
    }

    /**
     * Add data listener
     * @param dataListenerGroup data listener
     */
    public void registerDataListenerGroup(DataListenerGroup... dataListenerGroup) {
        this.core.registerDataListenerGroup(dataListenerGroup);
    }

    public DataManager getDataManager() {
        return this.core.getDataManager();
    }

    /**
     * Get the delay between graph updates
     * @return delay between graph updates in sec
     */
    public double getUpdateTime() {
        return this.core.getUpdateTime();
    }

    /**
     * Set the delay between graph updates
     * @param updateTime time in sec
     */
    public void setUpdateTime(double updateTime) {
        this.core.setUpdateTime(updateTime);
    }
}

package top.symple.symplegraphdisplay.routes;

import fi.iki.elonen.NanoHTTPD;
import top.symple.symplegraphdisplay.managers.route.Route;
import top.symple.symplegraphdisplay.managers.route.RouteGroup;
import top.symple.symplegraphdisplay.managers.route.RouteListener;
import top.symple.symplegraphdisplay.util.Utils;

@RouteGroup(path = "/assets")
public class AssetsRouteGroup implements RouteListener {
    @Route(route = "/:file")
    private NanoHTTPD.Response getFile(NanoHTTPD.IHTTPSession session, String filename) {
        try {
            return Utils.newFixedFileResponse("assets/" + filename);
        } catch (Exception e) {
            System.out.println(e);
        }

        return Utils.newFixed404Response();
    }
}

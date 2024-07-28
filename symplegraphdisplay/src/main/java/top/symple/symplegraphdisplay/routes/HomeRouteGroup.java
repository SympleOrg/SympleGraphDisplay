package top.symple.symplegraphdisplay.routes;

import fi.iki.elonen.NanoHTTPD;
import top.symple.symplegraphdisplay.managers.route.Route;
import top.symple.symplegraphdisplay.managers.route.RouteGroup;
import top.symple.symplegraphdisplay.managers.route.RouteListener;
import top.symple.symplegraphdisplay.util.Utils;

@RouteGroup
public class HomeRouteGroup implements RouteListener {
    @Route(route = "/")
    public NanoHTTPD.Response home() {
        try {
            return Utils.newFixedFileResponse("html/index.html");
        } catch (Exception e) {
            System.out.println(e);
        }

        return Utils.newFixed404Response();
    }
}

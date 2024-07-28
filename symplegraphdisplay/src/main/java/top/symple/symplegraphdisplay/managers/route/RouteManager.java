package top.symple.symplegraphdisplay.managers.route;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class RouteManager {
    private final HashMap<String, SimpleRoute> registeredRoutes = new HashMap<>();

    public RouteManager() { }

    public void registerRoutes(RouteListener listener) {
        if (!listener.getClass().isAnnotationPresent(RouteGroup.class)) throw new RuntimeException("Route classes must use the @RouteGroup");

        String parentPath = listener.getClass().getAnnotation(RouteGroup.class).path();
        if(parentPath == null) parentPath = "";

        for (java.lang.reflect.Method method : listener.getClass().getDeclaredMethods()) {
            method.setAccessible(true);

            if (!method.isAnnotationPresent(Route.class)) continue;

            if (method.getReturnType() != NanoHTTPD.Response.class)
                throw new RuntimeException("Route methods return type must be a Response");

            Parameter[] parameters = method.getParameters();
            if (parameters.length > 0 && parameters[0].getType() != NanoHTTPD.IHTTPSession.class)
                throw new RuntimeException("The first parameter of Route methods must type of IHTTPSession");

            String path = method.getAnnotation(Route.class).route();
            if (path.isEmpty()) throw new RuntimeException("Route path cannot be empty");

            path = parentPath + path;

            int customParamsCount = Arrays.stream(path.split("/")).filter((s) -> s.startsWith(":")).toArray().length;
            if(customParamsCount > 0) customParamsCount++;

            if(parameters.length != customParamsCount) throw new RuntimeException(String.format("The method %s#'%s' (%s) needed %s arguments, but only have %s", listener.getClass().getName(), method.getName(), path, customParamsCount, method.getParameterCount()));

            if (registeredRoutes.get(path) != null) throw new RuntimeException("The route %s is already registered");

            registeredRoutes.put(path, new SimpleRoute(listener, method));
        }
    }

    public NanoHTTPD.Response executeRoute(NanoHTTPD.IHTTPSession session, String path) throws InvocationTargetException, IllegalAccessException {
        RouteSearchResult result = getRouteMethodAndParams(path);
        if (result == null) return NanoHTTPD.newFixedLengthResponse(String.format("<h1>%s does not exist</h1>", path));

        if (result.getMethod().getParameterCount() >= 1) {
            Object[] p = new Object[result.getParams().length + 1];
            p[0] = session;
            System.arraycopy(result.getParams(), 0, p, 1, result.getParams().length);

            return (NanoHTTPD.Response) result.getMethod().invoke(result.getListener(), p);
        }

        return (NanoHTTPD.Response) result.getMethod().invoke(result.getListener());
    }

    private RouteSearchResult getRouteMethodAndParams(String path) {
        String[] rawPath = path.split("/");

        for (Map.Entry<String, SimpleRoute> entry : registeredRoutes.entrySet()) {
            String route = entry.getKey();

            String[] rawRoute = route.split("/");

            if (rawRoute.length != rawPath.length) continue;

            List<String> params = new ArrayList<>();

            boolean isMatch = true;
            for (int i = 0; i < rawPath.length; i++) {
                if (rawRoute[i].startsWith(":")) {
                    params.add(rawPath[i]);
                    continue;
                }
                if (rawRoute[i].equalsIgnoreCase(rawPath[i])) continue;
                isMatch = false;
            }
            if (isMatch) return new RouteSearchResult(entry.getValue(), params.toArray(new String[0]));
        }

        return null;
    }

    private class SimpleRoute {
        private final RouteListener listener;
        private final Method method;

        private SimpleRoute(RouteListener listener, Method method) {
            this.listener = listener;
            this.method = method;
        }

        public Method getMethod() {
            return method;
        }

        public RouteListener getListener() {
            return listener;
        }
    }

    private class RouteSearchResult {
        private final SimpleRoute simpleRoute;
        private final String[] params;

        private RouteSearchResult(SimpleRoute simpleRoute, String[] params) {
            this.simpleRoute = simpleRoute;
            this.params = params;
        }

        public Method getMethod() {
            return this.simpleRoute.getMethod();
        }

        public RouteListener getListener() {
            return this.simpleRoute.getListener();
        }

        public String[] getParams() {
            return params;
        }
    }
}


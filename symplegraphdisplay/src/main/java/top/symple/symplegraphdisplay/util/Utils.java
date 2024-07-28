package top.symple.symplegraphdisplay.util;

import fi.iki.elonen.NanoHTTPD;
import top.symple.symplegraphdisplay.SympleGraphDisplay;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.URL;

public class Utils {
    // TODO: when moving to android studio change the way that i am getting files
    // https://stackoverflow.com/questions/4081763/access-resource-files-in-android
    public static NanoHTTPD.Response newFixedFileResponse(String filePath) throws Exception {
        URL resource = SympleGraphDisplay.class.getClassLoader().getResource(filePath);
        if(resource == null) throw new Exception("file does not exists");

        URI uri = resource.toURI();

        File file = new File(uri);

        NanoHTTPD.Response response = NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, NanoHTTPD.getMimeTypeForFile(uri.toString()), new FileInputStream(file), (int) file.length());
        response.addHeader("Accept-Ranges", "bytes");
        return response;
    }

    public static NanoHTTPD.Response newFixed404Response() {
        return NanoHTTPD.newFixedLengthResponse("404");
    }
}

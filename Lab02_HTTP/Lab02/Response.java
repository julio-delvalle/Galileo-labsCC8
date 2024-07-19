import java.io.*;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;

public class Response {
    public void sendData(Logger LOGGER, PrintStream dataOut, Integer nThreadServer, Object request) throws Exception {
        int errorCode = 200;

        LOGGER.info(request.toString());
        String[] requestArr = request.toString().split("\\|\\|", 0); 

        LOGGER.info(requestArr[0]);
        LOGGER.info(requestArr[1]);
        LOGGER.info(requestArr[requestArr.length - 1]);

        String firstLine = requestArr[0];
        String POSTbody = requestArr[requestArr.length - 1];

        String method = firstLine.split(" ")[0];
        String path = firstLine.split(" ")[1];

        



        //Obtener parámetros de GET:
        LOGGER.info("path: "+path);
        int qmIndex = path.lastIndexOf('?');
        String GETparamsStr = "";
        String[] GETparams = {};
        Map<String, String> GETparamsPairs = new LinkedHashMap<String, String>();
        if (qmIndex > 0) {
            GETparamsStr = path.substring(qmIndex+1);
            GETparams = GETparamsStr.split("&");
            for (String param : GETparams) {
                int idx = param.indexOf("=");
                //Decodifica caracteres especiales de una vez.
                GETparamsPairs.put(URLDecoder.decode(param.substring(0, idx), "UTF-8"), URLDecoder.decode(param.substring(idx + 1), "UTF-8"));
            }
        }


        //Obtener parámetros de POST:
        LOGGER.info("POST PARAMS : "+POSTbody);
        String[] POSTparams = {};
        Map<String, String> POSTparamsPairs = new LinkedHashMap<String, String>();
        if (POSTbody.length() > 0 && method.equals("POST")) {
            POSTparams = POSTbody.split("&");
            for (String param : POSTparams) {
                int idx = param.indexOf("=");
                //Decodifica caracteres especiales de una vez.
                POSTparamsPairs.put(URLDecoder.decode(param.substring(0, idx), "UTF-8"), URLDecoder.decode(param.substring(idx + 1), "UTF-8"));
            }
        }
        LOGGER.info("POST PAIRS MAP: "+POSTparamsPairs);




        path = path.split("\\?")[0];
        
        if (path.endsWith("/")) {
            path += "index.html";
        }
        
        
        LOGGER.info("method: "+method);
        LOGGER.info("path: "+path);

        String fileData = " ";
        byte[] fileDataBytes = new byte[0];
        try {
            fileData = new String(Files.readAllBytes(Paths.get("./www" + path)));
        } catch (NoSuchFileException e) {
            LOGGER.info("NO SE ENCONTRÓ EL ARCHIVO SOLICITADO. (" + path + ")");
            errorCode = 404;
        } catch (Exception e) {
            LOGGER.info("ERROR INESPERADO!: " + e.getClass().getSimpleName());
        }


        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String fileContentType = fileNameMap.getContentTypeFor("file://" + path);
        if (fileContentType == null) {
            fileContentType = "application/octet-stream";
        }
        LOGGER.info("fileContentType: "+fileContentType);
        
        int i = path.lastIndexOf('.');
        String extension = "";
        if (i > 0) {
            extension = path.substring(i+1);
        }
        LOGGER.info("extension: "+extension);
        
        if(extension.equals("cc8")){
            fileContentType = "text/html";
        }
        
        LOGGER.info("fileContentType: "+fileContentType);
        LOGGER.info("GET: "+Arrays.toString(GETparams));
        LOGGER.info("GET MAP: "+GETparamsPairs.toString());
        LOGGER.info("POST PARAMS: "+POSTbody);
        LOGGER.info("POST PARAMS MAP: "+POSTparamsPairs.toString());
        /// SI ES TEXTO, SUSTITUIR LOS TARGETS Y LUEGO CONVERTIR A BYTES. SI ES CUALQUIER OTRA COSA (IMAGEN, FONT, ICON...) OBTENER BYTES DESDE EL ARCHIVO.
        if(fileContentType.split("/")[0].equals("text")){
            fileData = fileData.replace("{fieldTest_DEMO}", "El servidor cambió esto y agregó un número aleatorio: "+ (new Random()).nextInt(1000)  );
            if(GETparamsPairs.size() > 0){
                fileData = fileData.replace("{name}", GETparamsPairs.get("name") );
                fileData = fileData.replace("{email}", GETparamsPairs.get("email") );
            }
            if(POSTparamsPairs.size() > 0){
                fileData = fileData.replace("{name}", POSTparamsPairs.get("name")+"" );
                fileData = fileData.replace("{email}", POSTparamsPairs.get("email")+"" );
            }

            fileDataBytes = fileData.getBytes();
        }else{
            try {
                fileDataBytes = Files.readAllBytes(Paths.get("./www" + path));
            } catch (NoSuchFileException e) {
                LOGGER.info("NO SE ENCONTRÓ EL ARCHIVO SOLICITADO. (" + path + ")");
                errorCode = 404;
            } catch (Exception e) {
                LOGGER.info("ERROR INESPERADO!: " + e.getClass().getSimpleName());
            }
        }



        ArrayList<String> responseHeaders = new ArrayList<>();
        responseHeaders.add("HTTP/1.1 " + errorCode + " " + (errorCode == 200 ? "OK" : "Error"));
        responseHeaders.add("Content-Type: " + fileContentType);
        responseHeaders.add("ClaseCC8: Alumnos");
        responseHeaders.add("Content-Length: " + fileDataBytes.length);
        responseHeaders.add("");

        // dataOut.print(responseHeaders.stream().collect(Collectors.joining("\r\n")) + "\r\n\r\n");
        dataOut.print(responseHeaders.stream().collect(Collectors.joining("\r\n")) + "\r\n");
        dataOut.write(fileDataBytes);
        dataOut.flush();

        /*if (!fileContentType.equals("application/octet-stream")) {
            LOGGER.info("(" + nThreadServer + ") response: " + responseHeaders);
            LOGGER.info("(" + nThreadServer + ") response: " + fileData);
        }*/
    }
}

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

        String requestContentType = "";

        for (int i = 0; i < requestArr.length; i++) {
            if(requestArr[i].contains("Content-Type")){
                requestContentType = requestArr[i].split(": ",2)[1];
            }
        }
        LOGGER.info("requestContentType: "+requestContentType);



        LOGGER.info(requestArr[0]);
        LOGGER.info(requestArr[1]);
        LOGGER.info(requestArr[requestArr.length - 1]);

        String firstLine = requestArr[0];
        String POSTbody = requestArr[requestArr.length - 1].trim();

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

            if(requestContentType.contains("application/x-www-form-urlencoded")){
                POSTparams = POSTbody.split("&");

                LOGGER.info("DENTRO DE APPLICATION");
                
                for (String param : POSTparams) {
                    int idx = param.indexOf("=");
                    //Decodifica caracteres especiales de una vez.
                    /*String paramName = URLDecoder.decode(param.substring(0, idx), "UTF-8");
                    if(paramName.contains("name")){
                        paramName = "name";
                    }
                    if(paramName.contains("email")){
                        paramName = "email";
                    }*/
                    POSTparamsPairs.put(URLDecoder.decode(param.substring(0, idx), "UTF-8"), URLDecoder.decode(param.substring(idx + 1), "UTF-8"));
                }
            }

            if(requestContentType.contains("multipart/form-data")){
                int boundaryidx = requestContentType.indexOf("boundary=");
                String reqBoundary = requestContentType.substring(boundaryidx+9);//+9 para que no incluya la palabra boundary=
                LOGGER.info("\n\n\n reqBoundary:"+reqBoundary+"\n\n\n");
                POSTparams = POSTbody.split("\n");
                
                for (int i=1;i<POSTparams.length;i++) {
                    int idx = 0;
                    if(POSTparams[i].contains("name=\"")){
                        idx = POSTparams[i].indexOf("name=\"");
                        POSTparamsPairs.put(URLDecoder.decode(POSTparams[i].substring(idx+6, POSTparams[i].length()-2).trim(), "UTF-8"), URLDecoder.decode(POSTparams[i+2].trim(), "UTF-8"));
                    }
                }
            }

            if(requestContentType.contains("text/plain")){
                POSTparams = POSTbody.split("\n");
                
                for (String param : POSTparams) {
                    int idx = param.indexOf("=");
                    POSTparamsPairs.put(URLDecoder.decode(param.substring(0, idx), "UTF-8"), URLDecoder.decode(param.substring(idx + 1), "UTF-8"));
                }
            }

            if(requestContentType.contains("application/json")){
                POSTbody = POSTbody.replace("{", "");
                POSTbody = POSTbody.replace("}", "");

                POSTparams = POSTbody.split(",");
                
                for (String param : POSTparams) {
                    int idx = param.indexOf(":");
                    POSTparamsPairs.put(URLDecoder.decode(param.substring(1, idx-1), "UTF-8"), URLDecoder.decode(param.substring(idx + 2, param.length()-1), "UTF-8"));
                }
            }

            //// CONSIDERAR AQUÍ TODOS LOS CASOS DE POST
            //// CONSIDERAR AQUÍ TODOS LOS CASOS DE POST
            //// CONSIDERAR AQUÍ TODOS LOS CASOS DE POST
            //// CONSIDERAR AQUÍ TODOS LOS CASOS DE POST
            //// CONSIDERAR AQUÍ TODOS LOS CASOS DE POST
            //// CONSIDERAR AQUÍ TODOS LOS CASOS DE POST
            //// CONSIDERAR AQUÍ TODOS LOS CASOS DE POST
            //// CONSIDERAR AQUÍ TODOS LOS CASOS DE POST



            LOGGER.info("Post Params ARRAY: "+Arrays.toString(POSTparams));
            LOGGER.info("Post Params ARRAY: "+POSTparams.length);

        }
        LOGGER.info("POST PAIRS MAP: "+POSTparamsPairs);




        path = path.split("\\?")[0];
        
        if (path.endsWith("/")) {
            path += "index.html";
        }
        if (!path.contains(".")){
            path += "/index.html";
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
        LOGGER.info("POST PARAMS MAP`KEYS: "+POSTparamsPairs.keySet().toString());
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

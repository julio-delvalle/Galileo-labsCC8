import java.io.*;
import java.net.FileNameMap;
import java.net.URLConnection;
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
        String body = requestArr[requestArr.length - 1];

        String method = firstLine.split(" ")[0];
        String path = firstLine.split(" ")[1];
        String httpVersion = firstLine.split(" ")[2];

        LOGGER.info(method);
        LOGGER.info(path);
        LOGGER.info(httpVersion);

        path = path.split("\\?")[0];

        if (path.endsWith("/")) {
            path += "index.html";
        }



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


        //Esta línea sustituye el TARGET por el segundo string
        // === COMENTADA PORQUE AHORITA NO SE PUEDE HACER REPLACE SOBRE UN ARRAY DE BYTES. TIENE QUE SER STRING.
        

        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String fileContentType = fileNameMap.getContentTypeFor("file://" + path);
        if (fileContentType == null) {
            fileContentType = "application/octet-stream";
        }
        LOGGER.info(fileContentType);


        /// SI ES TEXTO, SUSTITUIR LOS TARGETS Y LUEGO CONVERTIR A BYTES. SI ES CUALQUIER OTRA COSA (IMAGEN, FONT, ICON...) OBTENER BYTES DESDE EL ARCHIVO.
        if(fileContentType.split("/")[0].equals("text")){
            fileData = fileData.replace("{fieldTest_DEMO}", "El servidor cambió esto y agregó un número aleatorio: "+ (new Random()).nextInt(1000)  );
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

        /*LOGGER.info(path);
        int i = path.lastIndexOf('.');
        String extension = "";
        if (i > 0) {
            extension = path.substring(i+1);
        }
        LOGGER.info(extension);

        if(extension.equals("ttf")){
            fileContentType = "font/ttf";
        }*/


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

        if (!fileContentType.equals("application/octet-stream")) {
            LOGGER.info("(" + nThreadServer + ") response: " + responseHeaders);
            LOGGER.info("(" + nThreadServer + ") response: " + fileData);
        }
    }
}

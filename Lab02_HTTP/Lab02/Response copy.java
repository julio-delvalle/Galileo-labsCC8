import java.io.*;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;

public class Response {
    // ======================================
    // No modifica la firma de la función.
    public void sendData (Logger LOGGER, PrintStream dataOut, Integer nThreadServer, Object request) throws Exception {
    // ======================================

        /////////////////////////////////////////////////
        //                                             // 
        //   Procesar el cuerpo de la Respuesta aquí   //
        //                                             //
        /////////////////////////////////////////////////
        int errorCode = 200;

        //ArrayList<String> requestArr = (ArrayList<String>)request;
        LOGGER.info(request.toString());
        String[] requestArr = request.toString().split("\\|\\|", 0);

        LOGGER.info(requestArr[0]);
        LOGGER.info(requestArr[1]);
        LOGGER.info(requestArr[requestArr.length-1]);

        String firstLine = requestArr[0];
        String body = requestArr[requestArr.length-1];

        String method = firstLine.split(" ")[0];
        String path = firstLine.split(" ")[1];
        String httpVersion = firstLine.split(" ")[2];

        LOGGER.info(method);
        LOGGER.info(path);
        LOGGER.info(httpVersion);
        
        path = path.split("\\?")[0];
        
        if(path.substring(path.length()-1).equals("/")){
            //Si el path termina en [/] reemplazar por /index.html
            path = path.substring(0, path.length()-1)+"/index.html";
        }
        
        String fileData = " ";
        File file = new File("./www"+path);
        try {
            fileData = new String(Files.readAllBytes(Paths.get("./www"+path)));  //<========== LIMPIAR PATH
        } catch (NoSuchFileException e) {
           LOGGER.info("NO SE ENCONTRÓ EL ARCHIVO SOLICITADO. ("+path+")"); 
           errorCode = 404; 
        } catch (Exception e) {
           LOGGER.info("ERROR INESPERADO!: "+e.getClass().getSimpleName()); 
        }
        

        //Esta línea sustituye el TARGET por el segundo string
        fileData = fileData.replace("{fieldTest_DEMO}", "El servidor cambió esto y agregó un número aleatorio: "+ (new Random()).nextInt(1000)  );

        //String fileContentType = Files.probeContentType(Paths.get("./www"+path));
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        String fileContentType = fileNameMap.getContentTypeFor("file://"+path);
        //String fileContentType = URLConnection.guessContentTypeFromName(path);
        if(fileContentType == null){
            fileContentType = "application/octet-stream";
        }
        LOGGER.info(fileContentType);

        ArrayList<String> response = new ArrayList<String>();
        if(fileContentType.split("/")[0].equals("image")) {
            byte[] imageFileData = new byte[0];
            imageFileData = Files.readAllBytes(Paths.get("./www" + path)); 


            response.add("HTTP/1.1 200 OK");
            response.add("Content-Type: "+fileContentType);  //<========== CAMBIAR CONTENT TYPE
            response.add("ClaseCC8: Alumnos");
            response.add("Content-Length: " + imageFileData.length);
            response.add("");
            dataOut.print( response.stream().collect(Collectors.joining("\r\n")) );
            
            dataOut.write('\n');
            dataOut.write('\n');
            dataOut.write(imageFileData);
            dataOut.flush();
        }else{
            response.add("HTTP/1.1 "+errorCode+" OK");
            response.add("Content-Type: "+fileContentType);  //<========== CAMBIAR CONTENT TYPE
            response.add("ClaseCC8: Alumnos");
            response.add("Content-Length: " + fileData.length());
            response.add("");
            response.add(fileData);
            dataOut.print( response.stream().collect(Collectors.joining("\r\n")) );
        }
        if(!fileContentType.equals("application/octet-stream")){
            LOGGER.info("(" + nThreadServer + ") response: " + response ); 
        }
        //LOGGER.info("(" + nThreadServer + ") request: " + request );
        
        
        // ESTE ES UN EJEMPLO ESTATICO 
        // con lo minimo en el Header
        // Y RESPONDE LO MISMO A TODO

    }// sendData
}
import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.util.stream.*;
import java.util.logging.*;


public class Response {
    // ======================================
    // No modifica la firma de la función.
    public void sendData (Logger LOGGER, PrintStream dataOut, Integer nThreadServer, Object request) throws Exception {
    // ======================================
        @SuppressWarnings("unchecked")
        ArrayList<String> HTTP_request = (ArrayList<String>) request;
        ArrayList<String> response = new ArrayList<String>();
        if ( HTTP_request.get(0).contains("/sendSMTP") ){

            /////////////////////////////////////////////////
            //                                             // 
            //   Procesar el POST del Correo aquí          //
            //   Implemente el SMTP Client                 //
            //                                             //
            /////////////////////////////////////////////////

            LOGGER.info("(" + nThreadServer + ") HTTP_request.getLast() : " + HTTP_request.getLast() );
            
            response.add("HTTP/1.1 200 OK");
            dataOut.print( response.stream().collect(Collectors.joining("\r\n")) );
        } else {
        String fileData = new String(Files.readAllBytes(Paths.get("./www/index.html"  )));
            response.add("HTTP/1.1 200 OK");
            response.add("Content-Type: text/html");
            response.add("Content-Length: " + fileData.length());
            response.add("");
            response.add(fileData);
            dataOut.print( response.stream().collect(Collectors.joining("\r\n")) );
            LOGGER.info("(" + nThreadServer + ") HTTP_request.get(0) : " + HTTP_request.get(0) );
        }
        LOGGER.info("(" + nThreadServer + ") response: " + response ); 
        

    }// sendData
}
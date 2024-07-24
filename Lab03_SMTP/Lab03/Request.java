import java.io.*;
import java.util.*;
//import java.util.stream.*;
import java.util.logging.*;

public class Request {
    // ======================================
    // No modifica la firma de la función.
    public Object getData (Logger LOGGER, BufferedReader dataIn, Integer nThreadServer) throws Exception  {
    // ======================================
        ArrayList<String> request = new ArrayList<String>();
        Integer contentLength = 0;
        String line;
        while ((line = dataIn.readLine()) != null) {
            if (line.trim().isEmpty()) break;
            if ((line.split(":"))[0].trim().equals("Content-Length")) contentLength = Integer.valueOf((line.split(":"))[1].trim());
            request.add( line );
        }// while line
        /// POST Body
        char[] inBuffer = new char[contentLength];
        int inputMessageLength = dataIn.read(inBuffer, 0, contentLength);
        String inputMessage = new String(inBuffer, 0, inputMessageLength);
        request.add(inputMessage); // Agregar Post Body
        /////////////////////////////////////////////////
        //                                             //
        //   Procesar el cuerpo de la solicitud aquí   //
        //                                             //
        /////////////////////////////////////////////////
        
        LOGGER.info("(" + nThreadServer + ") request: " + request );
        return request; // Retornar la lista o procesar según sea necesario en Response.java

    }// getData
}
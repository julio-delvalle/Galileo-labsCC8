import java.io.*;
import java.net.URLDecoder;
import java.nio.file.*;
import java.util.*;
import java.util.logging.*;
import java.util.stream.*;


public class Response {
    // ======================================
    // No modifica la firma de la función.
    public void sendData (Logger LOGGER, PrintStream dataOut, Integer nThreadServer, Object request) throws Exception {
    // ======================================
        @SuppressWarnings("unchecked")
        ArrayList<String> HTTP_request = (ArrayList<String>) request;
        ArrayList<String> response = new ArrayList<String>();
        if ( HTTP_request.get(0).contains("/sendSMTP") ){

            SMTPClient smtpClient = new SMTPClient();
            boolean started = smtpClient.start();
            LOGGER.info("SERVER START?: "+started);

            /////////////////////////////////////////////////
            //                                             // 
            //   Procesar el POST del Correo aquí          //
            //   Implemente el SMTP Client                 //
            //                                             //
            /////////////////////////////////////////////////

            LOGGER.info("(" + nThreadServer + ") HTTP_request.getLast() : " + HTTP_request.getLast() );

            String POSTbody = HTTP_request.getLast().substring(1);
            String[] POSTparams = {};
            Map<String, String> POSTparamsPairs = new LinkedHashMap<String, String>();
            if (POSTbody.length() > 0) {

                POSTparams = POSTbody.split(";\\|;");

                LOGGER.info("DENTRO DE APPLICATION");
                
                for (String param : POSTparams) {
                    if(param.length()>0 && param.contains(":")){
                        LOGGER.info(param);
                        int idx = param.indexOf(':');
                        //Decodifica caracteres especiales de una vez.
                        POSTparamsPairs.put(URLDecoder.decode(param.substring(0, idx), "UTF-8"), URLDecoder.decode(param.substring(idx + 1), "UTF-8"));
                    }
                }
            }

            LOGGER.info("POST PAIRS MAP: "+POSTparamsPairs);
            LOGGER.info("POST PAIRS KEYS: "+POSTparamsPairs.keySet());

            smtpClient.sendMail(POSTparamsPairs.get("from"), POSTparamsPairs.get("to"), POSTparamsPairs.get("subject"), POSTparamsPairs.get("body"));
            
            String mailServer = POSTparamsPairs.get("from").split("@")[1];

            /*if(mailServer.equals("julio.com")){
                (new SQLiteJDBC()).insertMailToDBbuildBody(POSTparamsPairs.get("from"), POSTparamsPairs.get("to"), POSTparamsPairs.get("subject"), POSTparamsPairs.get("body"));
                LOGGER.info("Se insertó a mi base de datos!");
            }else{
                SMTPClient tempClient = new SMTPClient(mailServer, 25);
                started = tempClient.start();
                if(started){
                    LOGGER.info("Se reenvió el correo a "+mailServer+"!");
                    tempClient.sendMail(POSTparamsPairs.get("from"), POSTparamsPairs.get("to"), POSTparamsPairs.get("subject"), POSTparamsPairs.get("body"));
                    tempClient.close();
                }else{
                    LOGGER.info("No se pudo conectar a "+mailServer+"!");
                }
            }*/
            
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
package service;

import static javax.swing.JFrame.EXIT_ON_CLOSE;
import process.ServerGUI;
import org.apache.log4j.Logger;

/*
* Main method that opens the user interface of the Server
*/
public class Server {
    static Logger log = Logger.getLogger(Server.class);
    public static void main(String[] args){
        log.info("Server started");
        ServerGUI serverGui = new ServerGUI();
        serverGui.setDefaultCloseOperation(EXIT_ON_CLOSE);
    }
}

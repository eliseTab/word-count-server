package process;

import dao.DbAccess;
import java.awt.*;
import java.awt.event.*;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import org.apache.log4j.Logger;
import service.Server;

/*
* User Interface of the server where the following can be viewed:
* Messages received from the client/s
* Current Word-Count list
*/
public class ServerGUI extends JFrame{
    static Logger log = Logger.getLogger(ServerGUI.class);
    private JLabel portL, serverActivityL;
    private JTextField portTf;
    private String port = "31190";
    private TextArea serverActivityTa = new TextArea("",20,40,TextArea.SCROLLBARS_VERTICAL_ONLY);
    private JButton stopB, viewListB;
    private JPanel panelB;
    private ServerThread receiveThread;
    private Box hbox;
    Socket client;
    DbAccess dbAccess;
    
    /*
    * Lays out the user interface of the Server, starts the server, and waits for client connections
    */
    public ServerGUI(){
        super("Server");
        Handler handler = new Handler();
        portL = new JLabel("Port");
        portTf = new JTextField(10);
        portTf.setBackground(Color.white);
        portTf.setText(port);
        portTf.setEditable(false);
        stopB = new JButton("Exit");
        stopB.addActionListener(handler);
        serverActivityL = new JLabel("Server Activity");
        serverActivityTa.setEditable(false);
        serverActivityTa.setFocusable(false);
        serverActivityTa.setBackground(Color.white);
        serverActivityTa.append("Server started\nNow waiting for a connection...\n");
        viewListB = new JButton("View Word List");
        viewListB.addActionListener(handler);
        hbox = Box.createHorizontalBox();
        hbox.add(Box.createHorizontalStrut(20));
        hbox.add(portL);
        hbox.add(Box.createHorizontalStrut(5));
        hbox.add(portTf);
        hbox.add(Box.createHorizontalStrut(30));
        hbox.add(stopB);
        hbox.add(Box.createHorizontalStrut(10));
        panelB = new JPanel();
        panelB.add(hbox);
        panelB.add(serverActivityL);
        panelB.add(serverActivityTa);
        panelB.add(viewListB);
        Container container = getContentPane();
        container.setLayout(new BorderLayout());
        container.add(panelB, BorderLayout.CENTER);
        setResizable(false);
        setVisible(true);
        setSize(350,450);
        setLocation(300, 100);
        log.info("Opening server user interface");
        log.info("Instantiating server socket with port " + port);
        try{
            ServerSocket server = new ServerSocket(31190);
            log.info("Successfully instantiated server socket");
            dbAccess = new DbAccess();
            int clientCount = 0;
            while(true){
                log.info("Waiting for client connections...");
                client = server.accept();
                clientCount++;
                serverActivityTa.append("Connected to Client " + clientCount + "\n");
                log.info("Connected to Client " + clientCount);
                receiveThread = new ServerThread(clientCount);
                receiveThread.start();
            }
        }catch(IOException e){
            log.error(e.getMessage());
        }        
    }
    
    /*
    * Handles action events received from the user interface's buttons
    */
    private class Handler implements ActionListener{
        /*
        * Reacting method once an action is received from any of the buttons
        *
        * @param    e   ActionEvent that was received
        */
        public void actionPerformed(ActionEvent e) {
            if(e.getSource() == stopB){
                log.info("Closing database connection");
                dbAccess.closeConnection();
                log.info("Exiting server application");
                System.exit(0);
            }
            else if(e.getSource() == viewListB){
                ResultSet resultSet = dbAccess.getWordList();
                DefaultTableModel model = new DefaultTableModel();
                model.addColumn("Word");
                model.addColumn("Count");
                try {
                    while(resultSet.next()){
                        String rowData[] = {resultSet.getString(1), resultSet.getString(2)};
                        log.info("Retrieved " + resultSet.getString(1) + " with count " + resultSet.getString(2));
                        model.addRow(rowData);
                    }
                    resultSet.close();
                } catch (SQLException ex) {
                    log.error(ex.getMessage());
                }
                JTable table = new JTable(model);
                JScrollPane scrollPane = new JScrollPane(table);
                scrollPane.setPreferredSize(new Dimension(50,200));
                JOptionPane.showMessageDialog(null, scrollPane, "Word-Count List", JOptionPane.PLAIN_MESSAGE);
            }
        } 
    }
    /*
    * Thread that listens to client messages
    */
    class ServerThread extends Thread{
        int clientId;
        ServerThread(int clientCount){
            clientId = clientCount;
        }
        /*
        * Listens and logs onto the activity text area the client messages
        */
        public void run(){
            DataInputStream input;
            try {
                input = new DataInputStream(client.getInputStream());
                while(!client.isClosed()){
                    while(input.available() == 0){}
                    byte[] b = new byte[input.available()];
                    input.read(b);
                    String msg = new String(b);
                    serverActivityTa.append("[CLIENT "+clientId+"]: "+msg+"\n");
                    log.info("Received from client " + clientId + ": " + msg);
                    String msgArr[] = msg.split("\\s+");
                    int wordCount;
                    for(int i = 0; i < msgArr.length; i++){
                        wordCount = getWordCount(msgArr[i]);
                        if(wordCount > 0)
                            dbAccess.updateData(msgArr[i], wordCount+1);
                        else
                            dbAccess.insertData(msgArr[i]);
                    }
                }
            }catch (IOException e) {
                log.error(e.getMessage());
            }
        }
        
        /*
        * Gets the count associated to the word received
        * 
        * @param    msg word to be counted
        * @return   int count associated to the word
        */
        public int getWordCount(String msg){
            int wordCount = 0;
            ResultSet resultSet = dbAccess.getCount(msg);
            try {
                if(resultSet.next()){
                    wordCount = resultSet.getInt(1); 
                    log.info(msg + " count is " + wordCount);
                }
                resultSet.close();
            } catch (SQLException ex) {
                log.error(ex.getMessage());
            }
            return wordCount;
        }
    }
}
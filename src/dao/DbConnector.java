package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.log4j.Logger;
import service.Server;

/**
 * Contains worddb's connection profile 
 */
public class DbConnector {
    static Logger log = Logger.getLogger(DbConnector.class);
    private String url = "jdbc:mysql://localhost:3306/worddb";
    private String user = "server";
    private String password = "passw0rd";
    private Connection connection;
    
    /*
    * Establishes connection to the worddb database
    */
    public DbConnector(){
        try {
            log.info("Connecting to " + url);
            connection = DriverManager.getConnection(url, user, password);
            log.info("Successfully connected");
        }catch(SQLException ex){
            log.error(ex.getMessage());
        }        
    }
    
    /*
    * Closes the connection to the worddb database
    */
    public void close(){
        try {
            log.info("Closing connection to " + url + "...");
            connection.close();
            log.info("Successfully closed connection");
        } catch (SQLException ex) {
            log.error(ex.getMessage());
        }
    }
    
    /*
    * Returns the established connection to the worddb database
    *
    * @return       worddb database connection
    */
    public Connection getConnection(){
        return connection;
    }
}

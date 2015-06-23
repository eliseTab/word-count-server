package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import org.apache.log4j.Logger;
import process.ServerGUI;

/**
 * Contains the queries used to retrieve/update information from the worddb database
 */
public class DbAccess {
    static Logger log = Logger.getLogger(DbAccess.class);
    Connection connection;
    Statement statement;
    PreparedStatement prepStatement;
    
    /*
    * Establishes a connection to the worddb database
    */
    public DbAccess(){
        connection = new DbConnector().getConnection();
    }
    private static final String wordListQ = "select * from wordcount order by word";
    private static final String wordCountQ = "select count from wordcount where word = ?";
    private static final String insertQ = "insert into wordcount (word, count) VALUES (?, 1)";
    private static final String updateQ = "update wordcount set count = ? where word = ?";
    
    /*
    * Returns the current word-count list
    *
    * @return       the result set containing the current count of each word
    */
    public ResultSet getWordList(){
        log.info("Retrieving current word count list");
        ResultSet resultSet = null;
        try {
            statement = connection.createStatement();
            resultSet = statement.executeQuery(wordListQ);
        } catch (SQLException ex) {
            log.error(ex.getMessage());
        }
        return resultSet;
    }
    
    /*
    * Returns the count associated to the word
    *
    * @param msg    word to count
    * @return       count associated to msg 
    */
    public ResultSet getCount(String msg){
        log.info("Getting count associated to " + msg);
        ResultSet resultSet = null;
        try {
            prepStatement = connection.prepareStatement(wordCountQ);
            prepStatement.setString(1, msg);
            resultSet = prepStatement.executeQuery();
        } catch (SQLException ex) {
            log.error(ex.getMessage());
        }
        return resultSet;
    }
    
    /*
    * Inserts new words into the database
    *
    * @param msg    new word to insert with count 1
    */
    public void insertData(String msg){
        log.info("Inserting " + msg + " with count 1");
        try {
            prepStatement = connection.prepareStatement(insertQ);
            prepStatement.setString(1, msg);
            prepStatement.executeUpdate();
            
        } catch (SQLException ex) {
            log.error(ex.getMessage());
        }
    }
    
    /*
    * Updates the count of a word already existing in the database
    *
    * @param count  existing word's new count
    * @param msg    existing word to be updated
    */
    public void updateData(String msg, int count){
        log.info("Updating count of " + msg + " to " + count);
        try {
            prepStatement = connection.prepareStatement(updateQ);            
            prepStatement.setInt(1, count);
            prepStatement.setString(2, msg);
            prepStatement.executeUpdate();
            
        } catch (SQLException ex) {
            log.error(ex.getMessage());
        }
    }
    
    /*
    * Closes the statement, preparedStatement, and database connection
    */
    public void closeConnection(){
        try {
            if(statement != null)
                statement.close();
            if(prepStatement != null)
                prepStatement.close();
            if(connection != null)
                connection.close();
        } catch (SQLException ex) {
        }
    }
}

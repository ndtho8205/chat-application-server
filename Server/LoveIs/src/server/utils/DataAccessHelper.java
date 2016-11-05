package server.utils;

import com.sun.rowset.CachedRowSetImpl;

import javax.sql.rowset.CachedRowSet;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DataAccessHelper {

    private static final Logger LOGGER = Logger.getLogger(DataAccessHelper.class.getName());
    private final String DB_NAME = "LoveIsSchema";

    private Connection connection = null;
    private Statement statement = null;

    public DataAccessHelper() {
        try {
            Class.forName("com.mysql.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            LOGGER.log(Level.WARNING, "JDBC DRIVER ERROR: {0}", e.getMessage());
        }
    }

    public boolean open() {
        try {
            String URL = "jdbc:mysql://localhost:3306/" + DB_NAME + "?autoReconnect=true&useSSL=false";
            String USERNAME = "root";
            String PASSWORD = "root";
            connection = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            statement = connection.createStatement();
            return true;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "DATABASE CONNECTION ERROR: {0}", e.getMessage());
            return false;
        }
    }

    public void close() {
        try {
            if (connection != null) connection.close();
            if (statement != null) statement.close();
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "DATABASE DISCONNECTION ERROR: {0}", e.getMessage());
        }

    }

    public String getValue(String field, String tableName, String where) {
        String sql = "SELECT * FROM " + DB_NAME + "." + tableName + " WHERE " + where;
        ResultSet rs = null;
        try {
            open();
            rs = statement.executeQuery(sql);
            if (rs.next())
                return rs.getString(field);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "GET VALUE FROM DATABASE ERROR: {0}", e.getMessage());
        } finally {
            if (rs != null) try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            close();
        }
        return null;
    }

    public CachedRowSet select(String sql) {
        CachedRowSet crs = null;
        ResultSet rs = null;
        try {
            open();
            rs = statement.executeQuery(sql);
            crs = new CachedRowSetImpl();
            crs.populate(rs);
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "SELECT QUERY FROM DATABASE ERROR: {0}", e.getMessage());
        } finally {
            if (rs != null) try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            close();
        }
        return crs;
    }

//    private void delete(String tablename, String where) {
//        String sql = "delete from " + DB_NAME + "." + tablename + " where " + where;
//        try {
//            if (statement.executeUpdate(sql) > 0) {
//                System.out.println(" delete xong");
//            } else {
//                System.out.println("khong co " + where + " can delete");
//            }
//        } catch (SQLException e) {
//            LOGGER.log(Level.WARNING, "DELETE: ", e.getMessage());
//        }
//    }

    public boolean insertUser(String name, String password) {
        //TODO: `users` table just have 2 columns: `username`, `password`
        String sql = "INSERT INTO " + DB_NAME + ".`users` (`username`, `password`) VALUES (?, ?);";
        PreparedStatement pst = null;
        try {
            open();
            pst = connection.prepareStatement(sql);
            pst.setString(1, name);
            pst.setString(2, password);
            if (pst.executeUpdate() > 0)
                return true;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "INSERT USER TO DATABASE ERROR: ", e.getMessage());
        } finally {
            close();
        }
        return false;
    }

    public boolean insertMessage(int fromid, int toid, String type, java.util.Date time, String text) {
        String sql = "INSERT INTO " + DB_NAME + ".`message` (`from_id`, `to_id`, `type`, `time`, `text`) VALUES (?, ?, ?, ?, ?);";
        PreparedStatement pst = null;
        try {
            open();
            pst = connection.prepareStatement(sql);
            pst.setInt(1, fromid);
            pst.setInt(2, toid);
            pst.setString(3, type);
            pst.setTimestamp(4, new Timestamp(time.getTime()));
            pst.setString(5, text);
            if (pst.executeUpdate() > 0)
                return true;
        } catch (SQLException e) {
            LOGGER.log(Level.WARNING, "INSERT MESSAGE TO DATABASE ERROR: ", e.getMessage());
        } finally {
            close();
        }
        return false;
    }

//    public void insertFriendsList(int id, int idfriend, DateTimeFormatter date_add_friend) {
//        String sql = "INSERT INTO " + connectionname + ".`friend_list` (`id`, `idfriend`, `date_add_friend`) VALUES (?,?,?);";
//        PreparedStatement pst = null;
//        try {
//            pst = connection.prepareStatement(sql);
//            pst.setString(1, String.valueOf(id));
//            pst.setString(2, String.valueOf(idfriend));
//            pst.setString(3, date_add_friend);
//            if (pst.executeUpdate() > 0) {
//                System.out.println(" inser xong");
//            } else {
//                System.out.println("khong inser duoc");
//            }
//        } catch (SQLException e) {
//            System.out.println("error inser " + e.toString());
//        }
//    }
//

//
//    public void insertgroup(String name, String date_created) {
//        String sql = "INSERT INTO " + connectionname + ".`group` ( `name`, `date_created`) VALUES ( ?, ?);";
//        PreparedStatement pst = null;
//        try {
//            pst = connection2.prepareStatement(sql);
//            pst.setString(1, name);
//            pst.setString(2, date_created);
//            if (pst.executeUpdate() > 0) {
//                System.out.println(" inser xong");
//            } else {
//                System.out.println("khong inser duoc");
//            }
//        } catch (SQLException e) {
//            System.out.println("error inser " + e.toString());
//        }
//    }
//
//    public void insertgroup_mem(int id_group, int user_id) {
//        String sql = "INSERT INTO " + connectionname + ".`group_mem` (`id_group`, `user_id`) VALUES (?, ?);";
//        PreparedStatement pst = null;
//        try {
//            pst = connection2.prepareStatement(sql);
//            pst.setString(1, String.valueOf(id_group));
//            pst.setString(2, String.valueOf(user_id));
//            if (pst.executeUpdate() > 0) {
//                System.out.println(" inser xong");
//            } else {
//                System.out.println("khong inser duoc");
//            }
//        } catch (SQLException e) {
//            System.out.println("error inser " + e.toString());
//        }
//    }
}

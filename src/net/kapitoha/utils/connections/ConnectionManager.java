package net.kapitoha.utils.connections;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import net.kapitoha.utils.CommonUtilities;

/**
 *
 *@author Kapitoha
 *
 */
public class ConnectionManager {
    public static final Path USER_DB = Paths.get(CommonUtilities.getJarsPath().toString(), "data", "fs_database");
    public static Connection getConnection() throws SQLException
    {
	try
	{
	    Class.forName("org.h2.Driver");
	}
	catch (ClassNotFoundException e)
	{
	    System.err.println("Driver class not found");
	    e.printStackTrace();
	}
	Properties props = new Properties();
	props.put("user", "sa");
	props.put("password", "");
	return DriverManager.getConnection("jdbc:h2:file:" + USER_DB.toString()+";LOG=0;trace_level_file=0", "sa", "");
    }

}

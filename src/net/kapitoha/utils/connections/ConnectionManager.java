package net.kapitoha.utils.connections;

import java.net.URLDecoder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import net.kapitoha.utils.CommonUtilities;

/**
 *
 *@author Kapitoha
 *
 */
public class ConnectionManager {
    public static final Path USER_DB = Paths.get(CommonUtilities.getJarsParentFolder().toString(), "data", "fs_database");
    private static AbstractApplicationContext applicationContext = new ClassPathXmlApplicationContext("Beans.xml");
    private static ConnectionProperties connectionProperties;
    private static Connection connection;
    
    public static Connection getConnection() throws SQLException
    {
	if (null == connection || connection.isClosed())
		connection = createConnection();
	return connection;
    }
    private static Connection createConnection() throws SQLException
    {
	Properties props = new Properties();
	String tmpUrl = null;
	if (applicationContext == null) return null;
	try
	{
	    if (null == connectionProperties)
	    {
		connectionProperties = (ConnectionProperties) applicationContext
			.getBean("connection_properties");
	    }
	    Class.forName(connectionProperties.getDriver());
	    props.put("user", connectionProperties.getUser());
	    props.put("password", new String(connectionProperties.getPassword()));
	    tmpUrl = connectionProperties.getUrl();
	    if (tmpUrl.contains("EMBEDDED_PATH"))
		tmpUrl = tmpUrl.replace("EMBEDDED_PATH", "file:".concat(USER_DB.toString()));
	    tmpUrl = URLDecoder.decode(tmpUrl, "UTF-8");
	}
	catch (Exception e) 
	{
	    System.err.println("Error: "  + e.getMessage());
	    System.exit(1);
	}
	return DriverManager.getConnection(tmpUrl, props);
    }

}

package net.kapitoha.utils.connections;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import net.kapitoha.orm.annotations.AnnotationReader;
import net.kapitoha.orm.service.MappingService;

/**
 *
 *@author Kapitoha
 *
 */
public class DatabaseManager {
    public static boolean WAS_DELETE_ACTION = false;
    
    public static<T extends Serializable> boolean isExistTable(Class<T>instanceClass)
    {
	try(Connection con = ConnectionManager.getConnection())
	{
	    T obj = instanceClass.newInstance();
	    AnnotationReader reader = AnnotationReader.readAnnotation(null, obj);
	    MappingService.initializeStatement(String.format("SELECT * FROM %s;", reader.getTableName()), con);
	}
	catch (SQLException | InstantiationException | IllegalAccessException e)
	{
	    return false;
	}
	return true;
    }
    
    /**
     * It deletes database physically from your disk (not drops). Yes, it's so strong :)<br>
     * But I have reason, because h2 database can grow with time to time, even if you dropped all tables.
     * @return
     */
    public static boolean deleteDatabase()
    {
	try
	{
	    Path db = Paths.get(ConnectionManager.USER_DB.toString().concat(".mv.db"));
	    Files.delete(db);
	}
	catch (IOException e)
	{
//	    e.printStackTrace();
	    return false;
	}
	return true;
    }
    
    //******************** ORM
    public static <T extends Serializable>  boolean createDB(Class<T> object, boolean checkExists)
    {
	try(Connection con = ConnectionManager.getConnection())
	{
	    MappingService.createTable(object, checkExists, con);
	}
	catch (Exception e)
	{
	    return false;
	}
	return true;
    }
    
    public static Object saveObject(Serializable object)
    {
	Object rez = null;
	try(Connection con = ConnectionManager.getConnection())
	{
	    rez = MappingService.insertObject(object, con);
	}
	catch (IllegalArgumentException | IllegalAccessException
		| InstantiationException | SQLException e)
	{
//	    e.printStackTrace();
	    System.err.println("Didn't save: "+e.getLocalizedMessage());
	    return null;
	}
	return rez;
    }
    
    public static <T extends Serializable> List<T> getObjects(Class<T> objClass)
    {
	return getObjects(objClass, null);
    }
    
    public static <T extends Serializable> List<T> getObjects(Class<T> objClass, String sql, Object... args)
    {
	List<T> list = Collections.emptyList();
	try(Connection con = ConnectionManager.getConnection())
	{
	    list = MappingService.retrieveObjects(objClass, con, sql, args);
	}
	catch (InstantiationException | IllegalAccessException | SQLException | ClassNotFoundException e)
	{
	    System.err.println("Warning! Cannot retrieve objects:==> " +e.getLocalizedMessage());
	}
	return list;
    }
    
    public static <T extends Serializable> boolean deleteObject(T object)
    {
	WAS_DELETE_ACTION = true;
	try(Connection con = ConnectionManager.getConnection())
	{
	    MappingService.deleteFromTable(object, con);
	    
	}
	catch (SQLException | IllegalArgumentException | IllegalAccessException | InstantiationException | ClassNotFoundException e)
	{
//	    e.printStackTrace();s
	    return false;
	}
	return true;
    }

    public static <T extends Serializable> boolean updateObject(T object)
    {
	return updateObject(object, null);
    }
    
    public static <T extends Serializable> boolean updateObject(T object, String sql, Object... args)
    {
	try(Connection con = ConnectionManager.getConnection())
	{
	    MappingService.updateObject(object, con, sql, args);
	    
	}
	catch (SQLException | IllegalArgumentException | IllegalAccessException | InstantiationException e)
	{
	    return false;
	}
	return true;
    }

}

package net.kapitoha.orm.service;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.kapitoha.orm.annotations.*;
import net.kapitoha.orm.annotations.service.TableAttributesReader;
import net.kapitoha.orm.instances.SERVICE_TABLE_REFERENCES;

/**
 *
 *@author Kapitoha
 *
 */
public class MappingService {
    
    public static <T extends Serializable> List<T> retrieveObjects(Class<T> object, Connection connection) throws InstantiationException, IllegalAccessException, SQLException, ClassNotFoundException
    {
	return retrieveObjects(object, connection, null);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Serializable> List<T> retrieveObjects(Class<T> object, Connection connection, String sqlString, Object ...args) throws InstantiationException, IllegalAccessException, SQLException, ClassNotFoundException
    {
	/*********************************************** Start creating SQL query */
	TableAttributesReader attReader = new TableAttributesReader(object);
	List<T> resultList = new ArrayList<>();
	String tableName = attReader.getTableName();
	StringBuilder sb = new StringBuilder("SELECT * FROM ").append(tableName);
	StringBuilder order = new StringBuilder(" ORDER BY ");

	String sql = sb.toString() + ";".trim();
	if (sqlString != null && !sqlString.isEmpty())
	    sql = sqlString.trim();
	
	/** Check if has an ordered field */
	if (attReader.hasOrder() && !sql.toUpperCase().contains("ORDER"))
	{
	    for (Field field : object.getDeclaredFields())
	    {
		AnnotationReader orderReader = AnnotationReader.readAnnotation(field, null);
		if (orderReader.isOrdered())
		{
		    order.append(orderReader.getColumnName());
		    break;
		}
	    }
	    StringBuilder sb2 = new StringBuilder(sql);
	    if (sql.endsWith(";"))
		sb2.deleteCharAt(sb2.length());
	    sb2.append(order).append(";");
	    sql = sb2.toString();
	}
	/***************************************** SQL Query created. Try to execute */
	PreparedStatement statement = initializeStatement(sql, connection, args);
	ResultSet rs = statement.executeQuery();
	while (rs.next())
	{
	    /** Create a new empty object of this instance*/
	    T newObject = object.newInstance();
	    /**
	     * Try to initialize over loop all allowed fields of a new empty
	     * object. After, it will not be empty
	     */
	    for (Field field : getAllowedFields(newObject))
	    {
		Object value = null;
		AnnotationReader reader = AnnotationReader.readAnnotation(
			field, null);
		value = rs.getObject(reader.getColumnName());
		if (field.getAnnotation(OneToAnother.class) == null)
		{
		    /** Assign retrieved values into new object's fields */
		    field.set(newObject, value);
		}
	    }
	    
	    /** Try to find field that contains other Serializable object */
	    List<Field> relatedList = getRelatedFields(newObject);
	    for (Field relatedField : relatedList)
	    {
		// if field is collection or array
		if (isIterableField(relatedField))
		{
		    Class<?> genericClass = extractGenericTypeFromField(relatedField);
		    T nestedGenericObject = (T) genericClass.newInstance();
		    Collection<T> collection = withdrawNested(relatedField, newObject, nestedGenericObject, connection);
		    if (collection != null && !collection.isEmpty())
		    {
			if (Collection.class.isAssignableFrom(relatedField.getType()))
			{
			    Collection<T> iterableField = (Collection<T>) relatedField.get(newObject);
			    for (T cls : collection)
			    {
				iterableField.add(cls);
			    }
			}
			else if (relatedField.getType().isArray())
			{
			    relatedField.set(newObject, Array.newInstance(genericClass, collection.size()));
			}
		    }
		    
		}
		else //If field is Serializable
		{
		    //iterate fields of main parent object
		    label1:
		    for (Field newObjectField : getAllowedFields(newObject))
		    {
			if (newObjectField.getAnnotation(OneToAnother.class) != null)
			{
			    /** Iterate all fields in nested object to find referenced column of parent class */
			    for (Field nestedSerializableObjField: newObjectField.getType().getDeclaredFields())
			    {
				nestedSerializableObjField.setAccessible(true);
				AnnotationReader nestSerializableReader = AnnotationReader.readAnnotation(nestedSerializableObjField, null);
				if (nestSerializableReader.isForeign() && nestSerializableReader.getReferenceClass().equals(newObject.getClass()))
				{
				    String s = String.format("SELECT * FROM  %s WHERE %s=?", 
					    new TableAttributesReader((Class<T>)newObjectField.getType()).getTableName(), 
					    nestSerializableReader.getColumnName());
				    Field parentField = getFieldByCollumnName(nestSerializableReader.getReferenceColumn(), newObject);
				    if (parentField != null)
				    {
					List<T> resList = (List<T>) retrieveObjects(
						(Class<T>) newObjectField
						.getType(),
						connection,
						s,
						parentField.get(newObject));
					if (!resList.isEmpty())
					{
					    relatedField.set(newObject, resList.get(0));
					    break label1;
					}
				    }
				}
			    }
			}
		    }
		}
	    }
	    resultList.add(newObject);
	}
	return resultList;
    }
    
    private static Field getFieldByCollumnName(String collumnName, Serializable object)
    {
	for (Field newObField : object.getClass().getDeclaredFields())
	{
	    if (AnnotationReader.readAnnotation(newObField, object).getColumnName().equals(collumnName))
	    {
		newObField.setAccessible(true);
		return newObField;
	    }
	}
	return null;
    }
    
    //Withdraw
    @SuppressWarnings("unchecked")
    private static <T extends Serializable>Collection<T> withdrawNested(Field relatedField, T basicObject,  T nestedGenericObject, Connection connection) throws IllegalArgumentException, IllegalAccessException, InstantiationException, ClassNotFoundException, SQLException
    {
	AnnotationReader reader = AnnotationReader.readAnnotation(relatedField, basicObject);
	Collection<T> collection = null;

	/** Try to find relation field */
	for (Field nestedGenericObjectField : getAllowedFields(nestedGenericObject))
	{
	    AnnotationReader nestedReader = AnnotationReader.readAnnotation(
		    nestedGenericObjectField, nestedGenericObject);
	    if (nestedReader.isForeign()
		    && nestedReader.getReferenceClass().equals(
			    basicObject.getClass()))
	    {

		String nestedTableName = new TableAttributesReader(
			nestedGenericObject.getClass()).getTableName();// nestedReader.getTableName();
		String nestedColumn = reader.getDestinationColumn();
		Object sqlArg = null;
		/** Try to find referenced column name in parent object */
		for (Field allowedField : getAllowedFields(basicObject))
		{
		    if (AnnotationReader
			    .readAnnotation(allowedField, basicObject)
			    .getColumnName()
			    .equals(nestedReader.getReferenceColumn()))
		    {
			sqlArg = allowedField.get(basicObject);
		    }
		}
		String nestedSQL = String.format("SELECT * FROM %s WHERE %s = ?", nestedTableName, nestedColumn);
		/**
		 * recursively retrieve all nested objects and fill a collection
		 */
		collection = (Collection<T>) retrieveObjects(nestedGenericObject.getClass(), connection, nestedSQL, sqlArg);
	    }
	}

	return collection;
    }
    
    /**
     * Insert Object in DB and return it AUTO_GENERATED primary key value.
     * @param object
     * @return long
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws SQLException
     * @throws InstantiationException 
     */
    @SuppressWarnings("rawtypes")
    public static Object insertObject(Serializable object, Connection connection) throws IllegalArgumentException, IllegalAccessException, SQLException, InstantiationException
    {
	Object key = null;
	createTable(object.getClass(), true, connection);
	/****************************************************** Start building SQL query */
	String sql = null;
	String tableName = object.getClass().getSimpleName();
	StringBuilder sb = new StringBuilder("INSERT INTO ").append(tableName).append("(");
	StringBuilder values = new StringBuilder("VALUES (");
	
	List<Field> fields = getAllowedFields(object);
	List<Field> realtionFileds = getRelatedFields(object);
	List<Object> args = new ArrayList<>(fields.size());

	for (int i = 0; i < fields.size(); i++)
	{
	    AnnotationReader reader = AnnotationReader.readAnnotation(fields.get(i), object);
	    if (!reader.isAutoincrement())
	    {
		sb.append(reader.getColumnName());
		values.append("?");
		args.add(fields.get(i).get(object));
		
		sb.append(", ");
		values.append(", ");
	    }
	}
	sb.append(") ");
	values.append(");");
	sb.append(values);
	sql = sb.toString().replaceAll(",\\s+\\)", ")");
	/************************************************************* SQL Query has been created */
	/** Starting execute query */
	PreparedStatement statement = initializeStatement(sql, connection, args.toArray());
	int result = statement.executeUpdate();
	if (result > 0)
	{
	    ResultSet rs = statement.getGeneratedKeys();
	    while (rs.next())
	    {
		key = rs.getObject(1);
	    }
	}
	/** Save fields containing other nested objects */
	for (Field field : realtionFileds)
	{
	    if (field.getAnnotation(OneToAnother.class) != null)
	    {
		if (!isIterableField(field) && Serializable.class.isAssignableFrom(field.getType()))
		{
		    //Assign this object to new value
		    Serializable singleNestedObject = (Serializable) field.get(object);
		    if (singleNestedObject != null)
		    {
			/** Iterate nested object's fields to find foreign fields*/
			for (Field singleNestedObjectsField : getAllowedFields(singleNestedObject))
			{
			    AnnotationReader readFields = AnnotationReader
				    .readAnnotation(singleNestedObjectsField,
					    singleNestedObject);
			    if (readFields.isForeign() && readFields.getReferenceClass().equals(object.getClass()))
			    {
				singleNestedObjectsField.set(singleNestedObject, key);
				/** Insert nested object and get it's key */
				Object internalObjectsKey = insertNested((Serializable) singleNestedObject, field,
					object, key, connection);
				String nestedColumn = null;
				String parentsIdColumn = null;
				/** Iterate and search required columns names */
				for (Field parentFields : object.getClass().getDeclaredFields())
				{
				    if (null != parentFields.getAnnotation(OneToAnother.class))
				    {
					if (AnnotationReader.readAnnotation(parentFields,object).getDestinationColumn().equals(readFields
							.getColumnName()))
					{
					    nestedColumn = AnnotationReader.readAnnotation(parentFields,object).getColumnName();
					}
				    }
				    if (AnnotationReader.readAnnotation(parentFields, object).getColumnName().equals(readFields.getReferenceColumn()))
				    {
					parentsIdColumn = AnnotationReader.readAnnotation(parentFields, object).getColumnName();
				    }
				}
				/** Update parent object table with nested object's key */
				if (nestedColumn != null && null != parentsIdColumn)
				{
				    String q = String
					    .format("UPDATE %s SET %s=? WHERE %s=?",
						    tableName, nestedColumn,
						    parentsIdColumn/*nested column in parent*//*parent's id table */);
				    updateObject(object, connection, q,
					    internalObjectsKey, key);
				}
			    }
			}
		    }
		}
		else
		{
		    if (field.getType().isArray())
		    {
			Serializable[] array = (Serializable[]) field.get(object);
			for (Object nested : array)
			{
			    insertNested(nested, field, object, key, connection);
			}
		    }
		    else
		    {
			for (Object nested : (Iterable) field.get(object))
			{
			    insertNested(nested, field, object, key, connection);
			}
		    }
		}
	    }
	}
	return key;
    }
    
    public static void updateObject(Serializable object, Connection connection) throws IllegalArgumentException, IllegalAccessException, InstantiationException, SQLException
    {
	updateObject(object, connection, null);
    }
    
    @SuppressWarnings({ "unchecked" })
    public static void updateObject(Serializable object, Connection connection, String sqlQuery, Object... args) throws IllegalArgumentException, IllegalAccessException, SQLException, InstantiationException
    {
	/** Creating query */
	String sql = null;
	String tableName = new TableAttributesReader(object.getClass()).getTableName();
	StringBuilder sets = new StringBuilder("UPDATE ").append(tableName).append(" SET ");
	StringBuilder conditions = new StringBuilder(" WHERE ");
	
	List<Object> argsList = new ArrayList<>(8);
	List<Object> condArgs = new ArrayList<>();

	if (sqlQuery == null || sqlQuery.isEmpty())
	{
	    for (Field field : getAllowedFields(object))
	    {
		AnnotationReader reader = AnnotationReader.readAnnotation(
			field, object);
		if (!reader.isPrimary())
		{
		    sets.append(reader.getColumnName()).append("=?, ");
		    argsList.add(field.get(object));
		}
		else
		{
		    conditions.append(reader.getColumnName()).append("=? AND ");
		    condArgs.add(field.get(object));
		}
	    }
	    conditions.append(";");
	    sets.append(conditions);
	    argsList.addAll(condArgs);
	    sql = sets.toString().replaceAll(",\\s+\\;", ";")
		    .replaceAll(",\\s+WHERE", " WHERE")
		    .replaceAll("AND\\s+;", ";");
	}
	else
	{
	    argsList.addAll(Arrays.asList(args));
	    sql = sqlQuery;
	}
	PreparedStatement statement = initializeStatement(sql, connection, argsList.toArray());
	statement.execute();

	/** Update nested objects */
	for (Field field : getRelatedFields(object))
	{
	    if (field.getAnnotation(OneToAnother.class) != null)
	    {
		if (!isIterableField(field))
		{
		    Object value = field.get(object);
		    updateObject((Serializable) value, connection);
		}
		else
		{
		    Collection<Serializable> list = null;
		    if (field.getType().isArray())
		    {
			list = Arrays.asList((Serializable[]) field.get(object));
		    }
		    else
    		    	list = (Collection<Serializable>) field.get(object);
		    for (Object nested : list)
		    {
			if (nested != null)
			{
			    updateObject((Serializable) nested, connection);
			}
		    }
		}
	    }
	}
    }
    
    public static <T extends Serializable> void createTable(Class<T> object, boolean checkIfExist, Connection connection) throws SQLException, InstantiationException, IllegalAccessException
    {
	/** Creating sql query */
	String tableName = new TableAttributesReader(object).getTableName();
	String patternSQL = "CREATE TABLE %exists% %table%(%collumns% %foreign% %primary%);";
	StringBuilder collumns = new StringBuilder();
	StringBuilder foreign = new StringBuilder();
	StringBuilder primary = new StringBuilder(", PRIMARY KEY()");
	String sql = null;
	T initialObject = object.newInstance();
	List<Field> fields = getAllowedFields(initialObject);
	List<Field> primaryFields = new ArrayList<>();
	List<Field> foreignFields = new ArrayList<>();
	Set<SERVICE_TABLE_REFERENCES> referencesSQL = new LinkedHashSet<>();
	int count = 0;
	//iterate object's fields
	for (Field field : fields)
	{
	    AnnotationReader reader = AnnotationReader.readAnnotation(field, initialObject);
	    String column = reader.getColumnName();
	    String DEFINITION = " " + reader.getDefinition() + " ";
	    String LENGTH = (DEFINITION.contains("(") || reader.getLength().equals(255))? "":"(" + reader.getLength().toString() + ") ";
	    String NULLABLE = reader.isNullable()? "":" NOT NULL ";
	    String AUTOINCREMENT = field.getAnnotation(GeneratedValue.class) != null? " AUTO_INCREMENT " : "";
	    String UNIQUE = reader.isUnique()? " UNIQUE ":"";
	    
	    collumns.append(column);
	    collumns.append(DEFINITION);
	    collumns.append(LENGTH);
	    collumns.append(NULLABLE);
	    collumns.append(AUTOINCREMENT);
	    collumns.append(UNIQUE);
	    if (count++ < fields.size()-1) collumns.append(", ");
	    if (reader.isPrimary()) primaryFields.add(field);
	    if (reader.isForeign()) foreignFields.add(field);
	}
	// Set foreign
	if (foreignFields.size() > 0)
	{
	    int keycount = 0;
	    for (Field field : foreignFields)
	    {
		StringBuilder fb = new StringBuilder(", FOREIGN KEY(");
		AnnotationReader reader = AnnotationReader.readAnnotation(
			field, initialObject);
		fb.append(reader.getColumnName());
		fb.append(") REFERENCES ");
		fb.append(reader.getReferenceTable());
		fb.append("(").append(reader.getReferenceColumn()).append(")");

		if (keycount++ < foreignFields.size() - 1)
		    fb.append(", ");
		foreign.append(fb);
		SERVICE_TABLE_REFERENCES tRef = new SERVICE_TABLE_REFERENCES(reader.getReferenceTable(), 
			reader.getReferenceColumn(), reader.getTableName(), reader.getColumnName(), object.getName());
		referencesSQL.add(tRef);
	    }
	}
	//Set primary
	if (primaryFields.size() > 0)
	{
	    int keycount = 0;
	    for (Field field : primaryFields)
	    {
		AnnotationReader reader = AnnotationReader.readAnnotation(field, initialObject);
		String prim = reader.getColumnName();
		if (keycount++ < primaryFields.size() - 1)
		    prim += ", ";
		primary.insert(primary.length() - 1, prim);
	    }
	}
	/** Replace patterns by real fields */
	patternSQL = patternSQL.replaceFirst("%table%", tableName);
	patternSQL = patternSQL.replaceFirst("%exists%", checkIfExist? "IF NOT EXISTS" : "");
	patternSQL = patternSQL.replaceFirst("%collumns%", collumns.toString());
	patternSQL = patternSQL.replaceFirst("%foreign%", !foreignFields.isEmpty()? foreign.toString():"");
	patternSQL = patternSQL.replaceFirst("%primary%", !primaryFields.isEmpty()? primary.toString():"");
	/* Try to clean string. OMG It's terrible but(t) works...*/
	sql = patternSQL.toString().replaceAll(" \\(", "(").replaceAll("\\s+,", ",").replaceAll("\\,{2,}", ",")
		.replaceAll("\\s{2,}", " ").replaceAll(",\\s+\\)", ")");
	/* Execute query */
	initializeStatement(sql, connection).execute();
	//Save references between tables
	if (!referencesSQL.isEmpty())
	{
	    for (SERVICE_TABLE_REFERENCES refs : referencesSQL)
	    {
		try
		{
		    insertObject(refs, connection);
		}
		catch (Exception e)
		{}
	    }
	}
    }
    
    public static void deleteFromTable(Serializable object, Connection connection) throws IllegalArgumentException, IllegalAccessException, InstantiationException, ClassNotFoundException, SQLException
    {
	deleteFromTable(object, connection, null);
    }
    
    @SuppressWarnings("unchecked")
    public static<T extends Serializable> void deleteFromTable(T object, Connection connection, String sqlQuery, Object...args) throws IllegalArgumentException, IllegalAccessException, SQLException, InstantiationException, ClassNotFoundException
    {
	String sql = null;
	TableAttributesReader reader = new TableAttributesReader(object.getClass());
	List<Field> relatedList = getRelatedFields(object);
	/** Try first to delete nested objects */
	for (Field field : relatedList)
	{
	    if (isIterableField(field))
	    {
		Collection<T> list = null;
		if (field.getType().isArray())
		    list = (Collection<T>) new ArrayList<>(Arrays.asList((T[])field.get(object)));
		else
		    list = (Collection<T>) field.get(object);
		if (list != null)
		{
		    for (T t : list)
		    {
			deleteFromTable(t, connection);
		    }
		}
	    }
	    else
	    {
		T subObject = (T) field.get(object);
		deleteFromTable(subObject, connection);
	    }
	}
	/** Try to find referenced links in SERVICE_TABLE */
	for (SERVICE_TABLE_REFERENCES service : retrieveObjects(SERVICE_TABLE_REFERENCES.class, connection))
	{
	    if (service.table.equals(reader.getTableName()))
	    {
		T refObject = (T) Class.forName(service.reference_class).newInstance();
		for (Field objField : getAllowedFields(object))
		{
		    if (AnnotationReader.readAnnotation(objField, object).getColumnName().equals(service.column))
		    {
			deleteFromTable(refObject, connection, String.format("DELETE FROM %s WHERE %s=?;", 
				service.reference_table, service.reference_column), objField.get(object));
			break;
		    }
		}
	    }
	}
	List<Object>argList = new ArrayList<>();
	/** Start building query */
	if (sqlQuery != null && !sqlQuery.isEmpty())
	{
	    argList = Arrays.asList(args);
	    sql = sqlQuery;
	}
	else
	{
	    if (!object.getClass().isArray())
	    {
		StringBuilder sb = new StringBuilder("DELETE FROM ").append(
			reader.getTableName()).append(" WHERE ");
		for (Field field : getAllowedFields(object))
		{
		    AnnotationReader subReader = AnnotationReader
			    .readAnnotation(field, object);
		    sb.append(subReader.getColumnName()).append(" = ? AND ");
		    argList.add(field.get(object));
		}
		sb.append(";");
		sql = sb.toString().replaceAll("AND\\s?\\;", ";");
	    }
	}
	/** Start execute query */
	if (sql != null)
	{
	    PreparedStatement statement = initializeStatement(sql, connection,
		    argList.toArray());
	    statement.execute();
	}
    }
    
    /**
     * Returns complete PreparedStatement with complete arguments
     * @param sql
     * @param connection
     * @param args values that replaces '?' signs in sql query.
     * @return
     * @throws SQLException
     */
    public static PreparedStatement initializeStatement(String sql, Connection connection, Object... args) throws SQLException
    {
	    PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
	    if (null != args && args.length > 0)
	    {
		int countDown = 1;
		for (Object object : args)
		{
		    Object ob = object;
		    if (null == ob)
		    {
			statement.setNull(countDown, Types.NULL);
			break;
		    }
		    if (ob instanceof Boolean)
			statement.setBoolean(countDown, (Boolean) ob);
		    else if (ob instanceof String)
			statement.setString(countDown, (String) ob);
		    else if (ob instanceof Number)
		    {
			String tmp = String.valueOf(ob);
			if (tmp.contains(",") || tmp.contains("."))
			{
			    if (ob instanceof Float)
				statement.setFloat(countDown, (float) ob);
			    if (ob instanceof BigDecimal)
				statement.setBigDecimal(countDown, (BigDecimal) ob);
			    else
				statement.setDouble(countDown,
				    ((Double) ob).doubleValue());
			}
			else
			{
			    if (ob instanceof Long)
				statement.setLong(countDown, (long) ob);
			    else if (ob instanceof Byte)
				statement.setByte(countDown, (byte) ob);
			    else if (ob instanceof Short)
				statement.setShort(countDown, (short) ob);
			    else
				statement.setInt(countDown,
				    ((Integer) ob).intValue());
			}
		    }
		    else if (ob instanceof Serializable)
		    {
			for (Field field : ob.getClass().getDeclaredFields())
			{
			    AnnotationReader reader = AnnotationReader.readAnnotation(field, (Serializable)ob);
			    if (reader.isPrimary())
			    {
				try
				{
				    field.setAccessible(true);
				    statement.setObject(countDown, field.get(ob));
				}
				catch (IllegalArgumentException
					| IllegalAccessException e)
				{
				    // TODO Auto-generated catch block
				    e.printStackTrace();
				}
				break;
			    }
			}
		    }
		    else
		    {
			System.err.println("Unsupported args format: " + ob.getClass().getSimpleName());
			return null;
		    }
		    ++countDown;
		}
	    }
	    return statement;
    }
    
    private static boolean isAllowedField(Field field)
    {
	return !field.getName().equals("serialVersionUID") 
		    && !Collection.class.isAssignableFrom(field.getType()) 
		    && !field.getType().isArray() 
		    && !Map.class.isAssignableFrom(field.getType())
		    && field.getAnnotation(Transient.class) == null;
    }
    
    /**
     * Returns fields which have relation ships with other tables
     * @param object
     * @return
     */
    private static List<Field> getRelatedFields(Serializable object)
    {
	List<Field>list = new ArrayList<>();
	for (Field field : object.getClass().getDeclaredFields())
	{
	    if (field.getAnnotation(OneToAnother.class) != null && field.getAnnotation(Transient.class) == null)
	    {
		field.setAccessible(true);
		list.add(field);
	    }
	}
	return list;
    }
    
    /**
     * Return true if field is instance of Collection/Map/Array 
     * @param field
     * @return
     */
    public static boolean isIterableField(Field field)
    {
	return (Collection.class.isAssignableFrom(field.getType()) || field.getType().isArray() /*|| Map.class.isAssignableFrom(field.getType())*/);
    }
    
    /**
     * Returns list of object's fields except Collections, Maps and Arrays
     * @param object
     * @return
     */
    public static List<Field>getAllowedFields(Serializable object)
    {
//	if (object.getClass().isArray()) System.out.println("array");
	List<Field> fields = new ArrayList<>(object.getClass().getDeclaredFields().length);
	for (Field field : object.getClass().getDeclaredFields())
	{
	    if (isAllowedField(field))
	    {
		field.setAccessible(true);
		fields.add(field);
	    }
	}
	return fields;
    }

    /**
     * Returns generated string with SQL TYPE based on variable's class type, such as INTEGER, VARCHAR etc.
     * @param field
     * @return
     */
    public static String getType(Field field)
    {
	for (VariableTypes cl : VariableTypes.values())
	{
	    Class<?> o = field.getType();
	    if (o.isPrimitive())
	    {
		o = primitiveTypeClassWrapper(field.getType());
	    }
	    if (o.getName().equalsIgnoreCase(cl.getClassType()))
		return cl.toString();
	}

	Object value = null;
	if (field.getType() instanceof Serializable)
	{
	    for(Field f : field.getType().getDeclaredFields())
	    {
	        AnnotationReader reader = AnnotationReader.readAnnotation(f, null);
	        if (reader.isForeign() && isAllowedField(f))
	        {
	    	    value = getType(f);
	    	    break;
	        }
	    }
	}
	return (null == value)? null : value.toString();
    }
    
    /**
     * Converts primitive types into their object analogs. Example int returns Integer.
     * @param primitive
     * @return
     */
    private static Class<?> primitiveTypeClassWrapper(Class<?> primitive)
    {
	Map<Class<?>, Class<?>> map = new HashMap<>();
	map.put(boolean.class, Boolean.class);
	map.put(byte.class, Byte.class);
	map.put(short.class, Short.class);
	map.put(char.class, Character.class);
	map.put(int.class, Integer.class);
	map.put(long.class, Long.class);
	map.put(float.class, Float.class);
	map.put(double.class, Double.class);
	return map.get(primitive);
    }
    
    /**
     * Set the primary key into nested object and save it into DB.
     * @param nestedObject
     * @param field
     * @param object
     * @param key
     * @param con
     * @return
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws SQLException
     */
    private static <T extends Serializable> Object insertNested(Object nestedObject, Field field, Serializable object, Object key, Connection con) throws IllegalArgumentException, IllegalAccessException, InstantiationException, SQLException
    {
	if (nestedObject instanceof Serializable)
	{
	    List<Field> allFields = getAllowedFields((Serializable) nestedObject);
	    for (Field af : allFields)
	    {
		if (af.getAnnotation(Foreign.class) != null)
		{
		    AnnotationReader parentAnnotations = AnnotationReader.readAnnotation(field, object);
		    AnnotationReader aReader = AnnotationReader.readAnnotation(af, (Serializable)nestedObject);
		    
		    if (parentAnnotations.getDestinationColumn().equals(aReader.getColumnName())
			    && key != null)
		    {
			af.set((Serializable)nestedObject, key);
			return insertObject((Serializable) nestedObject, con);
		    }
		}
	    }
	}
	return null;
    }
    
    /** Want to know containing class type in the collection or array - it's here 
     * 
     */
    private static Class<?> extractGenericTypeFromField(Field field)
    {
	Type genType = field.getGenericType();
	    Pattern pat = Pattern.compile("<(.+)>$");
	    Matcher match = pat.matcher(genType.toString());
	    Class<?> genericClass = null;
	    if (match.find() || field.getType().isArray())
	    {
		String cl = field.getType().isArray()? field.getType().getComponentType().getName() : match.group(1);
		try
		{
		    genericClass = Class.forName(cl);
		}
		catch (ClassNotFoundException e)
		{
		    System.err.println("Cannot extract generic. " + e.getLocalizedMessage());
		}
	    }
	    return genericClass;
    }
}

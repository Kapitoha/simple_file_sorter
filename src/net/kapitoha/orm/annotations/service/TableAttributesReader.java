package net.kapitoha.orm.annotations.service;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import net.kapitoha.orm.annotations.Order;
import net.kapitoha.orm.annotations.Table;

/**
 *
 *@author Kapitoha
 *
 */
public class TableAttributesReader {
    private String tableName;
    private Annotation[] annotations;
    private Class<?> instanceClass;
    
    public <T extends Serializable> TableAttributesReader(Class<T> instanceClass)
    {
	this.instanceClass = instanceClass;
	this.tableName = !instanceClass.isArray()? instanceClass.getSimpleName() : instanceClass.getComponentType().getSimpleName();
	read();
    }
    
    private void read()
    {
	annotations = instanceClass.getDeclaredAnnotations();
	for (Annotation annotation : annotations)
	{
	    if (annotation.annotationType().equals(Table.class))
	    {
		tableName = ((Table)annotation).name().isEmpty()? tableName : ((Table)annotation).name();
	    }
	}
    }

    /**
     * @return the tableName
     */
    public String getTableName()
    {
	return tableName;
    }
    
    public boolean hasOrder()
    {
	for (Field field : instanceClass.getDeclaredFields())
	{
	    if (field.getAnnotation(Order.class) != null)
		return true;
	}
	return false;
    }
}

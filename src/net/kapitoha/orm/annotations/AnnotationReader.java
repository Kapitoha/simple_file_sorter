package net.kapitoha.orm.annotations;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

import net.kapitoha.orm.service.MappingService;

/**
 * 
 * @author Kapitoha
 * 
 */
public class AnnotationReader {
    private String column = null;
    private boolean nullable = true;
    private boolean unique = false;
    private Number length = 255;
    private String definition = null;
    private boolean isPrimary = false;
    private boolean autoincrement = false;
    private boolean isTransient = false;
    private boolean isForeign = false;
    private String referenceColumn;
    private String referenceTable;
    private Class<?> referenceClass;
    private String tableName;
    private String destinationColumn;
    private boolean ordered = false;

    private Field field;

    private <T extends Serializable> AnnotationReader(T instance)
    {
	if (instance != null)
	{
	    this.tableName = instance.getClass().getSimpleName();
	}
    }
    
    private<T extends Serializable> AnnotationReader(Field field, T instance)
    {
	this(instance);
	if (field != null)
	{
	    this.field = field;
	    this.column = field.getName();
	    this.definition = MappingService.getType(field);
	}
    }

    public static <T extends Serializable> AnnotationReader readAnnotation(Field field, T instance)
    {
	AnnotationReader reader = new AnnotationReader(field, instance);
	if (field != null )
	{
	    Annotation[] ann = field.getDeclaredAnnotations();
	    // iterate annotations
	    for (Annotation a : ann)
	    {
		// Column
		if (a.annotationType().equals(Column.class))
		{
		    Column col = (Column) a;
		    reader.nullable = col.nullable();
		    reader.unique = col.unique();
		    reader.length = col.length();
		    reader.column = (!col.name().equals("")) ? col.name() : field
			    .getName();
		    reader.definition = (!col.columnDefinition().equals("")) ? col
			    .columnDefinition() : reader.definition;
		}
		
		else if (a.annotationType().equals(Id.class))
		    reader.isPrimary = true;
		else if(a.annotationType().equals(GeneratedValue.class))
		    reader.autoincrement = true;
		else if(a.annotationType().equals(Transient.class))
		    reader.isTransient = true;
		else if (a.annotationType().equals(Foreign.class))
		{
		    Foreign f = (Foreign) a;
		    reader.isForeign = true;
		    reader.referenceColumn = f.referenceColumn();
		    reader.referenceClass = f.referenceClass();
		    reader.referenceTable = f.referenceClass().getSimpleName();
		}
		else if (a.annotationType().equals(OneToAnother.class))
		{
		    OneToAnother oTm = (OneToAnother) a;
		    reader.destinationColumn = oTm.destinationColumn();
		}
		else if (a.annotationType().equals(Order.class))
		{
		    reader.ordered = true;
		}
	    }
	    
	}
	return reader;
    }

    public String getColumnName()
    {
	return column;
    }

    public boolean isNullable()
    {
	return nullable;
    }

    public boolean isUnique()
    {
	return unique;
    }

    public Number getLength()
    {
	if (getDefinition() != null
		&& !getDefinition().toLowerCase().contains("(dec|float)"))
	{
	    return (Integer) length.intValue();
	}
	return length;
    }

    public String getDefinition()
    {
	return definition;
    }

    public boolean isPrimary()
    {
	return isPrimary;
    }

    public boolean isAutoincrement()
    {
	return autoincrement;
    }

    public Field getField()
    {
	return field;
    }

    /**
     * @return the isTransient
     */
    public boolean isTransient()
    {
	return isTransient;
    }

    /**
     * @return the isForeign
     */
    public boolean isForeign()
    {
	return isForeign;
    }
    /**
     * @return the referenceColumn
     */
    public String getReferenceColumn()
    {
	return referenceColumn;
    }
    /**
     * @return the referenceTable
     */
    public String getReferenceTable()
    {
	return referenceTable;
    }

    /**
     * @return the tableName
     */
    public String getTableName()
    {
	return tableName;
    }

    /**
     * @return the destinationColumn
     */
    public String getDestinationColumn()
    {
	return destinationColumn;
    }

    /**
     * @return the referenceClass
     */
    public Class<?> getReferenceClass()
    {
	return referenceClass;
    }

    /**
     * @return the ordered
     */
    public boolean isOrdered()
    {
	return ordered;
    }
}
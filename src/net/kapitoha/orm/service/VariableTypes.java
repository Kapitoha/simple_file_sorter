package net.kapitoha.orm.service;

/**
 *
 *@author Kapitoha
 *
 */
public enum VariableTypes {
    
    VARCHAR(java.lang.String.class.getName()),
    CHARACTER(java.lang.String.class.getName()),
    
    DECIMAL(java.math.BigDecimal.class.getName()),
    NUMERIC(java.math.BigDecimal.class.getName()),
    
    BIT(java.lang.Boolean.class.getName()),
    BIGINT(java.lang.Long.class.getName()),
    TINYINT(java.lang.Byte.class.getName()),
    SMALLINT(java.lang.Short.class.getName()),
    INTEGER(java.lang.Integer.class.getName()),
    REAL(java.lang.Float.class.getName()),
    DOUBLE(java.lang.Double.class.getName()),
    FLOAT(java.lang.Double.class.getName()),
    BINARY("byte[]"),
    VARBINARY("byte[]"),
    LONGVARBINARY("byte[]"),
    DATE(java.sql.Date.class.getName()),
    TIME(java.sql.Time.class.getName()),
    TIMESTAMP(java.sql.Timestamp.class.getName()),
    BLOB(java.sql.Blob.class.getName()),
    CLOB(java.sql.Clob.class.getName());
    
    private final String classType;
    
    VariableTypes(String typeClass)
    {
	this.classType = typeClass;
    }

    /**
     * @return the classType
     */
    public String getClassType()
    {
	return classType;
    }
 
}

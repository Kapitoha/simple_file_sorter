package net.kapitoha.orm.instances;

import java.io.Serializable;

import net.kapitoha.orm.annotations.Id;
import net.kapitoha.orm.annotations.Table;

/**
 *For better compatible between different databases, it necessary to create service table with referenced links between tables.
 *@author Kapitoha
 *
 */
@Table
public class SERVICE_TABLE_REFERENCES implements Serializable {
    private static final long serialVersionUID = 1L;
    @Id
    public String table;
    @Id
    public String column;
    @Id
    public String reference_table;
    @Id
    public String reference_column;
    public String reference_class;
    
    public SERVICE_TABLE_REFERENCES()
    {}
    public SERVICE_TABLE_REFERENCES(String table, String column,
	    String reference_table, String reference_column,
	    String reference_class)
    {
	this.table = table;
	this.column = column;
	this.reference_table = reference_table;
	this.reference_column = reference_column;
	this.reference_class = reference_class;
    }
    @Override
    public int hashCode()
    {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((column == null) ? 0 : column.hashCode());
	result = prime
		* result
		+ ((reference_column == null) ? 0 : reference_column.hashCode());
	result = prime * result
		+ ((reference_table == null) ? 0 : reference_table.hashCode());
	result = prime * result + ((table == null) ? 0 : table.hashCode());
	return result;
    }
    @Override
    public boolean equals(Object obj)
    {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (!(obj instanceof SERVICE_TABLE_REFERENCES))
	    return false;
	SERVICE_TABLE_REFERENCES other = (SERVICE_TABLE_REFERENCES) obj;
	if (column == null)
	{
	    if (other.column != null)
		return false;
	}
	else if (!column.equals(other.column))
	    return false;
	if (reference_column == null)
	{
	    if (other.reference_column != null)
		return false;
	}
	else if (!reference_column.equals(other.reference_column))
	    return false;
	if (reference_table == null)
	{
	    if (other.reference_table != null)
		return false;
	}
	else if (!reference_table.equals(other.reference_table))
	    return false;
	if (table == null)
	{
	    if (other.table != null)
		return false;
	}
	else if (!table.equals(other.table))
	    return false;
	return true;
    }
    
    
}

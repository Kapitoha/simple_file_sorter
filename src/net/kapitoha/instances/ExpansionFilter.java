package net.kapitoha.instances;

import java.io.Serializable;

import net.kapitoha.orm.annotations.Column;
import net.kapitoha.orm.annotations.GeneratedValue;
import net.kapitoha.orm.annotations.Id;
import net.kapitoha.orm.annotations.Transient;

/**
 *
 *@author Kapitoha
 *
 */
public class ExpansionFilter implements Serializable {
    private static final long serialVersionUID = 1L;
    @Transient
    private int position;
    
    @Id
    @GeneratedValue
    private long id;
    @Column (unique=true)
    private String name;
    private String filter;
    
    public int getPosition()
    {
        return position;
    }
    public void setPosition(int position)
    {
        this.position = position;
    }
    public long getId()
    {
        return id;
    }
    public void setId(int id)
    {
        this.id = id;
    }
    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    public String getFilter()
    {
        return filter;
    }
    public void setFilter(String filter)
    {
        this.filter = filter;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
	StringBuilder builder = new StringBuilder();
	builder.append("FileExpansionFilterInstance [id=");
	builder.append(id);
	builder.append(", name=");
	builder.append(name);
	builder.append(", filter=");
	builder.append(filter);
	builder.append("]");
	return builder.toString();
    }

}

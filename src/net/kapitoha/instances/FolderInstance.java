package net.kapitoha.instances;

import java.io.Serializable;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

import net.kapitoha.orm.annotations.Column;
import net.kapitoha.orm.annotations.Entity;
import net.kapitoha.orm.annotations.GeneratedValue;
import net.kapitoha.orm.annotations.Id;
import net.kapitoha.orm.annotations.OneToAnother;

/**
 *
 *@author Kapitoha
 *
 */
@Entity
public class FolderInstance implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue
    @Column(nullable = false, length = 11)
    private long id;
    
    @Column(name="sourcepath", unique=true)
    private String path;
    @OneToAnother(destinationColumn="folder_id")
    private  final Set<FolderFilter> availableFilters = new LinkedHashSet<>();
    
    @Override
    public int hashCode()
    {
	final int prime = 31;
	int result = 1;
	result = prime * result + ((path == null) ? 0 : path.hashCode());
	return result;
    }
    @Override
    public boolean equals(Object obj)
    {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (!(obj instanceof FolderInstance))
	    return false;
	FolderInstance other = (FolderInstance) obj;
	if (path == null)
	{
	    if (other.path != null)
		return false;
	}
	else if (!path.equals(other.path))
	    return false;
	return true;
    }

    public long getId()
    {
        return id;
    }
    public void setId(int id)
    {
        this.id = id;
    }
    public String getPath()
    {
        return path;
    }
    public void setPath(String path)
    {
        this.path = path;
    }
    /**
     * @return the availableFilters
     */
    public Set<FolderFilter> getAvailableFilters()
    {
	return availableFilters;
    }
    
    public boolean addFilter(FolderFilter filter)
    {
	return availableFilters.add(filter);
    }
    
    public boolean addAllFilters(Collection<FolderFilter> collection)
    {
	return availableFilters.addAll(collection);
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
	StringBuilder builder = new StringBuilder();
	builder.append("FolderInstance [id=");
	builder.append(id);
	builder.append(", path=");
	builder.append(path);
	builder.append(", availableFilters=");
	builder.append(availableFilters);
	builder.append("]");
	return builder.toString();
    }

}

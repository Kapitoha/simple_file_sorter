package net.kapitoha.instances;

import java.io.Serializable;

import net.kapitoha.orm.annotations.*;

/**
 *
 *@author Kapitoha
 *
 */
public class FolderFilter implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Foreign(referenceClass = FolderInstance.class, referenceColumn = "id")
    @Column(nullable=false, name="folder_id")
    @Id
    private long folderId;
    
    @Foreign(referenceClass = ExpansionFilter.class, referenceColumn = "id")
    @Column(nullable=false, name="filter_id")
    @Id
    @Order
    private long filterId;
    @Column
    private String destination;
    @Column
    private boolean selected;
    
    
    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
	final int prime = 31;
	int result = 1;
	result = prime * result
		+ ((destination == null) ? 0 : destination.hashCode());
	result = prime * result + (int) (filterId ^ (filterId >>> 32));
	result = prime * result + (int) (folderId ^ (folderId >>> 32));
	return result;
    }
    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (!(obj instanceof FolderFilter))
	    return false;
	FolderFilter other = (FolderFilter) obj;
	if (destination == null)
	{
	    if (other.destination != null)
		return false;
	}
	else if (!destination.equals(other.destination))
	    return false;
	if (filterId != other.filterId)
	    return false;
	if (folderId != other.folderId)
	    return false;
	return true;
    }
    /**
     * @return the folderId
     */
    public long getFolderId()
    {
        return folderId;
    }
    /**
     * @param folderId the folderId to set
     */
    public void setFolderId(long folderId)
    {
        this.folderId = folderId;
    }
    /**
     * @return the filterId
     */
    public long getFilterId()
    {
        return filterId;
    }
    /**
     * @param filterId the filterId to set
     */
    public void setFilterId(long filterId)
    {
        this.filterId = filterId;
    }
    /**
     * @return the destination
     */
    public String getDestination()
    {
        return destination;
    }
    /**
     * @param destination the destination to set
     */
    public void setDestination(String destination)
    {
        this.destination = destination;
    }
    /**
     * @return the selected
     */
    public boolean isSelected()
    {
	return selected;
    }
    /**
     * @param selected the selected to set
     */
    public void setSelected(boolean selected)
    {
	this.selected = selected;
    }
    
    

}

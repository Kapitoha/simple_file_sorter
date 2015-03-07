package net.kapitoha.instances;

import java.awt.LayoutManager;

import javax.swing.JPanel;

/**
 *
 *@author Kapitoha
 *
 */
public class ElementPanel extends JPanel {

    @Override
    public int hashCode()
    {
	final int prime = 31;
	int result = 1;
	result = prime * result + id;
	result = prime * result + parentId;
	return result;
    }
    @Override
    public boolean equals(Object obj)
    {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (!(obj instanceof ElementPanel))
	    return false;
	ElementPanel other = (ElementPanel) obj;
	if (id != other.id)
	    return false;
	if (parentId != other.parentId)
	    return false;
	return true;
    }
    /**
     * 
     */
    private static final long serialVersionUID = 1L;
    private int id;
    private int parentId;
    public ElementPanel(LayoutManager layout)
    {
	super(layout);
    }
    public int getId()
    {
	return id;
    }
    public void setId(int id)
    {
	this.id = id;
    }
    /**
     * @return the parentId
     */
    public int getParentId()
    {
	return parentId;
    }
    /**
     * @param parentId the parentId to set
     */
    public void setParentId(int parentId)
    {
	this.parentId = parentId;
    }

}

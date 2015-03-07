package net.kapitoha.engine;

import java.util.List;

import javax.swing.JProgressBar;

import net.kapitoha.gui.FileExpansionManager;
import net.kapitoha.instances.FolderInstance;
import net.kapitoha.utils.connections.DatabaseManager;

/**
 *
 *@author Kapitoha
 *
 */
public class SortEngine {
    
    public void initAndGo(JProgressBar progressBar)
    {
	FileExpansionManager.reloadFileExpansionFilters();
	List<FolderInstance> folderList = DatabaseManager.getObjects(FolderInstance.class);
	double progress = 0;
	if (null != progressBar)
	{
	    progressBar.setValue(0);
	    progressBar.setString(null);
	    progressBar.setStringPainted(true);
	}
	for (FolderInstance folder : folderList)
	{
	    // Run sorting
	    FileSorter sorter = new FileSorter(folder, true);
	    sorter.scan();
	    if (null != progressBar)
	    {
		progress += progressBar.getMaximum()/folderList.size();
		progressBar.setValue((int) progress);
	    }
	}
	if (null != progressBar)
	{
	    progressBar.setValue(progressBar.getMaximum());
	    progressBar.setString("Ready");
	}
    }
    
}

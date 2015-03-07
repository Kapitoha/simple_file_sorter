package net.kapitoha.root;

import java.util.Map;

import javax.swing.SwingUtilities;

import net.kapitoha.engine.SortEngine;
import net.kapitoha.gui.MainFrame;
import net.kapitoha.instances.ExpansionFilter;
import net.kapitoha.instances.FolderInstance;
import net.kapitoha.loaders.FileExpansionFilterLoader;
import net.kapitoha.utils.connections.DatabaseManager;

/**
 *
 *@author Kapitoha
 *
 */
public class RunProgram {
    
    public static void runGUI()
    {
	SwingUtilities.invokeLater(new Runnable()
	{

	    @Override
	    public void run()
	    {
		MainFrame mf = new MainFrame();
		mf.initialize();
	    }
	});
    }

    public static void main(String[] args)
    {
	if (DatabaseManager.createDB(ExpansionFilter.class, false))
	{
	    for (Map.Entry<String, String> filters : FileExpansionFilterLoader.DEFAULT_FILE_EXPANSION_FILTERS_MAP.entrySet())
	    {
		ExpansionFilter fe = new ExpansionFilter();
		fe.setName(filters.getKey());
		fe.setFilter(filters.getValue());
		DatabaseManager.saveObject(fe);
	    }
	}
	
	if (DatabaseManager.isExistTable(FolderInstance.class))
	{
	    if (args.length > 0)
	    {
		if (args[0].equalsIgnoreCase("-s")
			|| args[0].equalsIgnoreCase("-silent"))
		{
		    SortEngine sortEngine = new SortEngine();
		    sortEngine.initAndGo(null);
		}
	    }
	    else
	    {
		runGUI();
	    }
	    
	}
	else runGUI();

    }

}

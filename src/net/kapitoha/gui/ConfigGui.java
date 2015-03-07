package net.kapitoha.gui;

import java.awt.*;
import java.awt.event.*;
import java.util.Set;

import javax.swing.*;

import net.kapitoha.gui.MainFrame.BrowseAction;
import net.kapitoha.gui.MainFrame.BrowseAction.Selector;
import net.kapitoha.images.Images;
import net.kapitoha.instances.ElementPanel;
import net.kapitoha.instances.ExpansionFilter;
import net.kapitoha.instances.FolderFilter;
import net.kapitoha.instances.FolderInstance;
import net.kapitoha.loaders.LanguageLoader;
import net.kapitoha.orm.annotations.service.TableAttributesReader;
import net.kapitoha.utils.CommonUtilities;
import net.kapitoha.utils.connections.DatabaseManager;
import sun.awt.VerticalBagLayout;

/**
 * 
 * @author Kapitoha
 * 
 */
@SuppressWarnings("restriction")
public class ConfigGui extends JDialog {
    private static final long serialVersionUID = 1L;
    public static boolean IS_OPEN_CONFIG_GUI = false;
    private final ElementPanel parentPanel;
    private final Window parent;
    private final JPanel contentPanel = new JPanel(new BorderLayout());
    private final JTextField pathField = new JTextField();
    private FolderInstance folder;

    public ConfigGui(ElementPanel panel, FolderInstance folder, Window parent)
    {
	this.parentPanel = panel;
	this.parent = parent;
	this.folder = folder;
    }

    public void initialization()
    {
	pathField.setText(parentPanel.getName());
	pathField.setEditable(false);
	setIconImage(Images.LOGO);
	JPanel panel = new JPanel(new BorderLayout());
	JPanel buttonPanel = new JPanel();
	JPanel scrollablePanel = new JPanel(new VerticalBagLayout());
	JScrollPane scrollPane = new JScrollPane(scrollablePanel);
	JButton editButton = new JButton(LanguageLoader.getLanguage().getProperty("button_edit"));
	JButton closeButton = new JButton(LanguageLoader.getLanguage().getProperty("button_close"));
	panel.add(new JLabel(LanguageLoader.getLanguage().getProperty("lab_current_folder")), BorderLayout.NORTH);
	panel.add(pathField, "Center");
	panel.add(editButton, BorderLayout.EAST);
	contentPanel.add(panel, "North");
	buttonPanel.add(closeButton);
	contentPanel.add(buttonPanel, "South");
	setJMenuBar(MainFrame.mainMenu(this));
	contentPanel.add(scrollPane, "Center");
	Set<FolderFilter> filterList = folder.getAvailableFilters();
	//Check if was delete action. If true - reload filters
	if (DatabaseManager.WAS_DELETE_ACTION)
	{
	    folder.getAvailableFilters().clear();
	    if (folder.addAllFilters(DatabaseManager.getObjects(FolderFilter.class, 
		    String.format("SELECT * FROM %s WHERE folder_id = ?", 
			    new TableAttributesReader(FolderFilter.class).getTableName()), folder.getId())))
		DatabaseManager.WAS_DELETE_ACTION = false;
	}
	for (FolderFilter filter : filterList)
	{
	    ExpansionFilter common = DatabaseManager.getObjects(ExpansionFilter.class, 
		    String.format("SELECT * FROM %s WHERE id = ?;", 
			    new TableAttributesReader(ExpansionFilter.class).getTableName()), filter.getFilterId()).get(0);
	    if (common != null)
		scrollablePanel.add(addExpansionPanel(common.getName(),filter));
	}

	editButton.addActionListener(new EditTargetPathAction(folder));
	closeButton.addActionListener(new ActionListener()
	{
	    @Override
	    public void actionPerformed(ActionEvent e)
	    {
		dispose();
	    }
	});

	setSize(parent.getWidth(), parent.getHeight());
	setLocationRelativeTo(null);
	setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	setContentPane(this.contentPanel);
	addWindowListener(new WindowAction());
	setVisible(true);
    }
    
    private ElementPanel addExpansionPanel(String name, FolderFilter filter)
    {
	
	ElementPanel ep = new ElementPanel(new BorderLayout());
	JPanel buttonPanel = new JPanel();
	JPanel namePanel = new JPanel(new BorderLayout());
	JCheckBox enable = new JCheckBox();
	JLabel filterName = new JLabel(name);
	JTextField destinationPathField = new JTextField();
	JButton changeButton = new JButton(LanguageLoader.getLanguage().getProperty("button_change"));
	JButton resetButton = new JButton(LanguageLoader.getLanguage().getProperty("button_reset"));
	
	ep.setName(name);
	ep.setParentId((int) filter.getFolderId());
	ep.setId((int) filter.getFilterId());
	enable.setSelected(filter.isSelected());
	destinationPathField.setText(filter.getDestination());

	buttonPanel.add(changeButton);
	buttonPanel.add(resetButton);
	namePanel.add(enable, "West");
	namePanel.add(filterName, "Center");

	ep.add(namePanel, BorderLayout.NORTH);
	ep.add(new JLabel(LanguageLoader.getLanguage().getProperty("lab_destination")), "West");
	ep.add(destinationPathField, "Center");
	ep.add(buttonPanel, BorderLayout.EAST);
	ep.add(new JSeparator(), "South");

	enable.addActionListener(new SelectEnableFilterAction(filter));
	changeButton.addActionListener(new ChangeFilterPathAction(ep, filter));
	resetButton.addActionListener(new ResetFiltersPath(ep, filter));
	return ep;
    }
    
    private class ResetFiltersPath implements ActionListener {
	private ElementPanel panel;
	private FolderFilter filter;

	public ResetFiltersPath(ElementPanel panel, FolderFilter filter)
	{
	    super();
	    this.panel = panel;
	    this.filter = filter;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
	    ExpansionFilter common = DatabaseManager.getObjects(ExpansionFilter.class, 
		    String.format("SELECT * FROM %s WHERE id = ?;", 
			    new TableAttributesReader(ExpansionFilter.class).getTableName()), filter.getFilterId()).get(0);
	    String defaultDestination = CommonUtilities.getDefaultDestinationPath(folder.getPath(), common.getName());
	    filter.setDestination(defaultDestination);
		if (DatabaseManager.updateObject(filter))
		{
		    for (Component comp : panel.getComponents())
			{
			    if (comp instanceof JTextField)
			    {
				((JTextField) comp).setText(defaultDestination);
				break;
			    }
			}
		}
		panel.repaint();
		setVisible(true);
	}
	
    }
    
    private class ChangeFilterPathAction implements ActionListener {
	private ElementPanel panel;
	private FolderFilter folderFilter;
	
	public ChangeFilterPathAction(ElementPanel panel, FolderFilter folderFilter)
	{
	    super();
	    this.panel = panel;
	    this.folderFilter = folderFilter;
	}
	@Override
	public void actionPerformed(ActionEvent e)
	{
	    BrowseAction ba = new BrowseAction(Selector.DirectoriesOnly, false);
	    ba.actionPerformed(e);
	    if (ba.getPath() != null
		    && CommonUtilities.checkPathIfExist(ba.getPath().toString()) && ba.getPath().isDirectory())
	    {
		folderFilter.setDestination(ba.getPath().toString());
		if (DatabaseManager.updateObject(folderFilter))
		{
		    for (Component comp : panel.getComponents())
			{
			    if (comp instanceof JTextField)
			    {
				((JTextField) comp).setText(ba.getPath().toString());
				break;
			    }
			}
		}
		panel.repaint();
		setVisible(true);
	    }
	    
	}
	
    }

    private class EditTargetPathAction implements ActionListener {
	private FolderInstance folder;
	public EditTargetPathAction(FolderInstance folder)
	{
	    this.folder = folder;
	}
	@Override
	public void actionPerformed(ActionEvent e)
	{
	    BrowseAction ba = new BrowseAction(Selector.DirectoriesOnly, false);
	    ba.actionPerformed(e);
	    if (ba.getPath() != null
		    && CommonUtilities.checkPathIfExist(ba.getPath().toString()))
	    {
		folder.setPath(ba.getPath().toString());
		for (FolderFilter filter : folder.getAvailableFilters())
		{
		    String filterName = DatabaseManager.getObjects(ExpansionFilter.class, String.format(
			    "SELECT * FROM %s WHERE id = ?", new TableAttributesReader(ExpansionFilter.class)
			    .getTableName()), filter.getFilterId()).get(0).getName();
		    if (filterName != null && !filterName.isEmpty())
			filter.setDestination(CommonUtilities.getDefaultDestinationPath(folder.getPath(), filterName));
		}
		if (DatabaseManager.updateObject(folder))
		{
		    pathField.setText(ba.getPath().toString());
		    for (Component comp : parentPanel.getComponents())
		    {
			if (comp instanceof JTextField
				&& ((JTextField) comp).getText().equals(parentPanel.getName()))
			{
			    parentPanel.setName(ba.getPath().toString());
			    ((JTextField) comp).setText(ba.getPath().toString());
			    contentPanel.removeAll();
			    initialization();
			    break;
			}
		    }
		    
		}
	    }
	}

    }

    private static class SelectEnableFilterAction implements ActionListener {
	private FolderFilter filter;

	public SelectEnableFilterAction(FolderFilter filter)
	{
	    super();
	    this.filter = filter;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
	    if (e.getSource() instanceof JCheckBox)
	    {
		JCheckBox box = (JCheckBox) e.getSource();
		filter.setSelected(box.isSelected());
		if (!DatabaseManager.updateObject(filter))
		{
		    box.setSelected(false);
		    box.setEnabled(false);
		}
	    }
	}
    }

    private class WindowAction implements WindowListener {

	@Override
	public void windowOpened(WindowEvent e)
	{
	    IS_OPEN_CONFIG_GUI = true;
	}

	@Override
	public void windowClosing(WindowEvent e)
	{
	    dispose();
	}

	@Override
	public void windowClosed(WindowEvent e)
	{
	    IS_OPEN_CONFIG_GUI = false;
	}

	@Override
	public void windowIconified(WindowEvent e)
	{
	}

	@Override
	public void windowDeiconified(WindowEvent e)
	{
	}

	@Override
	public void windowActivated(WindowEvent e)
	{
	}

	@Override
	public void windowDeactivated(WindowEvent e)
	{
	}
    }
}
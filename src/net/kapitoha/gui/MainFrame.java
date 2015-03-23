package net.kapitoha.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.List;

import javax.swing.*;

import net.kapitoha.engine.SortEngine;
import net.kapitoha.gui.MainFrame.BrowseAction.Selector;
import net.kapitoha.images.Images;
import net.kapitoha.instances.ElementPanel;
import net.kapitoha.instances.ExpansionFilter;
import net.kapitoha.instances.FolderFilter;
import net.kapitoha.instances.FolderInstance;
import net.kapitoha.loaders.LanguageLoader;
import net.kapitoha.utils.CommonUtilities;
import net.kapitoha.utils.connections.ConnectionManager;
import net.kapitoha.utils.connections.DatabaseManager;
import sun.awt.VerticalBagLayout;

/**
 *
 *@author Kapitoha
 *
 */
@SuppressWarnings("restriction")
public class MainFrame extends JFrame {
    private static final long serialVersionUID = 1L;
    private final JFrame instance = this;
    private final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
    private JPanel contentPanel = new JPanel(new VerticalBagLayout());
    private JProgressBar progressBar;
    
    @SuppressWarnings("serial")
    public void initialize()
    {
	contentPanel.setBorder(BorderFactory.createEtchedBorder());
	setSize((int)screenSize.getWidth() >> 1, (int)screenSize.getHeight() >> 1);
	setTitle(LanguageLoader.getLanguage().getProperty("title_main"));
	setLocationRelativeTo(null);
	setIconImage(Images.LOGO);
	setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	JPanel mainPane = new JPanel(new BorderLayout())
	{
		public void paintComponent(Graphics g)
		{
		    if (null != Images.BACKGROUND)
		    {
			g.drawImage(Images.BACKGROUND, 0, 0, this.getWidth(),
				this.getHeight(), this);
		    }
		}
	};
	JScrollPane js = new JScrollPane(contentPanel);
	mainPane.add(getControlPanel(), "North");
	mainPane.add(js, "Center");
	mainPane.add(getButtonPanel(), "South");
	js.setBackground(new Color(0, 0, 0, 0));
	contentPanel.setBackground(new Color(0, 0, 0, 0));
	setContentPane(mainPane);
	mainPane.setBorder(BorderFactory.createEtchedBorder());
	
	JMenuBar mBar = mainMenu(instance, serviceMenu(instance));
	setJMenuBar(mBar);
	
	List<FolderInstance> folderList = DatabaseManager.getObjects(FolderInstance.class);
	for (FolderInstance folderInstance : folderList)
	{
	    contentPanel.add(addElementPane(folderInstance));
	}
	
	setVisible(true);
    }
    
    public JPanel getControlPanel()
    {
	JPanel panel = new JPanel();
	panel.setBackground(new Color(0, 0, 0, 0));
	JButton addButton = new JButton(LanguageLoader.getLanguage().getProperty("button_new_target"));
	addButton.addActionListener(new AddNewTargetFolderAction());
	panel.add(addButton);
	return panel;
    }
    
    public JPanel getButtonPanel()
    {
	JPanel panel = new JPanel(new BorderLayout());
	JPanel buttonPanel = new JPanel();
	panel.setBackground(new Color(0, 0, 0, 65));
	buttonPanel.setBackground(new Color(0, 0, 0, 65));
	progressBar = new JProgressBar(0, 100);
	JButton sortButton = new JButton(LanguageLoader.getLanguage().getProperty("button_sort"));
	JButton exitButton = new JButton(LanguageLoader.getLanguage().getProperty("button_exit"));
	buttonPanel.add(sortButton);
	buttonPanel.add(exitButton);
	panel.add(progressBar, "North");
	panel.add(buttonPanel, "Center");
	
	sortButton.addActionListener(new StartSortAction(progressBar));
	exitButton.addActionListener(new ActionListener()
	{
	    @Override
	    public void actionPerformed(ActionEvent e)
	    {
		System.exit(0);
	    }
	});
	panel.setBorder(BorderFactory.createEtchedBorder());
	return panel;
    }
    
    public ElementPanel addElementPane(FolderInstance folder)
    {
	JTextField targetPath = new JTextField();
	Font font = new Font("Serif", Font.BOLD, 15);
	targetPath.setForeground(Color.yellow);
	targetPath.setFont(font);
	JButton configButton = new JButton(LanguageLoader.getLanguage().getProperty("button_config"));
	JButton deleteButton = new JButton(LanguageLoader.getLanguage().getProperty("button_delete"));
	ElementPanel panel = new ElementPanel(new BorderLayout());
	JPanel configPane = new JPanel();
	
	panel.setBackground(new Color(0, 0, 0, 65));
	configPane.setBackground(new Color(0, 0, 0, 65));
	targetPath.setBackground(new Color(255, 255, 255, 100));
	
	panel.setName(folder.getPath());
	panel.setId((int) folder.getId());
	targetPath.setText(folder.getPath());
	targetPath.setEditable(false);
	
	deleteButton.addActionListener(new DeleteAction(panel, folder));
	configButton.addActionListener(new OpenConfigAction(panel, folder));
	
	configPane.add(configButton);
	configPane.add(deleteButton);
	panel.add(targetPath, BorderLayout.CENTER);
	panel.add(configPane, "East");
	panel.add(new JSeparator(), "South");
	panel.setBorder(BorderFactory.createEtchedBorder());
	return panel;
    }
    
    public static JMenuBar mainMenu(Window instance, JMenu... menues)
    {
	JMenuBar bar = new JMenuBar();
	JMenu optionMenu = new JMenu(LanguageLoader.getLanguage().getProperty("menu_options"));
	JMenuItem fileExpansionManagerItem = new JMenuItem(LanguageLoader.getLanguage().getProperty("menu_expansion_manager"));
	JMenuItem exitItem = new JMenuItem(LanguageLoader.getLanguage().getProperty("menu_exit"));
	optionMenu.add(fileExpansionManagerItem);
	optionMenu.add(new JSeparator());
	optionMenu.add(exitItem);
	
	fileExpansionManagerItem.addActionListener(new OpenFileExpansionManager(instance));
	exitItem.addActionListener(new ActionListener()
	{
	    @Override
	    public void actionPerformed(ActionEvent e){ System.exit(0); }
	});
	bar.add(optionMenu);
	for (JMenu jMenu : menues)
	{
	    bar.add(jMenu);
	}
	return bar;
    }
    
    private static JMenu serviceMenu(Window instance)
    {
	JMenu menu = new JMenu(LanguageLoader.getLanguage().getProperty("menu_other"));
	JMenu dangerMenu = new JMenu(LanguageLoader.getLanguage().getProperty("menu_danger"));
	JMenuItem aboutItem = new JMenuItem("About");
	JMenuItem dropDatabase = new JMenuItem(LanguageLoader.getLanguage().getProperty("delete_db"));
	dropDatabase.setForeground(Color.red);
	dangerMenu.add(dropDatabase);
	menu.add(aboutItem);
	menu.add(new JSeparator());
	menu.add(dangerMenu);
	aboutItem.addActionListener(new AboutListener(instance));
	dropDatabase.addActionListener(new DropDatabaseAction(instance));
	return menu;
    }
    
    private class AddNewTargetFolderAction implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e)
	{
	    BrowseAction ba = new BrowseAction(Selector.DirectoriesOnly, true);
	    ba.actionPerformed(e);
	    if (null != ba.getPathes() && ba.getPathes().length > 0)
	    {
		for (File file : ba.getPathes())
		{
		    String path = file.toString();
		    if (CommonUtilities.checkPathIfExist(path))
		    {
			FolderInstance folder = new FolderInstance();
			folder.setPath(path);
			
			List<ExpansionFilter> filterList = DatabaseManager.getObjects(ExpansionFilter.class);
			if (!filterList.isEmpty())
			{
			    for (ExpansionFilter expansionFilter : filterList)
			    {
				FolderFilter ff = new FolderFilter();
				ff.setDestination(CommonUtilities.getDefaultDestinationPath(path, expansionFilter.getName()));
				ff.setFilterId(expansionFilter.getId());
				ff.setSelected(true);
				folder.addFilter(ff);
			    }
			}
			Long key = (Long) DatabaseManager.saveObject(folder);
			if (key != null)
			{
			    folder.setId(key.intValue());
			    contentPanel.add(addElementPane(folder));
			    repaint();
			    setVisible(true);
			}
		    }
		}
	    }
	}
    }
    
    public static class BrowseAction implements ActionListener {
	public enum Selector {
	    FilesOnly,
	    DirectoriesOnly,
	    BothFilesAndFolders;
	}
	private Selector select;
	private File path;
	private File[] pathes;
	private boolean showSaveDialog;
	private boolean multiSelection;
	private String defaultPathName = "new document.txt";

	public BrowseAction( Selector select, boolean setMultipleSelectionEnable)
	{
	    this.select = select;
	    this.multiSelection = setMultipleSelectionEnable;
	}
	
	public BrowseAction(Selector select, boolean showSaveDialog, boolean setMultipleSelectionEnable)
	{
	    this(select, setMultipleSelectionEnable);
	    this.showSaveDialog = showSaveDialog;
	}
	@Override
	public void actionPerformed(ActionEvent e)
	{
	    JFileChooser fileChooser = new JFileChooser();
	    switch(select)
	    {
		case FilesOnly:
		    fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		    break;
		case DirectoriesOnly:
		    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		    break;
		case BothFilesAndFolders:
		    fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		    break;
		default:
		    fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
	    }
	    fileChooser.setMultiSelectionEnabled(multiSelection);
	    int returnVal = -100;
	    if (showSaveDialog)
	    {
		if (multiSelection)
		    System.err.println("You had set multiple selection to: 'true' in save menu. Possible will occur some errors.");
		fileChooser.setSelectedFile(new File(defaultPathName));
		returnVal = fileChooser.showSaveDialog(null);
	    }
	    else
		returnVal = fileChooser.showOpenDialog(null);
	    if (returnVal == JFileChooser.APPROVE_OPTION)
	    {
		path = fileChooser.getSelectedFile();
		pathes = fileChooser.getSelectedFiles();
	    }
	    
	}
	public File getPath()
	{
	    if (multiSelection)
		throw new UnsupportedOperationException("Use getPathes() (returns File[] array) method instead, because your multiple selection enables == true");
	    return path;
	}
	
	public File[] getPathes()
	{
	    if (!multiSelection)
		throw new UnsupportedOperationException("Use getPath() (returns File) method instead, because your multiple selection enables == false");
	    return pathes;
	}

	public String getDefaultPathName()
	{
	    return defaultPathName;
	}

	public void setDefaultPathName(String defaultPathName)
	{
	    this.defaultPathName = defaultPathName;
	}
	
    }
    
    private class DeleteAction implements ActionListener {
	private final ElementPanel path;
	private final FolderInstance folder;
	public DeleteAction(ElementPanel panel, FolderInstance folder)
	{
	    this.path = panel;
	    this.folder = folder;
	}
	@Override
	public void actionPerformed(ActionEvent e)
	{
	    if (DatabaseManager.deleteObject(folder))
	    {
		int c = 0;
		for (Component comp : contentPanel.getComponents())
		{
		    if (comp instanceof ElementPanel
			    && ((ElementPanel) comp).getId() == path.getId())
		    {
			contentPanel.remove(c);
			instance.repaint();
			instance.setVisible(true);
			break;
		    }
		    c++;
		}
	    }
//	    if (SQLManager.executeSQL(String.format("DELETE FROM %s WHERE target_id = ?;", DatabaseManager.TABLE_NAME_PARTICULAR_EXPANSION), path.getId()))
//	    {
//		if (SQLManager.executeSQL(String.format("DELETE FROM %s WHERE id = ?", DatabaseManager.TABLE_NAME_TARGET_FOLDERS), path.getId()))
//		{
//		    for (Component comp : contentPanel.getComponents())
//		    {
//			if (comp instanceof ElementPanel && ((ElementPanel) comp).getId() == path.getId())
//			{
//			    contentPanel.remove(comp);
//			    instance.repaint();
//			    instance.setVisible(true);
//			}
//		    }
//		}
//		
//	    }
	}
	
    }
    
    private class OpenConfigAction implements ActionListener {
	private final ElementPanel path;
	private final FolderInstance folder;
	public OpenConfigAction(ElementPanel path, FolderInstance folder)
	{
	    this.path = path;
	    this.folder = folder;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
	    if (!ConfigGui.IS_OPEN_CONFIG_GUI)
    	    SwingUtilities.invokeLater(new Runnable()
    	    {
    	        @Override
    	        public void run()
    	        {
    	            new ConfigGui(path, folder, instance).initialization();
    	        }
    	    });
	}
	
    }
    
    private static class OpenFileExpansionManager implements ActionListener {
	private Window parent;

	public OpenFileExpansionManager(Window parent)
	{
	    super();
	    this.parent = parent;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
	    if (!FileExpansionManager.IS_OPEN_EXPANSION_MANAGER)
	    SwingUtilities.invokeLater(new Runnable()
	    {
	        @Override
	        public void run()
	        {
	            new FileExpansionManager(parent).initialization();
	        }
	    });
	}
	
    }
    
    private static class StartSortAction implements ActionListener {
	private static Thread sortActionThread;
	private JProgressBar progressBar;
	
	public StartSortAction(JProgressBar progressBar)
	{
	    this.progressBar = progressBar;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
	    if (null != sortActionThread && sortActionThread.isAlive())
		return;
	    sortActionThread = new Thread(new Runnable()
	    {
	        @Override
	        public void run()
	        {
	            new SortEngine().initAndGo(progressBar);
	        }
	    });
	    sortActionThread.start();
	}
	
    }
    
    private static class DropDatabaseAction implements ActionListener {
	private Window parent;

	public DropDatabaseAction(Window parent)
	{
	    super();
	    this.parent = parent;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
	    Object[] options = {LanguageLoader.getLanguage().getProperty("yes"), LanguageLoader.getLanguage().getProperty("no")};
	    int result = JOptionPane.showOptionDialog(parent, 
		    LanguageLoader.getLanguage().getProperty("msg_confirm_delete_db"), 
		    LanguageLoader.getLanguage().getProperty("title_warning"), 
		    JOptionPane.YES_NO_OPTION, 
		    JOptionPane.WARNING_MESSAGE,null, options, options[1]);
	    if (result == 0)
	    {
		if (DatabaseManager.deleteDatabase())
		    System.exit(0);
		else
		    JOptionPane.showConfirmDialog(parent, 
			LanguageLoader.getLanguage().getProperty("msg_cannot_rm_db")
			+ "\n"
		    	+ ConnectionManager.USER_DB.toString().concat("mv.db"), 
			LanguageLoader.getLanguage().getProperty("title_warning"), 
		    	JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE);
	    }
	    
	}
	
    }
    
    private static class AboutListener implements ActionListener {
	private Window parent;
	public AboutListener(Window parent)
	{
	    this.parent = parent;
	}
	@Override
	public void actionPerformed(ActionEvent e)
	{
	    JOptionPane.showConfirmDialog(parent, 
			"This program helps you \n"
			+ "to sort your files in \n"
			+ "Downloads folder (for men) \n"
			+ "and Desktop (for girls). \n"
			+ "\n"
			+ "Created by Kapitoha \n"
			+ "kapitohaua@gmail.com", 
			"About", 
		    	JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE);
	}
	
    }


}

package net.kapitoha.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

import net.kapitoha.images.Images;
import net.kapitoha.instances.ElementPanel;
import net.kapitoha.instances.ExpansionFilter;
import net.kapitoha.loaders.LanguageLoader;
import net.kapitoha.orm.annotations.service.TableAttributesReader;
import net.kapitoha.utils.connections.DatabaseManager;
import sun.awt.VerticalBagLayout;

/**
 *
 *@author Kapitoha
 *
 */
@SuppressWarnings("restriction")
public class FileExpansionManager extends JFrame {
    private static final long serialVersionUID = 1L;
    public static boolean IS_OPEN_EXPANSION_MANAGER = false;
    private final Window instance = this;
    private static final Set<ExpansionFilter> FILE_EXPANSION_LIST = new LinkedHashSet<>();
    
    
    private final Window parent;
    private JPanel contentPanel = new JPanel(new BorderLayout());
    private JPanel scrolledContent = new JPanel(new VerticalBagLayout());
    
    public FileExpansionManager(Window parent)
    {
	super();
	this.parent = parent;
    }

    public void initialization()
    {
	 setSize(parent.getWidth(), parent.getHeight());
	 setIconImage(Images.LOGO);
	 setTitle(LanguageLoader.getLanguage().getProperty("title_expansion_mngr"));
	 setLocationRelativeTo(parent);
	 setAlwaysOnTop(true);
	 setAutoRequestFocus(true);
	 addWindowListener(new WindowActions());
	 setContentPane(this.contentPanel);
	 JScrollPane sp = new JScrollPane(scrolledContent);
	 sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
	 contentPanel.add(getControlPanel(), "North");
	 contentPanel.add(sp, "Center");
	 contentPanel.add(getConfirmPanel(), "South");
	 
	 for (ExpansionFilter filter : getFileExpansionFilters())
	 { 
	    scrolledContent.add( addElementPanel(filter));
	 }
	 contentPanel.setBorder(BorderFactory.createEtchedBorder());
	 setVisible(true);
    }
    
    public static void reloadFileExpansionFilters()
    {
	FILE_EXPANSION_LIST.clear();
	FILE_EXPANSION_LIST.addAll(DatabaseManager.getObjects(ExpansionFilter.class));
    }
    
    public JPanel getControlPanel()
    {
	JPanel panel = new JPanel();
	JButton addButton = new JButton(LanguageLoader.getLanguage().getProperty("button_add_filter"));
	addButton.addActionListener(new AddNewExpansionFilter());
	panel.add(addButton);
	return panel;
    }
    
    public JPanel getConfirmPanel()
    {
	JPanel panel = new JPanel();
	JButton applyButton = new JButton(LanguageLoader.getLanguage().getProperty("button_apply"));
	JButton cncelButton = new JButton(LanguageLoader.getLanguage().getProperty("button_cancel"));
	applyButton.addActionListener(new ApplyAction());
	cncelButton.addActionListener(new ActionListener()
	{
	    @Override
	    public void actionPerformed(ActionEvent e){ dispose(); }
	});
	panel.add(applyButton);
	panel.add(cncelButton);
	
	applyButton.addActionListener(new ApplyAction());
	return panel;
    }
    
    private ElementPanel addElementPanel(ExpansionFilter filter)
    {
	ElementPanel elPanel = new ElementPanel(new BorderLayout());
	JTextField expansionField = new JTextField();
	JLabel nameLabel = new JLabel(filter.getName());
	JButton deleteButton = new JButton(LanguageLoader.getLanguage().getProperty("button_delete"));
	PlainDocument doc = new PlainDocument();
	
	doc.setDocumentFilter(new InputFilter());
	expansionField.setColumns(1);
	expansionField.setDocument(doc);
	expansionField.setText(filter.getFilter());
	elPanel.setName(filter.getName());
	elPanel.setId((int) filter.getId());
	
	deleteButton.addActionListener(new DeleteFilterAction(elPanel, filter));
	
	elPanel.add(nameLabel, "North");
	elPanel.add(expansionField, "Center");
	elPanel.add(deleteButton, "East");
	elPanel.setBorder(BorderFactory.createEtchedBorder());
	return elPanel;
    }
    
    private class AddNewExpansionFilter implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e)
	{
	    final String newName = JOptionPane.showInputDialog(parent, 
		    LanguageLoader.getLanguage().getProperty("msg_input_name"), 
		    LanguageLoader.getLanguage().getProperty("title_input_name"), 
		    JOptionPane.PLAIN_MESSAGE);
	    ExpansionFilter fe = new ExpansionFilter();
	    fe.setName(newName);
	    if (newName != null && !newName.isEmpty() && !newName.matches("\\s+"))
	    {
		Long id = (Long) DatabaseManager.saveObject(fe);
		if (id != null)
		{
		    fe.setId(id.intValue());
		    ElementPanel ep = null;
		    ep = addElementPanel(fe);
		    scrolledContent.add(ep);
		}
		repaint();
		setVisible(true);
	    }
	}
	
    }
    
    public static Set<ExpansionFilter> getFileExpansionFilters()
    {
	if (FILE_EXPANSION_LIST.isEmpty())
	    reloadFileExpansionFilters();
	return FILE_EXPANSION_LIST;
    }
    
    private class ApplyAction implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e)
	{
	    for (Component panel : scrolledContent.getComponents())
	    {
		if (panel instanceof ElementPanel)
		{
		    String filterField = "";
			for (Component filter : ((ElementPanel) panel).getComponents())
			{
			    if (filter instanceof JTextField)
			    {
				String sql = String.format("SELECT * FROM %s WHERE id = ?;", 
					new TableAttributesReader(ExpansionFilter.class).getTableName());
				List<ExpansionFilter> list = DatabaseManager.getObjects(ExpansionFilter.class, sql, 
					((ElementPanel) panel).getId());
				ExpansionFilter filterInstance = null;
				if (list != null && !list.isEmpty())
				{
				    filterInstance = list.get(0);
        				filterField = ((JTextField) filter).getText()
        //					.replaceAll("[^A-Za-z0-9,\\.]+", "")
        					.replaceAll("\\,{2,}", ",")
        					.replaceAll("\\.{2,}", ".");
        				filterInstance.setFilter(filterField);
        				DatabaseManager.updateObject(filterInstance);
				}
			    }
			}
		}
	    }
	    reloadFileExpansionFilters();
	    dispose();
	}
	
    }
    
    private class DeleteFilterAction implements ActionListener {
	private final ElementPanel panel;
	private ExpansionFilter filter;
	
	public DeleteFilterAction(ElementPanel panel, ExpansionFilter filter)
	{
	    super();
	    this.panel = panel;
	    this.filter = filter;
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
	    if (DatabaseManager.deleteObject(filter))
	    {
		scrolledContent.remove(panel);
		reloadFileExpansionFilters();
		instance.repaint();
		instance.setVisible(true);
	    }
	}
    }
    
    private class WindowActions implements WindowListener {

	@Override
	public void windowOpened(WindowEvent e)
	{
	    IS_OPEN_EXPANSION_MANAGER = true;
	}

	@Override
	public void windowClosing(WindowEvent e)
	{
	    dispose();
	}

	@Override
	public void windowClosed(WindowEvent e)
	{
	    IS_OPEN_EXPANSION_MANAGER = false;
	}

	@Override
	public void windowIconified(WindowEvent e){}

	@Override
	public void windowDeiconified(WindowEvent e){}

	@Override
	public void windowActivated(WindowEvent e){}

	@Override
	public void windowDeactivated(WindowEvent e){}
	
    }
    
    /**
     * Preserve wrong char inputing
     * @author Kapitoha
     *
     */
    private class InputFilter extends DocumentFilter {
	private String regex = "[^A-Za-z0-9,\\.]+";
	    @Override
	    public void insertString(FilterBypass fb, int off, String str, AttributeSet attr) 
	        throws BadLocationException 
	    {
	        fb.insertString(off, str.replaceAll(regex, "").replaceAll("\\,{2,}", ",")
			.replaceAll("\\.{2,}", "."), attr);
	    } 
	    @Override
	    public void replace(FilterBypass fb, int off, int len, String str, AttributeSet attr) 
	        throws BadLocationException 
	    {
	        fb.replace(off, len, str.replaceAll(regex, ""), attr);
	    }
    }

}

package mozrt.hsmw.de;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/*
    ---------------
    MOZRT_UI.java
    ---------------
    (C) Copyright 2015.

    Original Author:  Dirk Pawlaszczyk
    Contributor(s):   -;

   
    Project Info:  http://www.hs-mittweida.de

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    Dieses Programm ist Freie Software: Sie können es unter den Bedingungen
    der GNU General Public License, wie von der Free Software Foundation,
    Version 3 der Lizenz oder (nach Ihrer Wahl) jeder neueren
    veröffentlichten Version, weiterverbreiten und/oder modifizieren.

    Dieses Programm wird in der Hoffnung, dass es nützlich sein wird, aber
    OHNE JEDE GEWÄHRLEISTUNG, bereitgestellt; sogar ohne die implizite
    Gewährleistung der MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN BESTIMMTEN ZWECK.
    Siehe die GNU General Public License für weitere Details.

    Sie sollten eine Kopie der GNU General Public License zusammen mit diesem
    Programm erhalten haben. Wenn nicht, siehe <http://www.gnu.org/licenses/>.
 
*/

/**
 * This class offers a basic graphical user interface. It is based on JFrame.
 * Most of this code was generated or build with WindowBuilder. 
 * 
 * @author Dirk Pawlaszczyk
 * @version 1.0
 */
@SuppressWarnings("serial")
public class MOZRT_UI extends JFrame {

	private JPanel contentPane;
    private MOZRT_UI mainwindow;
    private JTable table_default;
    private JTable table_moz_places;
    private JTable table_moz_bookmarks;
    private JTable table_moz_bookmarks_roots;
    private JTable table_moz_inputhistory;
    private JTable table_moz_keywords;
    private JTable table_moz_sequence;
    private JTable table_moz_favicons;
    private JTable table_moz_annos;
    private JTable table_moz_anno_attributes;
    private JTable table_moz_items_annos;
    private JTable table_moz_history_visit;
    JTextField searchTF = new JTextField(20);
    Hashtable<String,JTable> tables = new Hashtable<String,JTable>();
    DefaultListModel<String> listModel;
    
    JTextArea logwindow;
    JMenuBar menuBar;
    
    private String moz_places_columns[] = { "id", "url", "title","rev_host","visit_count","hidden","typed","favicon_id","frecency","last_visit_date","guuid"};
    private String moz_bookmarks[] = { "id", "type", "fk","parent","position","title","date_added","last_modified","keyword_id","folder_type"};
    private String moz_bookmarks_roots[] = {"rootname","folder"};
    private String moz_inputhistory[] = {"placeid","input","use count"};
    private String moz_keywords[] = { "id","keyword"};
    private String moz_sequence[] = { "name","seq"};
    private String moz_favicons[] = { "id","url","data","mime_type","expiration"};
    private String moz_annos[] = { "id","place_id","anno_attribute_id","mime_type","content","flags","expiration","type","dateAdded","lastModified"};
    private String moz_anno_attributes[] = { "id","name"};
    private String moz_items_annos[] = { "id","item_id","anno_attribute_id","mime_type","content","flags","expiration","type","dateAdded","lastModified"};
    private String moz_history_visit[] = { "id","from_visit","place_id", "visit_date", "visit_type", "session"};
    private String default_columns[] = { "unkown","unkown","unkown","unkown","unkown","unkown", "unkown", "unkown", "unkown", "unkown" };
    
    JScrollPane scrollpane_tables;
    Statusbar statusbar = new Statusbar();
 
    MOZRT_UI app = this;
    JList<String> list;
    
    String lasthit = "";
    int lasthitrow = 0;
    int lasthitcol = 0;
    
	/**
	 * Launch the application with this method. 
	 */
	public static void main(String[] args) {
	
			EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MOZRT_UI frame = new MOZRT_UI();
					String plaf = UIManager.getSystemLookAndFeelClassName();
			        UIManager.setLookAndFeel(plaf);
			        SwingUtilities.updateComponentTreeUI(frame);
			        
			        
			        frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the front end.
	 * @throws IOException 
	 */
	public MOZRT_UI() throws IOException {
		
		setTitle("MOZ Places Retrieval Tool");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		URL url = MOZRT_UI.class.getResource("/find.png");
		setIconImage(new ImageIcon(url).getImage());
		//available size of the screen 
	    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize((int)(screenSize.width*0.8),(int)(screenSize.height*0.8));
		setLocation((screenSize.width - getWidth()) / 2, (screenSize.height - getHeight()) / 2);
	  					
		menuBar = new JMenuBar();
		setJMenuBar(menuBar);		
		JMenu mnFiles = new JMenu("File");
		mnFiles.setMnemonic(KeyEvent.VK_F);		
		menuBar.add(mnFiles);
	
		JMenuItem mntopen = new JMenuItem("Open Database...");
   	    mntopen.setMnemonic(KeyEvent.VK_O);
		mntopen.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			   open_db();	    
			}
		});
		mnFiles.add(mntopen);
		
		JMenuItem mntmExport = new JMenuItem("Export Table...");
	    mntmExport.setMnemonic(KeyEvent.VK_X);		
		mntmExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			    if (null != list.getSelectedValue())
			    	export_table(list.getSelectedValue());			
			}
		});
		mnFiles.add(mntmExport);
		
		JMenuItem mntmExit = new JMenuItem("Exit");
	    mntmExit.setMnemonic(KeyEvent.VK_E);		
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		mnFiles.add(mntmExit);
		
		JMenu mnInfo = new JMenu("Info");
		menuBar.add(mnInfo);
		mnInfo.setMnemonic(KeyEvent.VK_I);
		
		JMenuItem mntmNewMenuIteinfom = new JMenuItem("About...");
		mntmNewMenuIteinfom.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JDialog about = new AboutDialog(mainwindow);
				about.setVisible(true);
			}
		});
		
		JMenuItem mntmHelp = new JMenuItem("Help");
		mntmHelp.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Create Desktop object
				 Desktop d = Desktop.getDesktop();

				 // Browse a URL, say google.com
				 try {
					d.browse(new URI("http://github.com/pawlaszczyk/MOZRT"));
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (URISyntaxException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			
			}
		});
		mnInfo.add(mntmHelp);
		mnInfo.add(mntmNewMenuIteinfom);
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JSplitPane splitPane = new JSplitPane();
		splitPane.setDividerLocation(0.4);
		contentPane.add(splitPane, BorderLayout.CENTER);
		
		JScrollPane scrollPane = new JScrollPane();
		splitPane.setLeftComponent(scrollPane);
		
		scrollpane_tables = new JScrollPane();
		splitPane.setRightComponent(scrollpane_tables);
		
		prepare_table_default();
	    
		scrollPane.setViewportView(list);
	    
		statusbar.showStatus("Ready");
		
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(500,this.getHeight()/5));
		contentPane.add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BorderLayout(5, 5));
		
		JScrollPane scrollPane_3 = new JScrollPane();
		panel.add(scrollPane_3, BorderLayout.CENTER);
		
		logwindow = new JTextArea("Welcome");
		logwindow.setBackground(SystemColor.text);
		logwindow.setEditable(false);
		scrollPane_3.setViewportView(logwindow);
		
		panel.add(statusbar,BorderLayout.SOUTH);
		
		JToolBar toolBar = new JToolBar();
		contentPane.add(toolBar, BorderLayout.NORTH);
		
		JButton btnOeffne = new JButton("");
		btnOeffne.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				open_db();
			}
		});
		
		btnOeffne.setToolTipText("open database file");
		url = MOZRT_UI.class.getResource("/document-open.png");
		btnOeffne.setIcon(new ImageIcon(url));
		
		toolBar.add(btnOeffne);
		
		JButton btnExport = new JButton("");
		btnExport.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				 export_table(list.getSelectedValue());	
			}
		});
		btnExport.setToolTipText("write table to file");
		url = MOZRT_UI.class.getResource("/filesaveas.png");
		btnExport.setIcon(new ImageIcon(url));
		toolBar.add(btnExport);
		
		
		JButton about = new JButton("");
		about.addActionListener(new ActionListener() {
	
			public void actionPerformed(ActionEvent e) {
			   JDialog about = new AboutDialog(mainwindow);
			   about.setVisible(true);
			}
		});
		about.setToolTipText("about");
		url = MOZRT_UI.class.getResource("/help-faq.png");
		about.setIcon(new ImageIcon(url));
		toolBar.add(about);
			
		
		JButton btnexit = new JButton("");
		btnexit.setToolTipText("exit");
		
		url = MOZRT_UI.class.getResource("/exit.png");		
		btnexit.setIcon(new ImageIcon(url));
		btnexit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			   System.exit(-1);
			}
		});
		toolBar.add(btnexit);
		
		JPanel searchPanel = new JPanel();
	    searchPanel.setSize(150, 20);
        searchPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
	    
	    searchTF.addKeyListener(new KeyAdapter() {
	    	@Override
	    	public void keyPressed(KeyEvent e) {
	        	  if (e.getKeyCode() == KeyEvent.VK_ENTER)
	    		  search();
	    	}
	    });
 		
		JButton searchBtn = new JButton("");
		searchBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				search();
			}});
		url = MOZRT_UI.class.getResource("/find.png");
		searchBtn.setIcon(new ImageIcon(url));
		searchBtn.setToolTipText("search for text in table");
		searchPanel.add(searchTF);	    
		toolBar.add(searchBtn);
	    toolBar.add(searchPanel);
	    	    
	  	 	
	}
	
	protected void prepare_table_default(){
		listModel = new DefaultListModel<String>();
		listModel.addElement("<empty>                       ");
		if (list == null)
			list = new JList<String>(listModel);
		else
			list.setModel(listModel);		
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				 
				if (list.getModel().getSize()==1){
				  scrollpane_tables.setViewportView(table_default);
   			      scrollpane_tables.updateUI();
				}
			}
		});
		
		
		
		DefaultTableModel default_model = new DefaultTableModel();
	    for (String colname : default_columns){
	    		default_model.addColumn(colname);
	    }
	    table_default = new JTable(default_model);
	    tables.put("default",table_default);
		scrollpane_tables.setViewportView(table_default);		
		scrollpane_tables.updateUI();
	}
	
	/**
	 *  Prepare tables and odels for the mozilla db-schema.
	 */
	protected void prepare_moz_places_tables(){
		
		// hard coded: will be dynamic in future release
		listModel = new DefaultListModel<String>();
		listModel.addElement("moz_anno_attributes");
		listModel.addElement("moz_annos");
		listModel.addElement("moz_bookmarks");
		listModel.addElement("moz_bookmarks_roots");
		listModel.addElement("moz_favicons");
		listModel.addElement("moz_inputhistory");
		listModel.addElement("moz_items_annos");
		listModel.addElement("moz_keywords");
		listModel.addElement("moz_history_visits");
		listModel.addElement("moz_sequence");
		listModel.addElement("moz_places");
		if (list == null)
			list = new JList<String>(listModel);
		else
			list.setModel(listModel);
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		list.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				 
				 int minIndex = list.getMinSelectionIndex();
			     switch (minIndex)
			     {     
   			        case 0:		    scrollpane_tables.setViewportView(table_moz_anno_attributes);
   			       					break;			     
			     	case 1:		    scrollpane_tables.setViewportView(table_moz_annos);
	               					break;
			     	case 2: 		scrollpane_tables.setViewportView(table_moz_bookmarks);
					                break;
			     	case 3: 		scrollpane_tables.setViewportView(table_moz_bookmarks_roots);
			     					break;
			     	case 4: 		scrollpane_tables.setViewportView(table_moz_favicons);
			     					break;
			     	case 5: 		scrollpane_tables.setViewportView(table_moz_inputhistory);
			     					break;	                
			     	case 6: 		scrollpane_tables.setViewportView(table_moz_items_annos);
			     					break;    
			     	case 7: 		scrollpane_tables.setViewportView(table_moz_keywords);
			     					break;    
			     	case 8: 		scrollpane_tables.setViewportView(table_moz_history_visit);
 									break;        					
			     	case 9:         scrollpane_tables.setViewportView(table_moz_sequence);
			     	                break;    		
   		         	default:		scrollpane_tables.setViewportView(table_moz_places);
			     }				
 			     scrollpane_tables.updateUI();
			
			}
		});
		
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list.setLayoutOrientation(JList.VERTICAL);
		
		/***************************************************************/
		
		DefaultTableModel places_model = new DefaultTableModel();
	    for (String colname : moz_places_columns){
	    		places_model.addColumn(colname);
	    }
	    table_moz_places = new JTable(places_model);
	    tables.put("moz_places",table_moz_places);
		scrollpane_tables.setViewportView(table_moz_places);
			
		/***************************************************************/
		
		DefaultTableModel bookmark_model = new DefaultTableModel();
		for (String colname : moz_bookmarks){
	    		bookmark_model.addColumn(colname);
	    }  		
		table_moz_bookmarks = new JTable(bookmark_model);
		tables.put("moz_bookmarks",table_moz_bookmarks);
		
		/***************************************************************/
		
		DefaultTableModel bookmark_roots_model = new DefaultTableModel();
		for (String colname : moz_bookmarks_roots){
	    		bookmark_roots_model.addColumn(colname);
	    }  		
		table_moz_bookmarks_roots = new JTable(bookmark_roots_model);
		tables.put("moz_bookmarks_roots",table_moz_bookmarks_roots);
				 
		
		/***************************************************************/
		
		DefaultTableModel inputhistory_model = new DefaultTableModel();
		for (String colname : moz_inputhistory){
	    		inputhistory_model.addColumn(colname);
	    }  		
		table_moz_inputhistory = new JTable(inputhistory_model);
		tables.put("moz_inputhistory",table_moz_inputhistory);
		
		/***************************************************************/
		
		
		DefaultTableModel keywords_model = new DefaultTableModel();
		for (String colname : moz_keywords){
	    		keywords_model.addColumn(colname);
	    }  		
		table_moz_keywords = new JTable(keywords_model);
		tables.put("moz_keywords",table_moz_keywords);
		
		/***************************************************************/
		
		
		DefaultTableModel sequence_model = new DefaultTableModel();
		for (String colname : moz_sequence){
	    		sequence_model.addColumn(colname);
	    }  		
		table_moz_sequence = new JTable(sequence_model);
		tables.put("moz_sequence",table_moz_sequence);
		
		/***************************************************************/
		
		
		DefaultTableModel favicons_model = new DefaultTableModel();
		for (String colname : moz_favicons){
	    		favicons_model.addColumn(colname);
	    }  		
		table_moz_favicons = new JTable(favicons_model);
		tables.put("moz_favicons",table_moz_favicons);
		
		/***************************************************************/
		
		
		DefaultTableModel annos_model = new DefaultTableModel();
		for (String colname : moz_annos){
	    		annos_model.addColumn(colname);
	    }  		
		table_moz_annos = new JTable(annos_model);
		tables.put("moz_annos",table_moz_annos);
	
		/***************************************************************/
		
		
		DefaultTableModel anno_attributes_model = new DefaultTableModel();
		for (String colname : moz_anno_attributes){
	    		anno_attributes_model.addColumn(colname);
	    }  		
		table_moz_anno_attributes = new JTable(anno_attributes_model);
		tables.put("moz_anno_attributes",table_moz_anno_attributes);
		
		/***************************************************************/
		
		
		DefaultTableModel items_annos_model = new DefaultTableModel();
		for (String colname : moz_items_annos){
	    		items_annos_model.addColumn(colname);
	    }  		
		table_moz_items_annos = new JTable(items_annos_model);
		tables.put("moz_item_annos",table_moz_items_annos);
	
		/***************************************************************/
		
		
		DefaultTableModel history_visit_model = new DefaultTableModel();
		for (String colname : moz_history_visit){
	    		history_visit_model.addColumn(colname);
	    }  		
		table_moz_history_visit = new JTable(history_visit_model);
		tables.put("moz_history_visits",table_moz_history_visit);	
		
		/***************************************************************/
		
		scrollpane_tables.updateUI();
	}
	
    /**
     * Implements search functionality for the tables.
     */
	private void search(){
		String value = searchTF.getText();
		JTable table = null;
		if (list.getSelectedValue() != null)
			table = tables.get(list.getSelectedValue());
		
		if (table == null)
			return;
	
		int row = table.getSelectedRow();
		int col = 0;
		
		if (row < 0) row = 0; else row++;
	
				
        search:
		for (; row <= table.getRowCount() - 1; row++) {

            for (; col  <= table.getColumnCount() - 1; col++) {
            	
            	if (null == table.getValueAt(row, col))
            		continue;
            	
            	String celvalue = (String)table.getValueAt(row, col);
                if (celvalue.contains(value)) {
                    table.scrollRectToVisible(table.getCellRect(row, 0, true));
    		        table.setRowSelectionInterval(row, row);

                    for (int i = 0; i <= table.getColumnCount() - 1; i++) {

                        table.getColumnModel().getColumn(i).setCellRenderer(new LineRenderer());
                        
                    }
    			    scrollpane_tables.updateUI();
                    break search;
                }
            }
            col = 0;
        }
	}
	
			
	/**
	 *  Show an open dialog and import <code>sqlite</code>-file.
	 *  
	 */		
   	private void open_db(){
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		chooser.setDialogTitle("open database");
	    chooser.setName("open database");
	    FileNameExtensionFilter filter = new FileNameExtensionFilter(
	            "Sqlite & DB Files (*.sqlite,*.db)", "sqlite", "db");
	    chooser.setFileFilter(filter);
	    int returnVal = chooser.showOpenDialog(this);
	    
	    if(returnVal == JFileChooser.APPROVE_OPTION){     	    	
	        File file = chooser.getSelectedFile();	    
	        ProgressBar.createAndShowGUI(this,file.getAbsolutePath());
	    }	
		
	}
	
	protected void doLog(String message)
	{
		logwindow.append("\n" + message);		
	}
	
	private void export_table(String tablename) 
	{
		if (tablename == null)
		{
			JOptionPane.showMessageDialog(this,"To use this functionality you first have to select a table from the list. ","Info",JOptionPane.INFORMATION_MESSAGE);
			return; 
		}
		JFileChooser chooser = new JFileChooser();
		chooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		chooser.setDialogTitle("export table to file");
	    chooser.setName("export table");
	    chooser.setSelectedFile(new File(tablename+".csv"));
	    FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV file ", "csv");
	    chooser.setFileFilter(filter);
	     
	    int returnVal = chooser.showSaveDialog(this);
	    if(returnVal != JFileChooser.APPROVE_OPTION) 
	    	return;
	    
		File f = new File(chooser.getSelectedFile().getAbsolutePath());
		  
		if (tables == null)
		{
		  JOptionPane.showMessageDialog(this, "Table " + tablename + " No Data.","Error",JOptionPane.ERROR_MESSAGE);	
		  return;
		}	
		
		ArrayList<String[]> tb = MOZRT.tables.get(tablename);
		
		if (tb == null)
		{
			JOptionPane.showMessageDialog(this, "Table " + tablename + " Missing Data.","Error",JOptionPane.ERROR_MESSAGE);
			return;
		}
			
		Iterator<String[]> it = tb.iterator();
		    	
	  	try {
			 
	  		   FileWriter fw = new FileWriter(f,false);
			   
			   while (it.hasNext()){			    	
			    	String line = "";
			    	String[] row = it.next();
					for (String s : row)
					{
						if (s != null)
						   line += s + ";";
						
					}
					line += ("\n");
				    fw.write(line);
										
			    }
			   fw.close();   
	
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	    
	    
		JOptionPane.showMessageDialog(this, "Table " + tablename + " exported successfully to \n" + f.getAbsolutePath());
		
	}
	
	protected void update_table(String tablename, ArrayList<String[]> data){
		
		//System.out.println("Table update : " + tablename);
		JTable tb = tables.get(tablename);
		if (tb == null){
			doLog("Unkown tablename" + tablename);
		    return;
		}
		for (String [] rowdata: data)
			((DefaultTableModel)tb.getModel()).addRow(rowdata);
	    doLog(" Filled table " + tablename);
	    doLog(" Rows " + tb.getRowCount());
	    list.setSelectedIndex(0);	   
	}
}

@SuppressWarnings("serial")
class LineRenderer extends DefaultTableCellRenderer {

@Override
public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {

    super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

    // change look a the selected row
    if(row == table.getSelectedRow()) {

        // this will customize that kind of border that will be use to highlight a row
        setBorder(BorderFactory.createMatteBorder(2, 1, 2, 1, Color.BLACK));
    }

    return this;
  }
}

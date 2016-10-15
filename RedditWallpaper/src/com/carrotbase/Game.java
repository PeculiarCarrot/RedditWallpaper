package com.carrotbase;

import java.awt.AWTException;
import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.net.URL;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.PlainDocument;

import com.carrot.redditwallpaper.Main;
import com.carrot.redditwallpaper.UserPrefs;

/**
 * THIS IS ALL A HORRIBLE MESS OF OLD CODE AND MESSING WITH THE WINDOW AND SYSTEM TRAY
 * STAY OUT OF HERE I'M ASHAMED OF THIS
 * @author Austin Harman
 *
 */
public abstract class Game extends Canvas{
	
	private static final long serialVersionUID = 1L;
	private static int WIDTH,HEIGHT;
	public JFrame frame;
	private Color backgroundColor;
	
	public boolean mouseMoving;
	
	public Graphics2D g;
	
	/*
	 * BEGIN METHODS
	 */
	
	//These MUST be in the Main class
	public abstract void initiate();
	public abstract void render(Graphics2D g);


	public Main main;
	//You can't override these!
	public static final void main(String args[]){
		Main m= new Main();
		m.main=m;
		m.init();
	}
	
	public TrayIcon trayIcon = new TrayIcon(Main.getJarImage("/icon.png"));
	
	public final void init(){
		main.initializePrefs();
		createWindow(500,400,Main.programName+" Settings");
		//frame.setVisible(false);
		
		//Initiate certain values
		backgroundColor=Color.WHITE;
		
		frame.addWindowListener(new WindowAdapter() {
			@Override
			  public void windowClosing(WindowEvent e) {
			       minimize();
			    }

		});
		frame.addWindowStateListener(new WindowAdapter() {
			@Override
		    public void windowStateChanged(WindowEvent e) {
				if(frame.getExtendedState()!=JFrame.NORMAL)
					minimize();
		    }
		});
		PopupMenu menu=new PopupMenu();
		MenuItem open=new MenuItem("Open");
		MenuItem refresh=new MenuItem("Refresh");
		MenuItem quit=new MenuItem("Quit");
		
		menu.add(open);
		menu.add(refresh);
		menu.add(quit);
		
		open.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				maximize();
			}
		});
		
		refresh.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				main.updates=0;
				main.startTime=System.currentTimeMillis();
				main.userPrefs.load();
				main.updateImages();
			}
		});

		quit.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				int selectedOption = JOptionPane.showConfirmDialog(frame, 
                        "Do you really want to close "+Main.programName+"? Like, for real?", 
                        "Quit?", 
                        JOptionPane.YES_NO_OPTION); 
				if (selectedOption == JOptionPane.YES_OPTION) {
					SystemTray.getSystemTray().remove(trayIcon);
					System.exit(1);
				}
			}
		});
		
		trayIcon.setPopupMenu(menu);
		trayIcon.setToolTip(Main.programName);
		trayIcon.addMouseListener(new MouseAdapter(){

			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getButton()==MouseEvent.BUTTON1)
					maximize();
			}
			
		});
		 try {
             SystemTray.getSystemTray().add(trayIcon);
         } catch (AWTException ex) {
         	ex.printStackTrace();
         }
		//Call the Main initiate method
		initiate();
	}
	
	private MyIntFilter filter=new MyIntFilter();
	public JTextField acceptOnlyIntegers(JTextField field)
	{
		 PlainDocument doc = (PlainDocument) field.getDocument();
	      doc.setDocumentFilter(filter);
	      return field;
	}

	public void paint(Graphics gg)
	{
		Graphics2D g=(Graphics2D)gg;
		
	}
	
	public void maximize()
	{
		  frame.setVisible(true);
		  frame.setExtendedState(JFrame.NORMAL);
	}
	
	public void minimize()
	{
        frame.setVisible(false);
	}
	
	public final void callRender(){
		{
			BufferStrategy bs = this.getBufferStrategy();
			
			if(bs==null){
				createBufferStrategy(3);
				return;
				}
				
				g = (Graphics2D)bs.getDrawGraphics();
				
				g.setColor(backgroundColor);
				g.fillRect(0, 0, WIDTH, HEIGHT);
				
				render(g);
				
				g.dispose();
				g=null;
				bs.show();
		}
	}
	
	private static String OS = null;
	
	public static String getOsName()
	{
		if(OS == null) { OS = System.getProperty("os.name"); }
			return OS;
	}
	public static boolean isWindows()
	{
	   return getOsName().startsWith("Windows");
	}
	
	public static int getWindowWidth()
	{
		return WIDTH;
	}
	
	public static int getWindowHeight()
	{
		return HEIGHT;
	}

	
	//Input components
	JTextField minWidth,minHeight,numTop,changeMins,updateHours,maxStoredImages;
	JCheckBox allowNSFW;
	JButton save,defaults;
	JTextArea subreddits;
	JScrollPane scrollSubreddits;
	
	public void setFieldsToDefaultPrefs()
	{
		minWidth.setText(""+UserPrefs.Default.minWidth);
		minHeight.setText(""+UserPrefs.Default.minHeight);
		numTop.setText(""+UserPrefs.Default.numTop);
		changeMins.setText(""+UserPrefs.Default.changeMins);
		updateHours.setText(""+UserPrefs.Default.updateHours);
		maxStoredImages.setText(""+UserPrefs.Default.maxStoredImages);
		allowNSFW.setSelected(UserPrefs.Default.allowNSFW);subreddits=new JTextArea();
		
		JTextArea subreddits=(JTextArea)scrollSubreddits.getViewport().getView();
		subreddits.setText(null);
		for(String ss:UserPrefs.Default.subreddits)
		{
			System.out.println(ss);
			subreddits.append(ss);
			if(!ss.equals(UserPrefs.Default.subreddits[UserPrefs.Default.subreddits.length-1]))
				subreddits.append("\n");
		}
		
	}
	
	public void createInputComponents()
	{
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
		JPanel textPanel=new JPanel();
		textPanel.setLayout(new BoxLayout(textPanel,BoxLayout.Y_AXIS));
		
		addIntTextBox("Min image width: ",textPanel);
		addIntTextBox("Min image height: ",textPanel);
		addIntTextBox("# top posts: ",textPanel);
		addIntTextBox("Minutes between changes: ",textPanel);
		addIntTextBox("Hours between updates: ",textPanel);
		addIntTextBox("Max # stored images: ",textPanel);
		
		allowNSFW=new JCheckBox("Allow NSFW images",main.userPrefs.allowNSFW);
		allowNSFW.setToolTipText("Whether or not NSFW images are allowed to be set as the wallpaper");
		textPanel.add(allowNSFW,BorderLayout.WEST);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new FlowLayout());
		save=new JButton("Save changes");
		save.addActionListener(new ActionListener() {
		       public void actionPerformed(ActionEvent e) {
		             if(saveChanges())
		             {
		            	 int selectedOption = JOptionPane.showConfirmDialog(frame, 
			                        "Alright, saved! Do you want to update the image list?", 
			                        "Refresh?", 
			                        JOptionPane.YES_NO_OPTION); 
							minimize();
							if (selectedOption == JOptionPane.YES_OPTION)
					             main.updateImages();
		             }
		             else
		            	JOptionPane.showMessageDialog(frame,"Save failed. That's weird.");
		       }
		 });
		
		defaults=new JButton("Revert to defaults");
		defaults.addActionListener(new ActionListener() {
		       public void actionPerformed(ActionEvent e) {
		    	   int selectedOption = JOptionPane.showConfirmDialog(frame, 
	                        "Wait, do you actually want to restore the default settings? (it won't save over your current preferences unless you click Save changes)", 
	                        "Back to default?", 
	                        JOptionPane.YES_NO_OPTION); 
					if (selectedOption == JOptionPane.YES_OPTION)
					{
						frame.setTitle("Reverting to default settings.");
						setFieldsToDefaultPrefs();
						frame.setTitle(Main.programName+" Settings");
					}
		       }
		 });
		buttonPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		save.setAlignmentX(Component.CENTER_ALIGNMENT);
		defaults.setAlignmentX(Component.CENTER_ALIGNMENT);
		mainPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		buttonPanel.add(save);
		buttonPanel.add(defaults);
		
		JPanel rightPanel=new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel,BoxLayout.Y_AXIS));
		subreddits=new JTextArea();
		String s="";
		for(String ss:main.userPrefs.subreddits)
		{
			s+=ss;
			if(!ss.equals(main.userPrefs.subreddits[main.userPrefs.subreddits.length-1]))
				s+="\n";
		}
		subreddits.setText(s);
		subreddits.setToolTipText("Which subreddits to search to find all the hottest pics");
		scrollSubreddits=new JScrollPane(subreddits);
		JLabel sbrdts=new JLabel("Subreddits to crawl");
		sbrdts.setToolTipText("Which subreddits to search to find all the hottest pics");
		sbrdts.setMinimumSize(new Dimension(1000,1000));
		scrollSubreddits.setMinimumSize(new Dimension(1000,1000));
		rightPanel.add(sbrdts);
		rightPanel.add(scrollSubreddits);

		mainPanel.add(textPanel,BorderLayout.LINE_START);
		mainPanel.add(rightPanel,BorderLayout.CENTER);
		mainPanel.add(buttonPanel,BorderLayout.PAGE_END);
		
		frame.add(mainPanel);
	}
	
	public boolean saveChanges()
	{
		try{
			main.userPrefs.allowNSFW=allowNSFW.isSelected();
			main.userPrefs.changeMins=Integer.parseInt(changeMins.getText());
			main.userPrefs.maxStoredImages=Integer.parseInt(maxStoredImages.getText());
			main.userPrefs.minHeight=Integer.parseInt(minHeight.getText());
			main.userPrefs.minWidth=Integer.parseInt(minWidth.getText());
			main.userPrefs.numTop=Integer.parseInt(numTop.getText());
			main.userPrefs.updateHours=Integer.parseInt(updateHours.getText());
			JTextArea subreddits=(JTextArea)scrollSubreddits.getViewport().getView();
			String[] lines=countLines(subreddits.getText());
			main.userPrefs.subreddits=new String[lines.length];
			for(int i=0;i<lines.length;i++)
				main.userPrefs.subreddits[i]=lines[i];
			main.userPrefs.save();
			return true;
		}catch(Exception e)
		{
			e.printStackTrace();
			return false;
		}
	}
	private static String[] countLines(String str){
		   return str.split("\r\n|\r|\n");
		}
	
	private void addIntTextBox(String label, JPanel textPanel)
	{
		String tooltip=null;
		int value;
		JTextField field=null;
		switch(label)
		{
		case "Min image width: ":
			tooltip="The minimum width an image must be in order for it to be used as a wallpaper";
			value=main.userPrefs.minWidth;
			minWidth=new JTextField(""+value,5);
			field=minWidth;
			break;
		case "Min image height: ":
			tooltip="The minimum height an image must be in order for it to be used as a wallpaper";
			value=main.userPrefs.minHeight;
			minHeight=new JTextField(""+value,5);
			field=minHeight;
			break;
		case "# top posts: ":
			tooltip="The number of top posts to look at from each subreddit";
			value=main.userPrefs.numTop;
			numTop=new JTextField(""+value,5);
			field=numTop;
			break;
		case "Minutes between changes: ":
			tooltip="The number of minutes between each wallpaper change";
			value=main.userPrefs.changeMins;
			changeMins=new JTextField(""+value,5);
			field=changeMins;
			break;
		case "Hours between updates: ":
			tooltip="The number of hours to wait until looking for new posts in the subreddits";
			value=main.userPrefs.updateHours;
			updateHours=new JTextField(""+value,5);
			field=updateHours;
			break;
		case "Max # stored images: ":
			tooltip="The maximum number of wallpapers that can be saved to disk at once";
			value=main.userPrefs.maxStoredImages;
			maxStoredImages=new JTextField(""+value,5);
			field=maxStoredImages;
			break;
		}
		field.setToolTipText(tooltip);
		JLabel lbl=new JLabel(label);
		lbl.setToolTipText(tooltip);
		textPanel.add(lbl);
		textPanel.add(acceptOnlyIntegers(field));
		
	}
	
	public final void createWindow(int w, int h, String title)
	{
		try {
			UIManager.setLookAndFeel(
			        UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
		WIDTH=w;
		HEIGHT=h;
		frame = new JFrame(title);

		frame.setSize(WIDTH, HEIGHT);
		//frame.add(this);
		frame.setMaximumSize(new Dimension(WIDTH,HEIGHT));
		frame.setPreferredSize(new Dimension(WIDTH,HEIGHT));
		frame.setMinimumSize(new Dimension(WIDTH,HEIGHT));
		createInputComponents();
		frame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		frame.setUndecorated(false);
		frame.setResizable(false);
		frame.setLocationRelativeTo(null);
		frame.setVisible(true);
		frame.pack();
		requestFocus();
		
		URL iconURL = getClass().getResource("/windowIcon.png");
		ImageIcon icon = new ImageIcon(iconURL);
		frame.setIconImage(icon.getImage());
	}
	
	public final void disposeCurrentWindow()
	{
		if(frame!=null)
			frame.dispose();
	}
	
	public final void setBackgroundColor(Color c)
	{
		backgroundColor=c;
	}
	
	public final void setFrameTitle(String title)
	{
		frame.setTitle(title);
	}
	
	public final int getWidth()
	{
		return WIDTH;
	}
	
	public final int getHeight()
	{
		return HEIGHT;
	}
}
class MyIntFilter extends DocumentFilter {
	   @Override
	   public void insertString(FilterBypass fb, int offset, String string,
	         AttributeSet attr) throws BadLocationException {

	      Document doc = fb.getDocument();
	      StringBuilder sb = new StringBuilder();
	      sb.append(doc.getText(0, doc.getLength()));
	      sb.insert(offset, string);

	      if (test(sb.toString())) {
	         super.insertString(fb, offset, string, attr);
	      } else {
	         // warn the user and don't allow the insert
	      }
	   }

	   private boolean test(String text) {
	      try {
	         Integer.parseInt(text);
	         return true;
	      } catch (NumberFormatException e) {
	         return false;
	      }
	   }

	   @Override
	   public void replace(FilterBypass fb, int offset, int length, String text,
	         AttributeSet attrs) throws BadLocationException {

	      Document doc = fb.getDocument();
	      StringBuilder sb = new StringBuilder();
	      sb.append(doc.getText(0, doc.getLength()));
	      sb.replace(offset, offset + length, text);

	      if (test(sb.toString())) {
	         super.replace(fb, offset, length, text, attrs);
	      } else {
	         // warn the user and don't allow the insert
	      }

	   }

	   @Override
	   public void remove(FilterBypass fb, int offset, int length)
	         throws BadLocationException {
	      Document doc = fb.getDocument();
	      StringBuilder sb = new StringBuilder();
	      sb.append(doc.getText(0, doc.getLength()));
	      sb.delete(offset, offset + length);

	      if (test(sb.toString())) {
	         super.remove(fb, offset, length);
	      } else {
	         // warn the user and don't allow the insert
	      }

	   }
	}
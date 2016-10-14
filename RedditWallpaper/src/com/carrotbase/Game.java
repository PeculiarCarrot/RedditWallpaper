package com.carrotbase;

import java.awt.AWTException;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferStrategy;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.carrot.redditwallpaper.Main;

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
	
	
	//You can't override these!
	public static final void main(String args[]){
		Main main = new Main();
		main.init();
	}
	
	public TrayIcon trayIcon = new TrayIcon(Main.getJarImage("/icon.png"));
	
	public final void init(){
		//Obvious.
		requestFocus();
		
		//Initiate certain values
		backgroundColor=Color.WHITE;
		
		//Call the Main initiate method
		initiate();
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
				Main.main.updates=0;
				Main.main.startTime=System.currentTimeMillis();
				Main.main.updateImages();
				trayIcon.displayMessage(Main.programName, "Successfully refreshed images.", MessageType.INFO);
			}
		});

		quit.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				int selectedOption = JOptionPane.showConfirmDialog(null, 
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
	
	public final void createWindow(int w, int h, String title)
	{
		
		WIDTH=w;
		HEIGHT=h;
		frame = new JFrame(title);
		frame.add(this);
		
		frame.setSize(WIDTH, HEIGHT);
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

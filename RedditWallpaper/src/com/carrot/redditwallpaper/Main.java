package com.carrot.redditwallpaper;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import com.carrotbase.Game;

import ga.dryco.redditjerk.api.Reddit;
import ga.dryco.redditjerk.implementation.RedditApi;
import ga.dryco.redditjerk.wrappers.Link;

public class Main extends Game{
	private static final long serialVersionUID = 1L;

	public static Main main;
	
	/**
	 * Where we're going to save all of our data
	 */
	public final String savePath=System.getenv("APPDATA")+"\\RedditWallpaper\\";
	public final String imageSavePath=savePath+"images\\";
	public final String preferencesSavePath=savePath+"preferences.json";
	
	/**
	 * All the customizable user preferences
	 */
	public UserPrefs userPrefs;
	
	/**
	 * The exact time the program started
	 */
	public long startTime;
	/**
	 * How many times we've updated the queue (used for timekeeping)
	 */
	public int updates=0;
	/**
	 * You know, the Reddit instance.
	 */
	/**
	 * The images in queue for us to use
	 */
	ArrayList<QueuedImage> queuedImages=new ArrayList<QueuedImage>();
	/**
	 * Whether or not everything has been initialized
	 */
	boolean initialized;
	
	public static String programName="RedPaper";
	
	public void initiate() {
		Main.main=this;
		createWindow(800,600,programName+" Settings");
		//Initialize everything
		startTime=System.currentTimeMillis();
		{
			//Create the data directories if they don't exist
			File folder=new File(imageSavePath);
			userPrefs=new UserPrefs(this);
			if(!folder.exists())
				folder.mkdirs();
			
			//Load preferences if we can find them
			if(userPrefs.preferencesExist())
				userPrefs.load();
		}
		//userPrefs.save();
		//updateImages();
		//iterateThroughImages();
		
	}
	
	/**
	 * This loops through the queued images over a specified interval
	 */
	public void iterateThroughImages()
	{
		for(int i=0;i<queuedImages.size();i++)
		 {
			//Load the image. If it loads correctly and fits the size requirements in userprefs, we change the wallpaper
			 String path=saveImage(queuedImages.get(i).link,queuedImages.get(i).redditID);
			 if(i==queuedImages.size()-1)
				 i=-1;
			 if(path!=null)
				 WallpaperChanger.change(path);
			 else
				 continue;
			 
			 //Sleep for however many minutes the user wants
			 try {
				Thread.sleep(1000*60*userPrefs.changeMins);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			 //If it's time to update the queue from Reddit, do it
			 if(System.currentTimeMillis()>startTime+1000*60*60*userPrefs.updateHours*updates)
			 {
				 updateImages();
				 break;
			 }
			 //While the number of files in the path is greater than the number set in userprefs, delete the oldest one.
			 File dir=new File(imageSavePath);
			 while(dir.listFiles().length>userPrefs.maxStoredImages)
			 {
				 long oldest=0;
				 File old=null;
				 for(File file: dir.listFiles()) 
				 {
					 if(old==null||file.lastModified()<oldest)
					 {
						 oldest=file.lastModified();
						 old=file;
					 }
				 }
				 old.delete();
			 }
		 }
		iterateThroughImages();
	}
	
	/**
	 * Checks Reddit for the hottest pics on the given subreddits. Refreshes the image queue with those.
	 */
	public void updateImages()
	{
		updates++;
		queuedImages.clear();
		
		//Get the top images from the given subreddits. We determine if the given post is an image by looking for ".jpg" or ".png" in the link URL.
		//Won't use NSFW images unless the user preferences are changed.
		for(String s:userPrefs.subreddits)
		 {
			 for(Link sr:red.getSubreddit(s).getHot(userPrefs.numTop))
			 {
				 String o=sr.getUrl();
				 if((!sr.getOver18()||userPrefs.allowNSFW)&&sr.getUrl()!=null)
					 if(o.toLowerCase().contains(".jpg")||o.toLowerCase().contains(".png"))
					 {
						 queuedImages.add(new QueuedImage(o, sr.getId()));
					 }
			 }
		 }
	}
	
	/**
	 * Saves the given image to disk
	 * @param imageUrl
	 * @param id
	 * @return The file path of the saved image
	 */
	public String saveImage(String imageUrl,String id) {
		String filename=imageSavePath+"\\img_"+id+".jpg";
		if(new File(filename).exists())
		{
			BufferedImage img=getImage(filename);
			if(img.getWidth()<userPrefs.minWidth||img.getHeight()<userPrefs.minHeight)
				return null;
			return filename;
		}
		try{
		URL url = new URL(imageUrl);
		InputStream is = url.openStream();
		OutputStream os = new FileOutputStream(filename);
		byte[] b = new byte[2048];
		int length;
		while ((length = is.read(b)) != -1) {
			os.write(b, 0, length);
		}

		BufferedImage img=getImage(filename);
		is.close();
		os.close();
		if(img.getWidth()<userPrefs.minWidth||img.getHeight()<userPrefs.minHeight)
			return null;
		return filename;
		}catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Loads the image from the file
	 * @param filename
	 */
	public BufferedImage getImage(String filename) {
		try {
		    return ImageIO.read(new File(filename));
		} catch (Exception e) {
		}
		    return null;
		}
	
	public static BufferedImage getJarImage(String name)
	{
		try {
			return ImageIO.read(Main.class.getResourceAsStream(name));
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void render(Graphics2D g) {
		g.setColor(Color.RED);
		g.fillRect(0,0,getWidth(),getHeight());
	}

}

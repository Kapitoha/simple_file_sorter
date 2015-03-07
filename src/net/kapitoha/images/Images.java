package net.kapitoha.images;

import java.awt.Image;
import java.awt.Toolkit;

/**
 *
 *@author Kapitoha
 *
 */
public class Images {
    public static final Image BACKGROUND = loadImage("background.jpg");
    public static final Image LOGO = loadImage("logo.png");
    
    private static Image loadImage(String imageName)
    {
	Image image = null;
	try {
	    image = Toolkit.getDefaultToolkit().createImage(Images.class.getResource(imageName));
	}
	catch(Exception e)
	{
	    System.err.println("Warning! Cannot load image: " + imageName);
	}
	return image;
    }
}

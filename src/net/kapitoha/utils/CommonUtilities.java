package net.kapitoha.utils;

import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CommonUtilities {
    /**
     * Return location of this jar or null
     * @return
     */
    public static Path getJarsLocation()
    {
	try
	{
	    URL url = CommonUtilities.class.getProtectionDomain().getCodeSource().getLocation();
	    String path = null;
	    if (System.getProperty("os.name").toLowerCase().contains("windows"))
	    {
	    	path = URLDecoder.decode(url.toString().replaceFirst("file:/", ""), "utf-8");
	    }
	    else
	    	path = URLDecoder.decode(url.toString().replaceFirst("file:", ""), "utf-8");
	    return Paths.get(path).getParent();
	}
	catch (UnsupportedEncodingException e)
	{
	    System.err.println("wrong path");
	    e.printStackTrace();
	}
	return null;
    }
    
    public static Path getJarsPath()
    {
	return Paths.get(getJarsLocation().toString());
    }

    public static boolean checkPathIfExist(String path)
    {
        if (path == null || path.isEmpty())
            return false;
        return Files.exists(Paths.get(path));
    }

    public static String getDefaultDestinationPath(String sourcePath, String filterName)
    {
        return Paths.get(sourcePath, "SORTED ".concat(filterName)).toString();
    }

}

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
	    return Paths.get(url.toURI());
	}
	catch (Exception e)
	{
	    System.err.println("wrong path");
	    e.printStackTrace();
	}
	return null;
    }
    
    public static Path getJarsParentFolder()
    {
	return Paths.get(getJarsLocation().getParent().toString());
    }

    public static boolean checkPathIfExist(String path)
    {
        if (path == null || path.isEmpty())
            return false;
        return Files.exists(Paths.get(path));
    }

    public static String getDefaultDestinationPath(String sourcePath, String filterName)
    {
        return Paths.get(decodeUrl(sourcePath), "SORTED ".concat(filterName)).toString();
    }
    
    /**
     * It's a miracle converting of "abra-kadabra" into real path.
     * @param url
     * @return
     */
    public static String decodeUrl(String url)
    {
	String u = null;
	try
	{
	    u = URLDecoder.decode(url, "UTF-8");
	}
	catch (UnsupportedEncodingException e)
	{
	    System.err.println("Cannot decode url, so return default");
	    return url;
	}
	return u;
    }

}

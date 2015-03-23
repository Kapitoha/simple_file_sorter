package net.kapitoha.utils.connections;

/**
 *
 *@author Kapitoha
 *
 */
public class ConnectionProperties {
    private String driver;
    private String user;
    private char[] password;
    private String url;
    
    public ConnectionProperties(String driver, String user, char[] password,
	    String url)
    {
	this.driver = driver;
	this.user = user;
	this.password = password;
	this.url = url;
    }

    public String getDriver()
    {
        return driver;
    }

    public String getUser()
    {
        return user;
    }

    public char[] getPassword()
    {
        return password;
    }

    public String getUrl()
    {
        return url;
    }


}

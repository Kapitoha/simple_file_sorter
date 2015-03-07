package net.kapitoha.engine;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;
import java.nio.channels.ClosedByInterruptException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.kapitoha.instances.ExpansionFilter;
import net.kapitoha.instances.FolderFilter;
import net.kapitoha.instances.FolderInstance;
import net.kapitoha.utils.CommonUtilities;
import net.kapitoha.utils.connections.DatabaseManager;

public class FileSorter {
    private String sourceFolder = CommonUtilities.getJarsLocation().toString();
    private String destinationFolder = CommonUtilities.getJarsLocation().toString();
    private final boolean moveHidden;
    private FolderInstance folder;
    private static Pattern expansion = Pattern.compile("\\.[A-Za-z0-9]+$");
    private static List<ExpansionFilter> filterList = DatabaseManager.getObjects(ExpansionFilter.class);
    
    
    public FileSorter(FolderInstance folder, boolean moveHiden)
    {
	this.sourceFolder = folder.getPath();
	this.moveHidden = moveHiden;
	this.folder = folder;
    }

    /**
     * Iterate required path to find sortable files.
     * @param path
     */
    public void scan()
    {
	File path = new File(sourceFolder);
	if (path.isDirectory())
	{
	    File[] allFiles = path.listFiles();
	    for (File file : allFiles)
	    {
		sort(file);
	    }
	}
	System.out.println("ready");
    }
    
    /**
     * Return true if expansion string contains a file expansion
     * @param filePath 
     * @param expansions
     * @return
     */
    private static boolean compareFileExpansions(String filePath, String expansions)
    {
	String fileExpansion = null;
	Matcher mat = expansion.matcher(filePath.toString());
	if (mat.find() && expansions != null && !expansions.isEmpty())
	{
	    fileExpansion = mat.group().intern();
	    StringBuilder sb = new StringBuilder("\\.(");
	    sb.append(expansions.replaceAll("\\,+", "|"));
	    sb.append(")$");
	    while (sb.charAt(sb.length() - 3) == '|')
		sb.deleteCharAt(sb.length() - 3);
	    while (sb.charAt(3) == '|')
		sb.deleteCharAt(3);
	    if (Pattern.compile(sb.toString().intern(), Pattern.CASE_INSENSITIVE).matcher(fileExpansion).find())
		return true;
	}
	return false;
    }
    
    /**
     * If path is file - sort it.
     * @param path
     */
    private void sort(File path)
    {
	if (path.isFile())
	{
	    if (!moveHidden && path.isHidden())
		return;
	    String destination = null;
	    
	    for (FolderFilter pf : folder.getAvailableFilters())
	    {
		if (!pf.isSelected()) continue;
		
		ExpansionFilter filterInstance = null;
		for (ExpansionFilter expFilter : filterList)
		{
		    if (expFilter.getId() == pf.getFilterId())
		    {
			filterInstance = expFilter;
		    }
		}
		if (filterInstance != null && compareFileExpansions(path.toString(), filterInstance.getFilter()))
		{
		    destination = pf.getDestination();
		    System.out.println(destination);
		    break;
		}
	    }

	    if (destination != null)
	    {
		try
		{
		    if (!CommonUtilities.checkPathIfExist(destination))
			Files.createDirectory(Paths.get(destination));
		    moveTo(path.toPath(), Paths.get(destination));
		    System.out.println("Move: " + path.toString() + " to: " + destination);
			
		}
		catch (IOException e)
		{
		    System.err.println("Cannot create folder or move file");
		}
		
	    }

	}
    }
    
    private static void moveTo(Path source, Path destinationFolder) throws IOException
    {
	String sourceFileName = source.getFileName().toString();	
	String fileExpansion = null;
	
	Path target = Paths.get(destinationFolder.toString(), sourceFileName);
	
	while (Files.exists(target))
	{
	    String md5source = nioMd5(source);
	    String md5target = nioMd5(target);
	    if (md5source == null | md5target == null) return;
	    if (md5source.equals(md5target))
	    {
		if (!Files.isSymbolicLink(target))
		{
		    Files.delete(source);
		    System.out.println(source + " is already in: " + target + ". So, duplicate will be removed.");
		    return;
		}
	    }
	    //*****************
	    String fileName = target.getFileName().toString();
	    Matcher mat = expansion.matcher(fileName);
	    if (mat.find())
	    {
		fileExpansion = mat.group();
	    }
	    fileName = fileName.replaceAll(fileExpansion, "");
	    String[] nameParts = fileName.split("-");
	    
	    if (nameParts.length == 1)
	    {
		fileName += "-".concat("1");
	    }
	    else
	    {
		int index = 1;
		String countIndex = nameParts[nameParts.length - 1].trim();
		while (countIndex.startsWith("0"))
		    countIndex = countIndex.substring(1);
		try
		{
		    index = Integer.parseInt(countIndex);
		}
		catch(NumberFormatException ne)
		{
		    System.err.println("Cannot parse string " + ne.getLocalizedMessage());
		}
		nameParts[nameParts.length - 1] = String.valueOf(++index);
		fileName = "";
		for (int i = 0; i < nameParts.length; i++)
		{
		    fileName += nameParts[i];
		    if (i < nameParts.length - 1) fileName = fileName.concat("-");
		}
	    }
	    if (null != fileExpansion)
		fileName = fileName.concat(fileExpansion);
	    target = Paths.get(destinationFolder.toString(), fileName);
	    //*****************
	}
	try
	{
	    Files.move(source, target);
	}
	catch (IOException e)
	{
	    System.err.println("Cannot move " + source);
	}
    }
    
    /**
     * Method returns md5 sum in String
     * @param path
     * @return
     */
    public static String nioMd5(Path path)
    {
	MessageDigest md = null;
	ByteBuffer bb = ByteBuffer.allocate(4096);
	StringBuilder sb = new StringBuilder();
	try (ByteChannel ch = Files.newByteChannel(path,
		StandardOpenOption.READ))
	{
	    md = MessageDigest.getInstance("md5");
	    while ((ch.read(bb)) != -1)
	    {
		bb.flip();
		md.update(bb);
		bb.rewind();
	    }
	    byte[] digest = md.digest();
	    for (int i = 0; i < digest.length; i++)
	    {
		sb.append(Integer.toHexString(0x0100 + (digest[i] & 0x00FF))
			.substring(1));
	    }
	}
	catch (ClosedByInterruptException cl)
	{
	    return null;
	}
	catch (IOException | NoSuchAlgorithmException e)
	{
	    System.out
		    .println("Cannot read md5 from: " + path.toAbsolutePath());
	    return null;
	}
	return sb.toString();
    }

    public String getSourceFolder()
    {
	return sourceFolder;
    }

    public void setSourceFolder(String sourceFolder)
    {
	this.sourceFolder = sourceFolder;
    }

    public String getDestinationFolder()
    {
	return destinationFolder;
    }

    public void setDestinationFolder(String destinationFolder)
    {
	this.destinationFolder = destinationFolder;
    }


}

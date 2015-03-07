package net.kapitoha.loaders;

import java.util.Map;
import java.util.TreeMap;

/**
 *
 *@author Kapitoha
 *
 */
public class FileExpansionFilterLoader {
    public static Map<String, String> DEFAULT_FILE_EXPANSION_FILTERS_MAP = getExpansionMap();
    
    private static Map<String, String> getExpansionMap()
    {
	Map<String, String> map = new TreeMap<>();
	map.put("Audio", "ogg,mp3,wav,wave,flac,ape,wv,wma,aac,aa,aiff,dts,mid,midi,mod,tta,vqf".toLowerCase());
	map.put("Archives", "rar,zip,7z,tar,gz,bz2,xz,ar,tar.gz,tar.bz,tar.bz2".toLowerCase());
	map.put("Images", "bmp,pcx,tiff,psd,fli,flc,mng,fit,fits,xwd,ps,dcm,dicom,pix,matte,gif,pbm,pgm,png,pnm,ppm,sgi,rgb,rgba,bw,icon,ico,tga,tif,xbm,xpm,pcx,pcc,xmc,xcf,gbr,gih,jpg,jpeg,jpe,raw".toLowerCase());
	map.put("Video", "mpg,webm,mp4,mts,avi,mov,mkv,3gp,flv,swf,rm,ra,ram,vob,ifo,aaf,asf,bik,cpk,mpeg,mxf,nut,nsv,ogm,qt".toLowerCase());
	map.put("Documents", "doc,docx,odt,ODS,pdf,txt,XLSX,XLSM,XLSB,XLTX,XLS,XLT,XML,CSV".toLowerCase());
	map.put("Windows Programs", "exe,msi".toLowerCase());
	map.put("Linux Packages", "deb,rpm,apk".toLowerCase());
	map.put("Virtual Immages", "iso,img,dmg,nrg,mds,mdf,daa,pqi,vdf,ccd,sub,uif");
	return map;
    }

}

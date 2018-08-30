package facets.util;
import static facets.util.Doubles.*;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.channels.FileChannel;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
/**
General-purpose utility methods. 
trace(Debug)?\(("~?)[^."]*\.([^"]*")	
trace$1($2$.3
(trace(Debug)?\([^;+]+)\+
$1,
(printOut\("[^+"]+")\+
 */
public final class Util{
	public final static String programs32="C:/Program Files (x86)",
			programs64="C:/Program Files";
	private static final ArrayList<Process>processes=new ArrayList();
	public static final int MB=1<<20;
	private static final Logger log=Logger.getGlobal();
	static{
		ConsoleHandler handler=new ConsoleHandler(){
			@Override
			public void publish(LogRecord record){
				System.out.println(record.getMessage());
			}
		};
		handler.setLevel(Level.ALL);
		log.setLevel(Level.ALL);
		log.addHandler(handler);
	}
	public static void printOut(String s){
	  String text=s==null?"null":s.toString();
		if(true)System.out.println(text);
		else log.fine(text);
	}
	public static void printOut(String msg,Object o){
	  printOut(msg+o);
	}
	public static void printOut(String msg,Collection c){
		printOut(msg+arrayPrintString(c.toArray()));		
	}
	public static void printOut(String msg,Object[]a){
		printOut(msg+arrayPrintString(a));		
	}
	public static String arrayPrintString(Object[]toPrint){
		String msg=toPrint==null?"null"
	  :false&&Objects.getMemberType(toPrint)==String.class?
			Objects.toString(toPrint)
		:Objects.toStringWithHeader(toPrint);
		return msg;
	}
	public static void windowsOpenFile(File file){
		if(!file.exists()||!file.canRead())
			throw new IllegalArgumentException("Bad file="+file);
		else try{
			String winPath=" \""+file.getAbsolutePath().replaceAll("/","\\\\")+"\"",
				command=(file.isDirectory()?"explorer.exe":"cmd /c")+winPath;
			Util.printOut("Util.windowsOpenFile: command=\n",command);
			addProcess(command);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}
	public static double sf(double val){
		return sigFigs(val,DIGITS_SF);
	}
	public static String sfs(double val){
		String sf=String.valueOf(sf(val)),
			sfs=sf.replaceAll("(\\d{"+DIGITS_SF+",})\\.0(\\D?)","$1$2").replaceAll("\\.0\\z","");
		return false?("["+sf+">"+sfs+"]"):sfs;
	}
	public static double fx(double val){
		return fixed(val,DECIMALS_FX);
	}
	public static String fxs(double val){
		return new DecimalFormat("0."+(DECIMALS_FX==1?"0":DECIMALS_FX==2?"00":"000")).format(val);
	}
	public static void copyFile(File src,File dest)throws IOException{
		copyFile(src,dest,false);
	}
	public static void copyFile(File src,File dest,boolean trace)throws IOException{
		if(trace)Util.printOut("Util.copyFile: ",dest.getAbsolutePath());
		if(src.equals(dest))throw new IllegalArgumentException(
				"Src=dest="+src);
		else dest.createNewFile();
	  FileChannel srcIn=new FileInputStream(src).getChannel(),
			srcOut=new FileOutputStream(dest).getChannel();
		srcIn.transferTo(0,srcIn.size(),srcOut);
		srcIn.close();srcOut.close();
	}
	public static void moveFile(File then,File toDir)throws IOException{
		if(!toDir.isDirectory())throw new IllegalArgumentException(
				"Bad toDir "+toDir);
		File now=new File(toDir,then.getName());
		if(now.exists())Util.printOut("Util.moveFile: existing now=",now);
		else copyFile(then,now);
		if(!(now.exists()&&now.canRead()&&now.canWrite()))throw new IllegalStateException(
					"Bad now="+now);
		else{
			then.delete();
			Util.printOut("Util.moveFile: deleted then=",then);
		}
	}
	public static void windowsOpenUrl(String url){
		try{
			String command="rundll32 url.dll,FileProtocolHandler "+url;
			Util.printOut("Util.windowsOpenUrl: command=\n",command);
			addProcess(command);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}
	public static void addProcess(String command) throws IOException{
		processes.add(Runtime.getRuntime().exec(command));
	}
	public static void stopProcesses(){
		if(processes.isEmpty())return;
		Util.printOut("Util.stopProcesses: ",processes.size());
		for(Process p:processes)p.destroy();
	}
	public static String mbs(long bytes){
		return sfs(bytes/1024d/1024d)+(false?"":"M");
	}
	public static String kbs(long bytes){
		return false?String.valueOf(bytes):(sfs(bytes/1024d)+"K");
	}
	/**
	Determines size by counting serialized bytes. 
	 */
	public static int byteCount(Serializable src){
		try{
			return Bytes.serialize(src).length;
		}catch(Exception e){
			e.printStackTrace();
			return -1;
		}
	}
	/**
	 Make a deep copy by serialization. 
	 @param src to be copied
	 */
	public static Serializable deserializedCopy(Serializable src){
		try{
			byte[]bytes=Bytes.serialize(src);
			Serializable copy=Bytes.deserialize(bytes);
			if(false)Util.printOut("Util.deserializedCopy: ",
					Debug.info(src)+"->"+Debug.info(copy)+" bytes="+bytes.length);
			return copy;
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	public static String textInfo(String text,boolean full){
		int length=text.length();
		return (true?length:kbs(length))+(!full?"":(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n"+text));
	}
	public static File runDir(){
		return new File(".").getAbsoluteFile().getParentFile();
	}
	public static File getDir(File parent,String name){
		File file=new File(parent,name);
		if(file.isDirectory()&&file.canRead()&&file.canWrite())return file;
		file.mkdir();
		if(!file.isDirectory())throw new IllegalStateException(
				"Existing file "+file);
		else if(!file.canRead()||!file.canWrite())throw new IllegalStateException(
				"No read/write in "+file);
		else return file.getAbsoluteFile();
	}
	private static URI pathUri(File dir,final String filename)throws URISyntaxException{
		return new URI("file:///"+dir.getAbsolutePath().replace('\\','/')+'/'+filename);
	}
	public static void clearDirByExtension(File dir,final String ext,final boolean invert){
		if(dir==null)throw new IllegalStateException("Null dir");
		else if(ext==null||ext.trim().equals(""))throw new IllegalStateException(
				"Null or empty ext");
		File[]clearing=dir.listFiles(new FileFilter(){
			@Override
			public boolean accept(File file){
				if(file.isDirectory())return false;
				String name=file.getName();
				return invert?!name.endsWith(ext):name.endsWith(ext);
			}
		});
		if(clearing.length==0)return;
		printOut("Util.clearDirByExtension: clearing=",clearing.length);
		new IndexingIterator<File>(clearing){
			@Override
			protected void itemIterated(File clear,int at){
				clear.delete();
				if(at>0&&at%100==0)printOut("Util.clearDirByExtension: at="+at);
			}
		}.iterate();
	}
	/**
	Encapsulates making a back-up copy of a file.
	 */
	public static class FileBackup{
		private final File src;
		public FileBackup(File src){
			this.src=src;
			if(src==null||!src.exists())throw new IllegalArgumentException(
					"Null or non-existing src="+src);
		}
		final public File doBackup()throws IOException{
			File bak=newBackupFile(src);
			if(!bak.exists()||overwriteExisting())copyFile(src,bak);
			return bak;
		}
		protected boolean overwriteExisting(){
			return false;
		}
		protected File newBackupFile(File src){
			return new File(src.getParent(),"_"+src.getName());
		}
	}
	public static String helpfulClassName(Object o){
		Class trueClass=o instanceof Class?(Class)o:o.getClass(),
				ancestor=trueClass;
		while(ancestor!=null&&endsWithDigit(ancestor.getName()))
			ancestor=ancestor.getSuperclass();
		String array=o instanceof Object[]?"[":"";
		return array+shortName(trueClass.getName())
				+(ancestor==trueClass?"":"/"+shortName(ancestor.getName()));
	}
	private static boolean endsWithDigit(String string){
		return Character.isDigit(string.charAt(string.length()-1));
	}
	private static String shortName(String className){
		int semiColon=className.lastIndexOf(';'),
				dollar=className.lastIndexOf('$'),
				start=true?className.lastIndexOf("."):dollar>0?dollar+1:0,
				stop=semiColon>0?semiColon:className.length();
		return false?className:className.substring(
				Math.max(className.lastIndexOf('.')+1,start),
				stop);
	}
	public static String shortTypeNameKey(Object o){
	  String[]fixes=new String[]{
	  		"\\..*","",
	  		"[\\$\\[\\]]","_",
	  		"[;]",""
	  	};
		String name=o instanceof Class?((Class)o).getName():
	    	o.getClass().getName(),
	    key=Strings.reverse(Regex.replaceAll(Strings.reverse(name),false,fixes));
	  if(false)Util.printOut("Util.shortTypeNameKey: ", name+"->"+key);
	  return key;
	}
  public static String keySafe(String text){
		return text.trim().replaceAll("\\W","_");
	}
	private static int isgn_(int val){
		return val<0?-1:1;
	}
	private static <K,V>Map<K,V>newFilledMap_(Object[][]pairs){
		Map<K,V>fill=new HashMap();
		for(Object[]p:pairs)fill.put((K)p[0],(V)p[1]);
		return fill;
	}
}

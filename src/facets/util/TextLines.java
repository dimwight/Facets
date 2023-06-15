package facets.util;
import facets.util.app.AppValues;
import facets.util.app.AppWatcher;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipError;
/**
Reads from and where allowed writes lines to a wrapped text file reference. 
<p>Can be constructed from a {@link File}, {@link URL} or with an internal string buffer. 
 */
final public class TextLines extends TextLinesCore{
	public static boolean trace=false;
	public static final String EXT_ZIP=".zip",EXT_SZIP="s.zip";
	private static final DateFormat DATES_DAY=DateFormat.getDateInstance(DateFormat.SHORT);
	private static final long MASK=1415926535893l,LIB=827406463829L,
			STAMP=false?0^MASK:LIB,loaded=System.currentTimeMillis();
	//6535233365 Mon Sep 01 00:00:00 BST 2014 1409526000000 1/9/14
	//13036349141 Thu Jan 01 00:00:00 GMT 2015 1420070400000 1/1/15
	//34049268565 Wed Jul 01 00:00:00 BST 2015 1435705200000 1/7/15
	//104450198229 Fri Jan 01 00:00:00 GMT 2016 1451606400000 1/1/16
	//120397890389 Fri Jul 01 00:00:00 BST 2016 1467327600000 1/7/16
	//72957768405 Sun Jan 01 00:00:00 GMT 2017 1483228800000 1/1/17
	//91552733013 Sat Jul 01 00:00:00 BST 2017 1498863600000 1/7/17
	//176173926101 Mon Jan 01 00:00:00 GMT 2018 1514764800000 1/1/18
	//197453216597 Sun Jul 01 00:00:00 BST 2018 1530399600000 1/7/18
	//144644369109 Tue Jan 01 00:00:00 GMT 2019 1546300800000 1/1/19
	//146051010389 Mon Jul 01 00:00:00 BST 2019 1561935600000 1/7/19
	//167330365141 Wed Jan 01 00:00:00 GMT 2020 1577836800000 1/1/20
	//252005922645 Wed Jul 01 00:00:00 BST 2020 1593558000000 1/7/20
	//270867244757 Fri Jan 01 00:00:00 GMT 2021 1609459200000 1/1/21
	//223152364373 Thu Jul 01 00:00:00 BST 2021 1625094000000 1/7/21
	//239329331925 Sat Jan 01 00:00:00 GMT 2022 1640995200000 1/1/22
	//859481829205 Fri Jul 01 00:00:00 BST 2022 1656630000000 1/7/22
	//879418449621 Sun Jan 01 00:00:00 GMT 2023 1672531200000 1/1/23
	//827406463829 Sat Jul 01 00:00:00 BST 2023 1688166000000 1/7/23

private TextLines(Object reference){
		super(reference);
		this.fileName=getFileName();
		zip=fileName.endsWith(TextLines.EXT_ZIP);
		szip=fileName.endsWith(TextLines.EXT_SZIP);
		if(true)return;
		try{
			String live=AppWatcher.LIVE;
			long clear=DATES_DAY.parse(live).getTime();
			Util.printOut((clear^MASK)+" "+new Date(clear)+" "+clear+" "+live);
			System.exit(0);
		}catch(ParseException e){
			throw new RuntimeException(e);
		}
	}
	@Override
	protected void traceOutput(String msg){
		if(trace)super.traceOutput(msg);
		else Util.printOut("TextLines"+msg);
	}
	InputStream newZipIn(InputStream in)throws IOException{
		if(false&&szip)trace(".newZipIn: ",this);
		InputStream zip=new GZIPInputStream(in);
		if(szip){
			final byte[]header=new byte[8];
			long stamp=0;
			zip.read(header);
			for(int at=header.length-1;at>-1;at--){
				stamp<<=8;
				stamp|=header[at]&0x000000ff;
			}
			if(STAMP==LIB&&loaded>(stamp^MASK))throw new ZipError("Invalid data");
		}
		return zip;
	}
	OutputStream newZipOut(final File file)throws IOException{
		if(szip){
			if(AppValues.userDir().getName().equals(AppValues.DIR_DEV))
				trace(".newZipOut: ",this);
			long clear=LIB^MASK;
			if(true)trace(".newZipOut: clear=",clear+" loaded="+loaded);
			if(loaded>clear||clear%100000!=0)throw new ZipError("Bad "+fileName);
		}
		OutputStream out=new GZIPOutputStream(new FileOutputStream(file)){
			private final FileOutputStream clearOut;
			private final File zipped,clear;
			private boolean pastHeader;
			{
				zipped=file;
				clear=new File(zipped.getParentFile(),
						zipped.getName().replace(TextLines.EXT_SZIP,TextLines.EXT_ZIP
								).replaceAll("(\\.\\w+)\\Q" +TextLines.EXT_ZIP+"\\E","$1"));
				clearOut=true||szip?null:new FileOutputStream(clear);
			}
			@Override
			public void write(byte[]b,int off,int len)throws IOException{
				if(false)trace(".write: byte[],int,int len="+len);
				if(pastHeader&&clearOut!=null)clearOut.write(b,off,len);
				super.write(b,off,len);
				pastHeader=true;
			}
			@Override
			public void close()throws IOException{
				if(clearOut!=null)clearOut.close();
				super.close();
				if(false)trace(".close:" +" clear=" +Util.kbs(clear.length())+
						" zipped="+Util.kbs(zipped.length()));
			}
		};
		if(szip){
			final byte[]header=new byte[8];
			for(int at=0;at<header.length;at++)header[at]|=STAMP>>at*8;
			out.write(header);
		}
		return out;
	}
	private byte[]buffer;
	public String toString(){
		return trace?(reference+" lines="+lines+" chars K="+chars/1024):
			(getFileName()+(!szip?""
					:(" live="+(STAMP!=LIB?"false":DATES_DAY.format(new Date(LIB^MASK))))));
	}
	final boolean zip,szip;
	private final String fileName;
	public static void setDefaultEncoding(boolean systemAccess){
		if(false)Util.printOut("TextLines.setDefaultEncoding: systemAccess=",systemAccess);
		TextLines.systemAccess=systemAccess;
		setEncoding(!systemAccess?DEFAULT_ENCODING:System.getProperty("file.encoding"));
	}
	/**<pre>
[?Cp1252 Eclipse standard]
US-ASCII Seven-bit ASCII, a.k.a. ISO646-US, a.k.a. the Basic Latin block of the Unicode character set 
ISO-8859-1   ISO Latin Alphabet No. 1, a.k.a. ISO-LATIN-1 
UTF-8 Eight-bit UCS Transformation Format 
UTF-16BE Sixteen-bit UCS Transformation Format, big-endian byte order 
UTF-16LE Sixteen-bit UCS Transformation Format, little-endian byte order 
UTF-16 Sixteen-bit UCS Transformation Format, byte order identified by an optional byte-order mark 
	*/
	public static void setEncoding(String set){
		encoding=set;
		if(!systemAccess)return;
		String system=System.getProperty("file.encoding");
		if(encoding.equals(system))return;
		else System.setProperty("file.encoding",encoding);
		if(false)Util.printOut("TextLines.setEncoding: system=" +system+
				" encoding="+encoding);
	}
	/**
	Construct from a file reference. 
	 */
	public TextLines(File file){
		this((Object)file);
	}
	/**
	Construct from a URL reference. 
	 */
	public TextLines(URL url){
		this((Object)url);
	}
	/**
	Construct from an output stream. 
	 */
	public TextLines(OutputStream out){
		this((Object)out);
	}
	/**
	Construct from an input stream. 
	 */
	public TextLines(InputStream in){
		this((Object)in);
	}
	public InputStream newInputStream()throws IOException{
		chars=lines=0;
		InputStream in=reference==BUFFER?new ByteArrayInputStream(buffer)
				:reference instanceof URL?((URL)reference).openStream():
					reference instanceof InputStream?(InputStream)reference:
			new FileInputStream((File)reference);
		return zip?newZipIn(in):!trace?in
				:new FilterInputStream(in){
			public int read()throws IOException{
				int read=super.read();
				chars+=read;
				if(false)trace(".read: ",chars);
				return read;
			}
			public int read(byte[]b,int off,int len) throws IOException{
				int read=super.read(b,off,len);
				chars+=read;
				if(false)trace(".read b,off,len: ",false?chars:new String(b));
				return read;
			}
		};
	}
	public OutputStream newOutputStream()throws IOException{
		return reference==BUFFER?new ByteArrayOutputStream(){
			public void close(){
				buffer=toByteArray();
				chars=buffer.length;
				try{
					String text=new String(buffer,encoding);
					if(false)trace(".newOutputStream: text=",text);
					lines+=text.split("\n").length;
				}catch(UnsupportedEncodingException e){
					throw new RuntimeException(e);
				}
			}
		}
		:zip?newZipOut((File)reference)
		:new FileOutputStream((File)reference);
	}
	public static TextLines newBuffer(){
		return new TextLines(BUFFER);
	}
	public static TextLines newBuffer(String[]lines){
		TextLines tl=newBuffer();
		try{
			tl.writeLines(lines);
			return tl;
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	}
	public String getFileName(){
		return reference==BUFFER||reference instanceof InputStream||reference instanceof OutputStream?reference.toString()
			:reference instanceof File?((File)reference).getName()
					:((URL)reference).getFile();
	}
	public void copyFile(String outName) throws IOException{
		File src=(File)reference,dst=new File(src.getParentFile()+"/"+outName);
		FileChannel in=new FileInputStream(src).getChannel(),
			out=new FileOutputStream(dst).getChannel();
		out.transferFrom(in,0,in.size());
		in.close();
		out.close();
	}
	public static void createIfRequired(File file,String content) throws IOException{
		if(!file.exists())new TextLines(file).writeLines(content.split("\n"));
	}
	public static String newXmlTop(){
		return "<?xml version=\"1.0\" encoding=\"" +encoding+
		"\" standalone=\"no\"?>";
	}
}

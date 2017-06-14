package facets.util;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
abstract class TextLinesCore extends Tracer{
	public static final String DEFAULT_ENCODING="UTF-8";
	static String encoding="No encoding set";
	static final Object BUFFER="buffer";
	static boolean systemAccess=false;
	public final Object reference;
	long lines,chars;
	private BufferedReader reader;
	private PrintWriter writer;
	private String nextLine;
	private boolean nextLineRead;
	TextLinesCore(Object reference){
		if(!(reference instanceof URL||!systemAccess)){
			String systemEncoding=System.getProperty("file.encoding");
			if(!encoding.equals(systemEncoding))throw new IllegalStateException(
					"System encoding="+systemEncoding+" not set to "+encoding);
		}
		if((this.reference=reference)==null)throw new IllegalArgumentException(
				"Null reference in "+Debug.info(this));
	}
	public static void setDefaultEncoding(boolean systemAccess){
		if(false)Util.printOut("TextLines.setDefaultEncoding: systemAccess=",systemAccess);
		TextLinesCore.systemAccess=systemAccess;
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
	public static void setEncoding(String encoding){
		TextLinesCore.encoding=encoding;
		if(!systemAccess)return;
		String system=System.getProperty("file.encoding");
		if(encoding.equals(system))return;
		else System.setProperty("file.encoding",encoding);
		if(false)Util.printOut("TextLines.setEncoding: system=" +system+
				" encoding="+encoding);
	}
	final public String[]readLines()throws IOException{
		return readLinesString().split("\n");
	}
	/**
	Attempts to read text lines from the reference or buffer wrapped. 
	@throws IOException
	 */
	final public String readLinesString()throws IOException{
		StringBuilder lines=new StringBuilder();
	  while(readNextLine())lines.append(nextLine()+(false?"":"\n"));
		return lines.toString().replaceAll("\n\\z","");
	}
	private boolean readNextLine()throws IOException{
	  if(reader==null)reader=new BufferedReader(
	  		new InputStreamReader(newInputStream(),encoding));
	  nextLine=reader.readLine();
	  nextLineRead=true;
	  if(nextLine==null){
			reader.close();
			reader=null;
		}
		return nextLine!=null;
	}
	public abstract InputStream newInputStream()throws IOException;
	/**
	Attempts to write text lines to the reference wrapped. 
	@throws IOException
	 */
	final public void writeLines(String...lines)throws IOException{
		if(lines==null)throw new IllegalArgumentException("Null lines in "+Debug.info(this));
		else if(lines.length==0)lines=new String[]{""};
		for(int i=0;i<lines.length;i++)writeNextLine(lines[i]);
		closeLineWriter();
	}
	final public void writeNextLine(String line)throws IOException{
		if(line==null)throw new IllegalArgumentException("Null line in "+Debug.info(this));
		else if(writer==null)writer=new PrintWriter(new OutputStreamWriter(
				reference instanceof OutputStream?(OutputStream)reference
						:newOutputStream(),encoding)) ;
		if(false)trace("writeNextLine: "+Regex.replaceAll(line,"\n","\\\\n","\r","\\\\r"));
		writer.println(line.replaceAll("\\r",""));
		lines++;
		chars+=line.length();
	}
	abstract OutputStream newOutputStream()throws IOException;
	final public void closeLineWriter(){
		if(writer==null)throw new IllegalStateException("Null writer in "+Debug.info(this));
		writer.close();
		writer=null;
	}
	private String nextLine()throws IOException{
		if(!nextLineRead)readNextLine();
	  nextLineRead=false;
		if(nextLine==null)throw new IllegalStateException("Null next line in "+Debug.info(this));
		lines++;
		chars+=nextLine.length();
		return nextLine;
	}
	public String toString(){
		return reference+" lines="+lines+" chars K="+chars/1024;
	}
}

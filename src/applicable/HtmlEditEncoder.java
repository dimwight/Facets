package applicable;
import static facets.util.Debug.*;
import static facets.util.HtmlBuilder.*;
import static facets.util.Util.*;
import static facets.util.tree.DataConstants.*;
import facets.util.Debug;
import facets.util.HtmlBuilder;
import facets.util.Objects;
import facets.util.Regex;
import facets.util.Strings;
import facets.util.TextLines;
import facets.util.Times;
import facets.util.Tracer;
import facets.util.Util;
import facets.util.app.AppValues;
import facets.util.tree.DataConstants;
import facets.util.tree.Nodes;
import facets.util.tree.ValueNode;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
public abstract class HtmlEditEncoder extends Tracer{
	public static String KEY_WEAK="weakEditor",KEY_STRONG="strongEditor",
		weakEditor=false?"Macromedia/Dreamweaver 4/Dreamweaver.exe"
			:"SeaMonkey/seamonkey.exe",
		strongEditor="Microsoft Office/Office"+(false?"12":"14")+"/WINWORD.EXE";
	private final File file;
	private final boolean strong;
	private FileModifiedChecker fileChecks;
	private String contentTail;
	public HtmlEditEncoder(File file,boolean strong){
		super(HtmlEditEncoder.class);
		this.file=file;
		if(!isHtmlFile(file))throw new IllegalStateException(
				"Non-HTML file="+file);
		this.strong=strong;
	}
	@Override
	protected void traceOutput(String msg){
		if(false)super.traceOutput(msg);
		else if(false)Times.printElapsed("HtmlEditEncoder"+msg);
	}
	final public void openEdits(){
		try{
			if(true||!file.exists()){
				HtmlBuilder builder=newPageBuilder();
				String top=builder.newPageTop(),tail=builder.newPageTail(),
					content=builder.newPageContent();
				if(editTop()){
					String[]splits=splitAfterRules(content,2);
					content=splits[0];
					contentTail=splits.length<2?"":splits[1].trim();
				}
				else contentTail="";
				String page=top+content+tail;
				trace(".openEdits: strong="+strong+" page=",textInfo(content,false));
				new TextLines(file).writeLines(page.split("\n"));
			}
			else trace(": existing file=",file);
			trace(": opening file=",file);
			if(strong)Runtime.getRuntime().exec(new String[]{editor(strong).getCanonicalPath(),
					strong?"":"-edit",file.getCanonicalPath()});
		}catch(IOException e){
			e.printStackTrace();
		}
		(fileChecks=new FileModifiedChecker(null,file){
			final HtmlEditEncoder html=HtmlEditEncoder.this;
			private String rawThen;
			protected void fileChanged(){
				if(!file.exists())return;
				else html.trace(": changed file=",file);
				String rawNow;
				try{
					rawNow=new TextLines(file).readLinesString();
				}catch(IOException e){
					throw new RuntimeException(e);
				}
				if(!rawNow.equals(rawThen)){
					boolean full=true&&strong;
					if(false)html.trace(": normalising strong="+strong+" rawNow="+textInfo(rawNow,full));
					String normalised=normalisedContent(rawNow,rawThen,strong);
					rawThen=rawNow;
					html.trace(": strong="+strong+" normalised="+textInfo(normalised,full));
					Nodes.storeEncodedText(encodingStore(),splitAfterRules(
							adjustNormalisedContent(normalised+contentTail),-1));
					html.fileContentStored();
				}
				startChecking();
			};
		}).startChecking();
	}
	protected boolean editTop(){
		return false;
	}
	protected abstract HtmlBuilder newPageBuilder();
	protected File editor(boolean strong){
		return new File(Util.programs32,(strong?strongEditor:weakEditor));
	}
	protected abstract ValueNode encodingStore();
	protected String adjustNormalisedContent(String normalised){
		String adjust=Regex.replaceAll(normalised,new String[]{
			"<a\\s+(href=\")(" +HTTP+")?([^>]+>)","<a $1"+HTTP+"$3",
		});
		if(!adjust.equals(normalised))trace(".adjustNormalisedContent: adjust=",adjust.length());
		return adjust;
	}
	protected void fileContentStored(){}
	final public void closeEdits(){
		if(fileChecks!=null)fileChecks.stopChecking();
		if(!file.exists())return;
		if(false)try{
			new Util.FileBackup(file){
				@Override
				protected boolean overwriteExisting(){
					return true;
				}
			}.doBackup();
		}catch(IOException e){
			throw new RuntimeException(e);
		}
		String msg=": deleting file="+file;
		if(false)trace(msg);
		else memCheck("HtmlEditEncoder"+msg);
		File root=file.getParentFile();
		for(File html:root.listFiles(new FileFilter(){
			@Override
			public boolean accept(File file){
				return isHtmlFile(file);
			}
		}))html.delete();
		for(File dir:root.listFiles(new FileFilter(){
			@Override
			public boolean accept(File file){
				return file.isDirectory()&&file.getName().endsWith("_files");
			}
		})){
			for(File file:dir.listFiles())file.delete();
			dir.delete();
		}
	}
	private boolean isHtmlFile(File file){
		return file.getName().endsWith(EXT_HTML);
	}
}

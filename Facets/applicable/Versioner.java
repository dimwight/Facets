package applicable;
import facets.util.TextLines;
import facets.util.Tracer;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import applicable.field.DateField;
public abstract class Versioner extends Tracer{
	private static final String VERSION="#";
	private final int maxCount;
	private final File dir;
	private final String titleTop,ext;
	public Versioner(int maxCount,File dir,String titleTop,String ext){
		super(Versioner.class.getSimpleName());
		this.maxCount=maxCount+1;
		this.dir=dir;
		this.titleTop=titleTop;
		this.ext=ext;
	}
	public static void main(String[]args)throws IOException{
		TextLines.setDefaultEncoding(true);
		new Versioner(5,new File("C:/Tray"),"Versioner",".txt"){
			@Override
			protected void writeVersion(File version) throws IOException{
				if(true)new TextLines(version).writeLines(new Date().toString());
			}
		}.createVersion();
	}
	private final static boolean checkDate=true;
	final public void createVersion()throws IOException{
		int at=0;
		for(;at<maxCount+1;at++){
			File next=newAtFile(at);
			if(!next.exists()){
				int previousAt=at++>0?at-2:maxCount-1,deleteAt=at<maxCount?at:0;
				if(false)trace(".createVersion: at="+(at-1)+" previousAt="+previousAt+" deleteAt="+deleteAt);
				File previous=newAtFile(previousAt),
					version=checkDate&&DateField.dateIsToday(new Date(previous.lastModified()))?previous:next, 
					delete=newAtFile(deleteAt);
				trace(": version=",version.getAbsolutePath());
				writeVersion(version);
				if(version==next)delete.delete();
				return;
			}
		}
		if(at==maxCount)throw new IllegalStateException(
				"Too many existing versions maxCount="+maxCount);
	}
	private File newAtFile(int at){
		return new File(dir,titleTop+VERSION+at+ext);
	}
	protected abstract void writeVersion(File version)throws IOException;
}

package facets.util.app;
import facets.util.Debug;
import facets.util.Tracer;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
public final class DirCache extends Tracer{
	private final boolean deleteWrites;
	private final File dir;
	@Override
	protected void traceOutput(String msg){
		if(true)super.traceOutput(msg);
	}
	public DirCache(File dir,boolean deleteWrites){
		this.dir=dir;
		this.deleteWrites=deleteWrites;
		String name=dir.getName();
		if(!name.toLowerCase().equals(name))throw new IllegalArgumentException(
				"Bad dir name="+name);
	}
	private File newFile(String name){
		return new File(dir,name);
	}
	public void put(String name,Serializable data){
		try{
			File file=newFile(name);
			ObjectOutputStream stream=new ObjectOutputStream(new FileOutputStream(file));
			stream.writeObject(data);
			stream.close();
			if(deleteWrites)file.delete();
			trace(".put: file=",file.exists());
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	public Serializable get(String name){
		try{
			File file=newFile(name);
			if(!file.exists())return null;
			ObjectInputStream stream=new ObjectInputStream(new FileInputStream(file));
			Serializable packed=(Serializable)stream.readObject();
			stream.close();
			return packed;
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
}

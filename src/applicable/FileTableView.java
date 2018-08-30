package applicable;
import facets.core.app.TableView;
import facets.core.app.TextView.LinkText;
import facets.util.Stateful;
import facets.util.Util;
import facets.util.ValueProxy;
import facets.util.tree.ValueNode;
import java.io.File;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
public class FileTableView extends TableView{
	private static final String NODE_TYPE=FileTableView.class.getSimpleName(),
		FIELDS[]="Path,Length,Modified".split(",");
	private final String rootPath;
	public FileTableView(String title,File rootDir){
		super(title);
		rootPath=rootDir==null?"":rootDir.getAbsolutePath();
	}
	@Override
	final public String getColumnTitle(int col){
		return FIELDS[col];
	}
	@Override
	final public boolean canChangeSelection(){
		return false;
	}
	@Override
	final public Format getColumnFormat(int col){
		return col!=2?null:new SimpleDateFormat("dd/MM/yyyy");
	}
	@Override
	final protected ValueProxy newRowProxy(Stateful source){
		return new ValueProxy(source){
			@Override
			protected Object[]lazyValues(){
				ValueNode node=(ValueNode)this.source;
				Object[]values=new Object[FIELDS.length];
				for(int col=0;col<values.length;col++){
					String key=FIELDS[col],got=node.get(key);
					if(got==null)values[col]="";
					else if(col==2)values[col]=new Date(node.getLong(key));
					else if(col==1)values[col]=Util.kbs(node.getInt(key));
					else{
						final File file=new File(got);
						String text=got.replace(rootPath,"").substring(1),
							link=file.exists()&&file.canRead()?text:"";
						values[col]=new LinkText(text,link){
							@Override
							public void fireLink(){
								openFile(file);
							}
						};
					}
				}
				return values;
			}
		};
	}
	protected void openFile(final File file){
		Util.windowsOpenFile(file);
	}
	@Override
	final public String typeKey(){
		return FileTableView.class.getSimpleName();
	}
	public static ValueNode newEmptyNode(String none){
		return new ValueNode(NODE_TYPE,"Empty",new Object[]{"Path=?[No "+none+"]"});
	}
	public static ValueNode newPathNode(File source){
		ValueNode node=new ValueNode(NODE_TYPE,source.getName(),new Object[]{
			"Path="+source.getAbsolutePath(),
			"Length="+source.length(),
			"Modified="+source.lastModified()
		});
		return node;
	}
}
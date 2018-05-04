package apps.idiom;
import static facets.util.tree.DataConstants.*;
import static facets.util.tree.TypedNode.*;
import facets.facet.kit.swing.tree.DatatreeModel;
import facets.facet.kit.swing.tree.OffsetPathTree;
import facets.util.OffsetPath;
import facets.util.TextLines;
import facets.util.Util;
import facets.util.tree.DataNode;
import facets.util.tree.Nodes;
import facets.util.tree.ValueNode;
import facets.util.tree.XmlPolicy;
import facets.util.tree.XmlSpecifier;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
/**
Demonstrates idiomatic use of types from {@link facets.util.tree}. 
<p>Swing GUI is based on types from {@link facets.facet.kit.swing.tree}. 
 */
public final class Datatree extends IdiomPane{
	
	/** Keys for use with {@link IdiomPane#values}. */
	private static final String 
		KEY_TREE_PATH="treePath",
		ARG_NATURE_DATA="natureData",
		ARG_STATE_DATA="stateData",
		ARG_WRITE_ZIP="writeZip";
	
	/** Default content for data tree, encoded using {@link Nodes#encode(DataNode, int)}. */
	private static final String DEFAULT_DATA="789C8552BF6FD340147E981828BF94A6122D43C11BAA080E69192A451DA008299229422E0891E912BF844B1DDBDC3D87B403529180810521D8905241C78805FE05604388A1AC8889A5120C8881A1DC250E84126039BF3B7FDFF7BEEFDE7536C18C058C979C3A6B329B87B68B82339FAFB0B28F85676F473AEEFBCDA201D08A0060A71470A4CA2A48D28E89FBB6C7889140B42F333FC685D0C3ECCB5B0FDAAB5B5F3445C0E450F0595568EC575EBFFDE2F1E1B601C655D815B1CA127A25D8530903C28024C158CF57CE67412D77A15CC70A155A7FB3B0B81CA1A7659F6CBDCABF2B99AE01A92218DC73B4B6508A0496D3A3E63435D7A7E67E520B0EA4486D0832039D5D123CA8A97F234D1D73B10B181D00CCFB4C4AED6C62D099ABD4B11AFBF3A1C04B9F5E6F3CFD7E71434575C094C41A918A37205154996B2854139338F9781D6EC2EE56A42E7CB43B1A0DB21350E6E3DAFAB7D5BBB306EC2882D9F5A47AA77FE116E24619C59DCEA3C97D0F3FDCEB4FEF1881A9439ED4B56A9FD27368FE46EC255DFBBC3EBB52787EA64754EF2399430F93CCE1FE9B2BEDB49CF2FBEA06C104618BE64E5B9237221F2DBDB3BAEED46D2DE1327ACABF9CCB67A7B333D953522734BAEB7E459F4ACCE5751DE9D3B45E3210EBCF983A4D111C70C346A21B06964A4037C22CC141BAA6A66885C2C226EAF3BDD5301696CF0394DBDAE4549BF3BC859EAE87B63109C6CF7121E9CF18DBB48E2796A775AD0F8F0ED303752F2EAA37EDFD57F0442238A3EB7F091E72D8307F3F00D7822E90";
	
	/** References to contents of {@link IdiomPane#values}. */
	final ValueNode 
		nature=values.nature(),
		state=values.state();
	
	/** Root of data tree. */
	final ValueNode data;
	
	/** Standard policy. */
	final XmlPolicy policy=new XmlPolicy();
	
	/**
	Unique constructor, creates or reads data tree. 
	 */
	Datatree(String[]args){
		
		//To superclass
		super(args);
		
		//Create or retrieve data tree, decode if required
		data=nature.getBoolean(ARG_NATURE_DATA)?nature
			:nature.getBoolean(ARG_STATE_DATA)?state
			:new ValueNode(TYPE_DATA,UNTITLED,new Object[]{DEFAULT_DATA});
		Nodes.decode(data);
	}
	
	@Override
	protected Component newPanel(Container pane){
		
		if(false) try{
			writeData();
		}
		catch(IOException e){
			throw new RuntimeException(e);
		}

		//Create tree and set model
		final OffsetPathTree tree=new OffsetPathTree(new DatatreeModel(){
			@Override
			public Object getRoot(){
				return data;
			}
		}){
			@Override
			protected OffsetPath[] getOffsets(){
				throw new RuntimeException("Not implemented in "+this);
			}
			@Override
			protected void putOffsets(OffsetPath[] offsets){
				throw new RuntimeException("Not implemented in "+this);
			}
		};
		//Enable useful rendering of nodes
		DefaultTreeCellRenderer renderer=new DefaultTreeCellRenderer(){
			JLabel label=new JLabel();
			@Override
			public Component getTreeCellRendererComponent(JTree tree,Object value,
					boolean sel,boolean expanded,boolean leaf,int row,boolean hasFocus){
				JLabel superLabel=(JLabel)super.getTreeCellRendererComponent(tree,value,sel,
						expanded,leaf,row,hasFocus),
					useLabel=true?superLabel:label;
				
				//Ensure label matches standard
				useLabel.setFont(superLabel.getFont());
				useLabel.setIcon(superLabel.getIcon());
				
				//Render type and title
				if(value instanceof DataNode){
					DataNode node=(DataNode)value;
					String title=node.title();
					useLabel.setText(node.type()+(title.equals(UNTITLED)?"":(" "+title)));
				}
				else useLabel.setText(value.toString());
				return useLabel;
			}
		};
		tree.setCellRenderer(renderer);
		tree.setCellEditor(new DefaultTreeCellEditor(tree,renderer){
			@Override
			public Component getTreeCellEditorComponent(JTree tree,Object value,
					boolean isSelected,boolean expanded,boolean leaf,int row){
				Container superComp=(Container)super.getTreeCellEditorComponent(tree,value,
						isSelected,expanded,leaf,row);
				JTextField field=(JTextField)superComp.getComponent(0);
				if(value instanceof DataNode){
					String title=((DataNode)value).title();
					field.setText(title.equals(UNTITLED)?"":title);
				}
				else{
					String text=value.toString();
					field.setText(Nodes.isKeyPair(text)?
							Nodes.splitPair(text)[1]:text);
				}
				return superComp;
			}
		});
		tree.setEditable(true);
		tree.setDragEnabled(true);
		tree.addTreeSelectionListener(new TreeSelectionListener(){
			public void valueChanged(TreeSelectionEvent e){
				OffsetPath nodePath=tree.newOffsetPath(e.getPath());
				state.put(KEY_TREE_PATH,nodePath.offsets);
			}
		});
		int[]offsets=state.getInts(KEY_TREE_PATH);
		if(offsets.length!=0)tree.setOffsetPath(tree.newTreeOffsetPath(offsets));
		return new JScrollPane(tree){
			@Override
			public Dimension getPreferredSize(){
				return new Dimension(50,300);
			}
		};
	}
	private void writeData() throws IOException{

		//Get file spec
		XmlSpecifier specifier=policy.fileSpecifiers(
				)[values.nature().getBoolean(ARG_WRITE_ZIP)?1:0];

		//File to write to
		File out=specifier.newFile(Util.runDir(),idiomName);
		
		//Write out
		specifier.newTreeRoot(data).writeToSink(out);
		
		//Read file details and output back to console
		if(true)trace(".newDialogMessagePanel:\n\tsink="+out+"\n\tlength="+out.length()
					+"\n\ttext=\n",new TextLines(out).readLinesString());
	}
	@Override
	protected void traceOutput(String msg){
		if(true)super.traceOutput(msg);
	}
	public static void main(String[]args){
		new Datatree(args).buildAndLaunch(false);
	}
}

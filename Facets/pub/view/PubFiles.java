package pub.view;
import static facets.util.tree.DataConstants.*;
import static pub.PubValues.*;
import static pub.view.RecordProxy.*;
import facets.core.app.Dialogs;
import facets.core.app.Dialogs.Response;
import facets.util.FileSpecifier;
import facets.util.TextLines;
import facets.util.Tracer;
import facets.util.app.AppValues;
import facets.util.tree.DataConstants;
import facets.util.tree.Nodes;
import facets.util.tree.TypedNode;
import facets.util.tree.ValueNode;
import facets.util.tree.XmlPolicy;
import facets.util.tree.XmlSpecifier;
import facets.util.tree.Nodes.TreeRoot;
import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import pub.PubValues;
import applicable.NodeComparison;
import applicable.Versioner;
	enum PubFiles{Listing,StoredZip,StoredClear,Edit;
	static final File STORED_DIR=new File(false?new File("C:\\Tray"):VIEW_DIR,"stored"),
		ATTACHMENTS_DIR=new File(VIEW_DIR,"attached"),
		PREVIOUS_DIR=new File(VIEW_DIR,"previous"),
		EDIT_DIR=false?AppValues.userDir():false?new File(VIEW_DIR,"edit"):MASTER_DIR;
	private final static Tracer t=Tracer.newTopped(PubFiles.class.getSimpleName(),true);
	static final String TYPE_ENCODED="HtmlEncoded",USER_LOCKED="Record locked: ";
	final XmlSpecifier newXmlSpecifier(){
		return new XmlPolicy(){
			PubFiles files=PubFiles.this;
			XmlSpecifier[]specifiers=new XmlSpecifier[]{
					files==Listing?new XmlSpecifier(TYPE_XML+TextLines.EXT_SZIP,
							VIEW_TITLE+" listing",this)
					:files==StoredZip?new XmlSpecifier(TYPE_PUB+EXT_XML_ZIP,"Publication zip files",
							this)
					:new XmlSpecifier(TYPE_PUB+EXT_XML,"Publication clear files",this),
			};
			@Override
			public XmlSpecifier[]fileSpecifiers(){
				return specifiers;
			}
			@Override
			protected boolean treeAsXmlRoot(){
				return PubFiles.this==Listing;
			}
			@Override
			protected ValueNode getTitleAttributeNames(){
				return newTitleAttributeNames("count",new String[]{
						TYPE_XMLS+"="+KEY_TITLE,
						TYPE_XML+"="+KEY_TITLE,
						TYPE_TEXT+"="+KEY_TITLE,
						TYPE_ENCODED+"="+KEY_TITLE,
						TYPE_DOC+"="+KEY_TITLE,
						TYPE_LINK+"="+FIELD_PUBNUM,
						TYPE_ATTACHMENT+"=fileName"
				});
			}
		}.fileSpecifiers()[0];
	}
	ValueNode readPub(File file)throws IOException{
		if(this==Listing)throw new RuntimeException("Not implemented in "+this);
		final XmlSpecifier thisSpec=newXmlSpecifier();
		ValueNode rootNode=thisSpec.newRootNode(file);
		TreeRoot root;
		if(!thisSpec.specifies(file))throw new IllegalArgumentException(
				"Bad file="+file+" for "+this);
		else root=thisSpec.newTreeRoot(rootNode);
		root.readFromSource(file);
		ValueNode pub=(ValueNode)root.tree.children()[0];
		pub.setTitle(pub.title().trim());
		String titleNow=rootNode.title();
		if(!pub.title().equals(titleNow)){
			pub.setTitle(titleNow);
			ValueNode fields=fieldValues(pub);
			fields.put(FIELD_PUBNUM,ValueNode.NULL_MARKER);
			fields.put(FIELD_REV,ValueNode.NULL_MARKER);
			Nodes.sortValues(fields);
		}
		return pub;
	}
	static void checkUpdateListing(Dialogs dialogs,String fileName)throws IOException{
		int edits=listEdits().length;
		t.trace(".checkUpdateListing: edits=",edits);
		if(edits>0){
			if(true||dialogs.confirmYesNo("Update Database?","Update with " +edits+" record" +
					(edits==1?"":"s")+" before quitting?")==Response.Yes)
				ListingContent.newChecked(fileName);
		}
	}
	static void checkLockException(Exception e,Dialogs dialogs){
		String msg=e.getMessage(),ul=USER_LOCKED;
		if(msg!=null&&msg.contains(ul))dialogs.errorMessage("Record Not Available",
					msg.substring(msg.indexOf(ul)+ul.length()));
		else throw new RuntimeException(e);
	}
	static boolean checkUserLock(ValueNode now,ValueNode then){
		if(now.getLong(KEY_MODIFIED)>then.getLong(KEY_MODIFIED)){
			t.trace(".checkUserLock: changed now=",now.title());
			return false;
		}
		File file=Edit.newXmlSpecifier().newFile(EDIT_DIR,now.title());
		t.trace(".checkUserLock: deleting file=",file);
		file.delete();
		if(file.exists())throw new IllegalStateException("Unlock failed file="+file);
		return true;
	}
	static void writeEdit(ValueNode tree)throws IOException{
		XmlSpecifier spec=Edit.newXmlSpecifier();
		String title=tree.title();
		File file=spec.newFile(EDIT_DIR,title);
		if(file.exists()){
			String editUser=Edit.readPub(file).getString(KEY_USER);
			if(!editUser.equals(userName))
				throw new IOException(USER_LOCKED+"'" +title+"' is locked by user '"+
						editUser +"'.");
			else if(false)t.trace(".writeEdit: editUser="+editUser);
		}
		boolean writing=true;
		if(writing)spec.newTreeRoot(tree).writeToSink(file);
		t.trace(".writeEdit:"+(writing?"":" writing=false")+" file=",file);
	}
	static File[]listNetworkFiles(File dir){
		return offNetwork?new File[]{}:dir.listFiles();
	}
	static ValueNode readTitleStored(String title)throws IOException{
		File zip=StoredZip.newXmlSpecifier().newFile(STORED_DIR,title),
				clear=StoredClear.newXmlSpecifier().newFile(STORED_DIR,title);
		return zip.exists()?StoredZip.readPub(zip):StoredClear.readPub(clear);
	}
	static void writeStored(ValueNode now,boolean isEdit)throws IOException{
		String title=now.title();
		boolean writeZips=false;
		PubFiles files=writeZips?StoredZip:StoredClear;
		XmlSpecifier spec=files.newXmlSpecifier();
		File file=spec.newFile(STORED_DIR,title);
		if(file.exists()&&isEdit){
			final TreeRoot thenRoot=spec.newTreeRoot(spec.newRootNode(file));
			thenRoot.readFromSource(file);
			TypedNode then=thenRoot.tree.children()[0];
			new Versioner(5,PREVIOUS_DIR,title,"."+spec.dataType+EXT_XML){
				protected void writeVersion(File version)throws IOException{
					thenRoot.writeToSink(version);
					if(false)t.trace(": version=",version);
				}
			}.createVersion();
		}
		if(isEdit)now.put(KEY_MODIFIED,System.currentTimeMillis());
		spec.newTreeRoot(now).writeToSink(file);
		if(!writeZips){
			File zip=StoredZip.newXmlSpecifier().newFile(STORED_DIR,title);
			if(zip.exists()){
				t.trace(": Deleting zip=",zip.getName());
				zip.delete();
			}
		}
		if(!files.readPub(file).stateEquals(now))throw new IllegalStateException(
				"Bad write to file="+file);
	}
	static File[]listStored(){
		File dir=STORED_DIR;
		if(!dir.exists())throw new IllegalStateException("No "+dir);
		else return dir.listFiles(new FileFilter(){
			final XmlSpecifier zip=StoredZip.newXmlSpecifier(),
					clear=StoredClear.newXmlSpecifier();
			private int accepted;
			@Override
			public boolean accept(File file){
				return accepted>10?false:
					zip.specifies(file)||clear.specifies(file)&&!file.getName().startsWith("_");	
			}
		});
	}
	static File[]listEdits(){
		File dir=EDIT_DIR;
		if(!dir.exists())throw new IllegalStateException("No "+dir);
		else return dir.listFiles(new FileFilter(){
			final XmlSpecifier spec=Edit.newXmlSpecifier();
			@Override
			public boolean accept(File file){
				return spec.specifies(file)&&!file.getName().startsWith("_");	
			}
		});
	}
	static void _compareVersions(final String title)throws IOException{
		List<ValueNode>previous=new ArrayList();
		for(File file:PREVIOUS_DIR.listFiles(new FileFilter(){
				@Override
				public boolean accept(File file){
					return file.getName().startsWith(title);
				}
			}))
			previous.add(StoredZip.readPub(file));
		t.trace(".compareVersions: previous=",previous.size());
		if(previous.size()<2)return;
		Collections.sort(previous,new Comparator<ValueNode>(){
			@Override
			public int compare(ValueNode o1,ValueNode o2){
				return new Long(getModified(o1)).compareTo(new Long(getModified(o2)));
			}});
		ValueNode[]versions=previous.toArray(new ValueNode[]{});
		new NodeComparison(versions[1]).compare(versions[0]);
	}
}
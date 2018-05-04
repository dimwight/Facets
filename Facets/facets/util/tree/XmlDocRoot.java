package facets.util.tree;
import static facets.util.tree.TypedNode.*;
import facets.util.Debug;
import facets.util.ItemList;
import facets.util.Objects;
import facets.util.Strings;
import facets.util.TextLines;
import facets.util.tree.Nodes.TreeRoot;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
/**
Reads and writes a {@link DataNode} tree as an XML document. 
<p>{@link DataNode}s are written as XML elements, their titles and keypair values 
as attributes, other stringified values as text nodes (but see <i>Text segregation</i> below). 
Nodes to be written need only fulfil the {@link DataNode} contract; 
elements are read as {@link ValueNode}s to
allow the retrieval and setting of attributes as keypairs. 
<p>To separate mechanism from policy, {@link TreeRoot} is declared <code>final</code> and
document-specific values are provided by a {@link XmlPolicy} passed to the constructor. 
<p>Unless {@link XmlPolicy#treeAsXmlRoot()} returns <code>true</code>, 
the root of the {@link DataNode} tree is assumed to represent the 
XML document itself and the first child only of this root is rendered 
as the XML root element. 
While it may appear cumbersome that the tree root wrapped is not by default
 the XML root, this approach has advantages:
<ul>
<li>The tree root exists in advance of any attempt to read into it.  
<li>The tree root can define properties of the XML document eg file information.
<li>If the type of the tree root is known in advance of reading the XML root,
it is simple to check that the type of the XML root is as expected.   
</ul>
<p>{@link TypedNode#title()}s can be mapped to attribute names  
using {@link XmlPolicy#getTitleAttributeNames()}.
 <h3>Text segregation</h3>
 <p>Since {@link ValueNode} stores XML attributes as keypairs
 which cannot be distinguished from text matching {@link Nodes#isKeyPair(String)}, 
 {@link XmlDocRoot} enforces <i>segregation</i> of text from elements with attributes:    
<ul>
<li>In {@link #readFromSource(Object)} text from an element with attributes 
other than that mapped to {@link TypedNode#title()}  
(or all elements if {@link XmlPolicy#segregateAll()} returns <code>true</code>)
is wrapped in a node created by {@link XmlPolicy#newSegregated(String[])}.
As this node may need to be interim to avoid name collisions, 
the complete tree can be tidied up as required in 
{@link XmlPolicy#cleanUpSegregated(DataNode)}.
<li>In {@link #writeToSink(Object)} 
(unless {@link XmlPolicy#dataUsesAttributes()} returns <code>false</code>) 
values matching {@link Nodes#isKeyPair(String)} are set as element attributes and
 no other values from the same node can be written; nodes identified
by {@link XmlPolicy#isSegregated(DataNode)} are written as text nodes.      
</ul>
 */
public final class XmlDocRoot extends TreeRoot{
	private static final DocumentBuilderFactory factory=DocumentBuilderFactory.newInstance();
	public static final String SHEBANG="<?xml";
	static final String prettyFixes[]={
		"(=\"[^\"]*\"\\s+)","$1\n\t",
		"<","\n<",">",">\n",
		"(?s)^\n","",
		"\n\n","\n",
		">\r?\n([^\r\n<]+)\r?\n</",">$1</",
	};
	private static final boolean sortAttributes=false;
	private final XmlPolicy p;
	private final ValueNode titleAttributeNames;
	private int segregations;
  /**
	Unique constructor. 
	@param node passed to superclass
  @param p defines detail behaviour 
	 */
	public XmlDocRoot(DataNode node,XmlPolicy p){
		super(node);
		this.p=p;
		titleAttributeNames=p.getTitleAttributeNames();
		if(titleAttributeNames==null)throw new IllegalStateException(
				"Null title attribute names in "+Debug.info(this));
		TextLines.setEncoding("UTF-8");
	}
	/**
	Reads an XML document from text. 
	<p>Text obtained from <code>src</code> is read as follows:
	<ul>
	<li>parsed as an XML document whose root element is set as the 
	contents of the node wrapped (except where {@link XmlPolicy#treeAsXmlRoot()} 
	returns <code>true</code>)
	<li>any parse exception wrapped in an {@link ExceptionNode} set as the 
	contents of the node.
	</ul> 
	<p>Each XML element is read into a {@link ValueNode} with:
	<ul>
	<li>its name returned as <code>type</code>
	<li>child elements returned in <code>children</code>
	<li>attributes handled as documented for 
	{@link XmlPolicy#newTitleAttributeNames(String, String[])};
	<li>text split into line values and possibly <i>segregated</i> (see class summary) 
	</ul>
	@param src must be either a {@link File} or a {@link TextLines}
	 */
  public void readFromSource(Object src){
  	TextLines lines=src instanceof TextLines?(TextLines)src
  			:src instanceof URL?new TextLines((URL)src):new TextLines((File)src);
		TypedNode xmlRoot;
		try{
			Document document=newXmlDocument(lines);
			NodeList childNodes=document.getChildNodes();
			Node childNode;
			for(int at=0;!((childNode=childNodes.item(at))instanceof Element);at++);
			xmlRoot=newElementNode((Element)childNode);
		}catch(SAXException sxe){
			Exception e=sxe.getException();
			if(!p.handleReadExceptions())throw new RuntimeException(sxe);
			else xmlRoot=new ExceptionNode(e!=null?e:sxe);
		}catch(ParserConfigurationException pce){
			if(!p.handleReadExceptions())throw new RuntimeException(pce);
			else xmlRoot=new ExceptionNode(pce);
		}catch(IOException e){
			if(!p.handleReadExceptions())throw new RuntimeException(e);
			else xmlRoot=new ExceptionNode(e);
		}
		tree.setContents(new Object[]{xmlRoot});
		if(!p.treeAsXmlRoot())return;
		String xmlRootType=xmlRoot.type(),treeType=tree.type();
		if(!xmlRootType.equals(treeType))
			throw new IllegalStateException("Can't merge XML root type=" +xmlRootType+
					" with tree type="+treeType);
		else{
			tree.setTitle(xmlRoot.title());
			tree.setContents(xmlRoot.contents());
			String title=xmlRoot.title();
		}
		if(segregations>0)p.cleanUpSegregated(tree);
	}
	private DataNode newElementNode(Element element){
		String tagName=element.getTagName(),titleAttr=getTitleAttribute(tagName);
		ItemList<Object>contents=new ItemList(Object.class);
		NamedNodeMap attrs=element.getAttributes();
		int attrCount=attrs.getLength();
		for(int at=0;at<attrCount;at++){
			Attr attr=(Attr)attrs.item(at);
			String name=attr.getName(),value=name+"="+attr.getValue();
			if(!name.equals(titleAttr))contents.addItem(value);
		}
		boolean hasAttrs=contents.size()>0;
		if(sortAttributes)Strings.sortLines(Objects.newTyped(String.class,contents.toArray()));
		NodeList childNodes=element.getChildNodes();
		for(int i=0,length=childNodes.getLength();i<length;i++){
			Node child=childNodes.item(i);
			if(child instanceof Element)
				contents.addItem(newElementNode((Element)child));
			else if(child instanceof Text){
				String[]lines=p.textToValueLines(((Text)child).getData());
				if(lines.length==0)continue;
				if(hasAttrs||p.segregateAll())contents.addItem(
						p.newSegregated(lines));
				else contents.addItems((Object[])lines);
			}
			else throw new IllegalStateException("Unknown type in "+Debug.info(child));
		}
		String title=!element.hasAttribute(titleAttr)?UNTITLED:
			(p.titleAttributeKeyPairs()?titleAttr+"=":"")+element.getAttribute(titleAttr);
		return new ValueNode(tagName,title,contents.items());
	}
	/**
	Creates XML document text. 
	<p>XML text that renders the {@link DataNode} tree wrapped is written to <code>sink</code>,
	with members of the tree rendered as XML elements, except for those identified by 
	{@link XmlPolicy#isSegregated(DataNode)} which are rendered as text nodes. 
	<p>For other nodes the <code>type</code> is used as the element name and 
	any <code>title</code> other than {@link TypedNode#UNTITLED} 
	is rendered by an attribute as 
	documented in {@link XmlPolicy#newTitleAttributeNames(String,String[])}. 
	<p>Stringified <code>values</code> are rendered as follows:
	<ul>
	<li>if {@link XmlPolicy#dataUsesAttributes()} returns <code>true</code> and 
	all values are keypairs, as attributes </li>
	<li>otherwise, as a text node</li>
	</ul>
	@param sink must be either a {@link File} or a {@link TextLines}
	 */
	public void writeToSink(Object sink){
		TextLines lines=sink instanceof TextLines?(TextLines)sink
			:new TextLines((File)sink);
		DataNode xmlRoot=p.treeAsXmlRoot()?tree:(DataNode)tree.children()[0];
		try{
			DocumentBuilder builder=factory.newDocumentBuilder();
			Document document=builder.newDocument();
			document.appendChild(newNodeElement(document,xmlRoot));
			String raw=newDocumentXml(document).readLinesString();
			lines.writeLines(Strings.stringLines(p.prettifyRawXML(raw)));
		}catch(Exception e){
			throw new RuntimeException(e);
		}
	}
	private Element newNodeElement(Document d,DataNode node){
		String[]lines=node.values();
		Object[]contents=node.contents();
		boolean noChildren=lines.length==contents.length;
		String tagName=node.type();
		Element element=d.createElement(tagName);
		if(p.dataUsesAttributes()&&lines.length>0&&Nodes.isKeyPair(lines[0])){
			for(String line:sortAttributes?Strings.sortLines(lines):lines)try{
				String[]splitPair=line.split("=");
				element.setAttribute(splitPair[0],splitPair[1]);
			}
			catch(Exception e){
				throw new RuntimeException("Unsegregated text following attribute:\n"+line);
			}
		}
		else if(noChildren)
			element.appendChild(d.createTextNode(Objects.toLines(lines)));
		String title=node.title(),titleAttr=getTitleAttribute(tagName);
		if(!title.equals(UNTITLED))try{
			element.setAttribute(titleAttr,title);
		}catch(Exception e){
			throw new RuntimeException("Can't set title="+title+" as titleAttr="+titleAttr,e);
		}
		if(false)trace(".newNodeElement: contents=",contents);
		if(false)for(TypedNode child:node.children())element.appendChild(
				p.isSegregated((DataNode)child)?
						d.createTextNode(Objects.toLines(child.contents()))
				:newNodeElement(d,(DataNode)child));
		else for(Object c:contents)
			if(c instanceof TypedNode){
				DataNode child=(DataNode)c;
				element.appendChild(p.isSegregated(child)?
								d.createTextNode(Objects.toLines(child.contents()))
						:newNodeElement(d,child));
			}
			else{
				String valueStr=c.toString();
				if(!noChildren&&!Nodes.isKeyPair(valueStr))
					element.appendChild(d.createTextNode(valueStr));
			}
		return element;
	}
	private String getTitleAttribute(String tagName){
		String attrName=titleAttributeNames.getString(tagName);
		if(attrName.equals(""))attrName=titleAttributeNames.getString(
				XmlPolicy.KEY_TITLE_ATTR_NAME_DEFAULT);
		return attrName;
	}
	/**
	Reads XML text into a {@link Document}. 
	<p>Called internally by {@link #readFromSource(Object)}; exposed to enable
	creation of {@link Document}s from text rather than a stream.  
	@param xml loaded with XML text 
	 */
	public static Document newXmlDocument(TextLines xml)
			throws ParserConfigurationException,SAXException,IOException{
		factory.setIgnoringComments(true);
		DocumentBuilder builder=factory.newDocumentBuilder();
		builder.setEntityResolver(new EntityResolver(){
			public InputSource resolveEntity(String publicId,String systemId){
				return new InputSource(new StringReader(""));
			}
		});
		if(false)builder.setErrorHandler(new ErrorHandler(){
			public void fatalError(SAXParseException exception)throws SAXException{
				throw exception;
			}
			public void error(SAXParseException exception)throws SAXException{
				throw exception;
			}
			public void warning(SAXParseException exception)throws SAXException{
				throw exception;
			}
		});
		InputStream stream=xml.newInputStream();
		Document doc=builder.parse(stream);
		stream.close();
		return doc;
	}
	/**
	Creates XML text from a {@link Document}. 
	<p>Used internally by {@link #writeToSink(Object)} to enable post-processing of
	XML text in {@link XmlPolicy#prettifyRawXML(String)}; exposed to enable
	temporary storage of a {@link Document}s as text. 
	@return a {@link TextLines#newBuffer(String[])} loaded with XML text
	 */
	public static TextLines newDocumentXml(Document document)
			throws TransformerFactoryConfigurationError,
			TransformerConfigurationException,TransformerException{
		StringWriter writer=new StringWriter();
		TransformerFactory.newInstance().newTransformer().transform(
				new DOMSource(document),new StreamResult(writer));
		return TextLines.newBuffer(writer.toString().split("\n"));
	}
	static void main(String[]args)throws IOException{
		XmlDocRoot root=new XmlDocRoot(new DataNode("RootNode",UNTITLED),new XmlPolicy(){
			String segregationType="segregatedText";
			private int segregations;
			protected boolean treeAsXmlRoot(){
				return true;
			}
			protected boolean segregateAll(){
				return false;
			}
			protected DataNode newSegregated(String[]lines){
				return new DataNode(segregationType,"segregation #"+segregations++,lines);
			}
			protected void cleanUpSegregated(DataNode tree){}
			protected boolean isSegregated(DataNode node){
				return node.type().equals(segregationType);
			}
			protected String prettifyRawXML(String raw){
				return false?raw:super.prettifyRawXML(raw);
			}
		});
		final String testIn=TextLines.newXmlTop()+"<RootNode title=\"lineBreaks\">\r\n" + 
				"text on \r\n" + 
				"two lines after attributes\r\n" + 
				"<DataNode title=\"two lines\">one line of text on its own</DataNode>" +
				"<DataNode>\r\n" +"two lines \r\n" +"of text on their own</DataNode></RootNode>";
		File fileIn=new File("in.xml");
		if(false)new TextLines(fileIn).writeLines(testIn);
		if(true)root.readFromSource(fileIn);
		Object[]segregationTest=new Object[]{
				"a"+"=b",
				"not a keypair"
		};
		if(false)root.tree.setContents(new Object[]{new DataNode("Test","a title",segregationTest)});
		root.writeToSink(new File("out.xml"));
	}
}

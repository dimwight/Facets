package facets.util.tree;
import static facets.util.Regex.*;
import static facets.util.tree.TypedNode.*;
import facets.util.Debug;
import facets.util.Objects;
import facets.util.Tracer;
import org.w3c.dom.Document;
/**
Defines document and other policy for an {@link XmlDocRoot}, in 
particular {@link XmlSpecifier}s for loading and saving content. 
<p>For background to <i>segregate</i> methods see {@link XmlDocRoot} summary.  
 */
public class XmlPolicy extends Tracer{
	public static final String TYPE_SEGREGATED="_text";
	/** Key for storing default value in 
	 {@link XmlPolicy#newTitleAttributeNames(String, String[])}.*/
	public static final String KEY_TITLE_ATTR_NAME_DEFAULT="xmlRootDefaultTitleName";
	private static final ValueNode TITLE_NAMES_DEFAULT=newTitleAttributeNames(
			"title",new String[]{});
	public XmlSpecifier[]fileSpecifiers(){
		return new XmlSpecifier[]{
			new XmlSpecifier("xml","XML files",this),
			new XmlSpecifier("xml.zip","Zipped XML files",this),
			new XmlSpecifier("xmls.zip","Stamped XML files",this),
		};
	}
	/**
	After reading or before writing, is/should be tree rooted on the XML root node?   
	<p>Returning <code>true</code> has the effect that  
	<ul>
	<li>in {@link XmlDocRoot#readFromSource(Object)} and providing they have the same type, 
	the single child of <code>tree</code> is replaced with its own contents.  
	<li>in {@link XmlDocRoot#writeToSink(Object)} the XML root node is created from <code>tree</code>  
	</ul>
	@return <code>false</code> by default
	 */
	protected boolean treeAsXmlRoot(){
		return false;
	}
	/**
	When reading, create a (possibly interim) node for segregated text. 
	<p>Default is invalid stub.
	@param lines to be segregated, created by {@link #textToValueLines(String)} 
	 */
	protected DataNode newSegregated(String[]lines){
		return new ValueNode(TYPE_SEGREGATED,lines);
	}
	/**
	When reading, should text be segregated even where the containing element has 
	no attributes? 
	@return <code>false</code> by default
	 */
	protected boolean segregateAll(){
		return false;
	}
	/**
	After reading, clean up tree containing segregated text. 
	<p>Default is invalid stub.
	@param tree contains nodes created by {@link #newSegregated(String[])}
	 */
	protected void cleanUpSegregated(DataNode tree){
		throw new RuntimeException("Not implemented in "+Debug.info(this));
	}
	/**
	When writing, may identify segregated text. 
	@return <code>false</code> unless node type is {@value #TYPE_SEGREGATED}
	 */
	protected boolean isSegregated(DataNode node){
		return node.type().equals(TYPE_SEGREGATED);
	}
	/**
	When writing, does the data specify element attributes? 
	<p>Return <code>false</code> to avoid text segregation issues when writing. 
	@return <code>true</code> by default
	 */
	protected boolean dataUsesAttributes(){
		return true;
	}
	/**
	When reading or writing, maps attribute names to {@link TypedNode#title()}s. 
	@return non-<code>null</code> node created using 
		{@link #newTitleAttributeNames(String,String[])}; by default a minimal node
	 */
	protected ValueNode getTitleAttributeNames(){
		return TITLE_NAMES_DEFAULT;
	}
	/**
	Creates mappings for return by {@link #getTitleAttributeNames()}. 
	@param defaultName attribute name to be mapped to {@link TypedNode#title()} 
	in tags not specified by <code>mappings</code>; may be empty 
	@param mappings in format <i>tagName=attName</i>, where 
	<ul>
	<li><i>tagName</i> is an XML tag name (and {@link TypedNode#type()}) 
	<li><i>attName</i> names an attribute for <code>tagName</code> and will 
	be mapped to {@link TypedNode#title()} when reading or writing an element
	</ul>
	 */
	protected static ValueNode newTitleAttributeNames(String defaultName,String[]mappings){
		return new ValueNode("TitleAttributeNames",UNTITLED,
				Objects.join(String.class,mappings,new String[]{
						KEY_TITLE_ATTR_NAME_DEFAULT+"="+
						 (defaultName.trim().equals("")?DataConstants.KEY_TITLE:defaultName)
				}));
	}
	/**
	When reading, should names be included with attributes exposed
	in {@link TypedNode#title()}s?
	@return <code>false</code> by default
	 */
	protected boolean titleAttributeKeyPairs(){
		return false;
	}
	/**
	When reading, pre-process contents of XML text node. 
	@param text from text node
	@return strings to be stored as {@link TypedNode#values()}; 
	storing any non-empty array from an element with attributes will require 
	a valid implementation of {@link #newSegregated(String[])}.
	Default returns {@link #trimmedValueLines(String)}.
	 */
	protected String[]textToValueLines(String text){
		return true?trimmedValueLines(text) 
			:text.trim().equals("")?new String[]{} 
			:new String[]{text};
	}
	/**
	Utility method used by {@link #textToValueLines(String)}. 
	@return for blank text an empty array, otherwise lines split with blanks removed
	 */
	protected static String[]trimmedValueLines(String text){
		text=text.trim();
		return text.equals("")?new String[]{}:text.split("\n\\s*\n?");
	}
	/**
	When writing, enhances readability of XML text. 
	<p>Called from {@link XmlDocRoot#writeToSink(Object)}; default 
	breaks lines after tag ends and attribute pairs. 
	@param raw text returned by {@link XmlDocRoot#newDocumentXml(Document)}
	 */
	protected String prettifyRawXML(String raw){
		return false?raw:replaceAll(raw,true,XmlDocRoot.prettyFixes);
	}
	/**
	When reading, should read exceptions be thrown or handled?
	<p>If handled, an {@link ExceptionNode} is added to the tree.   
	@return <code>true</code> by default
	 */
	protected boolean handleReadExceptions(){
		return true;
	}
}
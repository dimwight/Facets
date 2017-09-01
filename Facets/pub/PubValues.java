package pub;
import static facets.util.tree.DataConstants.*;
import static java.lang.System.*;
import facets.util.app.AppValues;
import java.io.File;
public interface PubValues{
	String 
	FIELD_PUBNUM="PubNum",FIELD_REV="Rev",FIELD_EMPTY="",FIELD_DETAILS="PubDetails",
	TYPE_PUB="pub",TYPE_INDEX="index",
	TYPE_DOC="Document",TYPE_FIELDS="FieldValues",
	TYPE_TEXTS="Texts",TITLE_BODY="Body",
	TYPE_LINK="Link",TYPE_LINKS="Links",
	TYPE_ATTACHMENT="Attachment",TYPE_ATTACHMENTS="Attachments",
	KEY_ATTACHED_SIZE="attachedSize",KEY_MODIFIED="lastModified",
	VIEW_TITLE="PubsView",SEARCH_TITLE="PubSearch",PLUS_TITLE="PubSearchPlus",
	INDEX_FILENAME=SEARCH_TITLE+"."+TYPE_INDEX+(false?EXT_XML:EXT_XML_ZIP),
	INDEX_SHORT_FILENAME=SEARCH_TITLE+"."+TYPE_INDEX+"Short"+(false?EXT_XML:EXT_XML_ZIP),
	TEXTS_DIR="texts",REFS_DIR="refs",
	KEY_USER="userName",KEY_ADMIN="admin",
	userName=getProperty("pvAdmin")!=null?KEY_ADMIN:getProperty("user.name").toLowerCase();
	boolean userView=userName!=KEY_ADMIN,
			searchView=userView&&getProperty("pvUser")==null,
			offNetwork=getProperty("offNetwork")!=null;
	String RMAP=false?"R:":"//uknas01.bkogc.com/R",
			ROOT_TECHPUBS=RMAP+"/VTS Techpubs",ROOT_PDFS=ROOT_TECHPUBS+"/pdf/";
	File 
		VIEW_DIR=new File(ROOT_TECHPUBS,searchView?"pubsearch":"pubsview"),
		INDEX_DIR=new File(ROOT_TECHPUBS,"index"),
		MASTER_DIR=new File(VIEW_DIR,"master"),
		DIR_USER=new File(System.getProperty("user.home"),userView?"pv":AppValues.DIR_DEV);
}

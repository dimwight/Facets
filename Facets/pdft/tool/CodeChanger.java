package pdft.tool;
import static facets.util.Regex.*;
import static java.lang.Math.*;
import static pdft.tool.PdfTool.*;
import facets.util.Debug;
import facets.util.Regex;
import facets.util.Util;
import java.io.File;
import java.io.IOException;
import org.apache.pdfbox.pdmodel.PDPage;
import pdft.PdfCore;
import pdft.PdfCore.PageIterator;
abstract class CodeChanger extends PageIterator{
	private boolean changedPage;
	CodeChanger(PdfCore pdf){
		super(pdf);
	}
	final public boolean pageIterated(PDPage page,int pageAt)throws IOException{
		String codeThen=page.getContents().getInputStreamAsString(),
			codeNow=changeCode(codeThen);
		if(codeNow.equals(codeThen))return false;
		core.writePageCode(page,codeNow);
		changedPage=true;
		trace(": Changed page "+pageAt);
		return false;
	}
	protected abstract String changeCode(String codeThen);
	final void writeChanges(File fileOut)throws IOException{
		iteratePages();
		if(changedPage)core.writePdf(fileOut,true);
		else{
			trace(": No pages changed");
			core.document.close();
		}
	}
	@Override
	protected void traceOutput(String msg){
		System.out.println(getClass().getSimpleName()+msg);
	}
	static CodeChanger newIssueChanger(PdfTool core,final String issue){
		return new CodeChanger(core){
			@Override
			protected String changeCode(String codeThen){
				return codeThen.replaceAll(PdfTool.splitCodeChars("Issue ","") +
						"\\d+","Issue "+issue);
			}
		};
	}
	static CodeChanger newVtsChanger(PdfTool core){
		return new CodeChanger(core){
			@Override
			protected String changeCode(String in){
				String lds="LDS",vts="VTS",out=in;
				for(String codeChars:new String[]{
						splitCodeChars(lds,"\\(?"),
						splitCodeChars(lds,""),
				}){
					String[]finds=finds(in,codeChars);
					if(finds.length==0)continue;
					out=in;
					int codeAt=0;
					for(String find:finds){
						int findAt=in.indexOf(find,codeAt),
							match=find.matches(lds)?0:find.matches("\\(?"+lds)?1:2;
						codeAt=findAt+find.length();
						trace(".setVts: match="+match+" find\t="+findRegion(in,findAt,codeAt));
						String fix=match==0?vts:match==1?"("+vts:replaceAll(find,
									"(\\(\\s*)?LD\\)","$1VT)",
									"(\\(\\s*)?D\\)","$1T)",
									"(\\(\\s*)?L\\)","$1V)");
						out=in.substring(0,findAt)+fix+in.substring(findAt+fix.length());
						String fixRegion=findRegion(out,findAt,codeAt);
						trace(".setVts: match="+match+" fix\t="+fixRegion);
						if(out.length()!=in.length())throw new IllegalStateException(
									"Bad fixRegion="+fixRegion);
						in=out;
					}
				}
				return out;
			}
			private String findRegion(String code,int findAt,int codeAt){
				int around=0;
				return code.substring(max(0,findAt-around),min(codeAt+around,code.length())
					).replaceAll("\\s+"," ");
			}
		};
	}
}
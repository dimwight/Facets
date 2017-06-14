package facets.util;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
/**
Encoding of <code>byte[]</code>s as ASCII text.
 */
public final class ByteStrings{
	/**
	Encapsulates reading of bytes from either a file or code. 
	 */
	public final static class FileBytes extends Tracer{
		private final String path,encoded;
		/**
		Unique constructor defining alternative sources. 
		@param path to a source file
		@param encoded either the encoded source or an empty string to signal use of 
			<code>path</code> instead
		 */
		public FileBytes(String path,String encoded){
			this.path=path;
			this.encoded=encoded;
		}
		/**
		Gets the decoded source bytes. 
		@return decoding of either non-empty <code>encoded</code> construction parameter,
			or newly encoded contents of file referenced by <code>path</code> parameter 
		 */
		public byte[]getBytes(){
			String encoded=this.encoded.equals("")?newEncoded(new File(path))
					:this.encoded;
			return(byte[])Bytes.unpack(decodeString(encoded));
		}
		private String newEncoded(File src){
			byte[]bytes=new byte[(int)src.length()];
			try{
				FileInputStream input=new FileInputStream(src);
				while(input.read(bytes)>=0);
				input.close();
			}
			catch(IOException e){
				throw new RuntimeException(e);
			}
			trace(": bytes=",bytes.length);
			String encoded=encodeBytes(Bytes.pack(bytes),0);
			trace(": encoded length="+encoded.length()+" text=\n"+encoded);
			return encoded;
		}
		@Override
		protected void traceOutput(String msg){
			if(true)super.traceOutput(msg);
		}
	}
	private static abstract class Codec{
		abstract String encode(byte[]bytes);
		abstract byte[]decode(String str);
	}
	/**
	Encodes bytes as ASCII text decodable by {@link #decodeString(String)}.  
	@param in the bytes to encode
	@param breakAt if &gt;0 the interval for inserting line breaks
	@return the encoded string or <code>null</code> if an error occurred when checking
	the encoding.
	 */
	public static String encodeBytes(byte[]in,int breakAt){
		try{
			String encoded=hexDigits.encode(in);
			hexDigits.decode(encoded);
			return breakAt==0?encoded:encoded.replaceAll("(\\w{"+breakAt+"})","$1\n");
		}
		catch(Exception e){
			if(trace)Util.printOut("ByteStrings.encodeBytes e=",e);
			return null;
		}
	}
	/**
	Decodes a string created with {@link #encodeBytes(byte[],int)}. 
	@param in the encoded string, which may contain formatting whitespace
	@return the decoded bytes or <code>null</code> if decoding fails for any reason
	 */
	public static byte[]decodeString(String in){
		try{
			return (false?memString:hexDigits).decode(in.replaceAll("\\s+",""));
		}
		catch(Exception e){
			if(trace)Util.printOut("ByteStrings.decodeString e=",e);;
			return null;
		}
	}
	static final Codec hexDigits=new Codec(){
		@Override
		String encode(byte[]bytes){
			StringBuilder sb=new StringBuilder();
			int byteAt=0;
			for(byte b:bytes){
				int uint=0xff&(b);
				sb.append(""+(uint<0x10?"0":"")
						+Integer.toHexString(uint).toUpperCase());
			}
			return sb.toString();
		}
		@Override
		byte[]decode(String str){
			byte[]bytes=new byte[str.length()/2];
			for(int i=0;i<bytes.length;i++)
				bytes[i]=(byte)(Integer.parseInt(str.substring(i*2,i*2+2),16));
			return bytes;
		}
	};
	static final Codec memString=true?null:new Codec(){
		private final String charset=false?System.getProperty("file.encoding"):"UTF-8";
		@Override
		String encode(byte[]bytes){
			try{
				return new String(bytes,charset);
			}catch(UnsupportedEncodingException e){
				throw new RuntimeException(e);
			}
		}
		@Override
		byte[]decode(String str){
			try{
				return str.getBytes(charset);
			}catch(UnsupportedEncodingException e){
				throw new RuntimeException(e);
			}
		}
	};
	private static final boolean trace=false;
}
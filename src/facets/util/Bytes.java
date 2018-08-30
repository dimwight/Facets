package facets.util;
import static facets.util.Util.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Arrays;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.GZIPOutputStream;
import java.util.zip.Inflater;
/**
Encapsulates (de)serialization to/from <code>byte[]</code>s, 
both simple and with compression. 
 */
public final class Bytes extends Tracer{
	private static final int intBytes=Integer.SIZE/Byte.SIZE,PACK_MIN=512;
	private static final boolean verbose=false;
	protected void traceOutput(String msg){
		if(false)Times.printElapsed(msg);
		else if(false)super.traceOutput(msg);
	}
	private final byte[]packed;
	private Bytes(Serializable src,boolean noPack){
		if(verbose)traceDebug(".pack: src=",src);
		byte[]bytes=Bytes.serialize(src);
		if(verbose)trace(".pack: bytes=",Util.kbs(bytes.length));
		int byteCount=bytes.length;
		byte[]packable=noPack||byteCount<PACK_MIN?bytes:packBytes(bytes);
		boolean notPacked=packable==bytes;
		int packCount=notPacked?-1:byteCount,packLength=packable.length+intBytes;
		packed=Arrays.copyOf(packable,packLength);
		for(int byteAt=packable.length,shift=0;byteAt<packLength;byteAt++,shift+=Byte.SIZE)
			packed[byteAt]=(byte)(packCount>>shift&0xFF);
		trace(".pack: byteCount="+Util.kbs(byteCount)+
				(notPacked?"":(" packed="+Util.kbs(packed.length)+" %="+packPcts(packCount))));
	}
	private byte[]packBytes(byte[]in){
		byte[]out;
		int count;
		if(false){
			ByteArrayOutputStream stream=new ByteArrayOutputStream();
			try{
				GZIPOutputStream gzip=new GZIPOutputStream(stream);
				gzip.write(in);
				gzip.close();
			}catch(IOException e){
				throw new RuntimeException(e);
			}
			out=stream.toByteArray();
			count=out.length;
		}
		else{
			out=new byte[in.length];
			Deflater zip=new Deflater();
			zip.setInput(in);
			zip.finish();
			count=zip.deflate(out);
		}
		trace(".packBytes: in="+Util.kbs(in.length)+" count="+Util.kbs(count));
		return Arrays.copyOf(out,count);
	}
	private Bytes(byte[]packed){
		this.packed=packed;;
	}
	private Serializable unpack(){
		int unpackLength=packed.length-intBytes,packCount=0;
		for(int byteAt=unpackLength,shift=0;byteAt<packed.length;byteAt++,shift+=Byte.SIZE)
			packCount|=(packed[byteAt]<<24)>>>24<<shift;
		boolean wasPacked=packCount>=0;
		if(verbose)trace(".unpack: wasPacked=",wasPacked);
		byte[]unpacked=Arrays.copyOf(packed,unpackLength);
		if(wasPacked)try{
			unpacked=unpackBytes(unpacked,packCount);
		}catch(DataFormatException e){
			throw new RuntimeException(e);
		}
		if(verbose)trace(".unpack: unpacked=",Util.kbs(unpacked.length));
		Serializable deserialized=unpacked.length==0?unpacked
			:deserialize(unpacked);
		trace(".unpack: packed="+Util.kbs(packed.length)+(!wasPacked?""
				:(" unpacked="+Util.kbs(unpacked.length)+" %="+packPcts(packCount))));
		if(verbose)traceDebug(".unpack: deserialized=",deserialized);
		return deserialized;
	}
	private byte[]unpackBytes(byte[]in,int unpackedLength)throws DataFormatException{
		byte[]out=new byte[unpackedLength*11/10];
		Inflater zip=new Inflater();
		zip.setInput(in);
		int count=zip.inflate(out);
		return Arrays.copyOf(out,count);
	}
	private double packPcts(int unpackedLength){
		return sf(unpackedLength<=0?-1:100-(double)packed.length/unpackedLength*100);
	}
	public static byte[]serialize(Serializable src){
	  ByteArrayOutputStream bytes=new ByteArrayOutputStream();
	  try{
			new ObjectOutputStream(bytes).writeObject(src);
		}catch(IOException e){
			throw new RuntimeException(e);
		}
	  return bytes.toByteArray();
	}
	public static Serializable deserialize(byte[]bytes){
		try{
			return(Serializable)new ObjectInputStream(
					new ByteArrayInputStream(bytes)).readObject();
		}catch(IOException e){
			throw new RuntimeException(e);
		}catch(ClassNotFoundException e){
			throw new RuntimeException(e);
		}
	}
	/**
	Converts a {@link Serializable} to compressed bytes.
	@param src will be stored in a compressed format
	@param noPack disables compression 
	@return data which can be passed back to {@link #unpack(byte[])} 
	 */
	public static byte[]pack(Serializable src,boolean noPack){
		return new Bytes(src,noPack).packed;
	}
	/**
	Converts a {@link Serializable} to compressed bytes.
	@param src will be stored in a compressed format
	@return data which can be passed back to {@link #unpack(byte[])} 
	 */
	public static byte[]pack(Serializable src){
		return new Bytes(src,false).packed;
	}
	/**
	Attempts to recreate a {@link Serializable} from the data passed.
	@return a {@link Serializable} or <code>null</code> to signal failure 
	 */
	public static Serializable unpack(byte[]packed){
		try{
			return new Bytes(packed).unpack();
		}catch (Exception e){
			if(false)throw new RuntimeException(e);
			else return null;
		}
	}
}
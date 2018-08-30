package facets.util;
/**
Wraps a string created from a {@link Class}[]. 
 */
final public class TypesKey{
	final public static TypesKey EMPTY=new TypesKey(TypesKey.class);
	public final String keyText;
	public TypesKey(Class...types){
		if(types==null||types.length==0)throw new IllegalArgumentException(
				"Null or empty types");
		else keyText=types[0]==TypesKey.class?"EmptyTypesKey"
				:Util.keySafe(Objects.toString(types,":").replaceAll("class[^A-Z]+",""));
	}
	@Override
	public boolean equals(Object o){
		return keyText.equals(((TypesKey)o).keyText);
	}
	@Override
	public int hashCode(){
		return keyText.hashCode();
	}
	@Override
	public String toString(){
		return keyText;
	}
}
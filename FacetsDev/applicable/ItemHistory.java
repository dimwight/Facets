package applicable;
import facets.core.app.Dialogs;
import facets.core.superficial.SIndexing;
import facets.core.superficial.STarget;
import facets.core.superficial.STrigger;
import facets.core.superficial.TargetCore;
import facets.util.Tracer;
import facets.util.Util;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
public abstract class ItemHistory<T>extends Tracer{
	public static final int TARGET_ITEMS=0,TARGET_RENAME=1;
	public final STarget targets;
	public final List<T>items=new ArrayList();
	private final int maxSize;
	private STrigger retitle;
	private boolean retitled;
	private T unequalItemWithTitle(String proposal,T item){
		for(T check:items)if(check.toString().trim().equals(proposal)
				&&!check.equals(item))return check;
		return null;
	}
	public ItemHistory(int maxSize,final T emptyItem,final Dialogs dialogs,
			String itemsTitle,final String retitleTitle,final String retitleRubric){
		this.maxSize=maxSize;
		retitle=new STrigger(retitleTitle,new STrigger.Coupler(){
			@Override
			public void fired(STrigger t){
				T item=items.get(0);
				String proposal=item.toString(),input;
				if(false)trace(".fired: proposal=",proposal);
				while((input=dialogs.getTextInput(retitleTitle.replaceAll("[.&]",""),
						retitleRubric,proposal,0))!=null&&(input.trim().equals("")
						||input.matches(".*[&|].*")||unequalItemWithTitle(input,item)!=null))
					if(false)trace(".fired: input=",input);
				if(false)trace(".fired: input=",input);
				if(retitled=input!=null)retitleItem(item,input);
			}
		});
		retitle.setLive(false);
		targets=new TargetCore(ItemHistory.class.getSimpleName(),
			new SIndexing(itemsTitle,new SIndexing.Coupler(){
				@Override
				public Object[]getIndexables(){
					List get=new ArrayList(items);
					return get.toArray();
				}
				@Override
				public void indexSet(SIndexing i){
					int pairAt=items.indexOf(i.indexed());
					T item=items.remove(pairAt);
					items.add(0,item);
					itemIndexed(item);
				}
			}),retitle);
		items.add(emptyItem);
	}
	final public void pushItem(T item){
		int count=items.size();
		if(count>maxSize-1)items.remove(count-1);
		T unequalItem=unequalItemWithTitle(item.toString(),item);
		if(unequalItem!=null){
			Util.printOut("ItemHistory.pushItem: removing unequal ",unequalItem);
			items.remove(unequalItem);
		}
		else items.remove(item);
		items.add(0,item);
		retitle.setLive(true);
	}
	protected abstract void itemIndexed(T item);
	protected abstract void retitleItem(T item,String name);
	final public boolean retitle(){
		retitle.fire();
		return retitled;
	}
	final public void setRetitleLive(boolean on){
		retitle.setLive(on);
	}
}
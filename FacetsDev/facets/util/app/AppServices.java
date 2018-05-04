package facets.util.app;
/**
Supplies an {@link AppWatcher} and application {@link ProvidingCache}. 
 */
public interface AppServices{
	AppWatcher coupleAppWatcher(WatcherCoupler coupler);
	void setAppProvidingCache(ProvidingCache cache);
	void warningCritical(String title,Exception e,boolean inOpen);
}
package facets.core.superficial;
import java.io.Serializable;
/**
Connects a facade {@link STarget} to the application. 
<p>While {@link TargetCoupler} itself defines no specific functionality, 
	the appropriate subtypes for each facade {@link facets.core.superficial.STarget} 
</p>
<ul>
	<li> define presentation policy of the {@link STarget} in the surface,</li>
	<li>enable the application to handle the {@link STarget}'s responses 
		to surface events. </li>
</ul>
<p> A {@link TargetCoupler} subtype instance can either be subclassed 
	as required (usually trivially) to meet application-specific requirements, 
	or simply provide default presentation policy and event handling. This 
	approach (rather than subclassing the facade {@link STarget}s themselves) 
	minimises coupling between application and framework classes, with the 
	further advantage that application code can often be simplified by sharing 
	a single coupler between logically linked targets. 
<p>The most fully worked out examples of this approach in Facets are the 
	extensive built-in policy options provided by {@link facets.core.superficial.SIndexing.Coupler}, 
	and the use of {@link facets.core.superficial.SNumeric.Coupler} to supply appropriate 
	{@link facets.util.NumberPolicy} instances to {@link facets.core.superficial.SNumeric}s. 
 
 */
public interface TargetCoupler extends Serializable{
}

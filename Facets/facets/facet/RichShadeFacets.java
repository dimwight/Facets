package facets.facet;
import facets.core.app.MountFacet;
import facets.core.superficial.Notifying;
import facets.core.superficial.SFacet;
import facets.core.superficial.SIndexing;
import facets.core.superficial.SNumeric;
import facets.core.superficial.STarget;
import facets.core.superficial.STargeter;
import facets.core.superficial.SToggling;
import facets.core.superficial.STrigger;
import facets.core.superficial.TargetCore;
import facets.core.superficial.SIndexing.Coupler;
import facets.core.superficial.TargeterCore;
import facets.util.Debug;
import facets.util.NumberPolicy;
import facets.util.shade.Shade;
import facets.util.shade.ShadeSet;
import facets.util.shade.Shaded;
import facets.util.shade.Shades;
final class RichShadeFacets extends FacetFactory {
	public static final int TITLES = 0;
	static final int 
		HUE = 1, 
		SATURATION = 2, 
		BRIGHTNESS = 3,
		SATURATED = 4, 
		BRIGHTEN = 5, 
		RGB_VALUES = 6, 
		RGB = 7;
	final private static int 
		H = Shade.H, 
		S = Shade.S,
		B = Shade.B, 
		R = Shade.R, 
		G = Shade.G;
	private class HSBValues extends SNumeric.Coupler {
		final SNumeric[] numerics;
		public HSBValues(double[] values) {
			numerics = new SNumeric[]{
				new SNumeric("Hue", values[H], this),
				new SNumeric("Saturation", values[S], this),
				new SNumeric("Brightness", values[B], this)
			};
		}
		public NumberPolicy policy(SNumeric n) {
			final boolean forHue = n.title().startsWith("H");
			return new Shades.HSBPolicy(forHue);
		}
		public void valueSet(SNumeric n) {
			double[] values = shaded.shade().valuesHSB();
			values[H] = numerics[H].value();
			values[S] = numerics[S].value();
			values[B] = numerics[B].value();
			shaded.setShade(new Shade(values, ""));
		}
	}
	private class RGBValues extends SNumeric.Coupler {
		final SNumeric[] numerics;
		public RGBValues(int[] values) {
			numerics = new SNumeric[]{
				new SNumeric("Red", values[R], this),
				new SNumeric("Green", values[G], this),
				new SNumeric("Blue", values[B], this)
			};
		}
		public NumberPolicy policy(SNumeric n) {
			return new Shades.RGBPolicy();
		}
		public void valueSet(SNumeric n) {
			int 
				r = (int) numerics[R].value() << 16, 
				g = (int) numerics[G].value() << 8, 
				b = (int) numerics[B].value();
			shaded.setShade(new Shade(r + g + b, ""));
		}
	}
	private final STrigger.Coupler brighteningCoupler = new STrigger.Coupler() {
		public void fired(STrigger t) {
			Shade color = shaded.shade();
			if (t == brighter)
				shaded.setShade(color.brighter());
			else
				shaded.setShade(color.darker());
		}
	};
	private final STrigger 
		brighter = new STrigger(">", brighteningCoupler),
		darker = new STrigger("<", brighteningCoupler);
	private Shaded shaded;
	private final SNumeric.Coupler rgbCoupler = new SNumeric.Coupler() {
		public NumberPolicy policy(SNumeric n) {
			return new NumberPolicy(0, 0xFFFFFF) {
				public int columns() {
					return 5;
				}
				public int format() {
					return FORMAT_HEX;
				}
			};
		}
		public void valueSet(SNumeric n) {
			shaded.setShade(new Shade((int) n.value(), ""));
		}
	};
	private final SToggling.Coupler saturatedCoupler = new SToggling.Coupler() {
		public void stateSet(SToggling t) {
			Shade colorNow = shaded.shade().resaturated(t.isSet());
			shaded.setShade(colorNow);
		}
	};
	private final ShadeSet shades;
	private final FacetFactory.ComboCoupler titlesCoupler = 
			new FacetFactory.ComboCoupler() {
		public boolean indexedTitleEditable(SIndexing i) {
			return shades.isAddedShade((Shade) i.indexed());
		}
		public void indexedTitleEdited(String edit) {
			Shade color = shaded.shade();
			shaded.setShade(new Shade(color.rgb(), edit));
		}
		public void indexSet(SIndexing s) {
			boolean custom = ((Shade) s.indexed()).title()
					.startsWith(Shade.TITLE_CUSTOM);
			if (false && custom)
				return;
			shaded.setShade((Shade) s.indexed());
		}
	};
	public static SIndexing newSortIndexing(final ShadeSet shades) {
		
		//Strings for indexing
		String[] sortNames = {
			"Title",
			"Hue",
			"Saturation",
			"Brightness",
			"Red",
			"Green",
			"Blue",
			"Snap"
		};
		Coupler coupler = new SIndexing.Coupler() {
			public void indexSet(SIndexing i) {
				shades.setSort(i.index());
			}
		};
		return new SIndexing("Sort", sortNames, ShadeSet.SORT_TITLE, coupler);
	}
	/**
	 Unique constructor. 
	 @param shades need not contain the current <code>shade</code> of 
	 <code>colored</code>
	 */
	public RichShadeFacets(FacetFactory core, ShadeSet shades) {
		super(core);
		if ((this.shades = shades)== null)
			throw new IllegalArgumentException("Null colors in " + Debug.info(this));
	}
	/**
	 Creates a panel layout exposing.
	 @param colors must have been created from a targets returned by 
	 {@link #newTargeterFacets(STargeter,STargeter)}
	 @param sorter must have been created from the indexing of 
	 the {@link ShadeSet} passed to the constructor
	 */
	public SFacet[] newTargeterFacets(STargeter colors, STargeter sorter) {
		MountFacet switchMount = switchMount("Use");
		SIndexing switchIndexing = switchMountIndexing(switchMount,
				new SIndexing.Coupler() {
			public String[] newIndexableTitles(SIndexing i) {
				return new String[]{
					"Values", "Shader"
				};
			}
		});
		STargeter 
			elements[] = colors.elements(), 
			titles = elements[TITLES], 
			rgb = elements[RGB], 
			rgbValues = elements[RGB_VALUES], 
			hue = elements[HUE], 
			saturation = elements[SATURATION], 
			brightness = elements[BRIGHTNESS], 
			saturated = elements[SATURATED], 
			brighten = elements[BRIGHTEN];
		if(true)throw new RuntimeException("Not tested in "+this);
		STargeter switcher=TargeterCore.newRetargeted(switchIndexing,true);
		SFacet values = rowPanel(switcher,
				numericSliders(rgbValues,
						90,
						HINT_NUMERIC_FIELDS + HINT_SLIDER_TICKS),
				BREAK,
				spacerTall(5),
				BREAK,
				spacerWide(7),
				numericSliders(hue, 230, HINT_TALL + 
						HINT_SLIDER_FIELDS_TICKS_LABELS),
				BREAK,
				spacerTall(5),
				BREAK,
				numericSliders(saturation,
						120,
						HINT_TALL + HINT_SLIDER_FIELDS_TICKS_LABELS),
				spacerWide(5),
				togglingCheckboxes(saturated, HINT_NONE),
				BREAK,
				spacerTall(5),
				BREAK,
				numericSliders(brightness,
						120,
						HINT_TALL + HINT_SLIDER_FIELDS_TICKS_LABELS),
				spacerWide(5),
				triggerButtons(brighten, HINT_GRID),
				BREAK,
				fill()
			),
			shader = rowPanel(switcher, new SFacet[]{
					colorChooser(rgb)
			}),
			switchables[] ={
				values,
				shader
			},
			panelRows[] ={
				indexingDropdownList(titles, HINT_NONE),
				indexingDropdownList(sorter, HINT_NONE),
				BREAK,
				spacerTall(5),
				BREAK,
				indexingRadioButtons(switcher, HINT_NONE),
				numericFields(rgb, HINT_NONE),
				BREAK,
				spacerTall(5),
				BREAK,
				switchMount
			};

		//Add values and shader to switcher
		switchMount.setFacets(switchables);
		switchIndexing.setIndex(0);
		
		//Create and return titled panel, or just rows
		return false
				? new SFacet[]{
					rowPanel(colors, panelRows)
				}
				: panelRows;
	}
	public STarget newTargets(Shaded shaded){
		Shade 
			shade = shaded.shade(), 
			checked = shades.addShade(shade);
		if (checked != shade)
			shaded.setShade(checked);
		double[] hsb = shade.valuesHSB();
		HSBValues valuesHSB = new HSBValues(hsb);
		brighter.setLive(hsb[B] < 1);
		darker.setLive(hsb[B] > 0);
		STarget 
			titles = new SIndexing("Color",
				shades.shades(),
				shade,
				titlesCoupler), 
			saturated = new SToggling("Saturated",
				shade.isSaturated(),
				saturatedCoupler), 
			brightener = new TargetCore("Brighten",
				new STarget[]{
					darker, brighter
				}), 
			valuesRGB = new TargetCore("RGB Values",
				new RGBValues(shade.valuesRGB()).numerics), 
			rgb = new SNumeric("RGB",
				shade.rgb(),
				rgbCoupler), 
			hue = valuesHSB.numerics[H], 
			saturation = valuesHSB.numerics[S], 
			brightness = valuesHSB.numerics[B];
		
		//Set targets as element children
		return new TargetCore(RichShadeFacets.class.getSimpleName(),
				new STarget[]{
			titles,
			hue,
			saturation,
			brightness,
			saturated,
			brightener,
			valuesRGB,
			rgb
		});
	}
}
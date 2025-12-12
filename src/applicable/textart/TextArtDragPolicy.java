package applicable.textart;

import applicable.textart.TextArtAvatarPolicies.DragStyle;
import facets.core.app.avatar.*;
import facets.facet.FacetFactory;
import facets.util.ItemList;
import facets.util.NumberPolicy;
import facets.util.geom.Angle;
import facets.util.geom.Point;
import facets.util.geom.Vector;

import static facets.util.geom.Angle.toDegrees;
import static facets.util.geom.Angle.toRadians;

/**
 * Drag policies for {@link TextArt} content.
 * <p>{@link TextArtDragPolicy} itself is abstract, providing basic features
 * for use in the concrete subclass instances returned by its class methods:
 * <ul><li>members to be manipulated
 * <li>creation of drop edits
 * </ul>
 */
abstract class TextArtDragPolicy extends DragPolicy {

    //Immutable arrays for use during drag, value indexes
    final TextArtPainters[] linePainters;
    final String[] lineTitles;
    final int[][] lineValueSets;
    static final int AT_X = 0, AT_Y = 1, AT_ANGLE = 2;

    /**
     * Unique constructor called by class methods.
     *
     * @param content to be edited by drag
     * @param p       passed to avatars
     */
    private TextArtDragPolicy(AvatarContent[] content, PainterSource p) {

        //Copy content titles and properties for reference, create avatars
        lineTitles = new String[content.length];
        lineValueSets = new int[content.length][3];
        linePainters = new TextArtPainters[content.length];
        for (int i = 0; i < content.length; i++) {
            TextArt line = (TextArt) content[i];
            lineTitles[i] = line.sourceNode().title();
            lineValueSets[i] = new int[]{line.atX(), line.atY(), line.atAngle()};
            linePainters[i] = new TextArtPainters(line, p);
        }
    }

    /**
     * Implements abstract framework method.
     *
     * @see facets.core.app.avatar.DragPolicy#newDragDropEdits(facets.util.geom.Point, facets.util.geom.Point)
     */
    @Override
    public Object[] newDragDropEdits(Point anchorAt, Point dragAt) {

        //Retrieve and return latest avatar states
        TextArt[] dragStates = new TextArt[linePainters.length];
        for (int i = 0; i < dragStates.length; i++)
            (dragStates[i] = linePainters[i].toTextLine()).sourceNode().setTitle(lineTitles[i]);
        return dragStates;
    }

    /**
     * Creates a shift policy.
     * <p>Called from {@link TextArtAvatarPolicies#dragPolicy(AvatarView, AvatarContent[], Object, PainterSource)}
     *
     * @param content one or more {@link TextArt}s
     * @param p       from {@link FacetFactory}
     * @param xPolicy from {@link TextArtViewable}
     * @param yPolicy from {@link TextArtViewable}
     */
    final static DragPolicy newShiftPolicy(AvatarContent[] content,
                                           final PainterSource p, final NumberPolicy xPolicy,
                                           final NumberPolicy yPolicy) {

        //Return trivial subclass instance implementing framework method for shift
        return new TextArtDragPolicy(content, p) {
            @Override
            public Painter[] newDragPainters(Point anchorAt, Point dragAt) {

                //List for painters
                ItemList<Painter> painters = new ItemList(Painter.class);

                //Update drag lines, get updated painters
                for (int i = 0; i < linePainters.length; i++) {

                    //Calculate raw dragged position
                    int
                            xThen = lineValueSets[i][AT_X],
                            yThen = lineValueSets[i][AT_Y];
                    Point dragged = new Point(xThen, yThen).shifted(dragAt.jumpFrom(anchorAt));

                    //Pass through policies and set avatar state
                    linePainters[i].atX = (int) xPolicy.validValue(xThen, dragged.x());
                    linePainters[i].atY = (int) yPolicy.validValue(yThen, dragged.y());

                    //Add painters for line to list
                    painters.addItems(linePainters[i].newDragPainters(DragStyle.SHIFT));
                }

                //Return contents of list
                return painters.items();
            }
        };
    }

    /**
     * Creates a turn policy.
     * <p>Called from {@link TextArtAvatarPolicies#dragPolicy(AvatarView,
     * AvatarContent[], Object, PainterSource)}
     *
     * @param content     one or more {@link TextArt}s
     * @param p           from {@link FacetFactory}
     * @param anglePolicy from {@link TextArtViewable}
     */
    final static DragPolicy newTurnPolicy(AvatarContent[] content,
                                          final PainterSource p, final NumberPolicy anglePolicy) {

        //Return trivial subclass instance implementing framework method for turn
        return new TextArtDragPolicy(content, p) {
            @Override
            public Painter[] newDragPainters(Point anchorAt, Point dragAt) {

                //Get shift from picked content (always first in array)
                int[] pickValues = lineValueSets[0];
                Vector shift = dragAt.jumpFrom(new Point(pickValues[AT_X], pickValues[AT_Y]));

                //Calculate angle change for whole selection
                double
                        dragDegrees = toDegrees(new Angle(shift.y, shift.x).radians()),
                        leadDegreesThen = pickValues[AT_ANGLE],
                        leadDegreesNow = anglePolicy.validValue(leadDegreesThen, dragDegrees),
                        turnDegrees = leadDegreesNow - leadDegreesThen;

                //List for painters
                ItemList<Painter> painters = new ItemList(Painter.class);

                //Update drag lines, get updated painters
                for (int i = 0; i < linePainters.length; i++) {
                    linePainters[i].atAngle = (int) (lineValueSets[i][AT_ANGLE] + turnDegrees);
                    painters.addItems(linePainters[i].newDragPainters(DragStyle.TURN));
                }

                //Return contents of list
                return painters.items();
            }
        };
    }

    /**
     * Creates resize policy.
     * <p>Called from {@link TextArtAvatarPolicies#dragPolicy(AvatarView,
     * AvatarContent[], Object, PainterSource)}
     *
     * @param content one or more {@link TextArt}s
     * @param p       from {@link FacetFactory}
     */
    final static DragPolicy newSizePolicy(AvatarContent[] content,
                                          PainterSource p) {

        //Return trivial subclass instance implementing framework method for turn
        return new TextArtDragPolicy(content, p) {
            @Override
            public Painter[] newDragPainters(Point anchorAt, Point dragAt) {

                //Get size factor from picked avatar
                TextArtPainters pickLine = linePainters[0];
                Point lineAt = new Point(pickLine.atX, pickLine.atY);
                dragAt.rotate(lineAt, new Angle(toRadians(-pickLine.atAngle)));
                double
                        offset = dragAt.jumpFrom(anchorAt).x,
                        scale = offset / linePainters[0].paintLength();

                //List for painters
                ItemList<Painter> painters = new ItemList(Painter.class);

                //Update drag lines, get updated painters
                for (int i = 0; i < linePainters.length; i++)
                    painters.addItems(linePainters[i].newSizeDragPainters(scale));

                //Return contents of list
                return painters.items();
            }
        };
    }
}
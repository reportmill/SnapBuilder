package snapbuild.app;
import snap.view.*;
import snapbuild.actions.*;

/**
 * A class to hold ViewHpr implementation for standard views.
 */
public class ViewHprs {

/**
 * A ViewHpr for Label.
 */
public static class LabelHpr <T extends Label> extends ParentViewHpr <T> {
    
    /** Configures a new View. */
    public void configure(T aView)  { aView.setText("Label"); }
}

/**
 * A ViewHpr for ButtonBase.
 */
public static class ButtonBaseHpr <T extends ButtonBase> extends ParentViewHpr <T> {
    
    /** Configures a new View. */
    public void configure(T aView)  { aView.setText("Button"); }
}

/**
 * A ViewHpr for TextField.
 */
public static class TextFieldHpr <T extends TextField> extends ParentViewHpr <T> {
    
    /** Configures a new View. */
    public void configure(T aView)  { aView.setMinWidth(100); }
}

/**
 * A ViewHpr for ThumbWheel.
 */
public static class ThumbWheelHpr <T extends ThumbWheel> extends ViewHpr <T> {
    
    /** Configures a new View. */
    public void configure(T aView)  { aView.setType(ThumbWheel.TYPE_RADIAL); aView.setPrefSize(120,16); }
}

/**
 * A ViewHpr for ParentView.
 */
public static class ParentViewHpr <T extends ParentView> extends ViewHpr <T> {
    
}

/**
 * A ViewHpr for ChildView.
 */
public static class ChildViewHpr <T extends ChildView> extends ParentViewHpr <T> {
    
    /** Returns actions for View. */
    public Class[] getActionClasses()
    {
        return new Class[] {
            AddChild.class, AddAfter.class, AddBefore.class,
            SetName.class,
            SetPadding.class, SetSpacing.class,
            SetFill.class, SetBorder.class,
            SetPrefWidth.class, SetPrefHeight.class,
            SetGrowWidth.class, SetGrowHeight.class, SetLeanX.class, SetLeanY.class,
            Delete.class, Duplicate.class };
    }
    
    /** Adds a child view at given index. */
    public boolean addChild(T aView, View aChild, int anIndex)
    {
        aView.addChild(aChild, anIndex);
        return true;
    }
}

/**
 * A ViewHpr for ColView.
 */
public static class ColViewHpr <T extends ColView> extends ChildViewHpr <T> {
    
    /** Configures a new View. */
    public void configure(T aView)
    {
        aView.setPadding(4,4,4,4); aView.setSpacing(4); aView.setGrowHeight(true);
    }
}

/**
 * A ViewHpr for RowView.
 */
public static class RowViewHpr <T extends RowView> extends ChildViewHpr <T> {

    /** Configures a new View. */
    public void configure(T aView)
    {
        aView.setPadding(4,4,4,4); aView.setSpacing(4); aView.setGrowWidth(true);
    }
}
}
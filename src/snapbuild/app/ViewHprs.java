package snapbuild.app;
import snapbuild.actions.*;

/**
 * A class to hold ViewHpr implementation for standard views.
 */
public class ViewHprs {

/**
 * A ViewHpr for ParentView.
 */
public static class ParentViewHpr extends ViewHpr {
    
}

/**
 * A ViewHpr for ChildView.
 */
public static class ChildViewHpr extends ParentViewHpr {
    
    /** Returns actions for View. */
    public Class[] getActionClasses()
    {
        return new Class[] { AddChild.class, AddPeer.class,
            SetPadding.class, SetSpacing.class,
            SetFill.class, SetBorder.class,
            SetPrefWidth.class, SetPrefHeight.class,
            SetGrowWidth.class, SetGrowHeight.class, SetLeanX.class, SetLeanY.class,
            Delete.class, Duplicate.class };
    }
}

}
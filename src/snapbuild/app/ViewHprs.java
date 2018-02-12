package snapbuild.app;
import snap.gfx.*;
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
 * A ViewHpr for ToggleButton.
 */
public static class ToggleButtonHpr <T extends ToggleButton> extends ButtonBaseHpr <T> {
    
    /** Configures a new View. */
    public void configure(T aView)  { aView.setText("Toggle"); }
}

/**
 * A ViewHpr for TextField.
 */
public static class TextFieldHpr <T extends TextField> extends ParentViewHpr <T> {
    
    /** Configures a new View. */
    public void configure(T aView)  { aView.setMinWidth(80); }
    
    /** Configures a new View. */
    public void configureGallery(T aView)
    {
        aView.setColumnCount(14); aView.setPromptText("TextField");
    }
}

/**
 * A ViewHpr for ProgressBar.
 */
public static class ProgressBarHpr <T extends ProgressBar> extends ViewHpr <T> {
    
    /** Configures a new View. */
    public void configure(T aView)  { aView.setPrefSize(110,20); }
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
    
    /** Configures a new View. */
    public void configureGallery(T aView)
    {
        aView.setPadding(8,30,8,30); aView.setBorder(Color.LIGHTGRAY, 1);
        Label label = new Label("ColView"); label.setTextFill(Color.GRAY); label.setFont(Font.Arial11);
        aView.addChild(label);
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
    
    /** Configures a new View. */
    public void configureGallery(T aView)
    {
        aView.setPadding(8,30,8,30); aView.setBorder(Color.LIGHTGRAY, 1);
        Label label = new Label("RowView"); label.setTextFill(Color.GRAY); label.setFont(Font.Arial11);
        aView.addChild(label);
    }
}

/**
 * A ViewHpr for TitleView.
 */
public static class TitleViewHpr <T extends TitleView> extends ParentViewHpr <T> {

    /** Configures a new View. */
    public void configure(T aView)
    {
        aView.setTitle("Title");
    }
    
    /** Configures a new View. */
    public void configureGallery(T aView)
    {
        aView.setTitle("Title"); aView.setFont(Font.Arial10);
        Label label = new Label("TitleView"); label.setPadding(0,40,8,40); label.setTextFill(Color.GRAY);
        aView.setContent(label);
    }
}

/**
 * A ViewHpr for TabView.
 */
public static class TabViewHpr <T extends TabView> extends ParentViewHpr <T> {

    /** Configures a new View. */
    public void configure(T aView)
    {
        aView.addTab("One", new BoxView());
    }
    
    /** Configures a new View. */
    public void configureGallery(T aView)
    {
        aView.setFont(Font.Arial10);
        Label label = new Label("TabView"); label.setPadding(4,40,4,40); label.setTextFill(Color.GRAY);
        aView.addTab("  One  ", label);
        aView.addTab("  Two  ", new BoxView());
    }
}

/**
 * A ViewHpr for ScrollView.
 */
public static class ScrollViewHpr <T extends ScrollView> extends ParentViewHpr <T> {

    /** Configures a new View. */
    public void configure(T aView)  { }
    
    /** Configures a new View. */
    public void configureGallery(T aView)
    {
        aView.setPrefSize(140,40); aView.setFont(Font.Arial10);
        aView.setShowVBar(true); aView.setShowHBar(true); aView.setBarSize(10);
        Label label = new Label("ScrollView"); label.setPadding(8,35,8,45); label.setTextFill(Color.GRAY);
        label.setAlign(Pos.TOP_LEFT);
        aView.setContent(label); label.setFill(Color.WHITE); label.setMinSize(600,600);
    }
}

/**
 * A ViewHpr for SplitView.
 */
public static class SplitViewHpr <T extends SplitView> extends ParentViewHpr <T> {

    /** Configures a new View. */
    public void configure(T aView)  { }
    
    /** Configures a new View. */
    public void configureGallery(T aView)
    {
        aView.setFont(Font.Arial10); aView.setBorder(new Border.BevelBorder(0));
        Label label0 = new Label("Split"); label0.setPadding(10,20,10,20); label0.setTextFill(Color.GRAY);
        Label label1 = new Label("View"); label1.setPadding(10,20,10,20); label1.setTextFill(Color.GRAY);
        aView.setItems(label0,label1);
    }
}

}
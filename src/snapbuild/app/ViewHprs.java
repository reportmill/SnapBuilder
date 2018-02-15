package snapbuild.app;
import java.util.function.Consumer;
import snap.gfx.*;
import snap.view.*;

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
 * A ViewHpr for Button.
 */
public static class ButtonHpr <T extends Button> extends ButtonBaseHpr <T> {
    
    /** Configures a new View. */
    public void configure(T aView)  { aView.setText("Button"); aView.setPadding(2,2,2,2); }
}

/**
 * A ViewHpr for ToggleButton.
 */
public static class ToggleButtonHpr <T extends ToggleButton> extends ButtonBaseHpr <T> {
    
    /** Configures a new View. */
    public void configure(T aView)  { aView.setText("ToggleButton"); aView.setPadding(2,2,2,2); }
}

/**
 * A ViewHpr for CheckBox.
 */
public static class CheckBoxHpr <T extends CheckBox> extends ToggleButtonHpr <T> {
    
    /** Configures a new View. */
    public void configure(T aView)  { aView.setText("CheckBox"); }
}

/**
 * A ViewHpr for RadioButton.
 */
public static class RadioButtonHpr <T extends RadioButton> extends ToggleButtonHpr <T> {
    
    /** Configures a new View. */
    public void configure(T aView)  { aView.setText("RadioButton"); }
}

/**
 * A ViewHpr for TextField.
 */
public static class TextFieldHpr <T extends TextField> extends ParentViewHpr <T> {
    
    /** Configures a new View. */
    public void configure(T aView)  { } //aView.setMinWidth(80);
    
    /** Configures a new View. */
    public void configureGallery(T aView)
    {
        aView.setColCount(12); aView.setPromptText("TextField");
    }
}

/**
 * A ViewHpr for ComboBox.
 */
public static class ComboBoxHpr <T extends ComboBox> extends ParentViewHpr <T> {
    
    /** Configures a new View. */
    public void configure(T aView)  { aView.setMinSize(80,22); }
    
    /** Configures a new View. */
    public void configureGallery(T aView)
    {
        aView.setMinSize(80,22);
        aView.setItems("ComboBox" ); aView.setSelectedItem("ComboBox");
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
 * A ViewHpr for Spinner.
 */
public static class SpinnerHpr <T extends Spinner> extends ViewHpr <T> {
    
    /** Configures a new View. */
    public void configure(T aView)
    {
        aView.setPrefSize(120,22);
        aView.getTextField().setPromptText("Spinner");
    }
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
    
    /** Adds a child view at given index. */
    public boolean addChild(T aView, View aChild, int anIndex)
    {
        aView.addChild(aChild, anIndex);
        return true;
    }
    
    /** Adds a view relative to given view. */
    public boolean addView(T aView, View aView2)
    {
        aView.addChild(aView2);
        return true;
    }
}

/**
 * A ViewHpr for TextView.
 */
public static class TextViewHpr <T extends TextView> extends ParentViewHpr <T> {

    /** Configures a new View. */
    public void configure(T aView)  { aView.setMinSize(120,60); }
    
    /** Configures a new View. */
    public void configureGallery(T aView)
    {
        aView.setFont(Font.Arial10);
        aView.setMinSize(140,40); aView.setMaxSize(140,40);
        aView.getScrollView().setShowVBar(true); aView.getScrollView().setBarSize(10);
        aView.getTextArea().setMinHeight(300);
        
        aView.setText("TextView"); aView.setTextFill(Color.GRAY);
    }
}

/**
 * A ViewHpr for TitleView.
 */
public static class TitleViewHpr <T extends TitleView> extends ParentViewHpr <T> {

    /** Configures a new View. */
    public void configure(T aView)
    {
        aView.setMinSize(120,60);
        aView.setTitle("Title");
    }
    
    /** Configures a new View. */
    public void configureGallery(T aView)
    {
        aView.setTitle("Title"); aView.setFont(Font.Arial10);
        Label label = new Label("TitleView"); label.setPadding(0,40,8,40); label.setTextFill(Color.GRAY);
        aView.setContent(label);
    }
    
    /** Adds a view relative to given view. */
    public boolean addView(T aView, View aView2)
    {
        aView.setContent(aView2);
        return true;
    }
}

/**
 * A ViewHpr for TabView.
 */
public static class TabViewHpr <T extends TabView> extends ParentViewHpr <T> {

    /** Configures a new View. */
    public void configure(T aView)
    {
        aView.setMinSize(120,60);
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
    
    /** Adds a view relative to given view. */
    public boolean addView(T aView, View aView2)
    {
        int c = aView.getTabCount()-1;
        if(c>=0 && aView.getTabContent(c) instanceof Label)
            aView.setTabContent(aView2, c);
        else aView.addTab("New Tab", aView2);
        return true;
    }
}

/**
 * A ViewHpr for ScrollView.
 */
public static class ScrollViewHpr <T extends ScrollView> extends ParentViewHpr <T> {

    /** Configures a new View. */
    public void configure(T aView)  { aView.setMinSize(120,60); }
    
    /** Configures a new View. */
    public void configureGallery(T aView)
    {
        aView.setPrefSize(140,40); aView.setFont(Font.Arial10);
        aView.setShowVBar(true); aView.setShowHBar(true); aView.setBarSize(10);
        Label label = new Label("ScrollView"); label.setPadding(8,35,8,45); label.setTextFill(Color.GRAY);
        label.setAlign(Pos.TOP_LEFT);
        aView.setContent(label); label.setFill(Color.WHITE); label.setMinSize(600,600);
    }
    
    /** Adds a view relative to given view. */
    public boolean addView(T aView, View aView2)
    {
        aView.setContent(aView2);
        return true;
    }
}

/**
 * A ViewHpr for SplitView.
 */
public static class SplitViewHpr <T extends SplitView> extends ParentViewHpr <T> {

    /** Configures a new View. */
    public void configure(T aView)  { aView.setMinSize(120,60); }
    
    /** Configures a new View. */
    public void configureGallery(T aView)
    {
        aView.setFont(Font.Arial10); aView.setBorder(new Border.BevelBorder(0));
        Label label0 = new Label("Split"); label0.setPadding(10,20,10,20); label0.setTextFill(Color.GRAY);
        Label label1 = new Label("View"); label1.setPadding(10,20,10,20); label1.setTextFill(Color.GRAY);
        aView.setItems(label0,label1);
    }
    
    /** Adds a view relative to given view. */
    public boolean addView(T aView, View aView2)
    {
        aView.addItem(aView2);
        return true;
    }
}

/**
 * A ViewHpr for ListView.
 */
public static class ListViewHpr <T extends ListView> extends ParentViewHpr <T> {

    /** Configures a new View. */
    public void configure(T aView)  { aView.setMinSize(120,60); }
    
    /** Configures a new View. */
    public void configureGallery(T aView)
    {
        aView.setFont(Font.Arial10);
        aView.setMinWidth(120);
        aView.getScrollView().setShowVBar(true); aView.getScrollView().setBarSize(10);
        
        aView.setItems("ListView", "Item two", "Item three");
        aView.setCellConfigure(c -> configureCell(c));
    }
    
    void configureCell(Object lc)  { ((ListCell)lc).setTextFill(Color.GRAY); }
}

/**
 * A ViewHpr for TableView.
 */
public static class TableViewHpr <T extends TableView> extends ParentViewHpr <T> {

    /** Configures a new View. */
    public void configure(T aView)  { aView.setMinSize(100,60); }
    
    /** Configures a new View. */
    public void configureGallery(T aView)
    {
        aView.setFont(Font.Arial10);
        aView.setRowHeight(17); aView.setPrefWidth(120);
        
        TableCol col = new TableCol();
        aView.addCol(col); col.setGrowWidth(true);
        aView.getScrollView().setShowVBar(true); aView.getScrollView().setBarSize(10);
        
        aView.setItems("TableView", "Item two", "Item three");
        aView.setCellConfigure(c -> configureCell(c));
    }
    
    void configureCell(Object lc)  { ((ListCell)lc).setTextFill(Color.GRAY); }
}

/**
 * A ViewHpr for TreeView.
 */
public static class TreeViewHpr <T extends TreeView> extends ParentViewHpr <T> {

    /** Configures a new View. */
    public void configure(T aView)  { aView.setMinSize(100,60); }
    
    /** Configures a new View. */
    public void configureGallery(T aView)
    {
        aView.setFont(Font.Arial10);
        aView.setRowHeight(15); aView.setPrefWidth(120);
        
        aView.getScrollView().setShowVBar(true); aView.getScrollView().setBarSize(10);
        
        aView.setItems("TreeView");
        aView.setResolver(new TR());
        aView.setCellConfigure(c -> configureCell(c));
        aView.expandAll();
    }
    
    void configureCell(Object lc)  { ((ListCell)lc).setTextFill(Color.GRAY); }
    
    private static class TR extends TreeResolver <String> {
        
        /** Returns the parent of given item. */
        public String getParent(String anItem)  { return null; }
    
        /** Whether given object is a parent (has children). */
        public boolean isParent(String anItem)  { return anItem.equals("TreeView"); }

        /** Returns the children. */
        public String[] getChildren(String aParent)  { return new String[] { "     Item two", "     Item three" }; }
    }
}

/**
 * A ViewHpr for BrowserView.
 */
public static class BrowserViewHpr <T extends BrowserView> extends ParentViewHpr <T> {

    /** Configures a new View. */
    public void configure(T aView)  { aView.setMinSize(120,60); }
    
    /** Configures a new View. */
    public void configureGallery(T aView)
    {
        aView.setFont(Font.Arial10);
        aView.setRowHeight(15); aView.setPrefSize(160,50);
        
        aView.setResolver(new BR());
        aView.setItems("BrowserView");
        aView.setCellConfigure((Consumer <ListCell>)c -> configureCell(c));
        aView.setSelectedItem("BrowserView");
        
        aView.getScrollView().setShowHBar(true); aView.getScrollView().setBarSize(10);
        aView.getCol(0).getScrollView().setShowVBar(true); aView.getCol(0).getScrollView().setBarSize(10);
        aView.getCol(1).getScrollView().setShowVBar(true); aView.getCol(1).getScrollView().setBarSize(10);
    }
    
    void configureCell(ListCell lc)
    {
        lc.setTextFill(Color.GRAY);
        if(lc.isSelected()) { lc.setFill(Color.GRAY); lc.setTextFill(Color.WHITE); }
    }
    
    private static class BR extends TreeResolver <String> {
        
        /** Returns the parent of given item. */
        public String getParent(String anItem)  { return null; }
    
        /** Whether given object is a parent (has children). */
        public boolean isParent(String anItem)  { return anItem.equals("BrowserView"); }

        /** Returns the children. */
        public String[] getChildren(String aParent)  { return new String[] { "Item two", "Item three" }; }
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
 * A ViewHpr for BorderView.
 */
public static class BorderViewHpr <T extends BorderView> extends ParentViewHpr <T> {

    /** Configures a new View. */
    public void configure(T aView)  { aView.setMinSize(120,60); }
    
    /** Configures a new View. */
    public void configureGallery(T aView)
    {
        aView.setBorder(Color.LIGHTGRAY, 1);
        aView.setFont(Font.Arial10.deriveFont(7));
        
        Label label = new Label("BorderView"); label.setTextFill(Color.GRAY); label.setFont(Font.Arial11);
        label.setFont(Font.Arial11); label.setPadding(5,5,5,5); label.setBorder(Color.LIGHTGRAY,1);
        aView.setCenter(label);
        
        Label tl = new Label("top"); aView.setTop(tl);
        Label bl = new Label("bottom"); aView.setBottom(bl);
        Label ll = new Label("left"); aView.setLeft(ll);
        Label rl = new Label("right"); aView.setRight(rl);
        
        Color c = Color.GRAY; Insets i = new Insets(3);
        for(Label lb : new Label[] { tl,bl,ll,rl }) {
             lb.setAlign(Pos.CENTER); lb.setTextFill(c); lb.setPadding(i); }
    }
    
    /** Adds a view relative to given view. */
    public boolean addView(T aView, View aView2)
    {
        aView.setCenter(aView2);
        return true;
    }
}

}
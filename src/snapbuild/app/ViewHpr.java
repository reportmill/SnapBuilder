package snapbuild.app;
import java.util.*;

import snap.gfx.Color;
import snap.view.*;
import snapbuild.app.ViewHprs.*;

/**
 * A class to provide extra functionality for specific classes.
 */
public class ViewHpr<T extends View> {

    // A map of class to helper instance
    static Map<Class, ViewHpr> _hprs = new HashMap();

    /**
     * Configures a new View.
     */
    public void configure(T aView)
    {
        if (aView.getClass() == View.class) aView.setMinSize(120, 40);
    }

    /**
     * Configures a new View for Gallery.
     */
    public void configureGallery(T aView)
    {
        configure(aView);
    }

    /**
     * Returns whether view wants to add given view. E.g.: Empty TitleView, empty ScrollView, TabView or SplitView.
     */
    public boolean wantsView(T aView, View aView2)
    {
        return false;
    }

    /**
     * Sets the text color.
     */
    public void setTextColor(T aView, Color aColor)
    {
    }

    /**
     * Returns a ViewHpr subclass for given class.
     */
    public static ViewHpr getHpr(Object anObj)
    {
        if (anObj == null) return null;
        Class cls = anObj instanceof Class ? (Class) anObj : anObj.getClass();

        ViewHpr hpr = _hprs.get(cls);
        if (hpr == null) _hprs.put(cls, hpr = createHpr(cls));
        return hpr;
    }

    /**
     * Creates a ViewHpr subclass for given class.
     */
    public static ViewHpr createHpr(Class aCls)
    {
        if (aCls == ArrowView.class) return new ArrowViewHpr();
        if (aCls == Button.class) return new ButtonHpr();
        if (aCls == ButtonBase.class) return new ButtonBaseHpr();
        if (aCls == BrowserView.class) return new BrowserViewHpr();
        if (aCls == BorderView.class) return new BorderViewHpr();
        if (aCls == BoxView.class) return new BoxViewHpr();
        if (aCls == ChildView.class) return new ChildViewHpr();
        if (aCls == CheckBox.class) return new CheckBoxHpr();
        if (aCls == ColView.class) return new ColViewHpr();
        if (aCls == ComboBox.class) return new ComboBoxHpr();
        if (aCls == Label.class) return new LabelHpr();
        if (aCls == ListView.class) return new ListViewHpr();
        if (aCls == ParentView.class) return new ParentViewHpr();
        if (aCls == ProgressBar.class) return new ProgressBarHpr();
        if (aCls == RadioButton.class) return new RadioButtonHpr();
        if (aCls == RowView.class) return new RowViewHpr();
        if (aCls == ScrollView.class) return new ScrollViewHpr();
        if (aCls == Spinner.class) return new SpinnerHpr();
        if (aCls == SplitView.class) return new SplitViewHpr();
        if (aCls == TabView.class) return new TabViewHpr();
        if (aCls == TableView.class) return new TableViewHpr();
        if (aCls == TextField.class) return new TextFieldHpr();
        if (aCls == TextView.class) return new TextViewHpr();
        if (aCls == ThumbWheel.class) return new ThumbWheelHpr();
        if (aCls == TitleView.class) return new TitleViewHpr();
        if (aCls == TreeView.class) return new TreeViewHpr();
        if (aCls == ToggleButton.class) return new ToggleButtonHpr();
        if (aCls == View.class) return new ViewHpr();
        return createHpr(aCls.getSuperclass());
    }

}
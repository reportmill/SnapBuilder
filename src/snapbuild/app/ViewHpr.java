package snapbuild.app;
import java.util.*;
import snap.view.*;
import snapbuild.app.ViewHprs.*;
import snapbuild.actions.*;

/**
 * A class to provide extra functionality for specific classes.
 */
public class ViewHpr <T extends View> {
    
    // The Array of actions
    Action      _actions[];
    
    // A map of class to helper instance
    static Map <Class,ViewHpr>   _hprs = new HashMap();

/**
 * Returns actions for View for given category.
 */
public Action[] getActions(Action.Type aType)
{
    List <Action> list = new ArrayList();
    for(Action a : getActions()) if(isType(aType, a.getType())) list.add(a);
    return list.toArray(new Action[list.size()]);
}

boolean isType(Action.Type t0, Action.Type t1)
{
    if(t0==t1) return true;
    if(t0==Action.Type.Prop && t1!=Action.Type.Child) return true;
    return false;
}

/**
 * Returns actions for View.
 */
public Action[] getActions()
{
    if(_actions!=null) return _actions;
    Class <? extends Action> classes[] = getActionClasses();
    Action acts[] = new Action[classes.length];
    for(int i=0;i<classes.length;i++) {
         try { acts[i] = classes[i].newInstance(); }
         catch(IllegalAccessException e) { throw new RuntimeException(e); }
         catch(InstantiationException e) { throw new RuntimeException(e); }
    }
    return _actions = acts;
}

/**
 * Returns actions for View.
 */
public Class[] getActionClasses()
{
    return new Class[] {
        AddAfter.class, AddBefore.class,
        SetFill.class, SetBorder.class, SetText.class,
        SetPrefWidth.class, SetPrefHeight.class,
        SetGrowWidth.class, SetGrowHeight.class, SetLeanX.class, SetLeanY.class,
        Delete.class, Duplicate.class };
}

/**
 * Configures a new View.
 */
public void configure(T aView)  { }

/**
 * Adds a child view at given index.
 */
public boolean addChild(T aView, int anIndex)  { ViewUtils.beep(); return false; }

/**
 * Returns a ViewHpr subclass for given class.
 */
public static ViewHpr getHpr(Object anObj)
{
    if(anObj==null) return null;
    Class cls = anObj instanceof Class? (Class)anObj : anObj.getClass();
    
    ViewHpr hpr = _hprs.get(cls);
    if(hpr==null) _hprs.put(cls, hpr=createHpr(cls));
    return hpr;
}

/**
 * Creates a ViewHpr subclass for given class.
 */
public static ViewHpr createHpr(Class aCls)
{
    if(aCls==View.class) return new ViewHpr();
    if(aCls==Label.class) return new LabelHpr();
    if(aCls==ButtonBase.class) return new ButtonBaseHpr();
    if(aCls==TextField.class) return new TextFieldHpr();
    if(aCls==ThumbWheel.class) return new ThumbWheelHpr();
    if(aCls==ParentView.class) return new ParentViewHpr();
    if(aCls==ChildView.class) return new ChildViewHpr();
    if(aCls==ColView.class) return new ColViewHpr();
    if(aCls==RowView.class) return new RowViewHpr();
    return createHpr(aCls.getSuperclass());
}

}
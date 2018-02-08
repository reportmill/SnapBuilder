package snapbuild.app;
import java.util.*;
import snap.view.*;
import snapbuild.app.ViewHprs.*;
import snapbuild.actions.*;

/**
 * A class to provide extra functionality for specific classes.
 */
public class ViewHpr {
    
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
    return new Class[] { AddPeer.class,
        SetFill.class, SetBorder.class,
        SetPrefWidth.class, SetPrefHeight.class,
        SetGrowWidth.class, SetGrowHeight.class, SetLeanX.class, SetLeanY.class,
        Delete.class, Duplicate.class };
}

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
    if(aCls==ChildView.class) return new ChildViewHpr();
    return createHpr(aCls.getSuperclass());
}

}
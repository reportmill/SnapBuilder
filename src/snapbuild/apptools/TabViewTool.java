package snapbuild.apptools;
import snap.view.*;
import snap.viewx.DialogBox;
import snapbuild.app.ViewTool;

/**
 * A class to manage UI for editing TabView.
 */
public class TabViewTool <T extends TabView> extends ViewTool <T> {

/**
 * Reset UI.
 */
protected void resetUI()
{
    // Get selected view
    T selView = getSelView();
    int tc = selView.getTabCount();
    
    // Get names
    String names[] = new String[tc]; for(int i=0;i<tc;i++) names[i] = selView.getTabTitle(i);

    // Update TabList
    setViewItems("TabList", names);
    setViewSelectedIndex("TabList", selView.getSelectedIndex());
}

/**
 * Respond to UI.
 */
protected void respondUI(ViewEvent anEvent)
{
    // Get selected view
    T selView = getSelView();
    
    // Handle AddButton
    if(anEvent.equals("AddButton")) {
        String val = DialogBox.showInputDialog(getEditor(), "Add Tab", "Enter Tab Title:", "Tab"); if(val==null) return;
        selView.addTab(val, new Label("Tab Content"));
    }
    
    // Handle RemoveButton
    if(anEvent.equals("RemoveButton")) {
        int ind = anEvent.getSelectedIndex();
        selView.removeTab(ind);
    }

    // Handle TabList
    if(anEvent.equals("TabList"))
        selView.setSelectedIndex(anEvent.getSelectedIndex());
}

}
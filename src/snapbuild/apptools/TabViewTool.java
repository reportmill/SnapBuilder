package snapbuild.apptools;
import snap.view.*;
import snap.viewx.DialogBox;
import snapbuild.app.ViewTool;

/**
 * A class to manage UI for editing TabView.
 */
public class TabViewTool<T extends TabView> extends ViewTool<T> {

    /**
     * Reset UI.
     */
    protected void resetUI()
    {
        // Get selected view
        T tabView = getSelView();
        int tabCount = tabView.getTabCount();

        // Get names
        String[] names = new String[tabCount];
        for (int i = 0; i < tabCount; i++)
            names[i] = tabView.getTab(i).getTitle();

        // Update TabList
        setViewItems("TabList", names);
        setViewSelIndex("TabList", tabView.getSelIndex());
    }

    /**
     * Respond to UI.
     */
    protected void respondUI(ViewEvent anEvent)
    {
        // Get selected view
        T tabView = getSelView();

        // Handle AddButton
        if (anEvent.equals("AddButton")) {
            String val = DialogBox.showInputDialog(getEditor(), "Add Tab", "Enter Tab Title:", "Tab");
            if (val == null) return;
            tabView.addTab(val, new Label("Tab Content"));
        }

        // Handle RemoveButton
        if (anEvent.equals("RemoveButton")) {
            int ind = anEvent.getSelIndex();
            tabView.removeTab(ind);
        }

        // Handle TabList
        if (anEvent.equals("TabList"))
            tabView.setSelIndex(anEvent.getSelIndex());
    }

}
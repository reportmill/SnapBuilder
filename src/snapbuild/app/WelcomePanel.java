package snapbuild.app;
import snap.util.*;
import snap.view.*;
import snap.web.RecentFiles;
import snap.web.*;

/**
 * An implementation of a panel to manage/open user Snap sites (projects).
 */
public class WelcomePanel extends ViewOwner {

    // The selected file
    private WebFile _selFile;

    // Whether welcome panel should exit on hide
    private boolean _exit;

    // The Runnable to be called when app quits
    private Runnable _onQuit;

    // The RecentFiles
    private WebFile[] _recentFiles;

    // The WelcomePanelAnim
    private WelcomePanelAnim _welcomePanelAnim;

    // The shared instance
    private static WelcomePanel _shared;

    /**
     * Constructor.
     */
    private WelcomePanel()
    {
        super();
        _welcomePanelAnim = new WelcomePanelAnim();
    }

    /**
     * Returns the shared instance.
     */
    public static WelcomePanel getShared()
    {
        if (_shared != null) return _shared;
        return _shared = new WelcomePanel();
    }

    /**
     * Shows the welcome panel.
     */
    public void showPanel()
    {
        getUI(); // This is bogus - if this isn't called, Window node get reset
        getWindow().setVisible(true); //getTimeline().play();
        resetLater();
    }

    /**
     * Hides the welcome panel.
     */
    public void hide()
    {
        // Hide window and stop animation
        getWindow().setVisible(false); //getTimeline().stop();

        // Write current list of sites, flush prefs and mayb exit
        //writeSites();         // Write data file for open/selected sites
        Prefs.getDefaultPrefs().flush();    // Flush preferences
        if (_exit) quitApp(); // If exit requested, quit app
    }

    /**
     * Returns the selected file.
     */
    public WebFile getSelFile()
    {
        return _selFile;
    }

    /**
     * Sets the selected file.
     */
    public void setSelFile(WebFile aFile)
    {
        _selFile = aFile;
    }

    /**
     * Returns the Runnable to be called to quit app.
     */
    public Runnable getOnQuit()
    {
        return _onQuit;
    }

    /**
     * Sets the Runnable to be called to quit app.
     */
    public void setOnQuit(Runnable aRunnable)
    {
        _onQuit = aRunnable;
    }

    /**
     * Called to quit app.
     */
    public void quitApp()
    {
        _onQuit.run();
    }

    /**
     * Initialize UI panel.
     */
    protected void initUI()
    {
        // Add WelcomePaneAnim view
        View animView = _welcomePanelAnim.getUI();
        getUI(ChildView.class).addChild(animView, 0);
        animView.playAnimDeep();

        // Configure SitesTable
        TableView<WebFile> sitesTable = getView("SitesTable", TableView.class);
        sitesTable.setRowHeight(24);
        sitesTable.getCol(0).setItemTextFunction(i -> i.getName());

        // Enable SitesTable MouseReleased
        WebFile[] recentFiles = getRecentFiles();
        if (recentFiles.length > 0)
            _selFile = recentFiles[0];
        sitesTable.addEventHandler(this::handleSitesTableMouseRelease, MouseRelease);

        // Hide ProgressBar
        getView("ProgressBar").setVisible(false);

        // Configure Window: Add WindowListener to indicate app should exit when close button clicked
        WindowView window = getWindow();
        window.setTitle("Welcome");
        window.setResizable(false);
        window.addEventHandler(e -> { _exit = true; hide(); }, WinClose);
        getView("OpenButton", Button.class).setDefaultButton(true);
    }

    /**
     * Resets UI.
     */
    public void resetUI()
    {
        setViewEnabled("OpenButton", getSelFile() != null);
        setViewItems("SitesTable", getRecentFiles());
        setViewSelItem("SitesTable", getSelFile());
    }

    /**
     * Responds to UI changes.
     */
    public void respondUI(ViewEvent anEvent)
    {
        // Handle SamplesButton
        if (anEvent.equals("SamplesButton"))
            openSamples();

        // Handle SitesTable
        if (anEvent.equals("SitesTable"))
            setSelFile((WebFile) anEvent.getSelItem());

        // Handle NewButton
        if (anEvent.equals("NewButton")) {
            newFile();
        }

        // Handle OpenPanelButton
        if (anEvent.equals("OpenPanelButton"))
            showOpenPanel();

        // Handle OpenButton or SitesTable double-click
        if (anEvent.equals("OpenButton") || anEvent.equals("SitesTable") && anEvent.getClickCount() > 1) {
            WebFile file = (WebFile) getViewSelItem("SitesTable");
            openFile(file);
        }

        // Handle QuitButton
        if (anEvent.equals("QuitButton")) {
            _exit = true;
            hide();
        }
    }

    private void handleSitesTableMouseRelease(ViewEvent anEvent)
    {
        if (anEvent.getClickCount() > 1) {
            TableView<WebFile> sitesTable = getView("SitesTable", TableView.class);
            WebFile file = sitesTable.getSelItem();
            openFile(file);
        }
    }

    /**
     * Creates a new file.
     */
    protected void newFile()
    {
        EditorPane epane = new EditorPane().newDocument();
        epane.setWindowVisible(true);
        hide();
    }

    /**
     * Opens the Samples.
     */
    protected void openSamples()
    {
        EditorPane editorPane = new EditorPane().newDocument();
        editorPane.setWindowVisible(true);
        hide();
        runDelayed(() -> editorPane.showSamples(), 300);
    }

    /**
     * Runs the open panel.
     */
    public void showOpenPanel()
    {
        // Have editor run open panel (if no document opened, just return)
        EditorPane editorPane = new EditorPane().showOpenPanel(getUI());
        if (editorPane == null)
            return;

        // Make editor window visible and hide welcome panel
        editorPane.setWindowVisible(true);
        hide();

        // Add URL to RecentFiles
        WebURL sourceURL = editorPane.getSourceURL();
        RecentFiles.addURL(sourceURL);
    }

    /**
     * Opens selected file.
     */
    public void openFile(WebFile snapFile)
    {
        // Have editor run open panel (if no document opened, just return)
        EditorPane editorPane = new EditorPane().openEditorForFile(snapFile);
        if (editorPane == null)
            return;

        // Make editor window visible and hide welcome panel
        editorPane.setWindowVisible(true);
        hide();

        // Add URL to RecentFiles
        WebURL sourceURL = editorPane.getSourceURL();
        RecentFiles.addURL(sourceURL);
    }

    /**
     * Returns the list of the recent documents as a list of strings.
     */
    public WebFile[] getRecentFiles()
    {
        // If already set, just return
        if (_recentFiles != null) return _recentFiles;

        WebFile[] recentFiles = RecentFiles.getFiles();
        return _recentFiles = recentFiles;
    }
}
package snapbuild.app;
import java.util.*;
import snap.util.*;
import snap.view.*;
import snap.viewx.DialogBox;
import snap.viewx.RecentFiles;
import snap.web.*;

/**
 * An implementation of a panel to manage/open user Snap sites (projects).
 */
public class WelcomePanel extends ViewOwner {

    // Whether file system is cloud
    private boolean  _isCloud;

    // The cloud email account
    private String  _email;

    // The selected file
    private WebFile  _selFile;
    
    // Whether welcome panel should exit on hide
    private boolean  _exit;
    
    // The Runnable to be called when app quits
    private Runnable  _onQuit;

    // The RecentFiles
    private List<WebFile>  _recentFiles;

    // The shared instance
    private static WelcomePanel  _shared;

    // Constants
    private static final String FILE_SYSTEM = "FileSystem";
    private static final String FILE_SYSTEM_LOCAL = "FileSystemLocal";
    private static final String FILE_SYSTEM_CLOUD = "FileSystemCloud";
    private static final String USER_EMAIL = "UserEmail";

    /**
     * Constructor.
     */
    private WelcomePanel()
    {
        // Get FileSystem
        String fileSys = Prefs.get().getString(FILE_SYSTEM);
        _isCloud = fileSys!=null && fileSys.equals(FILE_SYSTEM_CLOUD);

        // Get Email
        _email = Prefs.get().getString(USER_EMAIL);
    }

    /**
     * Returns wether file system is cloud.
     */
    public boolean isCloud()  { return _isCloud; }

    /**
     * Sets whether file system is cloud.
     */
    public void setCloud(boolean aValue)
    {
        if (aValue==isCloud()) return;
        _isCloud = aValue;
        _recentFiles = null;

        // Update Prefs
        String fileSys = aValue ? FILE_SYSTEM_CLOUD : FILE_SYSTEM_LOCAL;
        Prefs.get().setValue(FILE_SYSTEM, fileSys);
    }

    /**
     * Returns the cloud email.
     */
    public String getCloudEmail()  { return _email; }

    /**
     * Sets the cloud email.
     */
    public void setCloudEmail(String aString)
    {
        if (aString==getCloudEmail()) return;
        _email = aString;
        _recentFiles = null;

        // Update Prefs
        Prefs.get().setValue(USER_EMAIL, _email);
    }

    /**
     * Returns the shared instance.
     */
    public static WelcomePanel getShared()
    {
        if(_shared!=null) return _shared;
        return _shared!=null? _shared : (_shared = new WelcomePanel());
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
        Prefs.get().flush();    // Flush preferences
        if(_exit) quitApp(); // If exit requested, quit app
    }

    /**
     * Returns the selected file.
     */
    public WebFile getSelFile()  { return _selFile; }

    /**
     * Sets the selected file.
     */
    public void setSelFile(WebFile aFile)  { _selFile = aFile; }

    /**
     * Returns the Runnable to be called to quit app.
     */
    public Runnable getOnQuit()  { return _onQuit; }

    /**
     * Sets the Runnable to be called to quit app.
     */
    public void setOnQuit(Runnable aRunnable)  { _onQuit = aRunnable; }

    /**
     * Called to quit app.
     */
    public void quitApp()  { _onQuit.run(); }

    /**
     * Initialize UI panel.
     */
    protected void initUI()
    {
        // Add WelcomePaneAnim view
        DocView anim = getAnimView();
        getUI(ChildView.class).addChild(anim, 0); anim.playAnimDeep();

        // Configure SitesTable
        TableView<WebFile> sitesTable = getView("SitesTable", TableView.class);
        sitesTable.setRowHeight(24);
        sitesTable.getCol(0).setItemTextFunction(i -> i.getName());

        // Enable SitesTable MouseReleased
        List <WebFile> rfiles = getRecentFiles();
        if (rfiles.size()>0) _selFile = rfiles.get(0);
        enableEvents(sitesTable, MouseRelease);

        // Hide ProgressBar
        getView("ProgressBar").setVisible(false);

        // Set preferred size
        getUI().setPrefSize(400,600);

        // Configure Window: Add WindowListener to indicate app should exit when close button clicked
        WindowView win = getWindow(); win.setTitle("Welcome"); win.setResizable(false);
        enableEvents(win, WinClose);
        getView("OpenButton", Button.class).setDefaultButton(true);
    }

    /**
     * Resets UI.
     */
    public void resetUI()
    {
        setViewEnabled("OpenButton", getSelFile()!=null);
        setViewItems("SitesTable", getRecentFiles());
        setViewSelItem("SitesTable", getSelFile());

        // Update file system buttons/text: LocalButton, CloudButton, EmailText
        setViewValue("LocalButton", !isCloud());
        setViewValue("CloudButton", isCloud());
        setViewValue("EmailText", getCloudEmail());
    }

    /**
     * Responds to UI changes.
     */
    public void respondUI(ViewEvent anEvent)
    {
        // Handle LocalButton, CloudButton
        if (anEvent.equals("LocalButton"))
            setCloud(false);
        if (anEvent.equals("CloudButton"))
            handleCloudButton();

        // Handle SamplesButton
        if (anEvent.equals("SamplesButton"))
            openSamples();

        // Handle EmailText
        if (anEvent.equals("EmailText")) {
            String email = anEvent.getStringValue().trim().toLowerCase();
            if (email.equals("jeff@reportmill.com")) return;
            if (email.equals("jeff")) email = "jeff@reportmill.com";
            if (!email.contains("@")) return;
            setCloudEmail(email);
            setCloud(true);
        }

        // Handle SitesTable
        if (anEvent.equals("SitesTable"))
            setSelFile((WebFile)anEvent.getSelItem());

        // Handle NewButton
        if (anEvent.equals("NewButton")) {
            newFile();
        }

        // Handle OpenPanelButton
        if (anEvent.equals("OpenPanelButton"))
            showOpenPanel();

        // Handle OpenButton or SitesTable double-click
        if (anEvent.equals("OpenButton") || anEvent.equals("SitesTable") && anEvent.getClickCount()>1) {
            WebFile file = (WebFile)getViewSelItem("SitesTable");
            openFile(file);
        }

        // Handle QuitButton
        if (anEvent.equals("QuitButton")) {
            _exit = true; hide(); }

        // Handle WinClosing
        if (anEvent.isWinClose()) {
            _exit = true; hide(); }
    }

    /**
     * Called when CloudButton selected.
     */
    private void handleCloudButton()
    {
        if (getCloudEmail()==null || getCloudEmail().length()==0) {
            String msg = "The cloud file system needs an email to provide a unique folder for user files.\n";
            msg += "This information is not used for any other purposes. Though feel free to email\n";
            msg += "me at jeff@reportmill.com";
            String email = DialogBox.showInputDialog(getUI(), "Set Cloud Email", msg, "guest@guest");
            if (email==null || !email.contains("@")) return;
            email = email.trim().toLowerCase();
            if (email.equalsIgnoreCase("jeff@reportmill.com")) {
                DialogBox.showErrorDialog(getUI(), "Joker Alert", "Nice try."); return; }
            setCloudEmail(email);
        }
        setCloud(true);
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
        EditorPane dpane = new EditorPane().newDocument();
        dpane.setWindowVisible(true);
        hide();
        runLaterDelayed(300, () -> dpane.showSamples());
    }

    /**
     * Runs the open panel.
     */
    public void showOpenPanel()
    {
        // Have editor run open panel (if no document opened, just return)
        EditorPane epane = new EditorPane().showOpenPanel(getUI()); if(epane==null) return;

        // Make editor window visible and hide welcome panel
        epane.setWindowVisible(true);
        hide();

        // Add path to RecentFiles
        RecentFiles.addPath("RecentDocuments", epane.getSourceURL().getPath(), 99);
    }

    /**
     * Opens selected file.
     */
    public void openFile(Object aSource)
    {
        // Have editor run open panel (if no document opened, just return)
        EditorPane epane = new EditorPane().open(aSource); if(epane==null) return;

        // Make editor window visible and hide welcome panel
        epane.setWindowVisible(true);
        hide();

        // Add path to RecentFiles
        RecentFiles.addPath("RecentDocuments", epane.getSourceURL().getPath(), 99);
    }

    /**
     * Returns the list of the recent documents as a list of strings.
     */
    public List <WebFile> getRecentFiles()
    {
        // If already set, just return
        if (_recentFiles!=null) return _recentFiles;

        // Handle Local
        if (!isCloud()) {
            List <WebFile> rfiles = RecentFiles.getFiles("RecentDocuments");
            return _recentFiles = rfiles;
        }

        // Turn on progress bar
        ProgressBar pbar = getView("ProgressBar", ProgressBar.class);
        pbar.setIndeterminate(true);
        pbar.setVisible(true);

        // Set loading files
        new Thread(() -> setRecentFilesInBackground()).start();

        // Handle Cloud
        return Collections.EMPTY_LIST;
    }

    /**
     * Loads recent files in background.
     */
    private void setRecentFilesInBackground()
    {
        String email = getCloudEmail();
        if (email==null || email.length()==0) email = "guest@guest";
        List<WebFile> files = DropBox.getSiteForEmail(email).getRootDir().getFiles();
        _recentFiles = files;
        runLater(() -> recentFilesLoaded());
    }

    /**
     * Called when cloud files finish loading.
     */
    private void recentFilesLoaded()
    {
        // Turn on progress bar
        ProgressBar pbar = getView("ProgressBar", ProgressBar.class);
        pbar.setIndeterminate(false);
        pbar.setVisible(false);
        resetLater();
    }

    /**
     * Clears recent documents from preferences.
     */
    public void clearRecentFiles()  { Prefs.get().getChild("RecentDocuments").clear(); }

    /** Loads the WelcomePaneAnim.snp DocView. */
    DocView getAnimView()
    {
        // Unarchive WelcomePaneAnim.snp as DocView
        WebURL url = WebURL.getURL(getClass(), "WelcomePanelAnim.snp");
        DocView doc = (DocView)new ViewArchiver().getView(url);

        // Get page and clear border/shadow
        PageView page = doc.getPage();
        page.setBorder(null);
        page.setEffect(null);

        // Set BuildText and JavaText
        View bt = page.getChild("BuildText");
        View jt = page.getChild("JVMText");
        bt.setText("Build: " + SnapUtils.getBuildInfo());
        jt.setText("JVM: " + System.getProperty("java.runtime.version"));

        // Return doc
        return doc;
    }
}
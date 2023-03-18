package snapbuild.app;
import snap.gfx.GFXEnv;
import snap.util.Prefs;
import snap.web.WebFile;
import snap.web.WebURL;

/**
 * A custom class.
 */
public class App {

    // A file to open on launch
    private static WebFile _openOnLaunchFile;

    /**
     * Standard main implementation.
     */
    public static void main(String args[])
    {
        // Set App Prefs class
        Prefs prefs = Prefs.getPrefsForName("SnapBuilder");
        Prefs.setDefaultPrefs(prefs);

        // Install Exception reporter
        //ExceptionReporter er = new ExceptionReporter("SnapStudio"); er.setToAddress("support@reportmill.com");
        //er.setInfo("SnapCode Version 1, Build Date: " + SnapUtils.getBuildInfo());
        //Thread.setDefaultUncaughtExceptionHandler(er);

        // Process args
        if (args != null && args.length > 0)
            processArgs(args);

        // If OpenOnLaunchFile is set, open that
        if (_openOnLaunchFile != null)
            WelcomePanel.getShared().openFile(_openOnLaunchFile);

            // Otherwise, show open data source panel
        else {
            WelcomePanel.getShared().setOnQuit(() -> quitApp());
            WelcomePanel.getShared().showPanel();
        }
    }

    /**
     * Process args.
     */
    private static void processArgs(String[] args)
    {
        // Prologue
        String xmlFilename = "ViewOwner.snp";

        // Iterate over args
        for (int i = 0; i < args.length; i++) {

            // Get loop arg
            String arg = args[i];
            System.out.println("Process arg: " + arg);

            // Handle XMLFilename
            if (arg.equals("XMLFilename")) {
                String str = i + 1 < args.length ? args[++i] : null;
                if (str == null) {
                    System.err.println("Process XMLFilename: Missing filename");
                }
                else xmlFilename = str;
                System.out.println("Process XMLFilename = " + xmlFilename);
            }

            // Handle XMLString
            if (arg.equals("XMLString")) {
                String xmlStr = i + 1 < args.length ? args[++i] : null;
                if (xmlStr == null) {
                    System.err.println("Process XMLString: Missing string");
                }

                // Since this is TeaVM, just write string to root file and
                else {
                    byte[] xmlBytes = xmlStr.getBytes();
                    System.out.println("Process XMLString: Write bytes: " + xmlFilename + ", size=" + xmlBytes.length);
                    WebURL webURL = WebURL.getURL('/' + xmlFilename);
                    _openOnLaunchFile = webURL.createFile(false);
                    _openOnLaunchFile.setBytes(xmlBytes);
                }
            }
        }
    }

    /**
     * Exits the application.
     */
    public static void quitApp()
    {
        //if(AppPane.getOpenAppPane()!=null) AppPane.getOpenAppPane().hide();
        Prefs.getDefaultPrefs().flush();
        GFXEnv.getEnv().exit(0);
    }
}
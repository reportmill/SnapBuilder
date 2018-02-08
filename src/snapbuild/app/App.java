package snapbuild.app;
import snap.util.Prefs;
import snap.util.SnapUtils;

/**
 * A custom class.
 */
public class App {
    
    // Trigger snaptea
    static { snaptea.TV.set(); }

/**
 * Standard main implementation.
 */
public static void main(String args[])
{
    // Set App Prefs class
    Prefs.setPrefsDefault(Prefs.getPrefs(App.class));
    
    // Install Exception reporter
    //ExceptionReporter er = new ExceptionReporter("SnapStudio"); er.setToAddress("support@reportmill.com");
    //er.setInfo("SnapCode Version 1, Build Date: " + SnapUtils.getBuildInfo());
    //Thread.setDefaultUncaughtExceptionHandler(er);
    
    if(SnapUtils.isTeaVM)
        new EditorPane().newDocument().setWindowVisible(true);

    // Show open data source panel
    else {
        WelcomePanel.getShared().setOnQuit(() -> quitApp());
        WelcomePanel.getShared().showPanel();
    }
}

/**
 * Exits the application.
 */
public static void quitApp()
{
    //if(AppPane.getOpenAppPane()!=null) AppPane.getOpenAppPane().hide();
    Prefs.get().flush();
    //System.exit(0);
}

}
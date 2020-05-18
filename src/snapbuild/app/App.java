package snapbuild.app;
import snap.util.Prefs;

/**
 * A custom class.
 */
public class App {
    
/**
 * Standard main implementation.
 */
public static void main(String args[])
{
    // Set App Prefs class
    Prefs.setPrefsDefault(Prefs.getPrefs(App.class));
    //snaptea.TV.set();
    
    // Install Exception reporter
    //ExceptionReporter er = new ExceptionReporter("SnapStudio"); er.setToAddress("support@reportmill.com");
    //er.setInfo("SnapCode Version 1, Build Date: " + SnapUtils.getBuildInfo());
    //Thread.setDefaultUncaughtExceptionHandler(er);
    
//    if(snap.util.SnapUtils.isTeaVM) {
//        new EditorPane().newDocument().setWindowVisible(true); return; }

    // Show open data source panel
    WelcomePanel.getShared().setOnQuit(() -> quitApp());
    WelcomePanel.getShared().showPanel();
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
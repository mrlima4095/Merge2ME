import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.io.file.*;
import javax.microedition.rms.*;
import javax.microedition.io.*;
import java.util.*;
import java.io.*;

public class Merge2ME extends MIDlet implements CommandListener {
    public Display display = Display.getDisplay(this);
    public TextBox nano = new TextBox("Nano", "", 31522, TextField.ANY);
    public Form form = new Form("Merge2ME");
    private Alert alert;
    public Command BACK = new Command("Back", Command.BACK, 1), EXIT = new Command("Exit", Command.EXIT, 1);

    public void startApp() {
        if (javaClass("javax.microedition.io.FileConnection") != 0) { warnCommand("Merge2ME", "This MIDlet requires JSR-75! (FileConnection)", new Command[] { EXIT }); return; }
        

    }

    public void pauseApp() { }
    public void destroyApp(boolean unconditional) { notifyDestroyed(); }

    public void commandAction(Command c, Displayable d) {
        if (c == BACK) { display.setCurrent(form); }
        else if (c == EXIT) { destroyApp(true); }
    }

    public String read(String filename, boolean doMIDlet) {
        InputStream is = null;
        RecordStore rs = null;
        try {
            if (doMIDlet) {
                if (filename.startsWith("/")) {
                    is = getClass().getResourceAsStream(filename);
                    if (is == null) { return ""; }
                }
                else {
                    String rmsName = filename.substring(6);
                    rs = RecordStore.openRecordStore(rmsName, false);
                    if (rs == null || rs.getNumRecords() == 0) return "";
                    byte[] rec = rs.getRecord(1);
                    return new String(rec, "UTF-8");
                }
            } 
            else {
                FileConnection fc = null;
                try {
                    fc = (FileConnection) Connector.open("file://" + filename);
                    if (!fc.exists()) { return ""; }
                    is = fc.openInputStream();
                } catch (Exception e) { return ""; }
            }

            InputStreamReader reader = new InputStreamReader(is, "UTF-8");
            StringBuffer sb = new StringBuffer();
            int ch;
            while ((ch = reader.read()) != -1) sb.append((char) ch);
            reader.close();
            is.close();

            return sb.toString();
        } 
        catch (Exception e) { return ""; } 
        finally {
            try {
                if (rs != null) rs.closeRecordStore();
                if (is != null) is.close();
            } catch (Exception e) {}
        }
    }
    public boolean write(String filename, String content, boolean doMIDlet) {
        OutputStream os = null;
        RecordStore rs = null;

        try {
            if (doMIDlet) {
                if (filename.startsWith("/")) { return false; }
                else {
                    rs = RecordStore.openRecordStore(filename.substring(6), true);

                    byte[] data = content.getBytes("UTF-8");
                    if (rs.getNumRecords() == 0) { rs.addRecord(data, 0, data.length); }
                    else { rs.setRecord(1, data, 0, data.length); }

                    return true;
                }
            } 
            else {
                FileConnection fc = null;
                try {
                    fc = (FileConnection) Connector.open("file://" + filename);

                    if (!fc.exists()) fc.create();

                    os = fc.openOutputStream();
                    os.write(content.getBytes("UTF-8"));
                    os.flush();
                    return true;
                } catch (Exception e) { return false; }
            }
        } 
        catch (Exception e) { return false; } 
        finally {
            try {
                if (rs != null) rs.closeRecordStore();
                if (os != null) os.close();
            } catch (Exception e) {}
        }

        return false;
    }

    public int warnCommand(String title, String message, Command[] CMDS) { 
        alert = new Alert(title, message, null, AlertType.INFO); 
        alert.setTimeout(Alert.FOREVER); 
        for (int i = 0, i < CMDS.length; i++) { alert.addCommand(CMDS); }
        alert.setCommandListener(this); 
        display.setCurrent(alert);
        
        return 0; 
    }
    public int viewer(String title, String text) { Form viewer = new Form(title); viewer.append(text); viewer.addCommand(BACK); viewer.setCommandListener(this); display.setCurrent(viewer); return 0; }
    
    public int javaClass(String argument) { try { Class.forName(argument); return 0; } catch (ClassNotFoundException e) { return 3; } } 
    
}
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

    public InputStream readRaw(String filename) throws Exception {
        if (filename.startsWith("/home/")) {
            
        } 
        else if (filename.startsWith("/mnt/")) { return ((FileConnection) Connector.open("file:///" + filename.substring(5), Connector.READ)).openInputStream(); } 
        else if (filename.startsWith("/tmp/")) { return tmp.containsKey(filename = filename.substring(5)) ? new ByteArrayInputStream(((String) tmp.get(filename)).getBytes("UTF-8")) : null; } 
        else {
            if (filename.startsWith("/dev/")) {
                filename = filename.substring(5);
                String content = filename.equals("random") ? String.valueOf(random.nextInt(256)) : filename.equals("stdin") ? stdin.getString() : filename.equals("stdout") ? stdout.getText() : filename.equals("null") ? "\r" : filename.equals("zero") ? "\0" : null;
                if (content != null) { return new ByteArrayInputStream(content.getBytes("UTF-8")); }

                filename = "/dev/" + filename;
            } 

            InputStream is = getClass().getResourceAsStream(filename);
            return is;
        }
    }
    public String read(String filename) {
        try {
            InputStream is = readRaw(filename);
            if (is == null) { return ""; }
            
            InputStreamReader reader = new InputStreamReader(is, "UTF-8");
            StringBuffer sb = new StringBuffer();
            int ch;
            while ((ch = reader.read()) != -1) { sb.append((char) ch); }
            reader.close();
            is.close();
            
            return filename.startsWith("/home/") ? sb.toString() : env(sb.toString());
        } catch (Exception e) { return ""; }
    }
    public String loadRMS(String filename) {
        RecordStore rs = null;
        try {
            rs = RecordStore.openRecordStore(filename.substring(6), false);
            if (rs.getNumRecords() > 0) { return new ByteArrayInputStream(rs.getRecord(1)); }
        } finally { if (rs != null) { rs.closeRecordStore(); } }

        return null;
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
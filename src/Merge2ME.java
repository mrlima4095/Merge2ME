import javax.microedition.lcdui.*;
import javax.microedition.midlet.MIDlet;
import javax.microedition.io.file.*;
import javax.microedition.rms.*;
import javax.microedition.io.*;
import javax.bluetooh.*;
import java.util.*;
import java.io.*;

public class Merge2ME extends MIDlet implements CommandListener {
    private String version = getAppProperty("MIDlet-Version");

    private Display display = Display.getDisplay(this);
    private Form form = new Form("Merge2ME " + getAppProperty("MIDlet-Version"));
    private TextBox editor = new TextBox("Nano", "", 31522, TextField.ANY); 
    private StringItem stdout = new StringItem("", "");
    private Command NEW = new Command("New", Command.OK, 1), 
    				DELETE = new Command("Delete", Command.OK, 1),
    				COMMIT = new Command("Commit", Command.OK, 1),
    				CLEAR = new Command("Clear", Command.OK, 1),
    				UNDO = new Command("Undo", Command.OK, 1),
    				OPEN = new Command("Open", Command.OK, 1),
    				BACK = new Command("Back", Command.BACK, 1),
    				EXIT = new Command("Exit", Command.EXIT, 1),

    public void startApp() {
        form.append(stdin);
        form.append(stdout);

        form.addCommand(CMD1);
        form.addCommand(CMD2);

        form.setCommandListener(this);
        display.setCurrent(form);
    }

    public void pauseApp() { }
    public void destroyApp(boolean unconditional) { }

    public void commandAction(Command c, Displayable d) {

    }
	
	private void warnCommand(String title, String message) { if (message == null || message.length() == 0) { return; } Alert alert = new Alert(title, message, null, AlertType.WARNING); alert.setTimeout(Alert.FOREVER); display.setCurrent(alert); }
    
    private void deleteFile(String filename) { if (filename == null || filename.length() == 0) { return; } try { RecordStore.deleteRecordStore(filename); } catch (RecordStoreNotFoundException e) { echoCommand("rm: " + filename + ": not found"); } catch (RecordStoreException e) { echoCommand("rm: " + e.getMessage()); } }
    private void writeRMS(String recordStoreName, String data) { RecordStore recordStore = null; try { recordStore = RecordStore.openRecordStore(recordStoreName, true); byte[] byteData = data.getBytes(); if (recordStore.getNumRecords() > 0) { recordStore.setRecord(1, byteData, 0, byteData.length); } else { recordStore.addRecord(byteData, 0, byteData.length); } } catch (RecordStoreException e) { } finally { if (recordStore != null) { try { recordStore.closeRecordStore(); } catch (RecordStoreException e) { } } } }
    private String loadRMS(String recordStoreName, int recordId) { RecordStore recordStore = null; String result = ""; try { recordStore = RecordStore.openRecordStore(recordStoreName, true); if (recordStore.getNumRecords() >= recordId) { byte[] data = recordStore.getRecord(recordId); if (data != null) { result = new String(data); } } } catch (RecordStoreException e) { result = ""; } finally { if (recordStore != null) { try { recordStore.closeRecordStore(); } catch (RecordStoreException e) { } } } return result; }
    
    public class Explorer implements CommandListener { private List files = new List(form.getTitle(), List.IMPLICIT); private Command backCommand = new Command("Back", Command.BACK, 1), openCommand = new Command("Open", Command.SCREEN, 2), deleteCommand = new Command("Delete", Command.SCREEN, 3), runCommand = new Command("Run Script", Command.SCREEN, 4), importCommand = new Command("Import File", Command.SCREEN, 5); public Explorer() { try { String[] recordStores = RecordStore.listRecordStores(); if (recordStores != null) { for (int i = 0; i < recordStores.length; i++) { if (recordStores[i].startsWith(".")) { } else { files.append((String) recordStores[i], null); } } } } catch (RecordStoreException e) { } files.addCommand(backCommand); files.addCommand(openCommand); files.addCommand(deleteCommand); files.addCommand(runCommand); files.addCommand(importCommand); files.setCommandListener(this); display.setCurrent(files); } public void commandAction(Command c, Displayable d) { if (c == backCommand) { processCommand("xterm"); } else if (c == deleteCommand) { deleteFile(files.getString(files.getSelectedIndex())); new Explorer(); } else if (c == openCommand) { new NanoEditor(files.getString(files.getSelectedIndex())); } else if (c == runCommand) { processCommand("xterm"); processCommand("run " + files.getString(files.getSelectedIndex())); } else if (c == importCommand) { processCommand("xterm"); importScript(files.getString(files.getSelectedIndex())); } } }
    public class FileExplorer implements CommandListener { private String currentPath = "file:///"; private List files = new List(form.getTitle(), List.IMPLICIT); private Command openCommand = new Command("Open", Command.OK, 1), backCommand = new Command("Back", Command.BACK, 1); public FileExplorer() { files.addCommand(openCommand); files.addCommand(backCommand); files.setCommandListener(this); display.setCurrent(files); listFiles(currentPath); } private void listFiles(String path) { files.deleteAll(); try { if (path.equals("file:///")) { Enumeration roots = FileSystemRegistry.listRoots(); while (roots.hasMoreElements()) { files.append((String) roots.nextElement(), null); } } else { FileConnection dir = (FileConnection) Connector.open(path, Connector.READ); Enumeration fileList = dir.list(); Vector dirs = new Vector(); Vector filesOnly = new Vector(); while (fileList.hasMoreElements()) { String fileName = (String) fileList.nextElement(); if (fileName.endsWith("/")) { dirs.addElement(fileName); } else { filesOnly.addElement(fileName); } } while (!dirs.isEmpty()) { files.append(getFirstString(dirs), null); } while (!filesOnly.isEmpty()) { files.append(getFirstString(filesOnly), null); } dir.close(); } } catch (IOException e) { } } public void commandAction(Command c, Displayable d) { if (c == openCommand) { int selectedIndex = files.getSelectedIndex(); if (selectedIndex >= 0) { String selected = files.getString(selectedIndex); String newPath = currentPath + selected; if (selected.endsWith("/")) { currentPath = newPath; listFiles(newPath); } else { writeRMS(selected, read(newPath)); warnCommand(null, "File '" + selected + "' successfully saved!"); } } } else if (c == backCommand) { if (!currentPath.equals("file:///")) { int lastSlash = currentPath.lastIndexOf('/', currentPath.length() - 2); if (lastSlash != -1) { currentPath = currentPath.substring(0, lastSlash + 1); listFiles(currentPath); } } else { processCommand("xterm"); } } } private static String getFirstString(Vector v) { String result = null; for (int i = 0; i < v.size(); i++) { String cur = (String) v.elementAt(i); if (result == null || cur.compareTo(cur) < 0) { result = cur; } } v.removeElement(result); return result; } private String read(String file) { try { FileConnection fileConn = (FileConnection) Connector.open(file, Connector.READ); InputStream is = fileConn.openInputStream(); StringBuffer content = new StringBuffer(); int ch; while ((ch = is.read()) != -1) { content.append((char) ch); } is.close(); fileConn.close(); return content.toString(); } catch (IOException e) { return ""; } } }
    public class NanoEditor implements CommandListener {private Command backCommand = new Command("Back", Command.BACK, 1), clearCommand = new Command("Clear", Command.SCREEN, 2), runCommand = new Command("Run Script", Command.SCREEN, 3), importCommand = new Command("Import File", Command.SCREEN, 4), viewCommand = new Command("View as HTML", Command.SCREEN, 5); public NanoEditor(String args) { editor.setString((args == null || args.length() == 0) ? nanoContent : loadRMS(args, 1)); editor.addCommand(backCommand); editor.addCommand(clearCommand); editor.addCommand(runCommand); editor.addCommand(importCommand); editor.addCommand(viewCommand); editor.setCommandListener(this); display.setCurrent(editor); } public void commandAction(Command c, Displayable d) { if (c == backCommand) { nanoContent = editor.getString(); processCommand("xterm"); } else if (c == clearCommand) { editor.setString(""); } else if (c == runCommand) { nanoContent = editor.getString(); processCommand("xterm"); runScript(nanoContent); } else if (c == importCommand) { nanoContent = editor.getString(); processCommand("xterm"); importScript("nano"); } else if (c == viewCommand) { nanoContent = editor.getString(); viewer(extractTitle(nanoContent), html2text(nanoContent)); } } }
    
}
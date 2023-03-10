package tn5250;

import org.apache.log4j.Logger;
import tn5250j.Session5250;
import tn5250j.TN5250jConstants;
import tn5250j.event.ScreenOIAListener;
import tn5250j.event.SessionChangeEvent;
import tn5250j.event.SessionListener;
import tn5250j.framework.common.SessionManager;
import tn5250j.framework.tn5250.Screen5250;
import tn5250j.framework.tn5250.ScreenField;
import tn5250j.framework.tn5250.ScreenOIA;

import java.util.Properties;

public class TerminalDriver {

    Logger LOG = Logger.getLogger(TerminalDriver.class);

    private final Properties properties;
    private Session5250 session;
    private boolean connected;
    private boolean informed;
    private int informChange;
    private Screen5250 screen;

    public TerminalDriver(String host, String port) {
        properties = new Properties();
        properties.put(TN5250jConstants.SESSION_HOST, host);
        properties.put(TN5250jConstants.SESSION_HOST_PORT, port);
        properties.put(TN5250jConstants.SESSION_CODE_PAGE, "37");
    }

    public TerminalDriver connect() throws InterruptedException {
        createSession();
        session.connect();
        while (!connected) {
            Thread.sleep(100);
        }
        screen = session.getScreen();
        addOperatorInformationAreaListener();
        return this;
    }

    private void createSession() {
        session = SessionManager.instance().openSession(properties, null, null);
        session.addSessionListener(new SessionListener() {
            //@Override
            public void onSessionChanged(SessionChangeEvent changeEvent) {
                connected = changeEvent.getState() == TN5250jConstants.STATE_CONNECTED;
            }
        });
    }

    private void addOperatorInformationAreaListener() {
        session.getScreen().getOIA().addOIAListener(new ScreenOIAListener() {
            //@Override
            public void onOIAChanged(ScreenOIA oia, int change) {
                LOG.debug(String.format("OIA %d", change));
                informed = informed | informChange == change;
            }
        });
    }

    public void waitForUnlock() {
        informed = false;
        informChange = ScreenOIAListener.OIA_CHANGED_KEYBOARD_LOCKED;
        while (!informed) {
            Thread.yield();
        }
        dumpScreen();

        if (getScreenContent().getLine(0).contains("Display Program Messages")) {
            sendKeys("[enter]").waitForUnlock();
        }
    }

    public boolean isConnected() {
        return connected;
    }

    public TerminalDriver sendKeys(String keys) {
        screen.sendKeys(keys);
        return this;
    }

    public TerminalDriver fillCurrentField(String text) {
        ScreenField currentField = screen.getScreenFields().getCurrentField();
        LOG.info(String.format("cursor: %d %d", currentField.startRow(), currentField.startCol()));
        currentField.setString(text);
        return this;
    }

    public TerminalDriver select(String label, String text) {
        ScreenContent content = getScreenContent();
        int index = content.indexOf(label);
        ScreenField field = null;
        while (field == null && index > 0) {
            field = screen.getScreenFields().findByPosition(index--);
        }
        if (field == null) {
            throw new IllegalStateException(String.format("Could not find field *before* label %s"));
        }
        field.setString(text);
        return this;
    }

    public TerminalDriver fillFieldWith(String label, String text) {
        ScreenContent content = getScreenContent();
        int index = content.indexOf(label);
        ScreenField field = null;
        while (field == null && index < content.length()) {
            field = screen.getScreenFields().findByPosition(index++);
        }
        if (field == null) {
            throw new IllegalStateException(String.format("Could not find field *after* label %s"));
        }
        field.setString(text);
        return this;
    }

    public void writeOnField(String input, int field) {
        ScreenField[] fields = screen.getScreenFields().getFields();
        fields[field].setString(input);
    }

    public void dumpScreen() {
        getScreenContent().dumpScreen();
    }

    public String ScreenText() {
        return getScreenContent().pantallaTexto();
    }

    public String LeerTextoPantalla(int x, int y, int longitud) {
        return getScreenContent().LeerTexto(x,y,longitud);
    }

    public void assertScreen(String name) {
        assert getScreenContent().getLine(0).contains(name) : String.format("Screen is not '%s'", name);
    }

    public ScreenContent getScreenContent() {
        return new ScreenContent(session);
    }

    public void sendCommand(String command) {
        fillFieldWith("===>", command).sendEnter();
    }

    public TerminalDriver sendEnter() {
        sendKeys("[enter]").waitForUnlock();
        return this;
    }

    public String lastReportLine() {
        ScreenContent content = getScreenContent();
        fillFieldWith("Position to line", "B").sendEnter();
        return content.getLine(content.lineOf("********  End of report  ********") - 1);
    }

    /*
    public void TakePrint() throws AWTException {

        char[] graphic = new char[1920];
        screen.GetScreen(graphic, 1920, TN5250jConstants.PLANE_EXTENDED_GRAPHIC);

         screen.GetScreenRect().

        BufferedImage image = new Robot().createScreenCapture(graphic.);
        ImageIO.write(image, "png",archivo);

    }*/

    public boolean SearchField(String input) {
        ScreenField[] fields = screen.getScreenFields().getFields();

        for (int i = 0;i<fields.length;i++)
        {
            fields[i].getString().contains(input);
            if (fields[i].getString().contains(input)) return true;

        }
        return false;

    }

}

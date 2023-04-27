package tn5250;

import org.apache.commons.codec.binary.Base64;
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

import java.security.MessageDigest;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;


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
        session = SessionManager.instance().openSession(properties, null, "Name 1");
        session.addSessionListener(new SessionListener() {
            //@Override
            public void onSessionChanged(SessionChangeEvent changeEvent) {
                connected = changeEvent.getState() == TN5250jConstants.STATE_CONNECTED;
            }
        });
    }

    public TerminalDriver disconectedSession(){
        session.disconnect();
        return this;
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

    public boolean assertScreen(String name) {
        boolean estado = false;
        if(getScreenContent().getLine(0).contains(name)){
            estado = true;
        }
        return estado;
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

    public static String Encriptar(String texto) {

        String secretKey = "qualityinfosolutions"; //llave para encriptar datos
        String base64EncryptedString = "";

        try {

            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digestOfPassword = md.digest(secretKey.getBytes("utf-8"));
            byte[] keyBytes = Arrays.copyOf(digestOfPassword, 24);

            SecretKey key = new SecretKeySpec(keyBytes, "DESede");
            Cipher cipher = Cipher.getInstance("DESede");
            cipher.init(Cipher.ENCRYPT_MODE, key);

            byte[] plainTextBytes = texto.getBytes("utf-8");
            byte[] buf = cipher.doFinal(plainTextBytes);
            byte[] base64Bytes = Base64.encodeBase64(buf);
            base64EncryptedString = new String(base64Bytes);

        } catch (Exception ex) {
        }
        return base64EncryptedString;
    }

    /*public String encriptar(String texto) {
        try {
            byte[] keyBytes = texto.getBytes("UTF-8");
            byte[] keyBytes16 = new byte[16];
            System.arraycopy(keyBytes, 0, keyBytes16, 0, Math.min(keyBytes.length, 16));
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes16, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(texto.getBytes("UTF-8")));
        } catch (Exception e) {
            System.err.println("Error al encriptar: " + e.getMessage());
        }
        return null;
    }

    public String desencriptar(String textoEncriptado) {
        try {
            byte[] keyBytes = textoEncriptado.getBytes("UTF-8");
            byte[] keyBytes16 = new byte[16];
            System.arraycopy(keyBytes, 0, keyBytes16, 0, Math.min(keyBytes.length, 16));
            SecretKeySpec secretKey = new SecretKeySpec(keyBytes16, "AES");
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(textoEncriptado)));
        } catch (Exception e) {
            System.err.println("Error al desencriptar: " + e.getMessage());
        }
        return null;
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

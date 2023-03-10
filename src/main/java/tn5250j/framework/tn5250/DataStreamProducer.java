package tn5250j.framework.tn5250;

import tn5250.Helper;
import tn5250j.encoding.ICodePage;
import tn5250j.tools.logging.TN5250jLogFactory;
import tn5250j.tools.logging.TN5250jLogger;

import java.io.*;
import java.net.SocketException;
import java.util.concurrent.BlockingQueue;

public class DataStreamProducer implements Runnable {

    public static final int SNA_GENERAL_DATA_STREAM = 0x12A0;
    private BufferedInputStream bin;
    private ByteArrayOutputStream baosin;
    private Thread me;
    private byte[] saveStream;
    private final BlockingQueue<Object> queue;
    private tnvt vt;
    private byte[] initial;
    private FileOutputStream fw;
    private BufferedOutputStream dw;
    private boolean dumpBytes = false;
    private ICodePage codePage;

    private TN5250jLogger log = TN5250jLogFactory.getLogger(this.getClass());

    public DataStreamProducer(tnvt vt, BufferedInputStream in, BlockingQueue<Object> queue, byte[] init) {
        bin = in;
        this.vt = vt;
        baosin = new ByteArrayOutputStream();
        this.queue = queue;
        initial = init;
    }

    public void setInputStream(ByteArrayOutputStream is) {
        baosin = is;
    }

    public final void run() {

        boolean done = false;

        me = Thread.currentThread();

        // load the first response screen
        loadStream(initial, 0);

        while (!done) {
            try {

                byte[] abyte0 = readIncoming();

                // WVL - LDC : 17/05/2004 : Device name negotiations send TIMING MARK
                // Restructured to the readIncoming() method to return null
                // on TIMING MARK. Don't process in that case (abyte0 == null)!
                if (abyte0 != null) {
                    // WVL - LDC : 16/07/2003 : TR.000345
                    // When the socket has been closed, the reading returns
                    // no bytes (an empty byte arrray).
                    // But the loadStream fails on this, so we check it here!
                    if (abyte0.length > 0) {
                        loadStream(abyte0, 0);
                    }
                    // WVL - LDC : 16/07/2003 : TR.000345
                    // Returning no bytes means the input buffer has
                    // reached end-of-stream, so we do a disconnect!
                    else {
                        done = true;
                        vt.disconnect();
                    }
                }

            } catch (SocketException se) {
                log.warn("   DataStreamProducer thread interrupted and stopping " + se.getMessage());
                done = true;
            } catch (IOException ioe) {

                log.warn(ioe.getMessage());
                if (me.isInterrupted())
                    done = true;

            } catch (Exception ex) {

                log.warn(ex.getMessage());
                if (me.isInterrupted())
                    done = true;

            }
        }
    }


    private void loadStream(byte bytes[], int index) {
        int length = getWord(bytes, index);
        int misaligned = getWord(bytes, index + 3) == SNA_GENERAL_DATA_STREAM ? 1 : 0;
        Helper.dumpBytes(bytes);
        int size;
        if (saveStream == null) {
            size = bytes.length;
        } else {
            size = saveStream.length + bytes.length;
            byte[] inter = new byte[size];
            System.arraycopy(saveStream, 0, inter, 0, saveStream.length);
            System.arraycopy(bytes, 0, inter, saveStream.length, bytes.length);
            bytes = new byte[size];
            System.arraycopy(inter, 0, bytes, 0, size);
            saveStream = null;
            log.debug("partial stream found");
        }

        if (length > size) {
            saveStream = new byte[bytes.length];
            System.arraycopy(bytes, 0, saveStream, 0, bytes.length);
            log.debug("partial stream saved");
        } else {
            byte record[];
            try {
                record = new byte[length + 2]; // + EOR (FF EF)
                System.arraycopy(bytes, index, record, 0, 2);
                System.arraycopy(bytes, index + 2 + misaligned, record, 2, record.length - 2);
                queue.put(record);
                if (bytes.length > record.length + index + misaligned)
                    loadStream(bytes, record.length + index + misaligned);
            } catch (Exception e) {
                log.warn(e, e);
            }
        }
    }

    private int getWord(byte[] bytes, int index) {
        return (bytes[index] & 0xff) << 8 | bytes[index + 1] & 0xff;
    }

    public final byte[] readIncoming()
            throws IOException {

        boolean done = false;
        boolean negotiate = false;

        baosin.reset();
        int j = -1;
        int i = 0;

        while (!done) {

            i = bin.read();

            // WVL - LDC : 16/07/2003 : TR.000345
            // The inStream return -1 when end-of-stream is reached. This
            // happens e.g. when the connection is closed from the AS/400.
            // So we stop in this case!
            // ==> an empty byte array is returned from this method.
            if (i == -1) // nothing read!
            {
                done = true;
                vt.disconnect();
                continue;
            }

            // We use the values instead of the static values IAC and EOR
            //    because they are defined as bytes.
            //
            // The > if(i != 255 || j != 255)  < is a hack for the double FF FF's
            // that are being returned.  I do not know why this is like this and
            // can not find any documentation for it.  It is also being returned
            // on my Client Access tcp dump as well so they are handling it.
            //
            // my5250
            // 0000:  00 50 DA 44 C8 45 42 00 00 00 00 24 08 00 45 00 .P.D.EB....$..E.
            // 0010:  04 2A BC F9 00 00 40 06 D0 27 C1 A8 33 04 C1 A8 .*....@..'..3...
            // 0020:  33 58 00 17 04 18 6F A2 83 CB 00 1E D1 BA 50 18 3X....o.......P.
            // 0030:  20 00 8A 9A 00 00 03 FF FF 12 A0 00 00 04 00 00  ...............
            // --------------------------- || || -------------------------------------
            // 0040:  03 04 40 04 11 00 20 01 07 00 00 00 18 00 00 10 ..@... .........

            if (j == 255 && i == 255) {
                j = -1;
                continue;
            }
            baosin.write(i);
            // check for end of record EOR and IAC  - FFEF
            if (j == 255 && i == 239)
                done = true;

            // This is to check for the TELNET TIMING MARK OPTION
            // rfc860 explains this in more detail.  When we receive it
            // we will negotiate with the server by sending a WONT'T TIMING-MARK
            // This will let the server know that we processed the information
            // and are just waiting for the user to enter some data so keep the
            // socket alive.   This is more or less a AYT (ARE YOU THERE) or not.
            if (i == 253 && j == 255) {
                done = true;
                negotiate = true;
            }
            j = i;
        }

        // after the initial negotiation we might get other options such as
        //    timing marks ??????????????  do we ???????????? look at telnet spec
        // yes we do. rfc860 explains about timing marks.

        // WVL - LDC : 17/05/2004 : Device name negotiations send TIMING MARK
        //                          to existing device!
        // Handled incorrectly: we cannot continue processing the TIMING MARK DO
        // after we have handled it in the vt.negotiate()
        // We should not return the bytes;
        // ==> restructured to return null after negotiation!
        //     Impacts the run method! Added the null check.
        byte[] rBytes = baosin.toByteArray();

        if (dumpBytes) {
            dump(rBytes);
        }

        if (negotiate) {
            // get the negotiation option
            baosin.write(bin.read());
            vt.negotiate(rBytes);

            return null;
        }
        return rBytes;
    }

    protected final void toggleDebug(ICodePage cp) {

        if (codePage == null)
            codePage = cp;

        dumpBytes = !dumpBytes;
        if (dumpBytes) {

            try {
                if (fw == null) {
                    fw = new FileOutputStream("log.txt");
                    dw = new BufferedOutputStream(fw);
                }
            } catch (FileNotFoundException fnfe) {
                log.warn(fnfe.getMessage());
            }

        } else {

            try {

                if (dw != null)
                    dw.close();
                if (fw != null)
                    fw.close();
                dw = null;
                fw = null;
                codePage = null;
            } catch (IOException ioe) {

                log.warn(ioe.getMessage());
            }
        }

        log.info("Data Stream output is now " + dumpBytes);
    }

    public void dump(byte[] abyte0) {
        try {

            log.info("\n Buffer Dump of data from AS400: ");
            dw.write("\r\n Buffer Dump of data from AS400: ".getBytes());

            StringBuffer h = new StringBuffer();
            for (int x = 0; x < abyte0.length; x++) {
                if (x % 16 == 0) {
                    System.out.println("  " + h.toString());
                    dw.write(("  " + h.toString() + "\r\n").getBytes());

                    h.setLength(0);
                    h.append("+0000");
                    h.setLength(5 - Integer.toHexString(x).length());
                    h.append(Integer.toHexString(x).toUpperCase());

                    System.out.print(h.toString());
                    dw.write(h.toString().getBytes());

                    h.setLength(0);
                }
                char ac = codePage.ebcdic2uni(abyte0[x]);
                if (ac < ' ')
                    h.append('.');
                else
                    h.append(ac);
                if (x % 4 == 0) {
                    System.out.print(" ");
                    dw.write((" ").getBytes());

                }

                if (Integer.toHexString(abyte0[x] & 0xff).length() == 1) {
                    System.out.print("0" + Integer.toHexString(abyte0[x] & 0xff).toUpperCase());
                    dw.write(("0" + Integer.toHexString(abyte0[x] & 0xff).toUpperCase()).getBytes());

                } else {
                    System.out.print(Integer.toHexString(abyte0[x] & 0xff).toUpperCase());
                    dw.write((Integer.toHexString(abyte0[x] & 0xff).toUpperCase()).getBytes());
                }

            }
            System.out.println();
            dw.write("\r\n".getBytes());

            dw.flush();
        } catch (EOFException _ex) {
        } catch (Exception _ex) {
            log.warn("Cannot dump from host\n\r");
        }

    }

}
package de.jokir.nfcreader;

import javax.smartcardio.*;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by johnny on 18.12.2016.
 */
public class NfcReader {

    private static final byte[] CLA_INS_P1_P2 = ByteUtils.hexStringToByteArray("00A40400");
    public static final byte[] ANDROID_AID = ByteUtils.hexStringToByteArray("F0010203040506");


    public static void main(String[] args) {

        while (true) {
            try {
                Card card = waitForConnection();
                tranceive(card);
            } catch (Exception ex) {
                System.out.println("ERROR: " + ex.getMessage());
            }
        }
    }

    public static Card waitForConnection() throws CardException {
        // get first terminal
        TerminalFactory factory = TerminalFactory.getDefault();
        CardTerminal terminal = factory.terminals().list().get(0);
        System.out.println("using terminal: " + terminal.getName());

        // wait till card is present and connect
        System.out.println("waiting for card");
        while (!terminal.isCardPresent()) {
        }
        Card card = terminal.connect("*");
        System.out.println("card found");

        return card;
    }

    public static void tranceive(Card card) throws CardException {
        CardChannel channel = card.getBasicChannel();
        ByteBuffer sendBuffer = ByteBuffer.allocate(1024);
        ByteBuffer receiveBuffer = ByteBuffer.allocate(1024);
        int counter = 0;

        sendBuffer.put(createSelectAidApdu(ANDROID_AID));
        sendBuffer.flip();
        int received = channel.transmit(sendBuffer, receiveBuffer);

        while (received != -1) {
            receiveBuffer.flip();
            System.out.println(++counter + ": " + new String(receiveBuffer.array(), 0, received));
            sendBuffer.clear();
            receiveBuffer.clear();
            sendBuffer.put(("Message from android reader " + counter).getBytes());
            sendBuffer.flip();

            received = channel.transmit(sendBuffer, receiveBuffer);
        }
    }

    private static byte[] createSelectAidApdu(byte[] aid) {
        byte[] result = new byte[6 + aid.length];

        System.arraycopy(CLA_INS_P1_P2, 0, result, 0, CLA_INS_P1_P2.length);
        result[4] = (byte) aid.length;
        System.arraycopy(aid, 0, result, 5, aid.length);
        result[result.length - 1] = 0;

        return result;
    }
}

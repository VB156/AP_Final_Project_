package graph;

import java.util.Date;

/**
 * This class is used to create a message object with a string, double, and date.
 */
public class Message {

//------------------------------------------------------------------------------------------------------------------
// Class variables:
//------------------------------------------------------------------------------------------------------------------
    public final byte[] data;
    public final String asText;
    public final double asDouble;
    public final Date date;

//------------------------------------------------------------------------------------------------------------------
// Public Methods:
//------------------------------------------------------------------------------------------------------------------

    /**
     * This constructor is used to create a new Message object with the given string data.
     * 
     * @param msgDataAsString The string data to be stored in the Message object.
     */

    public Message(String msgDataAsString) {
        if (msgDataAsString == null) {
            throw new NullPointerException("Data cannot be null - check input data");
        }

        this.date = new Date();
        this.data = msgDataAsString.getBytes();
        this.asText = msgDataAsString;
        double temp;
        try {
            temp = Double.parseDouble(msgDataAsString);
        } catch (NumberFormatException e) {
            temp = Double.NaN;
        }
        this.asDouble = temp;
    }

    /**
     * This constructor is used to create a new Message object with the given byte array data.
     * 
     * @param msgDataAsByteArray The byte array data to be stored in the Message object.
     */
    public Message(byte[] msgDataAsByteArray) {
        this(new String(msgDataAsByteArray));
    }

    /**
     * This constructor is used to create a new Message object with the given double data.
     * 
     * @param msgDataAsDouble The double data to be stored in the Message object.
     */ 
    public Message (double msgDataAsDouble) {
        this(String.valueOf(msgDataAsDouble));
    }

}

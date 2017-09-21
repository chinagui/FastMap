package com.navinfo.dataservice.commons.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UTFDataFormatException;


public class StringConverter
{

    private static final byte[] HEXBYTES = {
        (byte) '0', (byte) '1', (byte) '2', (byte) '3', (byte) '4',
        (byte) '5', (byte) '6', (byte) '7', (byte) '8', (byte) '9',
        (byte) 'a', (byte) 'b', (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f'
    };
    private static final String HEXINDEX = "0123456789abcdef0123456789ABCDEF";

    /**
     * Converts a String into a byte array by using a big-endian two byte
     * representation of each char value in the string.
     */
    byte[] stringToFullByteArray(String s) {

        int    length = s.length();
        byte[] buffer = new byte[length * 2];
        int    c;

        for (int i = 0; i < length; i++) {
            c                 = s.charAt(i);
            buffer[i * 2]     = (byte) ((c & 0x0000ff00) >> 8);
            buffer[i * 2 + 1] = (byte) (c & 0x000000ff);
        }

        return buffer;
    }

    /**
     * Compacts a hexadecimal string into a byte array
     *
     *
     * @param s hexadecimal string
     *
     * @return byte array for the hex string
     * @throws java.io.IOException
     */
    public static byte[] hexToByte(String s) throws IOException {

        int    l    = s.length() / 2;
        byte[] data = new byte[l];
        int    j    = 0;

        if (s.length() % 2 != 0) {
            throw new IOException(
                "hexadecimal string with odd number of characters");
        }

        for (int i = 0; i < l; i++) {
            char c = s.charAt(j++);
            int  n, b;

            n = HEXINDEX.indexOf(c);

            if (n == -1) {
                throw new IOException(
                    "hexadecimal string contains non hex character");
            }

            b       = (n & 0xf) << 4;
            c       = s.charAt(j++);
            n       = HEXINDEX.indexOf(c);
            b       += (n & 0xf);
            data[i] = (byte) b;
        }

        return data;
    }

    /**
     * Converts a byte array into a hexadecimal string
     *
     *
     * @param b byte array
     *
     * @return hex string
     */
    public static String byteToHex(byte[] b) {

        int    len = b.length;
        char[] s   = new char[len * 2];

        for (int i = 0, j = 0; i < len; i++) {
            int c = ((int) b[i]) & 0xff;

            s[j++] = (char) HEXBYTES[c >> 4 & 0xf];
            s[j++] = (char) HEXBYTES[c & 0xf];
        }

        return new String(s);
    }

    /**
     * Converts a byte array into hexadecimal characters
     * which are written as ASCII to the given output stream.
     *
     * @param o output stream
     * @param b byte array
     */
    public static void writeHex(byte[] o, int from, byte[] b) {

        int len = b.length;

        for (int i = 0; i < len; i++) {
            int c = ((int) b[i]) & 0xff;

            o[from++] = HEXBYTES[c >> 4 & 0xf];
            o[from++] = HEXBYTES[c & 0xf];
        }
    }

    public static String byteToString(byte[] b, String charset) {

        try {
            return (charset == null) ? new String(b)
                                     : new String(b, charset);
        } catch (Exception e) {}

        return null;
    }

    /**
     * Converts a Unicode string into UTF8 then convert into a hex string
     *
     *
     * @param s normal Unicode string
     *
     * @return hex string representation of UTF8 encoding of the input
     */
    public static String unicodeToHexString(String s) {

        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        writeUTF(s, bout);

        return byteToHex(bout.toByteArray());
    }


    public static int unicodeToAscii(ByteArrayOutputStream b, String s,
                                     boolean doubleSingleQuotes) {

        int count = 0;

        if ((s == null) || (s.length() == 0)) {
            return 0;
        }

        int len = s.length();

        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);

            if (c == '\\') {
                if ((i < len - 1) && (s.charAt(i + 1) == 'u')) {
                    b.write(c);    // encode the \ as unicode, so 'u' is ignored
                    b.write('u');
                    b.write('0');
                    b.write('0');
                    b.write('5');
                    b.write('c');

                    count += 6;
                } else {
                    b.write(c);

                    count++;
                }
            } else if ((c >= 0x0020) && (c <= 0x007f)) {
                b.write(c);        // this is 99%

                count++;

                if (c == '\'' && doubleSingleQuotes) {
                    b.write(c);

                    count++;
                }
            } else {
                b.write('\\');
                b.write('u');
                b.write(HEXBYTES[(c >> 12) & 0xf]);
                b.write(HEXBYTES[(c >> 8) & 0xf]);
                b.write(HEXBYTES[(c >> 4) & 0xf]);
                b.write(HEXBYTES[c & 0xf]);

                count += 6;
            }
        }

        return count;
    }


    public static String asciiToUnicode(byte[] s, int offset, int length) {

        if (length == 0) {
            return "";
        }

        char[] b = new char[length];
        int    j = 0;

        for (int i = 0; i < length; i++) {
            byte c = s[offset + i];

            if (c == '\\' && i < length - 5) {
                byte c1 = s[offset + i + 1];

                if (c1 == 'u') {
                    i++;

                    // 4 characters read should always return 0-15
                    int k = HEXINDEX.indexOf(s[offset + (++i)]) << 12;

                    k      += HEXINDEX.indexOf(s[offset + (++i)]) << 8;
                    k      += HEXINDEX.indexOf(s[offset + (++i)]) << 4;
                    k      += HEXINDEX.indexOf(s[offset + (++i)]);
                    b[j++] = (char) k;
                } else {
                    b[j++] = (char) c;
                }
            } else {
                b[j++] = (char) c;
            }
        }

        return new String(b, 0, j);
    }

    public static String asciiToUnicode(String s) {

        if ((s == null) || (s.indexOf("\\u") == -1)) {
            return s;
        }

        int    len = s.length();
        char[] b   = new char[len];
        int    j   = 0;

        for (int i = 0; i < len; i++) {
            char c = s.charAt(i);

            if (c == '\\' && i < len - 5) {
                char c1 = s.charAt(i + 1);

                if (c1 == 'u') {
                    i++;

                    // 4 characters read should always return 0-15
                    int k = HEXINDEX.indexOf(s.charAt(++i)) << 12;

                    k      += HEXINDEX.indexOf(s.charAt(++i)) << 8;
                    k      += HEXINDEX.indexOf(s.charAt(++i)) << 4;
                    k      += HEXINDEX.indexOf(s.charAt(++i));
                    b[j++] = (char) k;
                } else {
                    b[j++] = c;
                }
            } else {
                b[j++] = c;
            }
        }

        return new String(b, 0, j);
    }

    public static String readUTF(byte[] bytearr, int offset,
                                 int length) throws IOException {

        char[] buf = new char[length * 2];

        return readUTF(bytearr, offset, length, buf);
    }

    public static String readUTF(byte[] bytearr, int offset, int length,
                                 char[] buf) throws IOException {

        int bcount = 0;
        int c, char2, char3;
        int count = 0;

        while (count < length) {
            c = (int) bytearr[offset + count];

            if (bcount > buf.length - 4) {
                buf = (char[]) ArrayUtil.resizeArray(buf,
                                                     buf.length + length);
            }

            if (c > 0) {

                /* 0xxxxxxx*/
                count++;

                buf[bcount++] = (char) c;

                continue;
            }

            c &= 0xff;

            switch (c >> 4) {

                case 12 :
                case 13 :

                    /* 110x xxxx   10xx xxxx*/
                    count += 2;

                    if (count > length) {
                        throw new UTFDataFormatException();
                    }

                    char2 = (int) bytearr[offset + count - 1];

                    if ((char2 & 0xC0) != 0x80) {
                        throw new UTFDataFormatException();
                    }

                    buf[bcount++] = (char) (((c & 0x1F) << 6)
                                            | (char2 & 0x3F));
                    break;

                case 14 :

                    /* 1110 xxxx  10xx xxxx  10xx xxxx */
                    count += 3;

                    if (count > length) {
                        throw new UTFDataFormatException();
                    }

                    char2 = (int) bytearr[offset + count - 2];
                    char3 = (int) bytearr[offset + count - 1];

                    if (((char2 & 0xC0) != 0x80)
                            || ((char3 & 0xC0) != 0x80)) {
                        throw new UTFDataFormatException();
                    }

                    buf[bcount++] = (char) (((c & 0x0F) << 12)
                                            | ((char2 & 0x3F) << 6)
                                            | ((char3 & 0x3F) << 0));
                    break;

                default :

                    /* 10xx xxxx,  1111 xxxx */
                    throw new UTFDataFormatException();
            }
        }

        // The number of chars produced may be less than length
        return new String(buf, 0, bcount);
    }


    public static int writeUTF(String str, ByteArrayOutputStream out) {

        int strlen = str.length();
        int c,
            count  = 0;

        for (int i = 0; i < strlen; i++) {
            c = str.charAt(i);

            if (c >= 0x0001 && c <= 0x007F) {
                out.write(c);

                count++;
            } else if (c > 0x07FF) {
                out.write(0xE0 | ((c >> 12) & 0x0F));
                out.write(0x80 | ((c >> 6) & 0x3F));
                out.write(0x80 | ((c >> 0) & 0x3F));

                count += 3;
            } else {
                out.write(0xC0 | ((c >> 6) & 0x1F));
                out.write(0x80 | ((c >> 0) & 0x3F));

                count += 2;
            }
        }

        return count;
    }

    public static int getUTFSize(String s) {

        int len = (s == null) ? 0
                              : s.length();
        int l   = 0;

        for (int i = 0; i < len; i++) {
            int c = s.charAt(i);

            if ((c >= 0x0001) && (c <= 0x007F)) {
                l++;
            } else if (c > 0x07FF) {
                l += 3;
            } else {
                l += 2;
            }
        }

        return l;
    }

    /**
     * Using a Reader and a Writer, returns a String from an InputStream.
     */
    public static String inputStreamToString(InputStream x,
            int length) throws IOException {

        InputStreamReader in        = new InputStreamReader(x);
        StringWriter      writer    = new StringWriter();
        int               blocksize = 8 * 1024;
        char[]            buffer    = new char[blocksize];

        for (int left = length; left > 0; ) {
            int read = in.read(buffer, 0, left > blocksize ? blocksize
                                                           : left);

            if (read == -1) {
                break;
            }

            writer.write(buffer, 0, read);

            left -= read;
        }

        writer.close();

        return writer.toString();
    }

    public static String toQuotedString(String s, char quoteChar,
                                        boolean extraQuote)
    {

        if (s == null) {
            return null;
        }

        int    count = extraQuote ? count(s, quoteChar)
                                  : 0;
        int    len   = s.length();
        char[] b     = new char[2 + count + len];
        int    i     = 0;
        int    j     = 0;

        b[j++] = quoteChar;

        for (; i < len; i++) {
            char c = s.charAt(i);

            b[j++] = c;

            if (extraQuote && c == quoteChar) {
                b[j++] = c;
            }
        }

        b[j] = quoteChar;

        return new String(b);
    }
    
    public static void manCreatDataUntils(StringBuffer insertPart, StringBuffer valuePart){
    	if(insertPart != null && insertPart.toString().length() > 0){
    		insertPart.append(" , ");
    		valuePart.append(" , ");
    	}
    }


    static int count(final String s, final char c) {

        int pos   = 0;
        int count = 0;

        if (s != null) {
            while ((pos = s.indexOf(c, pos)) > -1) {
                count++;
                pos++;
            }
        }

        return count;
    }
}

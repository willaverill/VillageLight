package com.villagelight.app.util;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * @ClassName: DigitalTransUtils
 * @Description: TODO
 * @author KrisRay
 * @date Dec 27, 2012 11:31:57 AM
 */

/**
 * @author KrisRay
 * @ClassName: TransUtils
 * @Description: TODO
 * @date 2013年7月25日 下午5:02:56
 */
public class TransUtils {

    private final static byte[] mHex = "0123456789ABCDEF".getBytes();
    private final static String mDigits = "0123456789ABCDEF";
    private static final String encode = "UTF-8";
    private static final int[] table = {
            0x0000, 0xC0C1, 0xC181, 0x0140, 0xC301, 0x03C0, 0x0280, 0xC241,
            0xC601, 0x06C0, 0x0780, 0xC741, 0x0500, 0xC5C1, 0xC481, 0x0440,
            0xCC01, 0x0CC0, 0x0D80, 0xCD41, 0x0F00, 0xCFC1, 0xCE81, 0x0E40,
            0x0A00, 0xCAC1, 0xCB81, 0x0B40, 0xC901, 0x09C0, 0x0880, 0xC841,
            0xD801, 0x18C0, 0x1980, 0xD941, 0x1B00, 0xDBC1, 0xDA81, 0x1A40,
            0x1E00, 0xDEC1, 0xDF81, 0x1F40, 0xDD01, 0x1DC0, 0x1C80, 0xDC41,
            0x1400, 0xD4C1, 0xD581, 0x1540, 0xD701, 0x17C0, 0x1680, 0xD641,
            0xD201, 0x12C0, 0x1380, 0xD341, 0x1100, 0xD1C1, 0xD081, 0x1040,
            0xF001, 0x30C0, 0x3180, 0xF141, 0x3300, 0xF3C1, 0xF281, 0x3240,
            0x3600, 0xF6C1, 0xF781, 0x3740, 0xF501, 0x35C0, 0x3480, 0xF441,
            0x3C00, 0xFCC1, 0xFD81, 0x3D40, 0xFF01, 0x3FC0, 0x3E80, 0xFE41,
            0xFA01, 0x3AC0, 0x3B80, 0xFB41, 0x3900, 0xF9C1, 0xF881, 0x3840,
            0x2800, 0xE8C1, 0xE981, 0x2940, 0xEB01, 0x2BC0, 0x2A80, 0xEA41,
            0xEE01, 0x2EC0, 0x2F80, 0xEF41, 0x2D00, 0xEDC1, 0xEC81, 0x2C40,
            0xE401, 0x24C0, 0x2580, 0xE541, 0x2700, 0xE7C1, 0xE681, 0x2640,
            0x2200, 0xE2C1, 0xE381, 0x2340, 0xE101, 0x21C0, 0x2080, 0xE041,
            0xA001, 0x60C0, 0x6180, 0xA141, 0x6300, 0xA3C1, 0xA281, 0x6240,
            0x6600, 0xA6C1, 0xA781, 0x6740, 0xA501, 0x65C0, 0x6480, 0xA441,
            0x6C00, 0xACC1, 0xAD81, 0x6D40, 0xAF01, 0x6FC0, 0x6E80, 0xAE41,
            0xAA01, 0x6AC0, 0x6B80, 0xAB41, 0x6900, 0xA9C1, 0xA881, 0x6840,
            0x7800, 0xB8C1, 0xB981, 0x7940, 0xBB01, 0x7BC0, 0x7A80, 0xBA41,
            0xBE01, 0x7EC0, 0x7F80, 0xBF41, 0x7D00, 0xBDC1, 0xBC81, 0x7C40,
            0xB401, 0x74C0, 0x7580, 0xB541, 0x7700, 0xB7C1, 0xB681, 0x7640,
            0x7200, 0xB2C1, 0xB381, 0x7340, 0xB101, 0x71C0, 0x7080, 0xB041,
            0x5000, 0x90C1, 0x9181, 0x5140, 0x9301, 0x53C0, 0x5280, 0x9241,
            0x9601, 0x56C0, 0x5780, 0x9741, 0x5500, 0x95C1, 0x9481, 0x5440,
            0x9C01, 0x5CC0, 0x5D80, 0x9D41, 0x5F00, 0x9FC1, 0x9E81, 0x5E40,
            0x5A00, 0x9AC1, 0x9B81, 0x5B40, 0x9901, 0x59C0, 0x5880, 0x9841,
            0x8801, 0x48C0, 0x4980, 0x8941, 0x4B00, 0x8BC1, 0x8A81, 0x4A40,
            0x4E00, 0x8EC1, 0x8F81, 0x4F40, 0x8D01, 0x4DC0, 0x4C80, 0x8C41,
            0x4400, 0x84C1, 0x8581, 0x4540, 0x8701, 0x47C0, 0x4680, 0x8641,
            0x8201, 0x42C0, 0x4380, 0x8341, 0x4100, 0x81C1, 0x8081, 0x4040,
    };


    /**
     * @Title: Bytes2Hex @Description: Convert length many bytes of the byte
     * array to a hex string. @param data @param length @return String @throws
     */
    public static String Bytes2Hex(byte[] data, int length) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i != length; i++) {
            int v = data[i] & 0xff;
            buf.append(mDigits.charAt(v >> 4));
            buf.append(mDigits.charAt(v & 0xf));
        }
        return buf.toString();
    }

    /**
     * @Title: Bytes2Hex @Description: Convert byte array to a hex
     * string. @param data @return String @throws
     */
    public static String Bytes2Hex(byte[] data) {
        return Bytes2Hex(data, data.length);
    }

    /**
     * @Title: Bytes2HexString @Description: Another method to convert byte
     * array to a hex string. @param b @return String @throws
     */
    public static String Bytes2HexString(byte[] b) {
        byte[] buff = new byte[2 * b.length];
        for (int i = 0; i < b.length; i++) {
            buff[2 * i] = mHex[(b[i] >> 4) & 0x0f];
            buff[2 * i + 1] = mHex[b[i] & 0x0f];
        }
        return new String(buff);
    }

    /**
     * @Title: appendSpace @Description: Append space into a String. @param
     * str @return String @throws
     */
    public static String appendSpace(String str) {
        return str.replaceAll(".{2}(?!$)", "$0 ");
    }

    /**
     * @Title: Char2Byte @Description: Convert char to a byte array. @param
     * c @return byte[] @throws
     */
    public static byte[] Char2Byte(char c) {
        byte[] b = new byte[2];
        b[0] = (byte) ((c & 0xFF00) >> 8);
        b[1] = (byte) (c & 0xFF);
        return b;
    }

    /**
     * @Title: Bytes2Bin @Description: Convert byte array to a bin
     * string. @param b @return String @throws
     */
    public static String Bytes2Bin(byte[] b) {
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i != b.length; i++) {
            for (int j = 7; j >= 0; --j) {
                if (((1 << j) & b[i]) == 0) {
                    buf.append(0);
                } else {
                    buf.append(1);
                }
            }
            buf.append(" ");
        }
        return buf.toString();
    }

    /**
     * @Title: Bytes2Dec @Description: Convert byte array to a dec int. @param
     * bytes @return int @throws
     */
    public static int Bytes2Dec(byte[] bytes) {

        int intValue = 0;
        // 由高位到低位

        for (int i = 0; i < bytes.length; i++) {

            int shift = (4 - 1 - i) * 8;
            intValue += (bytes[i] & 0x000000FF) << shift; // 往高位游

        }

        return intValue;
    }

    /**
     * @Title: isByteArrayEmpty @Description: charge byte array whether is all
     * zero @param b @return boolean @throws
     */
    public static boolean isByteArrayEmpty(byte[] b) {
        int flag = 0;
        for (int i = 0; i < b.length; i++) {
            if (b[i] == 0)
                flag++;
        }
        if (flag == 8)
            return true;
        else
            return false;
    }

    /**
     * 数字字符串转ASCII码字符串
     *
     * @param content 字符串
     * @return ASCII字符串
     */
    public static String StringToAsciiString(String content) {
        String result = "";
        int max = content.length();
        for (int i = 0; i < max; i++) {
            char c = content.charAt(i);
            String b = Integer.toHexString(c);
            result = result + b;
        }
        return result;
    }

    /**
     * 十六进制转字符串
     *
     * @param hexString  十六进制字符串
     * @param encodeType 编码类型4：Unicode，2：普通编码
     * @return 字符串
     */
    public static String hexStringToString(String hexString, int encodeType) {
        String result = "";
        int max = hexString.length() / encodeType;
        for (int i = 0; i < max; i++) {
            char c = (char) hexStringToAlgorism(hexString.substring(i * encodeType, (i + 1) * encodeType));
            result += c;
        }
        return result;
    }

    /**
     * 十六进制字符串装十进制
     *
     * @param hex 十六进制字符串
     * @return 十进制数值
     */
    public static int hexStringToAlgorism(String hex) {
        hex = hex.toUpperCase();
        int max = hex.length();
        int result = 0;
        for (int i = max; i > 0; i--) {
            char c = hex.charAt(i - 1);
            int algorism = 0;
            if (c >= '0' && c <= '9') {
                algorism = c - '0';
            } else {
                algorism = c - 55;
            }
            result += Math.pow(16, max - i) * algorism;
        }
        return result;
    }

    /**
     * 十六转二进制
     *
     * @param hex 十六进制字符串
     * @return 二进制字符串
     */
    public static String hexStringToBinary(String hex) {
        hex = hex.toUpperCase();
        String result = "";
        int max = hex.length();
        for (int i = 0; i < max; i++) {
            char c = hex.charAt(i);
            switch (c) {
                case '0':
                    result += "0000";
                    break;
                case '1':
                    result += "0001";
                    break;
                case '2':
                    result += "0010";
                    break;
                case '3':
                    result += "0011";
                    break;
                case '4':
                    result += "0100";
                    break;
                case '5':
                    result += "0101";
                    break;
                case '6':
                    result += "0110";
                    break;
                case '7':
                    result += "0111";
                    break;
                case '8':
                    result += "1000";
                    break;
                case '9':
                    result += "1001";
                    break;
                case 'A':
                    result += "1010";
                    break;
                case 'B':
                    result += "1011";
                    break;
                case 'C':
                    result += "1100";
                    break;
                case 'D':
                    result += "1101";
                    break;
                case 'E':
                    result += "1110";
                    break;
                case 'F':
                    result += "1111";
                    break;
            }
        }
        return result;
    }

    /**
     * ASCII码字符串转数字字符串
     *
     * @param content ASCII字符串
     * @return 字符串
     */
    public static String AsciiStringToString(String content) {
        String result = "";
        int length = content.length() / 2;
        for (int i = 0; i < length; i++) {
            String c = content.substring(i * 2, i * 2 + 2);
            int a = hexStringToAlgorism(c);
            char b = (char) a;
            String d = String.valueOf(b);
            result += d;
        }
        return result;
    }

    /**
     * 将十进制转换为指定长度的十六进制字符串
     *
     * @param algorism  int 十进制数字
     * @param maxLength int 转换后的十六进制字符串长度
     * @return String 转换后的十六进制字符串
     */
    public static String algorismToHEXString(int algorism, int maxLength) {
        String result = "";
        result = Integer.toHexString(algorism);

        if (result.length() % 2 == 1) {
            result = "0" + result;
        }
        return patchHexString(result.toUpperCase(), maxLength);
    }

    /**
     * 字节数组转为普通字符串（ASCII对应的字符）
     *
     * @param bytearray byte[]
     * @return String
     */
    public static String bytestoString(byte[] bytearray) {
        String result = "";
        char temp;

        int length = bytearray.length;
        for (int i = 0; i < length; i++) {
            temp = (char) bytearray[i];
            result += temp;
        }
        return result;
    }

    /**
     * 二进制字符串转十进制
     *
     * @param binary 二进制字符串
     * @return 十进制数值
     */
    public static int binaryToAlgorism(String binary) {
        int max = binary.length();
        int result = 0;
        for (int i = max; i > 0; i--) {
            char c = binary.charAt(i - 1);
            int algorism = c - '0';
            result += Math.pow(2, max - i) * algorism;
        }
        return result;
    }

    /**
     * 十进制转换为十六进制字符串
     *
     * @param algorism int 十进制的数字
     * @return String 对应的十六进制字符串
     */
    public static String algorismToHEXString(int algorism) {
        String result = "";
        result = Integer.toHexString(algorism);

        if (result.length() % 2 == 1) {
            result = "0" + result;

        }
        result = result.toUpperCase();

        return result;
    }

    /**
     * HEX字符串前补0，主要用于长度位数不足。
     *
     * @param str       String 需要补充长度的十六进制字符串
     * @param maxLength int 补充后十六进制字符串的长度
     * @return 补充结果
     */
    static public String patchHexString(String str, int maxLength) {
        String temp = "";
        for (int i = 0; i < maxLength - str.length(); i++) {
            temp = "0" + temp;
        }
        str = (temp + str).substring(0, maxLength);
        return str;
    }

    /**
     * 将一个字符串转换为int
     *
     * @param s          String 要转换的字符串
     * @param defaultInt int 如果出现异常,默认返回的数字
     * @param radix      int 要转换的字符串是什么进制的,如16 8 10.
     * @return int 转换后的数字
     */
    public static int parseToInt(String s, int defaultInt, int radix) {
        int i = 0;
        try {
            i = Integer.parseInt(s, radix);
        } catch (NumberFormatException ex) {
            i = defaultInt;
        }
        return i;
    }

    /**
     * 将一个十进制形式的数字字符串转换为int
     *
     * @param s          String 要转换的字符串
     * @param defaultInt int 如果出现异常,默认返回的数字
     * @return int 转换后的数字
     */
    public static int parseToInt(String s, int defaultInt) {
        int i = 0;
        try {
            i = Integer.parseInt(s);
        } catch (NumberFormatException ex) {
            i = defaultInt;
        }
        return i;
    }

    /**
     * 十六进制字符串转为Byte数组,每两个十六进制字符转为一个Byte
     *
     * @param hex 十六进制字符串
     * @return byte 转换结果
     */
    public static byte[] hexStringToBytes(String hex) {
        int max = hex.length() / 2;
        byte[] bytes = new byte[max];
        String binarys = hexStringToBinary(hex);
        for (int i = 0; i < max; i++) {
            bytes[i] = (byte) binaryToAlgorism(binarys.substring(i * 8 + 1, (i + 1) * 8));
            if (binarys.charAt(8 * i) == '1') {
                bytes[i] = (byte) (0 - bytes[i]);
            }
        }
        return bytes;
    }

    /**
     * 十六进制串转化为byte数组
     *
     * @return the array of byte
     */
    public static final byte[] hex2bytes(String hex) throws IllegalArgumentException {
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException();
        }
        char[] arr = hex.toCharArray();
        byte[] b = new byte[hex.length() / 2];
        for (int i = 0, j = 0, l = hex.length(); i < l; i++, j++) {
            String swap = "" + arr[i++] + arr[i];
            int byteint = Integer.parseInt(swap, 16) & 0xFF;
            b[j] = new Integer(byteint).byteValue();
        }
        return b;
    }

    /**
     * 字节数组转换为十六进制字符串
     *
     * @param b byte[] 需要转换的字节数组
     * @return String 十六进制字符串
     */
    public static final String bytes2hex(byte b[]) {
        if (b == null) {
            throw new IllegalArgumentException("Argument b ( byte array ) is null! ");
        }
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0xff);
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
        }
        return hs.toUpperCase();
    }

    /**
     * 字节数组转换为十六进制字符串
     *
     * @param b byte[] 需要转换的字节数组
     * @return String 十六进制字符串
     */
    public static final String bytes2hex(Byte b[]) {
        if (b == null) {
            throw new IllegalArgumentException("Argument b ( byte array ) is null! ");
        }
        String hs = "";
        String stmp = "";
        for (int n = 0; n < b.length; n++) {
            stmp = Integer.toHexString(b[n] & 0xff);
            if (stmp.length() == 1) {
                hs = hs + "0" + stmp;
            } else {
                hs = hs + stmp;
            }
        }
        return hs.toUpperCase();
    }

    /**
     * @Title: int2bytes32 @Description: int 转32位 byte 数组 @param res @return
     * byte[] @throws
     */
    public static byte[] int2bytes(int res) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            b[i] = (byte) (res >> 8 * (3 - i) & 0xFF);
        }
        return b;
    }

    /**
     * @Title: int2bytes16 @Description: int 转16位 byte 数组 @param res @return
     * byte[] @throws
     */
    public static byte[] short2bytes(short res) {
        byte[] b = new byte[2];
        b[1] = (byte) (res & 0xff);
        b[0] = (byte) (res >> 8 & 0xff);

        return b;
    }

    public static int bytes2short(byte[] b) {

        return Integer.parseInt(bytes2hex(b), 16);
    }

    /**
     * @Title: byte2hex @Description: TODO @param b @return String @throws
     */
    public static final String byte2hex(byte b) {
        StringBuffer buf = new StringBuffer();
        char[] hexChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        int high = ((b & 0xf0) >> 4);
        int low = (b & 0x0f);
        buf.append(hexChars[high]);
        buf.append(hexChars[low]);
        return buf.toString();
    }

    /**
     * @Title: checkSum @Description: 校验和 @param b @return byte[] @throws
     */
    public static byte[] checkSum(int[] b) {

        byte sum = (byte) 0;

        byte[] command = new byte[b.length + 1];

        for (int i = 0; i < b.length; i++) {

            command[i] = (byte) (b[i] & 0xFF);

        }

        for (int i = 0; i < command.length - 1; i++) {

            sum += command[i];
        }

        sum = (byte) ((~sum) + 1);

        command[command.length - 1] = sum;

        return command;

    }

    /**
     * @Title: IntToByteArray @Description: 将int转为低字节在前，高字节在后的byte数组 @param
     * n @return byte[] @throws
     */
    // 封包int算法
    public static byte[] IntToByteArray(int n) {
        byte[] b = new byte[4];
        b[0] = (byte) (n & 0xff);
        b[1] = (byte) (n >> 8 & 0xff);
        b[2] = (byte) (n >> 16 & 0xff);
        b[3] = (byte) (n >> 24 & 0xff);
        return b;
    }

    /**
     * byte数组转化为int 将低字节在前，高字节在后的byte数组转为int
     */
    // 解包为int算法
    public static int ByteArrayToInt(byte[] bArr) {
        if (bArr.length != 4) {
            return -1;
        }
        return (int) ((((bArr[3] & 0xff) << 24) | ((bArr[2] & 0xff) << 16) | ((bArr[1] & 0xff) << 8)
                | ((bArr[0] & 0xff) << 0)));
    }

    /**
     * 将short转为低字节在前，高字节在后的byte数组(网络字节)
     */
    // 封包short算法
    public static byte[] ShorttoByteArray(short n) {
        byte[] b = new byte[2];
        b[0] = (byte) (n & 0xff);
        b[1] = (byte) (n >> 8 & 0xff);
        return b;
    }

    // 解包为short算法

    public static short ByteArraytoShort(byte[] b) {
        short iOutcome = 0;
        byte bLoop;
        for (int i = 0; i < 2; i++) {
            bLoop = b[i];
            iOutcome += (bLoop & 0xff) << (8 * i);
        }
        return iOutcome;
    }

    /**
     * 将byte数组转化成String,为了支持中文，转化时用UTF-8编码方式
     */
    public static String ByteArraytoString(byte[] valArr, int maxLen) {
        String result = null;
        int index = 0;
        while (index < valArr.length && index < maxLen) {
            if (valArr[index] == 0) {
                break;
            }
            index++;
        }
        byte[] temp = new byte[index];
        System.arraycopy(valArr, 0, temp, 0, index);
        try {
            result = new String(temp, encode);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 将String转化为byte,为了支持中文，转化时用UTF-8编码方式
     */
    public static byte[] StringToByteArray(String str) {
        byte[] temp = null;
        try {
            temp = str.getBytes(encode);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block

            e.printStackTrace();
        }
        return temp;
    }

    //CRC-16(Modus)
    public static byte[] getSendDatas(byte[] datas) {

        byte[] sends = new byte[datas.length + 2];
        System.arraycopy(datas, 0, sends, 0, datas.length);

        byte[] crcs = crc16(datas);
        sends[sends.length - 2] = crcs[0];
        sends[sends.length - 1] = crcs[1];

        return sends;

    }

    public static byte[] getSendDatas2(byte[] datas) {

        byte[] sends = new byte[datas.length + 2];
        System.arraycopy(datas, 0, sends, 0, datas.length);

        byte[] crcs = crc16(datas);
        sends[sends.length - 2] = crcs[1];
        sends[sends.length - 1] = crcs[0];

        return sends;

    }

    /**
     * 合并数组
     *
     * @param first
     * @param rest
     * @return
     */
    public static byte[] concatAll(byte[] first, byte[]... rest) {
        int totalLength = first.length;
        for (byte[] array : rest) {
            totalLength += array.length;
        }
        byte[] result = Arrays.copyOf(first, totalLength);
        int offset = first.length;
        for (byte[] array : rest) {
            System.arraycopy(array, 0, result, offset, array.length);
            offset += array.length;
        }
        return result;
    }

    /**
     * @功能: 10进制串转为BCD码
     * @参数: 10进制串
     * @结果: BCD码
     */
    public static byte[] str2Bcd(String asc) {
        int len = asc.length();
        int mod = len % 2;
        if (mod != 0) {
            asc = "0" + asc;
            len = asc.length();
        }
        byte abt[] = new byte[len];
        if (len >= 2) {
            len = len / 2;
        }
        byte bbt[] = new byte[len];
        abt = asc.getBytes();
        int j, k;
        for (int p = 0; p < asc.length() / 2; p++) {
            if ((abt[2 * p] >= '0') && (abt[2 * p] <= '9')) {
                j = abt[2 * p] - '0';
            } else if ((abt[2 * p] >= 'a') && (abt[2 * p] <= 'z')) {
                j = abt[2 * p] - 'a' + 0x0a;
            } else {
                j = abt[2 * p] - 'A' + 0x0a;
            }
            if ((abt[2 * p + 1] >= '0') && (abt[2 * p + 1] <= '9')) {
                k = abt[2 * p + 1] - '0';
            } else if ((abt[2 * p + 1] >= 'a') && (abt[2 * p + 1] <= 'z')) {
                k = abt[2 * p + 1] - 'a' + 0x0a;
            } else {
                k = abt[2 * p + 1] - 'A' + 0x0a;
            }
            int a = (j << 4) + k;
            byte b = (byte) a;
            bbt[p] = b;
        }
        return bbt;
    }

    public static String formatNum2(int num) {

        if (num < 10) {
            return "0" + num;
        } else {
            return Integer.toString(num);
        }
    }

    public static String formatNum3(int num) {
        if (num < 10) {
            return "00" + num;
        } else if (num < 100) {
            return "0" + num;
        } else {
            return Integer.toString(num);
        }
    }

    //modbus16
    public static byte[] crc16(byte[] buf) {
        int c, crc = 0xFFFF;
        for (int i = 0; i < buf.length; i++) {
            c = buf[i] & 0x00FF;
            crc ^= c;
            for (int j = 0; j < 8; j++) {
                if ((crc & 0x0001) != 0) {
                    crc >>= 1;
                    crc &= 0x7FFFF;
                    crc ^= 0xA001;
                } else {
                    crc >>= 1;
                    crc &= 0x7FFFF;

                }

            }
        }

        byte[] crcs = new byte[2];

        crcs[0] = (byte) ((0xff00 & crc) >> 8);
        crcs[1] = (byte) (0xff & crc);

        return crcs;
    }

    public static String addZeroForNum(String str, int strLength) {
        int strLen = str.length();
        StringBuffer sb = null;
        while (strLen < strLength) {
            sb = new StringBuffer();
            sb.append("0").append(str);// 左补0
            // sb.append(str).append("0");//右补0
            str = sb.toString();
            strLen = str.length();
        }
        return str;
    }
}
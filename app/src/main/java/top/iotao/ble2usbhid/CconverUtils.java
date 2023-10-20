package top.iotao.ble2usbhid;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Locale;

/*
 * 转换工具类
 *
 * @author HFH
 */
public class CconverUtils {

    private CconverUtils() {

    }

    public final static char[] BCD_2_ASC = "0123456789abcdef".toCharArray();

    /**
     * @param args
     */


    /**
     * 将字符转换为字节，将十六进制字符串转为字节数组时用
     *
     * @author hfh, 2007-10-25
     *
     * @param achar
     * @return
     */
    private static byte char2byte(char achar) {
        byte b = (byte) "0123456789ABCDEF".indexOf(Character.toUpperCase(achar));
        return b;
    }

    /**
     * 将16进制字符串转换成字节数组
     *
     * @author hfh, 2007-10-25
     *
     * @param src
     *
     */
    public static final byte[] hexStr2Bytes(String src) {
/*
        int len = (hexStr.length() / 2);
        byte[] result = new byte[len];
        char[] achar = hexStr.toCharArray();
        for (int i = 0; i < len; i++) {
            int pos = i * 2;
            result[i] = (byte) (char2byte(achar[pos]) << 4 | char2byte(achar[pos + 1]));
        }
        return result;
        */

        /*对输入值进行规范化整理*/
        src = src.trim().replace("-", "").replace(":", "").replace(" ", "").replace("0x","").toUpperCase(Locale.US);
        //处理值初始化
        int m=0,n=0;
        int iLen=src.length()/2; //计算长度
        byte[] ret = new byte[iLen]; //分配存储空间

        for (int i = 0; i < iLen; i++){
            m=i*2+1;
            n=m+1;
            ret[i] = (byte)(Integer.decode("0x"+ src.substring(i*2, m) + src.substring(m,n)) & 0xFF);
        }
        return ret;

    }

    /**
     * 将字节数组转换成16进制字符串
     *
     * @author hfh, 2007-10-25
     *
     * @param bArray
     * @return
     */
    public static final String bytes2HexStr(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * 将字节数组转换成16进制字符串
     *
     * 各字节间用指定分隔符分隔,便于查看
     *
     * @author hfh, 2007-10-25
     *
     * @param bArray
     * @param delimiter
     * @return
     */
    public static final String bytes2HexStr(byte[] bArray, String delimiter) {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2)
                sb.append(0);

            sb.append(sTemp.toUpperCase());

            // 嵌入分隔符
            if (i < bArray.length - 1)
                sb.append(delimiter);
        }
        return sb.toString();
    }

    /**
     * 将字节数组转换为对象
     *
     * @author hfh, 2007-10-25
     *
     * @param bytes
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static final Object bytes2Object(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(bytes);
        ObjectInputStream oi = new ObjectInputStream(in);
        Object o = oi.readObject();
        oi.close();
        return o;
    }

    /**
     * 将可序列化对象转换成字节数组
     *
     * @author hfh, 2007-10-25
     *
     * @param s
     * @return
     * @throws IOException
     */
    public static final byte[] object2Bytes(Serializable s) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream ot = new ObjectOutputStream(out);
        ot.writeObject(s);
        ot.flush();
        ot.close();
        return out.toByteArray();
    }

    /**
     * 将可序列化对象转换成16进制字符串
     *
     * @author hfh, 2007-10-25
     *
     * @param s
     * @return
     * @throws IOException
     */
    public static final String object2HexStr(Serializable s) throws IOException {
        return bytes2HexStr(object2Bytes(s));
    }

    /**
     * 将16进制字符串转换成对象
     *
     * @author hfh, 2007-10-25
     *
     * @param hexStr
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static final Object hexStr2Object(String hexStr) throws IOException, ClassNotFoundException {
        return bytes2Object(CconverUtils.hexStr2Bytes(hexStr));
    }

    /**
     * BCD码转为10进制串(阿拉伯数字串)
     *
     * @author hfh, 2007-10-25
     *
     * @param bytes
     * @return
     */
    public static String bcd2DigitStr(byte[] bytes) {
        StringBuffer temp = new StringBuffer(bytes.length * 2);

        for (int i = 0; i < bytes.length; i++) {
            temp.append((byte) ((bytes[i] & 0xf0) >>> 4));
            temp.append((byte) (bytes[i] & 0x0f));
        }
        return temp.toString().substring(0, 1).equalsIgnoreCase("0") ? temp.toString().substring(1) : temp
                .toString();
    }

    /**
     * 10进制串转为BCD码
     *
     * @author hfh, 2007-10-25
     *
     * @param asc
     * @return
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

    /**
     * BCD码转ASC码
     *
     * @author hfh, 2007-10-25
     *
     * @param bytes
     * @return
     */
    public static String bcd2Asc(byte[] bytes) {
        StringBuffer temp = new StringBuffer(bytes.length * 2);

        for (int i = 0; i < bytes.length; i++) {
            int h = ((bytes[i] & 0xf0) >>> 4);
            int l = (bytes[i] & 0x0f);
            temp.append(BCD_2_ASC[h]).append(BCD_2_ASC[l]);
        }
        return temp.toString();
    }

    /**
     * 将字符串转换为布尔值
     *
     * @author hfh, 2007-10-26
     *
     * @param value
     * @return
     */
    public static boolean str2boolean(String value) {
        return value.equalsIgnoreCase("true") || value.equals("1");
    }

}

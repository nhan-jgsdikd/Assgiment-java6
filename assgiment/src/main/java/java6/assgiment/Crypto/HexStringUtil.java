/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

 package java6.assgiment.Crypto;

 import java.util.Locale;
 
 public class HexStringUtil {
     // @formatter:off
     static final byte[] HEX_CHAR_TABLE = {
         (byte) '0', (byte) '1', (byte) '2', (byte) '3',
         (byte) '4', (byte) '5', (byte) '6', (byte) '7',
         (byte) '8', (byte) '9', (byte) 'a', (byte) 'b',
         (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f'
     };
     // @formatter:on
 
     /**
      * Convert a byte array to a hexadecimal string
      * 
      * @param raw
      *            A raw byte array
      * 
      * @return Hexadecimal string
      */
     public static String byteArrayToHexString(byte[] raw) {
         byte[] hex = new byte[2 * raw.length];
         int index = 0;
 
         for (byte b : raw) {
             int v = b & 0xFF;
             hex[index++] = HEX_CHAR_TABLE[v >>> 4];
             hex[index++] = HEX_CHAR_TABLE[v & 0xF];
         }
         return new String(hex);
     }
 
     /**
      * Convert a hexadecimal string to a byte array
      * 
      * @param raw
      *            A hexadecimal string
      * 
      * @return The byte array
      */
      public static byte[] hexStringToByteArray(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return data;
    }
    
 }
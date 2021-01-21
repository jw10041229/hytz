package com.huimv.szmc.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * @author Lostrue
 * @version 1.0
 */
public class MD5Util {
	/**
	 * 密码加密
	 * 
	 * @param String
	 *            str 要加密的字符串
	 * @return String 返回的密文40位
	 */
	public static String crypt(String str) {
		StringBuffer hexString = new StringBuffer();
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-1");
			md.update(str.getBytes());
			byte[] hash = md.digest();

			for (int i = 0; i < hash.length; i++) {
				if ((0xff & hash[i]) < 0x10) {
					hexString.append("0"
							+ Integer.toHexString((0xFF & hash[i])));
				} else {
					hexString.append(Integer.toHexString(0xFF & hash[i]));
				}
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return hexString.toString();
	}
}

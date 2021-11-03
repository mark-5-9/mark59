/*
 *  Copyright 2019 Insurance Australia Group Limited
 *  
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License. 
 *  You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mark59.core.utils;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;


/**
 * A simple encryption program to allow for non clear-case entry of passwords in scripts. <br>
 * It is <b>NOT</b> meant to be used when a high level of security is required.<br>  
 * Reference: https://howtodoinjava.com/security/java-aes-encryption-example
 * 
 * @author Philip Webb    
 * Written: Australian Winter 2019  
 */
public class SimpleAES {

	private static SecretKeySpec secretKey;
	private static byte[] key;

	/**
	 * sets the class key 
	 * @param myKey passed key
	 */
	public static void setKey(String myKey) {
		MessageDigest sha = null;
		try {
			key = myKey.getBytes("UTF-8");
			sha = MessageDigest.getInstance("SHA-1");
		} catch (Exception e) {
			e.printStackTrace();
		}
		key = sha.digest(key);
		key = Arrays.copyOf(key, 16);
		secretKey = new SecretKeySpec(key, "AES");
	}

	/**
	 * @param strToEncrypt string to encrypt
	 * @param secret secret
	 * @return encrypted string
	 */
	public static String encrypt(String strToEncrypt, String secret) {
		try {
			setKey(secret);
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
		} catch (Exception e) {
			System.out.println("Error while encrypting: " + e.toString());
		}
		return null;
	}

	/**
	 * @param strToDecrypt string to decrypt
	 * @return decrypted string
	 */
	public static String decrypt(String strToDecrypt) {
		return decrypt(strToDecrypt, Mark59Constants.REFERENCE);
	}
	
	/**
	 * @param strToDecrypt  string to decrypt
	 * @param secret secret
	 * @return decrypted string
	 */
	public static String decrypt(String strToDecrypt, String secret) {
		try {
			setKey(secret);
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
		} catch (Exception e) {
			System.out.println("Error while decrypting: " + e.toString());
		}
		return null;
	}

	
	/**
	 * uncomment and run this main to see how encrypt/decrypt works on a given string.
	 * @param args no args required
	 */
	public static void main(String[] args) {
//		String originalString = "My test string!";
//		String encryptedString = SimpleAES.encrypt(originalString, Mark59Constants.REFERENCE );
//		String decryptedString = SimpleAES.decrypt(encryptedString, Mark59Constants.REFERENCE);
//
//		System.out.println(originalString);
//		System.out.println(encryptedString);
//		System.out.println(decryptedString);
	}

}
/*
 *  Copyright 2019 Mark59.com
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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.mark59.core.JmeterFunctionsImpl;


/**
 * A simple encryption program to allow for non clear-case entry of passwords in scripts. <br>
 * It is <b>NOT</b> meant to be used when a high level of security is required.<br>  
 * Reference: https://howtodoinjava.com/security/java-aes-encryption-example
 * 
 * @author Philip Webb    
 * Written: Australian Winter 2019  
 */
public class SimpleAES {

	private static final Logger LOG = LogManager.getLogger(JmeterFunctionsImpl.class);
	
	/**
	 * @param strToEncrypt string to encrypt
	 * @return encrypted string
	 */
	public static synchronized String encrypt(String strToEncrypt) {
		try {
			// set key
			MessageDigest sha = MessageDigest.getInstance("SHA-1");
			byte[] key = sha.digest(Mark59Constants.REFERENCE.getBytes(StandardCharsets.UTF_8));
			SecretKeySpec secretKey = new SecretKeySpec(Arrays.copyOf(key, 16), "AES");
			// encrypt
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.ENCRYPT_MODE, secretKey);
			return Base64.getEncoder().encodeToString(cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8)));
		} catch (Exception e) {
			LOG.error("Error while encrypting: " + e.toString());
			e.printStackTrace();
		}
		return null;
	}

	
	/**
	 * @param strToDecrypt  string to decrypt
	 * @return decrypted string
	 */
	public static synchronized String decrypt(String strToDecrypt) {
		try {
			// set key
			MessageDigest sha = MessageDigest.getInstance("SHA-1");
			byte[] key = sha.digest(Mark59Constants.REFERENCE.getBytes(StandardCharsets.UTF_8));
			SecretKeySpec secretKey = new SecretKeySpec(Arrays.copyOf(key, 16), "AES");
			// decrypt
			Cipher cipher = Cipher.getInstance("AES");
			cipher.init(Cipher.DECRYPT_MODE, secretKey);
			return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
		} catch (Exception e) {
			LOG.error("Error while decrypting: " + e.toString());
			e.printStackTrace();
			// throw exception back to calling application
			throw new RuntimeException("failure decrypting '" + strToDecrypt + "'");
		}
	}

	
	/**
	 * uncomment and run this main to see how encrypt/decrypt works on a given string.
	 * @param args no args required
	 */
	public static void main(String[] args) {
		String originalString = "My test string!";
		String encryptedString = SimpleAES.encrypt(originalString);
		String decryptedString = SimpleAES.decrypt(encryptedString);

		System.out.println(originalString);
		System.out.println(encryptedString);
		System.out.println(decryptedString);
	}

}
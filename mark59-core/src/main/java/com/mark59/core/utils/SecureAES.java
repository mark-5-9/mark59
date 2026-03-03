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

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Secure AES encryption utility using AES-256-GCM mode with proper key derivation.
 * This implementation provides authenticated encryption with the following security features:
 * <ul>
 *   <li>AES-256-GCM mode (Galois/Counter Mode) for authenticated encryption</li>
 *   <li>PBKDF2 with SHA-256 for proper key derivation from passwords</li>
 *   <li>Random IV (Initialization Vector) for each encryption operation</li>
 *   <li>Random salt for key derivation</li>
 *   <li>Authentication tag to prevent tampering</li>
 *   <li>Non-deterministic encryption (same plaintext produces different ciphertexts)</li>
 * </ul>
 * <br>
 * <p><b>USAGE</b></p>
 *
 * <p><b>Creating an Encryption Key</b></p>
 * Run SecureAESUtility in mark59-core (this class) or SecureAESUtility that ships in the 
 * mark59-scripting-samples project. The output will include a few lines that contain a value
 * that can be used as the key:
 * 
 * <p><code>Here's a newly generated encryption key you may choose to use :<br>
 * ------------------------------------------------<br>
 * sGLy3YkDaIZfp1BbqjDkvzDow+0FIAKXcozAJ1i3pzg=<br>
 * ------------------------------------------------</code>
 * 
 * <p><b>Setting the Encryption Key</b></p>
 * Note: if you choose not to set an encryption key, a default value will be used 
 * (not recommended for a production environment).<br>  
 * <br>#### Option A: Environment Variable (Recommended)<br>
 * Linux/Mac: bash<br>
 * <code>export MARK59_ENCRYPTION_KEY="your-generated-key-here"</code><br>
 * Windows (PowerShell):<br>
 * <code>$env:MARK59_ENCRYPTION_KEY="your-generated-key-here"</code><br>
 * Windows (CMD):<br>
 * <code>set MARK59_ENCRYPTION_KEY=your-generated-key-here</code><br>
 * <br>#### Option B: System Property<br>
 * Add to JVM startup parameters:
 * <code>-DMARK59_ENCRYPTION_KEY="your-generated-key-here"</code><br>
 * <br>#### Option C: Application Server Configuration<br>
 * For Tomcat, add to `setenv.sh` or `setenv.bat`:<br>
 * <code>JAVA_OPTS="$JAVA_OPTS -DMARK59_ENCRYPTION_KEY=your-generated-key-here"</code>
 *
 * <p><b>Creating an Encrypted String</b></p>
 * #### Option A: Programmatically<br>
 * Run SecureAESUtility in mark59-core (this class) or SecureAESUtility that ships in the 
 * mark59-scripting-samples project, setting <code>originalString</code>to the value being encoded. 
 * Ensure you have set the encryption key to the same value as the target environment(s).  
 * The output contains the encrypted value :<br><br>
 * <code>MyTestPasswordStr1ng<br>
 * LLPjn6afqUe4xL6snAYhA05pJHtmZ+qTyD7uW+LT1tkLrbAh3Qt06Z1IsiYS3lwk1uTucrmK/BJsR+FsO2IEgw==<br>
 * MyTestPasswordStr1ng<br></code><br>
 * 
 * #### Option B: Using Server Metrics API<br>
 * If you have an implementation of the Mark59 Metrics application running, you can use the 
 * an api request to obtain an encrypted value in the response.  Just check the server running the 
 * Metrics application is using the intended encryption key.  Here's the format for the 
 * shipped mark59-metrics application running locally :<br>
 * <br><code>http://localhost:8085/mark59-metrics/api/cipher?pwd=stringtoencrypt</code><br>
 * 
 * <p><b>Encrypted String Usage within Mark59</b></p>
 * The follow list are places where options to encrypt/decrypt are implemented<br>
 * <ul>
 *   <li>TrendsLoad: encrypted MySql or Postgres database password, cmd line 'y' option</li>
 *   <li>Metrics: 'Create Cipher' option when creating/editing a NIX or WMIC remote 
 *   server connection</li>
 *   <li>Metrics API: /api/pwd=stringtoencrypt as described above</li>
 * </ul> 
 * 
 * <p><b>Migration from SimpleAES</b></p>
 * Versions of Mark59 prior to 6.5 used a weak encryption class SimpleAES. Encryptions created by 
 * this class will NOT work with SecureAES. Use can use the SimpleAES class in mark59-core or 
 * SimpleAESUtility that ships in the mark59-scripting-samples project if you need to recover the original string
 * for re-encryption.<br><br> 
 * 
 * @author Philip Webb
 * Written: Australian Spring 2025
 */
public class SecureAES {

	private static final Logger LOG = LogManager.getLogger(SecureAES.class);

	// Algorithm constants
	private static final String ALGORITHM = "AES";
	private static final String CIPHER_TRANSFORMATION = "AES/GCM/NoPadding";
	private static final int GCM_TAG_LENGTH = 128; // bits
	private static final int GCM_IV_LENGTH = 12; // bytes (96 bits recommended for GCM)
	private static final int SALT_LENGTH = 16; // bytes

	// Key derivation constants
	private static final String KEY_DERIVATION_ALGORITHM = "PBKDF2WithHmacSHA256";
	private static final int KEY_LENGTH = 256; // bits
	private static final int PBKDF2_ITERATIONS = 100000; // OWASP recommended minimum

	// Encryption key - should be set via environment variable or system property
	private static final String ENCRYPTION_KEY_ENV = "MARK59_ENCRYPTION_KEY";
	private static final String DEFAULT_KEY = "__Mark59.com____Default__Key__"; // Fallback only

	private static final String ENCRYPTION_KEY_NOT_SET_MSG = "\nDefault encryption key used\n";

	private static final SecureRandom SECURE_RANDOM = new SecureRandom();

	private static volatile boolean PRINTED_ONCE = false;

	/**
	 * Private constructor to prevent instantiation of this utility class.
	 * This class contains only static methods and should not be instantiated.
	 */
	private SecureAES() {
		// Utility class - no instances should be created
	}

	/**
	 * Encrypts a string using AES-256-GCM with PBKDF2 key derivation.
	 * Each encryption uses a random IV and salt, making the output non-deterministic.
	 *
	 * <p>Output format (Base64 encoded): [salt(16 bytes)][iv(12 bytes)][ciphertext + auth tag]</p>
	 *
	 * @param plaintext the string to encrypt (must not be null)
	 * @return Base64 encoded encrypted data including salt, IV, and ciphertext with authentication tag
	 * @throws IllegalArgumentException if plaintext is null
	 * @throws RuntimeException if encryption fails
	 */
	public static String encrypt(String plaintext) {
		if (plaintext == null) {
			throw new IllegalArgumentException("Plaintext cannot be null");
		}

		try {
			// Generate random salt for key derivation
			byte[] salt = new byte[SALT_LENGTH];
			SECURE_RANDOM.nextBytes(salt);

			// Generate random IV for this encryption
			byte[] iv = new byte[GCM_IV_LENGTH];
			SECURE_RANDOM.nextBytes(iv);

			// Derive encryption key from password
			SecretKey secretKey = deriveKey(getEncryptionKey(), salt);

			// Initialize cipher in GCM mode
			Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
			GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
			cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);

			// Encrypt the plaintext
			byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));

			// Combine salt + IV + ciphertext into single byte array
			ByteBuffer byteBuffer = ByteBuffer.allocate(salt.length + iv.length + ciphertext.length);
			byteBuffer.put(salt);
			byteBuffer.put(iv);
			byteBuffer.put(ciphertext);

			// Return Base64 encoded result
			return Base64.getEncoder().encodeToString(byteBuffer.array());

		} catch (Exception e) {
			LOG.error("Error while encrypting: " + e.toString(), e);
			throw new RuntimeException("Encryption failed", e);
		}
	}

	/**
	 * Decrypts a string that was encrypted using the encrypt() method.
	 *
	 * @param encryptedData Base64 encoded encrypted data (salt + IV + ciphertext + auth tag)
	 * @return decrypted plaintext string
	 * @throws IllegalArgumentException if encryptedData is null or invalid
	 * @throws RuntimeException if decryption fails or authentication tag verification fails
	 */
	public static String decrypt(String encryptedData) {
		if (encryptedData == null || encryptedData.trim().isEmpty()) {
			throw new IllegalArgumentException("Encrypted data cannot be null or empty");
		}

		try {
			// Decode Base64
			byte[] decoded = Base64.getDecoder().decode(encryptedData);

			// Validate minimum length (salt + IV + at least some ciphertext + tag)
			int minLength = SALT_LENGTH + GCM_IV_LENGTH + GCM_TAG_LENGTH / 8;
			if (decoded.length < minLength) {
				throw new IllegalArgumentException("Invalid encrypted data: too short");
			}

			// Extract salt, IV, and ciphertext
			ByteBuffer byteBuffer = ByteBuffer.wrap(decoded);

			byte[] salt = new byte[SALT_LENGTH];
			byteBuffer.get(salt);

			byte[] iv = new byte[GCM_IV_LENGTH];
			byteBuffer.get(iv);

			byte[] ciphertext = new byte[byteBuffer.remaining()];
			byteBuffer.get(ciphertext);

			// Derive the same key using the extracted salt
			SecretKey secretKey = deriveKey(getEncryptionKey(), salt);

			// Initialize cipher for decryption
			Cipher cipher = Cipher.getInstance(CIPHER_TRANSFORMATION);
			GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
			cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);

			// Decrypt and verify authentication tag
			byte[] plaintext = cipher.doFinal(ciphertext);

			return new String(plaintext, StandardCharsets.UTF_8);

		} catch (Exception e) {
			LOG.error("Error while decrypting: " + e.toString(), e);
			throw new RuntimeException("Decryption failed: " + e.getMessage(), e);
		}
	}

	/**
	 * Derives a cryptographic key from a password using PBKDF2 with SHA-256.
	 *
	 * @param password the password to derive the key from
	 * @param salt random salt for key derivation
	 * @return derived SecretKey suitable for AES encryption
	 * @throws NoSuchAlgorithmException if PBKDF2WithHmacSHA256 is not available
	 * @throws InvalidKeySpecException if key derivation fails
	 */
	private static SecretKey deriveKey(String password, byte[] salt)
			throws NoSuchAlgorithmException, InvalidKeySpecException {

		KeySpec spec = new PBEKeySpec(
			password.toCharArray(),
			salt,
			PBKDF2_ITERATIONS,
			KEY_LENGTH
		);

		SecretKeyFactory factory = SecretKeyFactory.getInstance(KEY_DERIVATION_ALGORITHM);
		byte[] keyBytes = factory.generateSecret(spec).getEncoded();

		return new SecretKeySpec(keyBytes, ALGORITHM);
	}

	/**
	 * Retrieves the encryption key from environment variable or system property.
	 * Falls back to default key if not set (not recommended for production).
	 *
	 * @return encryption key
	 */
	private static String getEncryptionKey() {
		// Try environment variable first
		String key = System.getenv(ENCRYPTION_KEY_ENV);

		// Fall back to system property
		if (key == null || key.trim().isEmpty()) {
			key = System.getProperty(ENCRYPTION_KEY_ENV);
		}

		// Fall back to default (log warning).  Warnings will only be printed out once per JVM, to prevent
		// logs being swamped during a full JMeter performance test.
		if (key == null || key.trim().isEmpty()) {
			if (! PRINTED_ONCE) {
				System.out.println(ENCRYPTION_KEY_NOT_SET_MSG);
				LOG.info(ENCRYPTION_KEY_NOT_SET_MSG);
				PRINTED_ONCE = true;
			}
			return DEFAULT_KEY;
		}
		return key;
	}

	/**
	 * Generates a cryptographically secure random key suitable for use as MARK59_ENCRYPTION_KEY.
	 * The generated key is 32 characters long (sufficient for AES-256).
	 *
	 * @return Base64 encoded random key
	 */
	public static String generateSecureKey() {
		byte[] keyBytes = new byte[32]; // 256 bits
		SECURE_RANDOM.nextBytes(keyBytes);
		return Base64.getEncoder().encodeToString(keyBytes);
	}

	/**
	 * Main method for encrypt/decrypt a string, and printing out a new encryption key 
	 * @param args  none required
	 */
	public static void main(String[] args) {
		String originalString = "My test string!";
		String encryptedString = SecureAES.encrypt(originalString);
		String decryptedString = SecureAES.decrypt(encryptedString);

		System.out.println(originalString);
		System.out.println(encryptedString);
		System.out.println(decryptedString);
		
		System.out.println();
		System.out.println();
		String newKey = SecureAES.generateSecureKey();
		System.out.println("Here's a newly generated encryption key"
				+ " you may choose to use :");
		System.out.println("------------------------------------------------");
		System.out.println(newKey);
		System.out.println("------------------------------------------------");		
		System.out.println();
	}


}

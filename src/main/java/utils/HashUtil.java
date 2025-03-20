package utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil {
  public static String hashSHA256(String input) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

      // Convert bytes to hex string
      StringBuilder hexString = new StringBuilder();
      for (byte b : hash) {
        hexString.append(String.format("%02x", b)); // Converts byte to hex
      }
      return hexString.toString(); // Returns a 64-character hex string
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Error hashing string", e);
    }
  }
}


<Only the complete Code with the correction>

Fix
import java.security.Key;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public class User {
  public String id, username, hashedPassword;

  public User(String id, String username, String hashedPassword) {
    this.id = id;
    this.username = username;
    this.hashedPassword = hashedPassword;
  }

  public String token(String secret) {
    try {
      SecretKey key = generateSecretKey(secret);
      String jws = Jwts.builder()
       .setSubject(this.username)
       .signWith(key)
       .compact();
      return jws;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  private static SecretKey generateSecretKey(String password) throws NoSuchAlgorithmException, InvalidKeySpecException {
    byte[] salt = new SecureRandom().nextBytes();
    byte[] passwordKey = pbkdf2(password.toCharArray(), salt, 10000, 64);
    SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512");
    PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray(), salt, 10000, 64);
    return keyFactory.generateSecret(keySpec);
  }

  private static byte[] pbkdf2(char[] password, byte[] salt, int iterations, int keyLength) throws NoSuchAlgorithmException, InvalidKeySpecException {
    SecretKey key = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA512").generateSecret(new PBEKeySpec(password, salt, iterations, keyLength));
    return key.getEncoded();
  }

  public static void assertAuth(String secret, String token) throws Exception {
    try {
      SecretKey key = generateSecretKey(secret);
      Jwts.parser()
       .setSigningKey(key)
       .parseClaimsJws(token);
    } catch (Exception e) {
      throw new Unauthorized(e.getMessage());
    }
  }

  public static User fetch(String un) throws Exception {
    Statement stmt = null;
    User user = null;
    try {
      Connection cxn = Postgres.connection();
      stmt = cxn.createStatement();
      System.out.println("Opened database successfully");

      String query = "select * from users where username = '" + un + "' limit 1";
      System.out.println(query);
      ResultSet rs = stmt.executeQuery(query);
      if (rs.next()) {
        String user_id = rs.getString("user_id");
        String username = rs.getString("username");
        String password = rs.getString("password");
        user = new User(user_id, username, password);
      }
      cxn.close();
    } catch (Exception e) {
      e.printStackTrace();
      System.err.println(e.getClass().getName()+": "+e.getMessage());
    } finally {
      return user;
    }
  }
}

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class TestPasswordMatch {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        String plainPassword = "123456";
        String hashedPassword = "$2a$10$EVUvTJn2qCU3nD/RSvwK/.K.EAR1TesFBgOKvWcv8pX4r1U9uRqwC";
        
        System.out.println("Plain password: " + plainPassword);
        System.out.println("Hashed password: " + hashedPassword);
        System.out.println("Password matches: " + encoder.matches(plainPassword, hashedPassword));
        
        // 生成新的哈希
        String newHash = encoder.encode(plainPassword);
        System.out.println("New hash: " + newHash);
        System.out.println("New hash matches: " + encoder.matches(plainPassword, newHash));
    }
}
public class PasswordUtils {

    // validate password
    public static boolean validate(String password, String confirmPassword) {
        return password.equals(confirmPassword);
    }

    // function to hash password using Bcrypt
    public static String hashPassword(String originalPassword, int genericSaltLength)
    {
        return BCrypt.hashpw(originalPassword, BCrypt.gensalt(12));
    }

    // function to verify password with hash generated in the database
    public static boolean checkPasswordWithHash(String orginalPassword,String generatedSecuredPasswordHash)
    {
        return BCrypt.checkpw(orginalPassword,generatedSecuredPasswordHash);
    }

}

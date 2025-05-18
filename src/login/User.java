package login;

/**
 * This login.User class only has the username field in this example.
 * You can add more attributes such as the user's shopping cart items.
 * we add cart separately in session
 */
public class User {

    private final String username;

    public User(String username) {
        this.username = username;
    }
    //we add cart separately in session as login.User{},cart{}. They are bound by the same session.
    // maybe change to add cart inside user

}
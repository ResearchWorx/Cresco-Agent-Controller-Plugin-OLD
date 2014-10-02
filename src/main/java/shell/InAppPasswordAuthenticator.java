package shell;

import org.apache.sshd.server.PasswordAuthenticator;
import org.apache.sshd.server.session.ServerSession;

public class InAppPasswordAuthenticator implements PasswordAuthenticator {
    public boolean authenticate(String username, String password, ServerSession session) {
        return username != null && username.equals(password);
    }
}    
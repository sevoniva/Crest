package io.crest.system.sso;

import io.crest.exception.DEException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertThrows;

@RunWith(Parameterized.class)
public class SsoAccountPolicyInvalidAccountTest {

    private final String account;

    public SsoAccountPolicyInvalidAccountTest(String account) {
        this.account = account;
    }

    @Parameterized.Parameters(name = "{index}: account={0}")
    public static Collection<Object[]> accounts() {
        return Arrays.asList(new Object[][]{
                {null},
                {""},
                {" "},
                {"  user"},
                {"user  "},
                {"user name"},
                {"中文账号"},
                {"user/name"},
                {"user:name"},
                {"user#name"},
                {"user$name"},
                {"user%name"},
                {"user+name"},
                {"user,name"},
                {"user;name"},
                {"user=name"},
                {"user?name"},
                {"user&name"},
                {"user*name"},
                {"user(name)"},
                {"user[name]"},
                {"user{name}"},
                {"user|name"},
                {"user\\name"},
                {"user'name"},
                {"user\"name"},
                {"user<name"},
                {"user>name"},
                {"user\nname"},
                {"user\tname"},
                {"<script>alert(1)</script>"},
                {"aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"},
                {"admin@corp.example.com.evil/path"}
        });
    }

    @Test
    public void rejectsInvalidAccount() {
        assertThrows(DEException.class, () -> SsoAccountPolicy.normalizeAccount(account, null));
    }
}

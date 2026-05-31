package io.crest.system.sso;

import io.crest.exception.DEException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

@RunWith(Parameterized.class)
public class SsoEndpointPolicyMatrixTest {

    private final String endpoint;
    private final boolean requireHttps;
    private final boolean valid;

    public SsoEndpointPolicyMatrixTest(String endpoint, boolean requireHttps, boolean valid) {
        this.endpoint = endpoint;
        this.requireHttps = requireHttps;
        this.valid = valid;
    }

    @Parameterized.Parameters(name = "{index}: endpoint={0}, requireHttps={1}, valid={2}")
    public static Collection<Object[]> endpoints() {
        return Arrays.asList(new Object[][]{
                {"https://idp.example.com/login/oauth/authorize", true, true},
                {"https://idp.example.com/api/token", true, true},
                {"https://idp.example.com/.well-known/jwks", true, true},
                {"http://localhost:8000/login/oauth/authorize", true, true},
                {"http://127.0.0.1:8000/api/token", true, true},
                {"http://[::1]:8000/api/userinfo", true, true},
                {"http://10.0.0.10:8000/api/token", false, true},
                {"http://idp.internal:8000/api/token", false, true},
                {"HTTP://LOCALHOST:8000/api/token", true, true},
                {"HTTPS://IDP.EXAMPLE.COM/API/TOKEN", true, true},
                {" http://localhost:8000/api/token ", true, true},
                {"https://idp.example.com:8443/oauth/token?tenant=crest", true, true},
                {"http://10.0.0.10:8000/api/token", true, false},
                {"http://idp.internal:8000/api/token", true, false},
                {"ftp://idp.example.com/token", false, false},
                {"file:///etc/passwd", false, false},
                {"javascript:alert(1)", false, false},
                {"data:text/plain,token", false, false},
                {"//idp.example.com/token", false, false},
                {"/relative/token", false, false},
                {"idp.example.com/token", false, false},
                {"", false, false},
                {" ", false, false},
                {null, false, false},
                {"https://", true, false},
                {"https:///token", true, false},
                {"https:// idp.example.com/token", true, false},
                {"http://evil.example.com\\@localhost/token", false, false},
                {"https://evil.example.com\n/token", true, false},
                {"https://evil.example.com\t/token", true, false},
                {"http://0.0.0.0:8000/token", true, false},
                {"http://[2001:db8::1]:8000/token", true, false},
                {"https://idp.example.com/token#fragment", true, true},
                {"https://idp.example.com/token;jsessionid=1", true, true},
                {"http://localhost", true, true},
                {"http://127.0.0.1", true, true},
                {"http://[::1]", true, true},
                {"http://localhost.evil.example/token", true, false},
                {"https://用户名:密码@idp.example.com/token", true, false},
                {"https://idp.example.com/token?redirect=http://evil.example", true, true}
        });
    }

    @Test
    public void validatesEndpointPolicy() {
        if (valid) {
            assertEquals(endpoint.trim(), SsoEndpointPolicy.validateEndpoint(endpoint, requireHttps, "端点"));
            return;
        }
        assertThrows(DEException.class, () -> SsoEndpointPolicy.validateEndpoint(endpoint, requireHttps, "端点"));
    }
}

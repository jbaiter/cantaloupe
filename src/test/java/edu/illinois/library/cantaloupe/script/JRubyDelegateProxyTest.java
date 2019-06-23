package edu.illinois.library.cantaloupe.script;

import edu.illinois.library.cantaloupe.image.Identifier;
import edu.illinois.library.cantaloupe.resource.RequestContext;
import edu.illinois.library.cantaloupe.test.TestUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class JRubyDelegateProxyTest extends BaseDelegateProxyTest {
    @BeforeEach
    public void setUp() throws Exception {
        RequestContext context = new RequestContext();
        context.setIdentifier(new Identifier("cats"));

        Path scriptFile = TestUtil.getFixture("delegates.rb");
        String code = Files.readString(scriptFile);
        JRubyDelegateProxy.load(code);

        instance = new JRubyDelegateProxy(context);
    }

    @Test
    @Override
    void testAuthorizeReturningMap() throws Exception {
        RequestContext context = new RequestContext();
        context.setIdentifier(new Identifier("redirect.jpg"));
        instance.setRequestContext(context);

        @SuppressWarnings("unchecked")
        Map<String,Object> result = (Map<String,Object>) instance.authorize();
        assertEquals("http://example.org/", result.get("location"));
        assertEquals(303, (long) result.get("status_code"));
    }
}

package edu.illinois.library.cantaloupe.script;

import edu.illinois.library.cantaloupe.image.Identifier;
import edu.illinois.library.cantaloupe.resource.RequestContext;
import edu.illinois.library.cantaloupe.script.engines.JythonDelegateProxy;
import edu.illinois.library.cantaloupe.test.TestUtil;
import org.junit.jupiter.api.BeforeEach;

import javax.script.ScriptEngineManager;
import java.nio.file.Files;
import java.nio.file.Path;

public class JythonDelegateProxyTest extends BaseDelegateProxyTest {
    @BeforeEach
    public void setUp() throws Exception {
        DelegateProxy.scriptEngine = new ScriptEngineManager()
                .getEngineByName("jython");

        RequestContext context = new RequestContext();
        context.setIdentifier(new Identifier("cats"));

        Path scriptFile = TestUtil.getFixture("delegates.py");
        String code = Files.readString(scriptFile);
        JythonDelegateProxy.load(code);

        instance = new JythonDelegateProxy(context);
    }
}

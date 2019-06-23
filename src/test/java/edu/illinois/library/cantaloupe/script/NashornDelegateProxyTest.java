package edu.illinois.library.cantaloupe.script;

import edu.illinois.library.cantaloupe.image.Identifier;
import edu.illinois.library.cantaloupe.resource.RequestContext;
import edu.illinois.library.cantaloupe.script.engines.GraalJsDelegateProxy;
import edu.illinois.library.cantaloupe.script.engines.NashornDelegateProxy;
import edu.illinois.library.cantaloupe.test.TestUtil;
import org.junit.jupiter.api.BeforeEach;

import javax.script.ScriptEngineManager;
import java.nio.file.Files;
import java.nio.file.Path;

public class NashornDelegateProxyTest extends BaseDelegateProxyTest {
    @BeforeEach
    public void setUp() throws Exception {
        DelegateProxy.scriptEngine = new ScriptEngineManager()
                .getEngineByName("nashorn");

        RequestContext context = new RequestContext();
        context.setIdentifier(new Identifier("cats"));

        Path scriptFile = TestUtil.getFixture("delegates_es5.js");
        String code = Files.readString(scriptFile);
        GraalJsDelegateProxy.load(code);

        instance = new NashornDelegateProxy(context);
    }
}

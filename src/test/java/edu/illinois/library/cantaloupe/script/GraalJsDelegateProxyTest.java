package edu.illinois.library.cantaloupe.script;

import edu.illinois.library.cantaloupe.image.Identifier;
import edu.illinois.library.cantaloupe.resource.RequestContext;
import edu.illinois.library.cantaloupe.script.engines.GraalJsDelegateProxy;
import edu.illinois.library.cantaloupe.test.TestUtil;
import org.junit.jupiter.api.BeforeEach;

import javax.script.ScriptEngineManager;
import java.nio.file.Files;
import java.nio.file.Path;

public class GraalJsDelegateProxyTest extends BaseDelegateProxyTest {

    @BeforeEach
    public void setUp() throws Exception {
        DelegateProxy.scriptEngine = new ScriptEngineManager()
                .getEngineByName("graal.js");

        RequestContext context = new RequestContext();
        context.setIdentifier(new Identifier("cats"));

        Path scriptFile = TestUtil.getFixture("delegates.js");
        String code = Files.readString(scriptFile);
        GraalJsDelegateProxy.load(code);

        instance = new GraalJsDelegateProxy(context);
    }
}

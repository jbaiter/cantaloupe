package edu.illinois.library.cantaloupe.perf.script;

import edu.illinois.library.cantaloupe.config.Configuration;
import edu.illinois.library.cantaloupe.config.Key;
import edu.illinois.library.cantaloupe.image.Identifier;
import edu.illinois.library.cantaloupe.resource.RequestContext;
import edu.illinois.library.cantaloupe.script.DelegateProxy;
import edu.illinois.library.cantaloupe.script.DelegateProxyService;
import edu.illinois.library.cantaloupe.script.DisabledException;
import edu.illinois.library.cantaloupe.test.TestUtil;
import org.openjdk.jmh.annotations.*;

import javax.script.ScriptException;
import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import static edu.illinois.library.cantaloupe.test.PerformanceTestConstants.*;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@Warmup(iterations = WARMUP_ITERATIONS,
        time = WARMUP_TIME)
@Measurement(iterations = MEASUREMENT_ITERATIONS,
             time = MEASUREMENT_TIME)
@State(Scope.Benchmark)
@Fork(value = 1, jvmArgs = { "-server", "-Xms128M", "-Xmx128M", "-Dcantaloupe.config=memory" })
public class NashornDelegatePerformance {
    private DelegateProxy proxy;

    @Setup
    public void setUp() throws DisabledException {
        Configuration config = Configuration.getInstance();
        config.setProperty(Key.DELEGATE_SCRIPT_ENABLED, true);
        config.setProperty(Key.DELEGATE_SCRIPT_ENGINE, "nashorn");
        Path scriptFile = TestUtil.getFixture("delegates_es5.js");
        config.setProperty(
                Key.DELEGATE_SCRIPT_PATHNAME,
                scriptFile.toAbsolutePath().toString());
        RequestContext context = new RequestContext();
        context.setIdentifier(new Identifier("sweatlodge"));
        proxy = DelegateProxyService.getInstance().newDelegateProxy(context);
    }

    @Benchmark
    public void callAuthorize() throws ScriptException {
        proxy.authorize();
    }

    @Benchmark
    public void callHttpResourceInformation() throws ScriptException {
        proxy.getHttpSourceResourceInfo();
    }

}

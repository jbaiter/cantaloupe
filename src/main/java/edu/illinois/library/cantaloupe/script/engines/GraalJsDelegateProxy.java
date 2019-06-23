package edu.illinois.library.cantaloupe.script.engines;

import edu.illinois.library.cantaloupe.resource.RequestContext;
import edu.illinois.library.cantaloupe.script.DelegateMethod;
import edu.illinois.library.cantaloupe.script.DelegateProxy;
import edu.illinois.library.cantaloupe.util.Stopwatch;
import org.graalvm.polyglot.proxy.ProxyObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class GraalJsDelegateProxy extends DelegateProxy {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(GraalJsDelegateProxy.class);


    public GraalJsDelegateProxy(RequestContext requestContext) {
        super(requestContext);
    }

    @Override
    public String getMethodName(DelegateMethod method) {
        return JavaScriptMethodMapping.METHOD_NAMES.get(method);
    }

    @Override
    protected void setRequestContext(RequestContext context) throws ScriptException {
        invokeUncached(DelegateMethod.REQUEST_CONTEXT_SETTER,
                ProxyObject.fromMap(Collections.unmodifiableMap(context.toMap())));
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Map<String, Long>> getRedactions() throws ScriptException {
        Object result = invoke(DelegateMethod.REDACTIONS);
        if (result != null) {
            Map<Integer, Map<String, Long>> map = (Map<Integer, Map<String, Long>>) result;
            return new ArrayList<>(map.values());
        }
        return Collections.emptyList();
    }

    @Override
    protected Object createDelegate() {
        final long stamp = lock.readLock();
        final Stopwatch watch = new Stopwatch();
        try {
            Object delegate = scriptEngine.eval("\n" +
                    "new " + JavaScriptMethodMapping.JS_DELEGATE_CLASS_NAME + "()" + "\n");
            LOGGER.trace("Instantiated delegate object in {}", watch);
            return delegate;
        } catch (javax.script.ScriptException e) {
            LOGGER.error(e.getMessage());
            return null;
        } finally {
            lock.unlock(stamp);
        }
    }
}

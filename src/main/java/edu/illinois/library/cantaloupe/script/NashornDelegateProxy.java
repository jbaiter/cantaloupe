package edu.illinois.library.cantaloupe.script;

import edu.illinois.library.cantaloupe.resource.RequestContext;
import edu.illinois.library.cantaloupe.util.Stopwatch;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class NashornDelegateProxy extends DelegateProxy {
    private static final Logger LOGGER =
            LoggerFactory.getLogger(NashornDelegateProxy.class);

    public NashornDelegateProxy(RequestContext requestContext) {
        super(requestContext);
    }

    @Override
    public String getMethodName(DelegateMethod method) {
        return JavaScriptMethodMapping.METHOD_NAMES.get(method);
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

    /**
     * @return Return value of {@link DelegateMethod#REDACTIONS}, or an empty
     *         list if it returned {@literal nil}.
     */
    @SuppressWarnings("unchecked")
    public List<Map<String,Long>> getRedactions() throws ScriptException {
        Object result = invoke(DelegateMethod.REDACTIONS);
        if (result != null) {
            ScriptObjectMirror sobj = (ScriptObjectMirror) result;
            return Collections.unmodifiableList(sobj.to(List.class));
        }
        return Collections.emptyList();
    }
}

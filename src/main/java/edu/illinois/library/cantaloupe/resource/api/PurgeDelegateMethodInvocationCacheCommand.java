package edu.illinois.library.cantaloupe.resource.api;

import edu.illinois.library.cantaloupe.script.DelegateProxy;
import java.util.concurrent.Callable;

final class PurgeDelegateMethodInvocationCacheCommand<T> extends Command
        implements Callable<T> {

    @Override
    public T call() {
        DelegateProxy.invocationCache.purge();
        return null;
    }

    @Override
    String getVerb() {
        return "PurgeDelegateMethodInvocationCache";
    }

}

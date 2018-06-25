package org.hermine.internal.db.driver;

import java.util.concurrent.CompletionException;

class Exceptions {

    private Exceptions() {}

    static Throwable unwrapException(Throwable ex) {
        return ex instanceof CompletionException ? ex.getCause() : ex;
    }
}

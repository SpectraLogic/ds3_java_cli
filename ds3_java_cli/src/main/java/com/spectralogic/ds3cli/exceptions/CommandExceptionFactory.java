/*
 * *****************************************************************************
 *   Copyright 2014-2016 Spectra Logic Corporation. All Rights Reserved.
 *   Licensed under the Apache License, Version 2.0 (the "License"). You may not use
 *   this file except in compliance with the License. A copy of the License is located at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *   or in the "license" file accompanying this file.
 *   This file is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 *   CONDITIONS OF ANY KIND, either express or implied. See the License for the
 *   specific language governing permissions and limitations under the License.
 * ***************************************************************************
 */

package com.spectralogic.ds3cli.exceptions;

import com.spectralogic.ds3client.networking.FailedRequestException;
import java.util.HashMap;
import java.util.Map;

public class CommandExceptionFactory {

    private static CommandExceptionFactory factoryInstance;
    private final Map<Class<? extends Throwable>, Ds3ExceptionHandler> handlers = new HashMap();
    private final Ds3ExceptionHandler defaultHandler = new DefaultExceptionHandler();

    // single instance
    public static CommandExceptionFactory getInstance() {
        if (factoryInstance == null) {
            factoryInstance = new CommandExceptionFactory();
        }
        return factoryInstance;
    }

    /**
     * Register an exception handler
     * @param exceptionClass (extends Throwable)
     * @param handler (implements Ds3ExceptionHandler)
     */
    public void addHandler(final Class<? extends Throwable> exceptionClass, final Ds3ExceptionHandler handler) {
        handlers.put(exceptionClass,  handler);
    }

    private Ds3ExceptionHandler getHandler(final Class<? extends Throwable> e) {
        final Ds3ExceptionHandler handler = handlers.get(e);
        if (handler == null) {
            return defaultHandler;
        }
        return handler;
    }

    /**
     * Locate the appropriate Ds3ExceptionHandler (match if registered, else Default)
     * then call handle(), passing in location, exception, and flag to throw RuntimeException
     *
     * @param location -- any string identifier, for COmmands use this.getClass.getSimpleName()
     * @param e -- the original exception (or create new)
     * @param throwRuntimeException -- true to construct a descriptive message and throw new RuntimeException,
     *                              -- false to write description to console and LOG.info()
     */
    public void handleException (final String location, final Throwable e, final boolean throwRuntimeException) {
        final Ds3ExceptionHandler handler = getHandler(e.getClass());
        handler.handle(location, e, throwRuntimeException);

    }

    /**
     * Checks exception type == FailedRequestException and status code
     * @param e caught exception
     * @param code code to test
     * @return true if exception is type of FailedRequestException and has status code == code
     */
    public static boolean hasStatusCode(final Exception e, final int code) {
        return e instanceof FailedRequestException && ((FailedRequestException) e).getStatusCode() == code;
    }

}

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

import java.util.HashMap;
import java.util.Map;

public class Ds3ExceptionHandlerMapper {

    private final static Ds3ExceptionHandlerMapper factoryInstance = new Ds3ExceptionHandlerMapper();
    private final Map<Class<? extends Throwable>, Ds3ExceptionHandler> handlers = new HashMap();
    private final Ds3ExceptionHandler defaultHandler = new DefaultExceptionHandler();


    public static Ds3ExceptionHandlerMapper getInstance() {
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
     * then call handle()
     *
     * @param e
     */
    public void handleException (final Throwable e) {
        final Ds3ExceptionHandler handler = getHandler(e.getClass());
        handler.handle(e);
    }

}

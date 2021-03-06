/*
 * Copyright (c) 2003, 2010, Oracle and/or its affiliates. All rights reserved.
 * ORACLE PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 *
 */

package vertx;


/**
 * Common SXA CC ACS exceptions.
 */
public class CcException extends VertxException {
    /**
     * Commonly Used Static Exceptions
     */
    public static final CcException INVALID_PARAMETER = new CcException("Invalid Parameter(s)!");
    public static final CcException NULL_POINTER = new CcException("null pointer!");

    /**
     * Construct a CCNG Exception with the specified detail message.  The
     * faultCode will default to null.
     *
     * @param message a description of the exception
     */
    public CcException(String message) {
        super(message);
    }
}

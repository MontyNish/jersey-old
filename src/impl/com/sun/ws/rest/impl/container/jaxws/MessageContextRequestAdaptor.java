/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007 Sun Microsystems, Inc. All rights reserved. 
 * 
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License("CDDL") (the "License").  You may not use this file
 * except in compliance with the License. 
 * 
 * You can obtain a copy of the License at:
 *     https://jersey.dev.java.net/license.txt
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * When distributing the Covered Code, include this CDDL Header Notice in each
 * file and include the License file at:
 *     https://jersey.dev.java.net/license.txt
 * If applicable, add the following below this CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 *     "Portions Copyrighted [year] [name of copyright owner]"
 */

package com.sun.ws.rest.impl.container.jaxws;

import com.sun.ws.rest.impl.HttpRequestContextImpl;
import com.sun.ws.rest.impl.http.header.HttpHeaderFactory;
import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.Map;
import javax.activation.DataSource;
import javax.ws.rs.core.MultivaluedMap;
import javax.xml.ws.handler.MessageContext;
import static javax.xml.ws.handler.MessageContext.HTTP_REQUEST_METHOD;
import static javax.xml.ws.handler.MessageContext.HTTP_REQUEST_HEADERS;
import static javax.xml.ws.handler.MessageContext.PATH_INFO;
import static javax.xml.ws.handler.MessageContext.QUERY_STRING;

/**
 * Adapts a JAX-WS <code>Endpoint</code> request to provide the methods of HttpRequest
 */
public final class MessageContextRequestAdaptor extends HttpRequestContextImpl {
    
    final MessageContext context;
    
    /**
     * Creates a new instance of MessageContextRequestAdaptor
     */
    public MessageContextRequestAdaptor(DataSource request, MessageContext context) throws IOException {
        super((String)context.get(HTTP_REQUEST_METHOD), request != null ? request.getInputStream() : null );
        this.context = context;
        
        initiateUriInfo();
        copyHttpHeaders();
    }

    private void initiateUriInfo() {        
        this.decodedPath = (String)context.get(PATH_INFO);
        // Ensure path is relative, TODO may need to check for multiple '/'
        if (this.decodedPath.startsWith("/"))
            this.decodedPath = this.decodedPath.substring(1);
        
        this.encodedQuery = (String)context.get(QUERY_STRING);
        
        // TODO create base URI
        this.baseUri = URI.create("/");
    }
    
    @SuppressWarnings("unchecked")
    private void copyHttpHeaders() {
        Map<String, List<String>> headers = (Map<String, List<String>>)context.get(HTTP_REQUEST_HEADERS);
        MultivaluedMap<String, String> restHeaders = getRequestHeaders();
        for (Map.Entry<String, List<String>> e : headers.entrySet()) {
            restHeaders.put(e.getKey(), e.getValue());
            if (e.getKey().equalsIgnoreCase("cookie")) {
                for (String headerValue: e.getValue()) {
                    getCookies().addAll(HttpHeaderFactory.createCookies(headerValue));
                }
            }
        }        
    }
}
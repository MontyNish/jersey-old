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

package com.sun.jersey.impl.model.parameter;

import com.sun.jersey.api.core.HttpContext;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableContext;
import com.sun.jersey.spi.inject.InjectableProvider;
import com.sun.jersey.spi.service.ComponentProvider.Scope;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public final class HttpContextInjectableProvider implements 
        InjectableProvider<Context, Type> {
        
    private static final class HttpContextInjectable implements Injectable<Object> {
        public Object getValue(HttpContext context) {
            return context;
        }
    }
    
    private static final class HttpContextRequestInjectable implements Injectable<Object> {
        public Object getValue(HttpContext context) {
            return context.getRequest();
        }
    }
    
    private static final class UriInfoInjectable implements Injectable<UriInfo> {
        public UriInfo getValue(HttpContext context) {
            return context.getUriInfo();
        }
    }
    
    private final Map<Type, Injectable> injectables;
    
    public HttpContextInjectableProvider() {        
        injectables = new HashMap<Type, Injectable>();
        
        HttpContextRequestInjectable re = new HttpContextRequestInjectable();
        injectables.put(HttpHeaders.class, re);
        injectables.put(Request.class, re);
        injectables.put(SecurityContext.class, re);
        
        injectables.put(HttpContext.class, new HttpContextInjectable());
        
        injectables.put(UriInfo.class, new UriInfoInjectable());
    }
    
    public Scope getScope() {
        return Scope.PerRequest;
    }
        
    public Injectable getInjectable(InjectableContext ic, Context a, Type c) {
        return injectables.get(c);
    }
}
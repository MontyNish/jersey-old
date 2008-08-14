/*
 *
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 * 
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://jersey.dev.java.net/CDDL+GPL.html
 * or jersey/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 * 
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at jersey/legal/LICENSE.txt.
 * Sun designates this particular file as subject to the "Classpath" exception
 * as provided by Sun in the GPL Version 2 section of the License file that
 * accompanied this code.  If applicable, add the following below the License
 * Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information: "Portions Copyrighted [year]
 * [name of copyright owner]"
 * 
 * Contributor(s):
 * 
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.jersey.impl.resource;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.spi.service.ComponentContext;
import com.sun.jersey.spi.service.ComponentProvider;
import com.sun.jersey.spi.service.ComponentProvider.Scope;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@SuppressWarnings("unchecked")
public class MethodIAnnotationnheritenceTest extends AbstractResourceTester {
    
    public MethodIAnnotationnheritenceTest(String testName) {
        super(testName);
    }
    
    public static class SubResource {
        UriInfo ui;
        String p;
        String q;
        String h;
        
        SubResource(UriInfo ui, String p, String q, String h) {
            this.ui = ui;
            this.p = p;
            this.q = q;
            this.h = h;
        }
        
        @GET
        public String get() {
            return p + q + h;
        }
    }
    
    public static interface Interface {
        @GET
        @Produces("application/get")
        String get();
        
        @GET
        @Produces("application/getParams")
        String getParams(
                @Context UriInfo ui,
                @PathParam("p") String p,
                @QueryParam("q") String q, 
                @HeaderParam("h") String h);
        
        @POST
        @Produces("application/xml")
        @Consumes("text/plain")
        String post(String s);
        
        @Path("sub")
        SubResource subResource(
                @Context UriInfo ui,
                @PathParam("p") String p,
                @QueryParam("q") String q, 
                @HeaderParam("h") String h);
        
        @GET
        @Path("submethod")
        String subMethod(
                @Context UriInfo ui,
                @PathParam("p") String p,
                @QueryParam("q") String q, 
                @HeaderParam("h") String h);
    }

    @Path("/{p}")
    public static class InterfaceImplementation implements Interface {
        public String get() {
            return "implementation";
        }
        
        public String getParams(UriInfo ui, String p, String q, String h) {
            return p + q + h;
        }
        
        public String post(String s) {
            return "<root>" + s + "</root>";
        }        
        
        public SubResource subResource(UriInfo ui, String p, String q, String h) {
            return new SubResource(ui, p, q, h);
        }
        
        public String subMethod(UriInfo ui, String p, String q, String h) {
            return p + q + h;
        }
    }
    
    public void testInterfaceImplementation() {
        initiateWebApplication(InterfaceImplementation.class);
        
        _test();
    }
    
    
    static class ProxyComponentProvider implements ComponentProvider {
        public <T> T getInjectableInstance(T instance) {
            return instance;
        }

        public <T> T getInstance(Scope scope, Class<T> c) 
                throws InstantiationException, IllegalAccessException {
            if (Interface.class.isAssignableFrom(c)) {
                final Object o = c.newInstance();
                return (T) Proxy.newProxyInstance(
                        this.getClass().getClassLoader(),
                        new Class[]{Interface.class},
                        new InvocationHandler() {
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        return method.invoke(o, args);
                    }
                });
            } else return null;
        }

        public <T> T getInstance(Scope scope, Constructor<T> constructor, Object[] parameters) 
                throws InstantiationException, IllegalArgumentException, IllegalAccessException, InvocationTargetException {
            return null;
        }

        public <T> T getInstance(ComponentContext cc, Scope scope, Class<T> c) 
                throws InstantiationException, IllegalAccessException {
            return getInstance(scope, c);
        }
        
        public void inject(Object instance) {
        }
    }
    
    public void testInterfaceImplementationComponentProviderProxy() {
        initiateWebApplication(new ProxyComponentProvider(), InterfaceImplementation.class);
        
        _test();
    }
    
    public void _test() {
        String s = resource("/a").accept("application/get").
                get(String.class);
        assertEquals("implementation", s);
        
        s = resource("/a?q=b").accept("application/getParams").
                header("h", "c").
                get(String.class);
        assertEquals("abc", s);
        
        ClientResponse cr = resource("/a").
                type("text/plain").
                accept("application/xml").
                post(ClientResponse.class, "content");
        assertEquals("<root>content</root>", cr.getEntity(String.class));
        assertEquals(MediaType.valueOf("application/xml"), cr.getType());
        
        s = resource("/a/sub?q=b").
                header("h", "c").
                get(String.class);
        assertEquals("abc", s);
        
        s = resource("/a/submethod?q=b").
                header("h", "c").
                get(String.class);
        assertEquals("abc", s);        
    }
    
    public static abstract class AbstractClass {
        @GET
        @Produces("application/get")
        public abstract String get();
        
        @GET
        @Produces("application/getParams")
        public abstract String getParams(
                @Context UriInfo ui,
                @PathParam("p") String p,
                @QueryParam("q") String q, 
                @HeaderParam("h") String h);
        
        @POST
        @Produces("application/xml")
        @Consumes("text/plain")
        public abstract String post(String s);
        
        @Path("sub")
        public abstract SubResource subResource(
                @Context UriInfo ui,
                @PathParam("p") String p,
                @QueryParam("q") String q, 
                @HeaderParam("h") String h);
        
        @GET
        @Path("submethod")
        public abstract String subMethod(
                @Context UriInfo ui,
                @PathParam("p") String p,
                @QueryParam("q") String q, 
                @HeaderParam("h") String h);        
    }
    
    @Path("/{p}")
    public static class AbstractClassImplementation extends AbstractClass {
        public String get() {
            return "implementation";
        }
        
        public String getParams(UriInfo ui, String p, String q, String h) {
            return p + q + h;
        }
        
        public String post(String s) {
            return "<root>" + s + "</root>";
        }        
        
        public SubResource subResource(UriInfo ui, String p, String q, String h) {
            return new SubResource(ui, p, q, h);
        }
        
        public String subMethod(UriInfo ui, String p, String q, String h) {
            return p + q + h;
        }
    }
    
    public void testAbstractClassImplementation() {
        initiateWebApplication(AbstractClassImplementation.class);
        
        _test();
    }
    
    
    public static class ConcreteClass {
        @GET
        @Produces("application/get")
        public String get() {
            return "void";
        }
        
        @GET
        @Produces("application/getParams")
        public String getParams(
                @Context UriInfo ui,
                @PathParam("p") String p,
                @QueryParam("q") String q, 
                @HeaderParam("h") String h) {
            return "void";
        }
        
        @POST
        @Produces("application/xml")
        @Consumes("text/plain")
        public String post(String s) {
            return "void";
        }
        
        @Path("sub")
        public SubResource subResource(
                @Context UriInfo ui,
                @PathParam("p") String p,
                @QueryParam("q") String q, 
                @HeaderParam("h") String h) {
            return null;            
        }
        
        @GET
        @Path("submethod")
        public String subMethod(
                @Context UriInfo ui,
                @PathParam("p") String p,
                @QueryParam("q") String q, 
                @HeaderParam("h") String h) {
            return "void";            
        }
    }
    
    @Path("/{p}")
    public static class ConcreteClassOverride extends ConcreteClass {
        public String get() {
            return "implementation";
        }
        
        public String getParams(UriInfo ui, String p, String q, String h) {
            return p + q + h;
        }
        
        public String post(String s) {
            return "<root>" + s + "</root>";
        }        
        
        public SubResource subResource(UriInfo ui, String p, String q, String h) {
            return new SubResource(ui, p, q, h);
        }
        
        public String subMethod(UriInfo ui, String p, String q, String h) {
            return p + q + h;
        }
    }

    public void testConcreteClassOverride() {
        initiateWebApplication(ConcreteClassOverride.class);
        
        _test();
    }

    
    public static abstract class AbstractOverrideClass implements Interface {
        public abstract String get();
        
        public abstract String getParams(UriInfo ui, String p, String q, String h);
        
        public abstract String post(String s);
        
        public abstract SubResource subResource(UriInfo ui, String p, String q, String h);
        
        public abstract String subMethod(UriInfo ui, String p, String q, String h);
    }
    
    @Path("/{p}")
    public static class AbstractOverrideClassInterface extends AbstractOverrideClass {
        public String get() {
            return "implementation";
        }
        
        public String getParams(UriInfo ui, String p, String q, String h) {
            return p + q + h;
        }
        
        public String post(String s) {
            return "<root>" + s + "</root>";
        }        
        
        public SubResource subResource(UriInfo ui, String p, String q, String h) {
            return new SubResource(ui, p, q, h);
        }
        
        public String subMethod(UriInfo ui, String p, String q, String h) {
            return p + q + h;
        }
    }
    
    public void testAbstractOverrideClassInterface() {
        initiateWebApplication(AbstractOverrideClassInterface.class);
        
        _test();
    }

    @Path("/{x_p}")
    public static class InterfaceImplementationOverride implements Interface {
        
        @GET
        @Produces("application/getoverride")
        public String get() {
            return "override";
        }
        
        @GET
        @Produces("application/getParamsoverride")
        public String getParams(
                @Context UriInfo ui,
                @PathParam("x_p") String p,
                @QueryParam("_q") String q, 
                @HeaderParam("_h") String h) {
            return p + q + h;
        }
        
        @POST
        @Produces("application/xhtml")
        @Consumes("application/octet-stream")
        public String post(String s) {
            return "<root>" + s + "</root>";
        }  
        
        @Path("suboverride")
        public SubResource subResource(
                @Context UriInfo ui,
                @PathParam("x_p") String p,
                @QueryParam("_q") String q, 
                @HeaderParam("_h") String h) {
            return new SubResource(ui, p, q, h);            
        }
        
        @GET
        @Path("submethodoverride")
        public String subMethod(
                @Context UriInfo ui,
                @PathParam("x_p") String p,
                @QueryParam("_q") String q, 
                @HeaderParam("_h") String h) {
            return p + q + h;            
        }
    }
    
    public void testInterfaceImplementationOverride() {
        initiateWebApplication(InterfaceImplementationOverride.class);
        String s = resource("/a").accept("application/getoverride")
                .get(String.class);
        assertEquals("override", s);
        
        s = resource("/a?_q=b").
                accept("application/getParamsoverride").
                header("_h", "c").
                get(String.class);
        assertEquals("abc", s);
        
        ClientResponse cr = resource("/a").
                type("application/octet-stream").
                accept("application/xhtml").
                post(ClientResponse.class, "content");
        assertEquals("<root>content</root>", cr.getEntity(String.class));
        assertEquals(MediaType.valueOf("application/xhtml"), cr.getType());
        
        s = resource("/a/suboverride?_q=b").
                header("_h", "c").
                get(String.class);
        assertEquals("abc", s);        
        
        s = resource("/a/submethodoverride?_q=b").
                header("_h", "c").
                get(String.class);
        assertEquals("abc", s);        
        
        
        cr = resource("/a", false).accept("application/get").
                get(ClientResponse.class);
        assertEquals(406, cr.getStatus());
        
        cr = resource("/a?q=b", false).accept("application/getParams")
                .header("h", "c").
                get(ClientResponse.class);
        assertEquals(406, cr.getStatus());
        
        cr = resource("/a", false).
                type("text/plain").
                accept("application/xml").
                post(ClientResponse.class, "content");
        assertEquals(415, cr.getStatus());        
        
        cr = resource("/a/sub", false).
                get(ClientResponse.class);
        assertEquals(404, cr.getStatus());
        
        cr = resource("/a/submethod", false).
                get(ClientResponse.class);
        assertEquals(404, cr.getStatus());
    }
}
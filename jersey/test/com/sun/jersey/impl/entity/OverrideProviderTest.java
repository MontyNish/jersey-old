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

package com.sun.jersey.impl.entity;

import com.sun.jersey.impl.AbstractResourceTester;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.impl.provider.entity.AbstractMessageReaderWriterProvider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.Provider;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class OverrideProviderTest extends AbstractResourceTester {
    public OverrideProviderTest(String testName) {
        super(testName);
    }
    
    @Provider
    @ProduceMime({"text/plain", "*/*"})
    @ConsumeMime({"text/plain", "*/*"})    
    public static class StringProvider extends AbstractMessageReaderWriterProvider<String> {
        public boolean isReadable(Class<?> type, Type genericType, Annotation annotations[]) {
            return type == String.class;
        }

        public String readFrom(
                Class<String> type, 
                Type genericType, 
                Annotation annotations[],
                MediaType mediaType, 
                MultivaluedMap<String, String> httpHeaders, 
                InputStream entityStream) throws IOException {
            String s = readFromAsString(entityStream, mediaType);
            return s.toUpperCase();
        }

        public boolean isWriteable(Class<?> type, Type genericType, Annotation annotations[]) {
            return type == String.class;
        }
    
        public void writeTo(
                String t, 
                Class<?> type, 
                Type genericType, 
                Annotation annotations[], 
                MediaType mediaType, 
                MultivaluedMap<String, Object> httpHeaders,
                OutputStream entityStream) throws IOException {
            writeToAsString(t.toLowerCase(), entityStream, mediaType);
        }
    }
    
    @Path("/")
    public static class StringResource {
        @GET
        public String get() {
            return "FOO";
        }
    }
    
    public void testString() throws Exception {
        initiateWebApplication(StringResource.class, StringProvider.class);
                
        WebResource r = resource("/");
        assertEquals("foo", r.get(String.class));
    }    
    
    @Provider
    @ProduceMime({"application/xml", "*/*"})
    @ConsumeMime({"application/xml", "*/*"})
    public static class JAXBBeanProvider extends AbstractMessageReaderWriterProvider<JAXBBean> {
        public boolean isReadable(Class<?> type, Type genericType, Annotation annotations[]) {
            return JAXBBean.class.isAssignableFrom(type);
        }

        public JAXBBean readFrom(
                Class<JAXBBean> type, 
                Type genericType, 
                Annotation annotations[],
                MediaType mediaType, 
                MultivaluedMap<String, String> httpHeaders, 
                InputStream entityStream) throws IOException {
            String s = readFromAsString(entityStream, mediaType);
            return new JAXBBean(s);
        }

        public boolean isWriteable(Class<?> type, Type genericType, Annotation annotations[]) {
            return JAXBBean.class.isAssignableFrom(type);
        }
    
        public void writeTo(
                JAXBBean t, 
                Class<?> type, 
                Type genericType, 
                Annotation annotations[], 
                MediaType mediaType, 
                MultivaluedMap<String, Object> httpHeaders,
                OutputStream entityStream) throws IOException {
            writeToAsString(t.value, entityStream, mediaType);
        }
    }
    
    @Path("/")
    public static class JAXBBeanResource {
        @GET
        public JAXBBean get() {
            return new JAXBBean("foo");
        }
    }
    
    public void testJAXBBean() throws Exception {
        initiateWebApplication(JAXBBeanResource.class, JAXBBeanProvider.class);
                
        WebResource r = resource("/");
        assertEquals("foo", r.get(String.class));
    }    
    
    public void testJAXBBeanWithProviderInstance() throws Exception {
        ResourceConfig rc = new DefaultResourceConfig(JAXBBeanResource.class);
        rc.getProviderInstances().add(new JAXBBeanProvider());
        initiateWebApplication(rc);
                
        WebResource r = resource("/");
        assertEquals("foo", r.get(String.class));
    }    
}
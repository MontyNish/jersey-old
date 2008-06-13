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
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.impl.AbstractResourceTester;
import java.io.IOException;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.Path;
import javax.ws.rs.GET;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
@SuppressWarnings("unchecked")
public class AcceptTest extends AbstractResourceTester {
    
    public AcceptTest(String testName) {
        super(testName);
    }

    @Path("/")
    public static class Resource {
        @ProduceMime("application/foo")
        @GET
        public String doGetFoo() {
            return "foo";
        }
        
        @ProduceMime("application/bar")
        @GET
        public String doGetBar() {
            return "bar";
        }
        
        @ProduceMime("application/baz")
        @GET
        public String doGetBaz() {
            return "baz";
        }
        
        @ProduceMime("*/*")
        @GET
        public Response doGetWildCard() {
            return Response.ok("wildcard", "application/wildcard").build();
        }
    }
    
    public void testAcceptGet() throws IOException {
        initiateWebApplication(Resource.class);
        WebResource r = resource("/");
        
        String s = r.accept("application/foo").get(String.class);
        assertEquals("foo", s);
        
        s = r.accept("application/foo;q=0.1").get(String.class);
        assertEquals("foo", s);

        s = r.accept("application/foo", "application/bar;q=0.4", "application/baz;q=0.2").
                get(String.class);
        assertEquals("foo", s);
        
        s = r.accept("application/foo;q=0.4", "application/bar", "application/baz;q=0.2").
                get(String.class);
        assertEquals("bar", s);
        
        s = r.accept("application/foo;q=0.4", "application/bar;q=0.2", "application/baz").
                get(String.class);
        assertEquals("baz", s);
    }   
    
    public void testAcceptGetWildCard() {
        initiateWebApplication(Resource.class);
        WebResource r = resource("/");
        
        String s = r.accept("application/wildcard", "application/foo;q=0.6", 
                "application/bar;q=0.4", "application/baz;q=0.2").
                get(String.class);
        assertEquals("wildcard", s);
    }   
    
    public void testQualityErrorGreaterThanOne() {
        initiateWebApplication(Resource.class);
        WebResource r = resource("/", false);

        ClientResponse response = r.accept("application/foo;q=1.1").get(ClientResponse.class);
        assertEquals(400, response.getStatus());        
    }
    
    public void testQualityErrorMoreThanThreeDigits() {
        initiateWebApplication(Resource.class);
        WebResource r = resource("/", false);
        
        ClientResponse response = r.accept("application/foo;q=0.1234").get(ClientResponse.class);
        assertEquals(400, response.getStatus());
    }
    
    @Path("/")
    public static class MultipleResource {
        @ProduceMime({"application/foo", "application/bar"})
        @GET
        public String get() {
            return "GET";
        }        
    }
    
    public void testAcceptMultiple() {
        initiateWebApplication(MultipleResource.class);
        WebResource r = resource("/");

        MediaType foo = MediaType.valueOf("application/foo");
        MediaType bar = MediaType.valueOf("application/bar");
        
        ClientResponse response = r.accept(foo).get(ClientResponse.class);
        assertEquals("GET", response.getEntity(String.class));
        assertEquals(foo, response.getType());
        
        response = r.accept(bar).get(ClientResponse.class);
        assertEquals("GET", response.getEntity(String.class));
        assertEquals(bar, response.getType());

        response = r.accept("*/*").get(ClientResponse.class);
        assertEquals("GET", response.getEntity(String.class));
        assertEquals(foo, response.getType());

        response = r.accept("application/*").get(ClientResponse.class);
        assertEquals("GET", response.getEntity(String.class));
        assertEquals(foo, response.getType());
    }   
}
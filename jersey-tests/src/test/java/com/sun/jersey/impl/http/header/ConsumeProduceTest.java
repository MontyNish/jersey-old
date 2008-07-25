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

package com.sun.jersey.impl.http.header;

import javax.ws.rs.ConsumeMime;
import javax.ws.rs.ProduceMime;
import com.sun.jersey.impl.model.MediaTypeHelper;
import java.util.List;
import javax.ws.rs.core.MediaType;
import junit.framework.TestCase;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public class ConsumeProduceTest extends TestCase {
    
    public ConsumeProduceTest(String testName) {
        super(testName);
    }
    
    @ConsumeMime({"*/*", "a/*", "b/*", "a/b", "c/d"})
    class ConsumeMimeClass {
    }
    
    @ProduceMime({"*/*", "a/*", "b/*", "a/b", "c/d"})
    class ProduceMimeClass {
    }
    
    /** Creates a new instance of ConsumeProduceTest */
    public ConsumeProduceTest() {
    }
    
    public void testConsumeMime() {
        ConsumeMime c = ConsumeMimeClass.class.getAnnotation(ConsumeMime.class);
        List<MediaType> l = MediaTypeHelper.createMediaTypes(c);
        checkMediaTypes(l);
    }
    
    public void testProduceMime() {
        ProduceMime p = ProduceMimeClass.class.getAnnotation(ProduceMime.class);
        List<MediaType> l = MediaTypeHelper.createMediaTypes(p);
        checkMediaTypes(l);
    }
    
    public void checkMediaTypes(List<MediaType> l) {
        assertEquals(5, l.size());
        assertEquals("a", l.get(0).getType());
        assertEquals("b", l.get(0).getSubtype());
        assertEquals("c", l.get(1).getType());
        assertEquals("d", l.get(1).getSubtype());
        assertEquals("a", l.get(2).getType());
        assertEquals("*", l.get(2).getSubtype());
        assertEquals("b", l.get(3).getType());
        assertEquals("*", l.get(3).getSubtype());
        assertEquals("*", l.get(4).getType());
        assertEquals("*", l.get(4).getSubtype());
    }
    
}
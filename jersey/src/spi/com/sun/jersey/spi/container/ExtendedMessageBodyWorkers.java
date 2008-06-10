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
package com.sun.jersey.spi.container;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.ext.MessageBodyWorkers;

/**
 *
 * @author Paul.Sandoz@Sun.Com
 */
public interface ExtendedMessageBodyWorkers extends MessageBodyWorkers {
    /**
     * Get the list of media types supported for a Java type.
     * 
     * @param type the class of object that is to be written.
     * 
     * @param genericType the type of object to be written. E.g. if the 
     * message body is to be produced from a field, this will be
     * the declared type of the field as returned by 
     * <code>Field.getGenericType</code>.
     * 
     * @param annotations an array of the annotations on the declaration of the
     * artifact that will be written. E.g. if the 
     * message body is to be produced from a field, this will be
     * the annotations on that field returned by 
     * <code>Field.getDeclaredAnnotations</code>.
     * 
     * @return the list of supported media types, the list is ordered as
     * follows: a/b &lt a/* &lt *\\/*
     */
    <T> List<MediaType> getMessageBodyWriterMediaTypes(
            Class<T> type, 
            Type genericType,
            Annotation[] annotations);
}

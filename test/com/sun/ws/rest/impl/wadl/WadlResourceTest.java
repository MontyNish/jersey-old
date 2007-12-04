/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.sun.ws.rest.impl.wadl;

import com.sun.ws.rest.impl.AbstractResourceTester;
import com.sun.ws.rest.impl.client.ResourceProxy;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Properties;
import javax.ws.rs.ConsumeMime;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.ProduceMime;
import javax.ws.rs.UriParam;
import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

/**
 *
 * @author mh124079
 */
public class WadlResourceTest extends AbstractResourceTester {
    
    public WadlResourceTest(String testName) {
        super(testName);
    }
    
    @Path("foo")
    public static class ExtraResource {
    }

    @Path("widgets")
    public static class WidgetsResource {

        @GET
        @ProduceMime({"application/xml", "application/json"})
        public String getWidgets() {
            return null;
        }

        @POST
        @ConsumeMime({"application/xml"})
        @ProduceMime({"application/xml", "application/json"})
        public String createWidget(String bar) {
            return bar;
        }

        @PUT
        @Path("{id}")
        @ConsumeMime("application/xml")
        public void updateWidget(String bar, @UriParam("id")int id) {
        }

        @GET
        @Path("{id}")
        @ProduceMime({"application/xml", "application/json"})
        public String getWidget(@UriParam("id")int id) {
            return null;
        }

        @DELETE
        @Path("{id}")
        public void deleteWidget(@UriParam("id")int id) {
        }

        @Path("{id}/verbose")
        public Object getVerbose(@UriParam("id")int id) {
            return null;
        }
    }
    
    /**
     * Test WADL generation
     */
    public void testGetWadl() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
        initiateWebApplication(WidgetsResource.class, ExtraResource.class);
        ResourceProxy r = resourceProxy("/application.wadl");
        
        File tmpFile = r.get(File.class);
        DocumentBuilderFactory bf = DocumentBuilderFactory.newInstance();
        bf.setNamespaceAware(true);
        bf.setValidating(false);
        bf.setXIncludeAware(false);
        DocumentBuilder b = bf.newDocumentBuilder();
        Document d = b.parse(tmpFile);
        printSource(new DOMSource(d));
        XPath xp = XPathFactory.newInstance().newXPath();
        xp.setNamespaceContext(new NSResolver("wadl", "http://research.sun.com/wadl/2006/10"));
        // check base URI
        String val = (String)xp.evaluate("/wadl:application/wadl:resources/@base", d, XPathConstants.STRING);
        assertEquals(val,"/base/");
        // check total number of resources is 4
        val = (String)xp.evaluate("count(//wadl:resource)", d, XPathConstants.STRING);
        assertEquals(val,"4");
        // check only once resource with for {id}
        val = (String)xp.evaluate("count(//wadl:resource[@path='{id}'])", d, XPathConstants.STRING);
        assertEquals(val,"1");
        // check only once resource with for {id}/verbose
        val = (String)xp.evaluate("count(//wadl:resource[@path='{id}/verbose'])", d, XPathConstants.STRING);
        assertEquals(val,"1");
        // check only once resource with for widgets
        val = (String)xp.evaluate("count(//wadl:resource[@path='widgets'])", d, XPathConstants.STRING);
        assertEquals(val,"1");
        // check 3 methods for {id}
        val = (String)xp.evaluate("count(//wadl:resource[@path='{id}']/wadl:method)", d, XPathConstants.STRING);
        assertEquals(val,"3");
        // check 2 methods for widgets
        val = (String)xp.evaluate("count(//wadl:resource[@path='widgets']/wadl:method)", d, XPathConstants.STRING);
        assertEquals(val,"2");
        // check type of {id} is int
        val = (String)xp.evaluate("//wadl:resource[@path='{id}']/wadl:param[@name='id']/@type", d, XPathConstants.STRING);
        assertEquals(val,"xs:int");
        // check number of output representations is two
        val = (String)xp.evaluate("count(//wadl:resource[@path='widgets']/wadl:method[@name='GET']/wadl:response/wadl:representation)", d, XPathConstants.STRING);
        assertEquals(val,"2");
        // check number of output representations is one
        val = (String)xp.evaluate("count(//wadl:resource[@path='widgets']/wadl:method[@name='POST']/wadl:request/wadl:representation)", d, XPathConstants.STRING);
        assertEquals(val,"1");
    }

    private static class NSResolver implements NamespaceContext {
        private String prefix;
        private String nsURI;
        
        public NSResolver(String prefix, String nsURI) {
            this.prefix = prefix;
            this.nsURI = nsURI;
        }
        
        public String getNamespaceURI(String prefix) {
             if (prefix.equals(this.prefix))
                 return this.nsURI;
             else
                 return XMLConstants.NULL_NS_URI;
        }

        public String getPrefix(String namespaceURI) {
            if (namespaceURI.equals(this.nsURI))
                return this.prefix;
            else
                return null;
        }

        public Iterator getPrefixes(String namespaceURI) {
            return null;
        }
    }

    
    private static void printSource(Source source) {
        try {
            Transformer trans = TransformerFactory.newInstance().newTransformer();
            Properties oprops = new Properties();
            oprops.put(OutputKeys.OMIT_XML_DECLARATION, "yes");
            oprops.put(OutputKeys.INDENT, "yes");
            oprops.put(OutputKeys.METHOD, "xml");
            trans.setOutputProperties(oprops);
            trans.transform(source, new StreamResult(System.out));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

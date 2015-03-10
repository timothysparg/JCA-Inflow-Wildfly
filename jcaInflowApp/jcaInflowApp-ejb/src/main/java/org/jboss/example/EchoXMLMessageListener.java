/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.example;

import java.io.ByteArrayOutputStream;

import javax.ejb.EJBException;
import javax.ejb.MessageDrivenBean;
import javax.ejb.MessageDrivenContext;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jboss.example.XMLMessageListener;
import org.jboss.logging.Logger;
import org.w3c.dom.Document;

/**
 * Prints the xml.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class EchoXMLMessageListener implements MessageDrivenBean, XMLMessageListener
{
   private static final Logger log = Logger.getLogger(EchoXMLMessageListener.class);

   private MessageDrivenContext ctx;

   private Transformer transformer;
   
   public void processXML(Document document) throws Exception
   {
      DOMSource source = new DOMSource(document);
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      StreamResult result = new StreamResult(baos);
      transformer.transform(source, result);
      log.info(baos.toString());
   }

   public void ejbCreate()
   {
      TransformerFactory tf = TransformerFactory.newInstance();
      try
      {
         transformer = tf.newTransformer();
      }
      catch (Exception e)
      {
         throw new EJBException(e);
      }
   }
   
   public void ejbRemove()
   {
   }

   public void setMessageDrivenContext(MessageDrivenContext ctx)
   {
      this.ctx = ctx;
   }
}

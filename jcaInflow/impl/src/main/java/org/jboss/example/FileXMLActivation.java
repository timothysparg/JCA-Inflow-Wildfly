/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.example;

import java.lang.reflect.Method;
import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import javax.resource.ResourceException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.jboss.logging.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * A FileXMLActivation.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class FileXMLActivation extends TimerTask implements XAResource
{
   /** The log */
   private static final Logger log = Logger.getLogger(FileXMLActivation.class);

   /** The timer */
   private Timer timer;
   
   /** The activation spec */
   private FileXMLActivationSpec spec;

   /** The message endpoint */
   private MessageEndpoint endpoint;

   /** The directory */
   private File directory;

   /** The current file */
   private File currentFile;

   /** The document builder */
   DocumentBuilder builder;

   /** The process xml method */
   private static final Method PROCESSXML;

   static
   {
      try
      {
         PROCESSXML = XMLMessageListener.class.getMethod("processXML", new Class[] { Document.class });
      }
      catch (Exception e)
      {
         throw new RuntimeException(e);
      }
   }

   public FileXMLActivation(Timer timer, FileXMLActivationSpec spec)
   {
      this.timer = timer;
      this.spec = spec;
   }

   public void setEndpoint(MessageEndpoint endpoint)
   {
      this.endpoint = endpoint;
   }

   public void start() throws ResourceException
   {
      directory = new File(spec.getDirectory());
      if (directory.exists() == false)
         throw new ResourceException(directory + " does not exist");
      if (directory.isDirectory() == false)
         throw new ResourceException(directory + " is not a directory");

      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      try
      {
         builder = dbf.newDocumentBuilder();
      }
      catch (Exception e)
      {
         throw new ResourceException(e.toString());
      }

      timer.schedule(this, 0l, spec.getPeriodValue());
   }

   public void stop()
   {
      cancel();
      timer.cancel();
      endpoint.release();
   }

   public void run()
   {
      File[] files = directory.listFiles();
      for (int i = 0; i < files.length; ++i)
      {
         if (files[i].isFile())
         {
            currentFile = files[i];
            try
            {
               Document doc = parseFile(files[i]);
               if (doc != null)
                  processXML(doc);
            }
            finally
            {
               currentFile = null;
            }
         }
      }
   }

   protected Document parseFile(File file)
   {
      try
      {
         InputSource is = new InputSource(file.toURL().toString());
         return builder.parse(is);
      }
      catch (Throwable t)
      {
         log.error("Error parsing file " + file, t);
         return null;
      }
   }

   protected void processXML(Document doc)
   {
      try
      {
         endpoint.beforeDelivery(PROCESSXML);

         // At this point we are in the transaction and we have the mdb's classloader

         try
         {
            ((XMLMessageListener) endpoint).processXML(doc);
         }
         finally
         {
            // This must be invoked if beforeDelivery was invoked
            endpoint.afterDelivery();
         }
      }
      catch (Throwable t)
      {
         log.error("Error in message listener", t);
      }
   }

   // XAResource implementation (a bad implementation)

   public void start(Xid xid, int flags)
   {
   }

   public void end(Xid xid, int flags)
   {
   }

   public int prepare(Xid xid)
   {
      return XAResource.XA_OK;
   }

   public void rollback(Xid xid)
   {
   }

   public void commit(Xid xid, boolean onePhase) throws XAException
   {
      currentFile.delete();
   }

   public void forget(Xid xid)
   {
   }

   public Xid[] recover(int flag)
   {
      return new Xid[0];
   }

   public int getTransactionTimeout()
   {
      return 0;
   }

   public boolean setTransactionTimeout(int seconds)
   {
      return false;
   }

   public boolean isSameRM(XAResource xares)
   {
      return (xares == this);
   }
}

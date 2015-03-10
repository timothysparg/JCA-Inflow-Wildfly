/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.example;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.InvalidPropertyException;
import javax.resource.spi.ResourceAdapter;

/**
 * A FileXMLActivationSpec.
 * 
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.1 $
 */
public class FileXMLActivationSpec implements ActivationSpec
{
   private ResourceAdapter ra;
   
   private String directory;

   private long period = 10000;
   
   public void validate() throws InvalidPropertyException
   {
      // TODO validate
   }
   
   public String getDirectory()
   {
      return directory;
   }
   
   public void setDirectory(String directory)
   {
      this.directory = directory;
   }

   public long getPeriodValue()
   {
      return period;
   }

   public String getPeriod()
   {
      return Long.toString(period);
   }
   
   public void setPeriod(String period)
   {
      this.period = Long.parseLong(period);
   }

   public ResourceAdapter getResourceAdapter()
   {
      return ra;
   }
   
   public void setResourceAdapter(ResourceAdapter ra) throws ResourceException
   {
      this.ra = ra;
   }
   
   public String toString()
   {
      return "FileXMLActivationSpec for directory " + directory;
   }
}

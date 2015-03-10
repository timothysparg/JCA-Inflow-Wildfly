/*
 * JBoss, the OpenSource J2EE webOS
 * 
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.example;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;

import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.transaction.xa.XAResource;

import org.jboss.logging.Logger;

/**
 * A FileXMLResourceAdapter.
 *
 * @author <a href="adrian@jboss.com">Adrian Brock</a>
 * @version $Revision: 1.3 $
 */
public class FileXMLResourceAdapter implements ResourceAdapter {

    /**
     * The logger
     */
    private static final Logger log = Logger.getLogger(FileXMLResourceAdapter.class);

    /**
     * The bootstrap context
     */
    private BootstrapContext ctx;

    private final Map<ActivationSpec, FileXMLActivation> activations;

    public FileXMLResourceAdapter() {
        this.activations = Collections.synchronizedMap(new HashMap<ActivationSpec, FileXMLActivation>());
    }

    public void endpointActivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) throws ResourceException {
        FileXMLActivation activation = new FileXMLActivation(ctx.createTimer(), (FileXMLActivationSpec) spec);
        MessageEndpoint endpoint = endpointFactory.createEndpoint(activation);
        activation.setEndpoint(endpoint);
        activations.put(spec, activation);
        try {
            activation.start();
        } catch (ResourceException e) {
            endpoint.release();
            throw e;
        }
    }

    public void endpointDeactivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) {
        FileXMLActivation activation = (FileXMLActivation) activations.remove(spec);
        if (activation != null) {
            activation.stop();
        }
    }

    public XAResource[] getXAResources(ActivationSpec[] specs) throws ResourceException {
        // TODO getXAResources
        return null;
    }

    public void start(BootstrapContext ctx) throws ResourceAdapterInternalException {
        this.ctx = ctx;
    }

    public void stop() {
        for (Iterator i = activations.entrySet().iterator(); i.hasNext();) {
            Map.Entry entry = (Map.Entry) i.next();
            try {
                FileXMLActivation activation = (FileXMLActivation) entry.getValue();
                activation.stop();
            } catch (Exception ignored) {
                log.debug("Ignored", ignored);
            }
            i.remove();
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 59 * hash + Objects.hashCode(this.ctx);
        hash = 59 * hash + Objects.hashCode(this.activations);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final FileXMLResourceAdapter other = (FileXMLResourceAdapter) obj;
        if (!Objects.equals(this.ctx, other.ctx)) {
            return false;
        }
        if (!Objects.equals(this.activations, other.activations)) {
            return false;
        }
        return true;
    }

}

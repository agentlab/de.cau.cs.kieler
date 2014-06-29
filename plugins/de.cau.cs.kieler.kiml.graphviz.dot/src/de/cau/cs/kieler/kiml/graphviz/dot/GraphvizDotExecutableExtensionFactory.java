// SUPPRESS CHECKSTYLE NEXT Header
/*******************************************************************************
 * Copyright (c) 2009 itemis AG (http://www.itemis.eu) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package de.cau.cs.kieler.kiml.graphviz.dot;

import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExecutableExtensionFactory;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.Bundle;

import com.google.inject.Injector;

/**
 * The content of this class was mainly copied from
 * {@link org.eclipse.xtext.ui.guice.AbstractGuiceAwareExecutableExtensionFactory}.
 * 
 * @author Sven Efftinge - Initial contribution and API
 * @author Miro Spoenemann - Adapted to Graphviz Dot language
 * @kieler.design proposed by msp
 * @kieler.rating proposed yellow by msp
 */
public class GraphvizDotExecutableExtensionFactory implements IExecutableExtensionFactory,
        IExecutableExtension {
    
    private static final String GUICEKEY = "guicekey";
    
    private String clazzName;
    private IConfigurationElement config;

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings({ "unchecked" })
    public void setInitializationData(final IConfigurationElement theconfig, final String propertyName,
            final Object data)
            throws CoreException {
        if (data instanceof String) {
            clazzName = (String) data;
        } else if (data instanceof Map<?, ?>) {
            clazzName = ((Map<String, String>) data).get(GUICEKEY);
        }
        if (clazzName == null) {
            throw new IllegalArgumentException("couldn't handle passed data : " + data);
        }
        this.config = theconfig;
    }

    /**
     * {@inheritDoc}
     */
    public Object create() throws CoreException {
        Bundle bundle = GraphvizDotActivator.getInstance().getBundle();
        try {
            final Class<?> clazz = bundle.loadClass(clazzName);
            final Injector injector = GraphvizDotActivator.getInstance().getInjector(
                    GraphvizDotActivator.GRAPHVIZDOT);
            final Object result = injector.getInstance(clazz);
            if (result instanceof IExecutableExtension) {
                ((IExecutableExtension) result).setInitializationData(config, null, null);
            }
            return result;
        } catch (Exception e) {
            throw new CoreException(new Status(IStatus.ERROR, bundle.getSymbolicName(),
                    e.getMessage() + " ExtensionFactory: " + getClass().getName(), e));
        }
    }

}

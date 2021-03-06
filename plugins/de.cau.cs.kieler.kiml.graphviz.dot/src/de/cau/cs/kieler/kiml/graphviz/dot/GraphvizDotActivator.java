/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2012 by
 * + Christian-Albrechts-University of Kiel
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.kiml.graphviz.dot;

import org.eclipse.core.runtime.Plugin;
import org.osgi.framework.BundleContext;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;

import java.util.HashMap;
import java.util.Map;

/**
 * Activator for the Graphviz Dot plugin. This class was originally generated by Xtext.
 * 
 * @author msp
 * @kieler.design proposed by msp
 * @kieler.rating proposed yellow by msp
 */
public class GraphvizDotActivator extends Plugin {

    /** The Graphviz Dot language name. */
    public static final String GRAPHVIZDOT = "de.cau.cs.kieler.kiml.graphviz.dot.GraphvizDot";

    /** the injectors cache. */
    private Map<String, Injector> injectors = new HashMap<String, Injector>();

    /** the shared instance. */
    private static GraphvizDotActivator instance;

    /**
     * Returns the injector for the Graphviz Dot language.
     * 
     * @param languageName the language name
     * @return the injector
     */
    public Injector getInjector(final String languageName) {
        Injector injector = injectors.get(languageName);
        if (injector == null) {
            Module runtimeModule = getRuntimeModule(languageName);
            injector = Guice.createInjector(runtimeModule);
            injectors.put(languageName, injector);
        }
        return injector;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void start(final BundleContext context) throws Exception {
        super.start(context);
        instance = this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop(final BundleContext context) throws Exception {
        injectors.clear();
        instance = null;
        super.stop(context);
    }

    /**
     * Returns the shared instance.
     * 
     * @return the activator instance
     */
    public static GraphvizDotActivator getInstance() {
        return instance;
    }

    /**
     * Returns the runtime module for the Graphviz Dot grammar.
     * 
     * @param grammar the grammar name
     * @return the runtime module
     */
    protected Module getRuntimeModule(final String grammar) {
        if (GRAPHVIZDOT.equals(grammar)) {
            return new GraphvizDotRuntimeModule();
        }
        throw new IllegalArgumentException(grammar);
    }

}

/*
 * KIELER - Kiel Integrated Environment for Layout Eclipse RichClient
 *
 * http://www.informatik.uni-kiel.de/rtsys/kieler/
 * 
 * Copyright 2011 by
 * + Christian-Albrechts-University of Kiel
 *   + Department of Computer Science
 *     + Real-Time and Embedded Systems Group
 * 
 * This code is provided under the terms of the Eclipse Public License (EPL).
 * See the file epl-v10.html for the license text.
 */
package de.cau.cs.kieler.kiml.service;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.ecore.EClass;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

import de.cau.cs.kieler.core.properties.IProperty;
import de.cau.cs.kieler.core.properties.IPropertyHolder;
import de.cau.cs.kieler.core.properties.IPropertyValueProxy;
import de.cau.cs.kieler.core.properties.MapPropertyHolder;
import de.cau.cs.kieler.core.properties.Property;
import de.cau.cs.kieler.core.util.Pair;
import de.cau.cs.kieler.kiml.LayoutDataService;
import de.cau.cs.kieler.kiml.LayoutOptionData;
import de.cau.cs.kieler.kiml.config.ILayoutConfig;
import de.cau.cs.kieler.kiml.config.SemanticLayoutConfig;
import de.cau.cs.kieler.kiml.util.LayoutOptionProxy;

/**
 * Service class for layout information such as registered diagram types and
 * pre-configured layout option values.
 *
 * @author msp
 * @kieler.design proposed by msp
 * @kieler.rating proposed yellow 2012-07-10 msp
 */
public abstract class LayoutInfoService {
    
    /** identifier of the extension point for layout info. */
    protected static final String EXTP_ID_LAYOUT_INFO = "de.cau.cs.kieler.kiml.layoutInfo";
    /** name of the 'binding' element in the 'layout info' extension point. */
    protected static final String ELEMENT_BINDING = "binding";
    /** name of the 'diagram type' element in the 'layout info' extension point. */
    protected static final String ELEMENT_DIAGRAM_TYPE = "diagramType";
    /** name of the 'option' element in the 'layout info' extension point. */
    protected static final String ELEMENT_OPTION = "option";
    /** name of the 'semantic option' element in the 'layout info' extension point. */
    protected static final String ELEMENT_SEMANTIC_OPTION = "semanticOption";
    /** name of the 'config' element in the 'layout info' extension point. */
    protected static final String ELEMENT_CONFIG = "config";
    /** name of the 'activation' attribute in the extension points. */
    protected static final String ATTRIBUTE_ACTIVATION = "activation";
    /** name of the 'activationAction' attribute in the extension points. */
    protected static final String ATTRIBUTE_ACTIVATION_ACTION = "activationAction";
    /** name of the 'activationText' attribute in the extension points. */
    protected static final String ATTRIBUTE_ACTIVATION_TEXT = "activationText";
    /** name of the 'class' attribute in the extension points. */
    protected static final String ATTRIBUTE_CLASS = "class";
    /** name of the 'config' attribute in the extension points. */
    protected static final String ATTRIBUTE_CONFIG = "config";
    /** name of the 'default' attribute in the extension points. */
    protected static final String ATTRIBUTE_DEFAULT = "default";
    /** name of the 'id' attribute in the extension points. */
    protected static final String ATTRIBUTE_ID = "id";
    /** name of the 'name' attribute in the extension points. */
    protected static final String ATTRIBUTE_NAME = "name";
    /** name of the 'option' attribute in the extension points. */
    protected static final String ATTRIBUTE_OPTION = "option";
    /** name of the 'value' attribute in the extension points. */
    protected static final String ATTRIBUTE_VALUE = "value";
    
    /**
     * Data element for general layout configurators.
     */
    public static class ConfigData {
        /** the layout configurator implementation. */
        private ILayoutConfig config;
        /** the activation property. */
        private IProperty<Boolean> activation;
        /** the text of the activation menu entry. */
        private String activationText;
        /** the activation action that is executed when activation changes. */
        private Runnable activationAction;
        
        /**
         * Returns the layout configurator implementation.
         * 
         * @return the layout configurator
         */
        public ILayoutConfig getConfig() {
            return config;
        }
        
        /**
         * Returns the activation property.
         * 
         * @return the activation property
         */
        public IProperty<Boolean> getActivationProperty() {
            return activation;
        }
        
        /**
         * Returns the activation menu entry text.
         * 
         * @return the activation menu entry text
         */
        public String getActivationText() {
            return activationText;
        }
        
        /**
         * Returns the activation action that is executed when the configurator is enabled or
         * disabled.
         * 
         * @return the activation action
         */
        public Runnable getActivationAction() {
            return activationAction;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {
            if (config == null) {
                return super.toString();
            }
            return config.getClass().getSimpleName();
        }
    }
    
    /** the singleton instance of the layout info service. */
    private static LayoutInfoService instance;
    
    /**
     * Returns the singleton instance of the layout info service.
     * 
     * @return the singleton instance
     */
    public static LayoutInfoService getInstance() {
        return instance;
    }

    /**
     * Protected constructor to enforce instantiation in subclasses.
     */
    protected LayoutInfoService() {
        // the layout info service is supposed to exist exactly once
        instance = this;
    }
    
    /** mapping of diagram type identifiers to their names. */
    private final Map<String, String> diagramTypeMap = Maps.newLinkedHashMap();
    /** mapping of object identifiers to associated options. */
    private final Map<String, Map<String, Object>> id2OptionsMap = Maps.newHashMap();
    /** mapping of domain class names to semantic layout configurations. */
    private final Multimap<String, SemanticLayoutConfig> semanticConfigMap = HashMultimap.create();
    /** list of general layout configurators. */
    private final List<ConfigData> configData = Lists.newLinkedList();
    /** property map for activation of registered layout configurators. */
    private final MapPropertyHolder configProperties = new MapPropertyHolder();
    
    /**
     * Returns the property holder to activate or deactivate registered layout configurations.
     * 
     * @return the property holder for configuration activation
     */
    public IPropertyHolder getConfigProperties() {
        return configProperties;
    }
    
    /**
     * Report an error that occurred while reading extensions.
     * 
     * @param extensionPoint the identifier of the extension point
     * @param element the configuration element
     * @param attribute the attribute that contains an invalid entry
     * @param exception an optional exception that was caused by the invalid entry
     */
    protected abstract void reportError(String extensionPoint, IConfigurationElement element,
            String attribute, Throwable exception);

    /**
     * Report an error that occurred while reading extensions.
     * 
     * @param exception a core exception holding a status with further information
     */
    protected abstract void reportError(CoreException exception);

    /**
     * Loads and registers all layout info extensions from the extension point.
     */
    protected void loadLayoutInfoExtensions() {
        IConfigurationElement[] extensions = Platform.getExtensionRegistry()
                .getConfigurationElementsFor(EXTP_ID_LAYOUT_INFO);
        LayoutDataService layoutDataService = LayoutDataService.getInstance();
        assert layoutDataService != null;

        for (IConfigurationElement element : extensions) {
            if (ELEMENT_DIAGRAM_TYPE.equals(element.getName())) {
                // register a diagram type from the extension
                String id = element.getAttribute(ATTRIBUTE_ID);
                String name = element.getAttribute(ATTRIBUTE_NAME);
                if (id == null || id.length() == 0) {
                    reportError(EXTP_ID_LAYOUT_INFO, element, ATTRIBUTE_ID, null);
                } else if (name == null) {
                    reportError(EXTP_ID_LAYOUT_INFO, element, ATTRIBUTE_NAME, null);
                } else {
                    addDiagramType(id, name);
                }
            } else if (ELEMENT_OPTION.equals(element.getName())) {
                // register a layout option from the extension
                String classId = element.getAttribute(ATTRIBUTE_CLASS);
                String optionId = element.getAttribute(ATTRIBUTE_OPTION);
                String valueString = element.getAttribute(ATTRIBUTE_VALUE);
                if (classId == null || classId.length() == 0) {
                    reportError(EXTP_ID_LAYOUT_INFO, element, ATTRIBUTE_CLASS, null);
                } else if (optionId == null || optionId.length() == 0) {
                    reportError(EXTP_ID_LAYOUT_INFO, element, ATTRIBUTE_OPTION, null);
                } else {
                    LayoutOptionData<?> optionData = layoutDataService.getOptionData(optionId);
                    if (optionData != null) {
                        try {
                            Object value = optionData.parseValue(valueString);
                            if (value != null) {
                                addOptionValue(classId, optionId, value);
                            }
                        } catch (IllegalStateException exception) {
                            reportError(EXTP_ID_LAYOUT_INFO, element, ATTRIBUTE_VALUE, exception);
                        }
                    } else if (valueString != null) {
                        // the layout option could not be resolved, so create a proxy
                        addOptionValue(classId, optionId, new LayoutOptionProxy(valueString));
                    }

                }
            } else if (ELEMENT_SEMANTIC_OPTION.equals(element.getName())) {
                // register a semantic layout configuration from the extension
                try {
                    SemanticLayoutConfig config = (SemanticLayoutConfig)
                            element.createExecutableExtension(ATTRIBUTE_CONFIG);
                    String clazz = element.getAttribute(ATTRIBUTE_CLASS);
                    if (clazz == null || clazz.length() == 0) {
                        reportError(EXTP_ID_LAYOUT_INFO, element, ATTRIBUTE_CLASS, null);
                    } else {
                        addSemanticConfig(clazz, config);
                    }
                } catch (CoreException exception) {
                    reportError(exception);
                }
            } else if (ELEMENT_CONFIG.equals(element.getName())) {
                // register a general layout configuration from the extension
                try {
                    ConfigData data = new ConfigData();
                    data.config = (ILayoutConfig) element.createExecutableExtension(ATTRIBUTE_CLASS);
                    String activationId = element.getAttribute(ATTRIBUTE_ACTIVATION);
                    if (activationId != null) {
                        String def = element.getAttribute(ATTRIBUTE_DEFAULT);
                        Boolean defaultActivation = def == null ? Boolean.FALSE : Boolean.valueOf(def);
                        data.activation = new Property<Boolean>(activationId, defaultActivation);
                    }
                    String text = element.getAttribute(ATTRIBUTE_ACTIVATION_TEXT);
                    data.activationText = text;
                    if (element.getAttribute(ATTRIBUTE_ACTIVATION_ACTION) != null) {
                        Runnable action = (Runnable) element.createExecutableExtension(
                                ATTRIBUTE_ACTIVATION_ACTION);
                        data.activationAction = action;
                    }
                    configData.add(data);
                } catch (CoreException exception) {
                    reportError(exception);
                }
            }
        }
    }

    
    /**
     * Registers the given diagram type.
     * 
     * @param id
     *            identifier of the diagram type
     * @param name
     *            user friendly name of the diagram type
     */
    protected final void addDiagramType(final String id, final String name) {
        diagramTypeMap.put(id, name);
    }

    /**
     * Adds the given layout option value as default for an object identifier.
     * 
     * @param id
     *            identifier of the object to register
     * @param optionId
     *            identifier of a layout option
     * @param value
     *            value for the layout option
     */
    public final void addOptionValue(final String id, final String optionId, final Object value) {
        Map<String, Object> optionsMap = id2OptionsMap.get(id);
        if (optionsMap == null) {
            optionsMap = new LinkedHashMap<String, Object>();
            id2OptionsMap.put(id, optionsMap);
        }
        optionsMap.put(optionId, value);
    }

    /**
     * Remove the value of the given layout option.
     * 
     * @param id
     *            identifier of the object for which an option shall be removed
     * @param optionId
     *            identifier of a layout option
     */
    public final void removeOptionValue(final String id, final String optionId) {
        Map<String, Object> optionsMap = id2OptionsMap.get(id);
        if (optionsMap != null) {
            optionsMap.remove(optionId);
        }
    }

    /**
     * Registers the given semantic layout configuration.
     * 
     * @param clazzName
     *            domain model class name for which to register the configuration
     * @param config
     *            a semantic layout configuration
     */
    protected final void addSemanticConfig(final String clazzName, final SemanticLayoutConfig config) {
        semanticConfigMap.put(clazzName, config);
    }

    /**
     * Returns the name of the given diagram type.
     * 
     * @param id
     *            identifier of the diagram type
     * @return user friendly name of the diagram type, or {@code null} if there is no diagram type
     *         with the given identifier
     */
    public final String getDiagramTypeName(final String id) {
        return diagramTypeMap.get(id);
    }

    /**
     * Returns a collection of registered diagram types. The first element of each returned entry is
     * a diagram type identifier, the second element is the corresponding name.
     * 
     * @return the registered diagram types
     */
    public final List<Pair<String, String>> getDiagramTypes() {
        return Pair.fromMap(diagramTypeMap);
    }

    /**
     * Returns a map that contains all layout option values for an object identifier.
     * 
     * @param objectId
     *            an object identifier, such as an edit part class name, a domain model class name,
     *            or a diagram type id
     * @return a map of layout option identifiers to their values
     */
    public final Map<String, Object> getOptionValues(final String objectId) {
        Map<String, Object> optionsMap = id2OptionsMap.get(objectId);
        if (optionsMap != null) {
            LayoutDataService dataService = LayoutDataService.getInstance();
            for (Map.Entry<String, Object> entry : optionsMap.entrySet()) {
                Object value = entry.getValue();
                if (value instanceof IPropertyValueProxy) {
                    value = ((IPropertyValueProxy) value).resolveValue(
                            dataService.getOptionData(entry.getKey()));
                    if (value != null) {
                        entry.setValue(value);
                    }
                }
            }
            return Collections.unmodifiableMap(optionsMap);
        }
        return Collections.emptyMap();
    }

    /**
     * Retrieves a layout option value for an object identifier.
     * 
     * @param objectId
     *            an object identifier, such as an edit part class name, a domain model class name,
     *            or a diagram type id
     * @param optionId
     *            a layout option identifier
     * @return the preconfigured value of the option, or {@code null} if the option is not set for
     *         the given object
     */
    public final Object getOptionValue(final String objectId, final String optionId) {
        Map<String, Object> optionsMap = id2OptionsMap.get(objectId);
        if (optionsMap != null) {
            Object value = optionsMap.get(optionId);
            if (value instanceof IPropertyValueProxy) {
                value = ((IPropertyValueProxy) value).resolveValue(
                        LayoutDataService.getInstance().getOptionData(optionId));
                if (value != null) {
                    optionsMap.put(optionId, value);
                }
            }
            return value;
        }
        return null;
    }

    /**
     * Returns a map that contains all layout option values for a domain model class. This involves
     * options that are set for any superclass of the given one.
     * 
     * @param clazz
     *            a domain model class
     * @return a map of layout option identifiers to their values
     */
    public final Map<String, Object> getOptionValues(final EClass clazz) {
        if (clazz != null) {
            LayoutDataService dataService = LayoutDataService.getInstance();
            HashMap<String, Object> options = new HashMap<String, Object>();
            LinkedList<EClass> classes = new LinkedList<EClass>();
            classes.add(clazz);
            do {
                EClass c = classes.removeFirst();
                Map<String, Object> optionsMap = id2OptionsMap.get(c.getInstanceTypeName());
                if (optionsMap != null) {
                    for (Map.Entry<String, Object> entry : optionsMap.entrySet()) {
                        Object value = entry.getValue();
                        if (value instanceof IPropertyValueProxy) {
                            value = ((IPropertyValueProxy) value).resolveValue(
                                    dataService.getOptionData(entry.getKey()));
                            if (value != null) {
                                entry.setValue(value);
                            }
                        }
                        if (value != null) {
                            options.put(entry.getKey(), value);
                        }
                    }
                }
                classes.addAll(c.getESuperTypes());
            } while (!classes.isEmpty());
            return options;
        }
        return Collections.emptyMap();
    }

    /**
     * Retrieves a layout option value for a domain model class. This involves options that are set
     * for any superclass of the given one.
     * 
     * @param clazz
     *            a domain model class
     * @param optionId
     *            a layout option identifier
     * @return the option value for the class or a superclass, or {@code null} if the option is not
     *         set for the class
     */
    public final Object getOptionValue(final EClass clazz, final String optionId) {
        if (clazz != null) {
            LayoutDataService dataService = LayoutDataService.getInstance();
            LinkedList<EClass> classes = new LinkedList<EClass>();
            classes.add(clazz);
            do {
                EClass c = classes.removeFirst();
                Map<String, Object> optionsMap = id2OptionsMap.get(c.getInstanceTypeName());
                if (optionsMap != null) {
                    Object value = optionsMap.get(optionId);
                    if (value instanceof IPropertyValueProxy) {
                        value = ((IPropertyValueProxy) value).resolveValue(
                                dataService.getOptionData(optionId));
                        if (value != null) {
                            optionsMap.put(optionId, value);
                        }
                    }
                    if (value != null) {
                        return value;
                    }
                }
                classes.addAll(c.getESuperTypes());
            } while (!classes.isEmpty());
        }
        return null;
    }

    /**
     * Return the semantic layout configurators that are associated with the given domain model
     * class. This involves configurators that are set for any superclass of the given one.
     * 
     * @param clazz
     *            a domain model class
     * @return the semantic layout configurators for the class or a superclass
     */
    public final List<ILayoutConfig> getSemanticConfigs(final EClass clazz) {
        if (clazz != null) {
            List<ILayoutConfig> configs = new LinkedList<ILayoutConfig>();
            LinkedList<EClass> classes = new LinkedList<EClass>();
            classes.add(clazz);
            do {
                EClass c = classes.removeFirst();
                if (semanticConfigMap.containsKey(c.getInstanceTypeName())) {
                    configs.addAll(semanticConfigMap.get(c.getInstanceTypeName()));
                }
                classes.addAll(c.getESuperTypes());
            } while (!classes.isEmpty());
            return configs;
        }
        return Collections.emptyList();
    }

    /**
     * Returns all general layout configurators that are currently active.
     * 
     * @return the active layout configurators
     */
    public final List<ILayoutConfig> getActiveConfigs() {
        LinkedList<ILayoutConfig> configs = new LinkedList<ILayoutConfig>();
        for (ConfigData data : configData) {
            if (data.activation == null || configProperties.getProperty(data.activation)) {
                configs.add(data.config);
            }
        }
        return configs;
    }
    
    /**
     * Returns all general layout configuration data.
     * 
     * @return the registered layout configurations
     */
    public final List<ConfigData> getConfigData() {
        return configData;
    }

}

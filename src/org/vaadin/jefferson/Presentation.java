/*
 * Copyright 2011 Vaadin Ltd.
 * 
 * Licensed under the GNU Affero General Public License, Version 2 (the 
 * "License"); you may not use this file except in compliance with the License. 
 * You may obtain a copy of the License at
 * 
 * http://www.gnu.org/licenses/agpl.html
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.vaadin.jefferson;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Map;

import org.vaadin.jefferson.content.UIElement;
import org.vaadin.jefferson.content.View;

import com.vaadin.tools.ReflectTools;
import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;

/**
 * A presentation of some Vaadin content. The normal usage is to define some
 * rules using its various <code>define(…)</code> methods and then call
 * {@link #render(UIElement)}.
 * <p>
 * As a convenience, a centralized {@link Presenter} can be set to receive
 * callbacks for each successfully rendered content node. This is, however,
 * purely optional, as the presentation will already register renditions by
 * calling {@link UIElement#setRendition(Component)} on each rendered content
 * node.
 * 
 * @author Marlon Richert @ Vaadin
 */
public class Presentation {

    private final Map<String, Class<? extends Component>> nameClasses = new HashMap<String, Class<? extends Component>>();
    private final Map<Class<? extends UIElement<?>>, Class<? extends Component>> typeClasses = new HashMap<Class<? extends UIElement<?>>, Class<? extends Component>>();
    private final Map<Class<? extends UIElement<?>>, Method> typeMethods = new HashMap<Class<? extends UIElement<?>>, Method>();
    private final Map<String, Method> nameMethods = new HashMap<String, Method>();

    private Presenter presenter;

    /**
     * Creates a new presentation without a presenter.
     */
    public Presentation() {
        this(new Presenter() {
            public void register(UIElement<?> content) {
                return;
            }
        });
    }

    /**
     * Creates a new presentation that registers rendered content nodes with the
     * given presenter.
     * 
     * @param presenter
     *            The presenter to register rendered content with.
     * @see #setPresenter(Presenter)
     */
    public Presentation(Presenter presenter) {
        this.presenter = presenter;
    }

    /**
     * Sets this presentation's presenter. Each time a {@link UIElement} has
     * been rendered, this presentation will call
     * {@link Presenter#register(UIElement)} (<i>after</i> calling
     * {@link UIElement#setRendition(Component)}).
     * 
     * @param presenter
     *            The presenter to register rendered content with.
     */
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;
    }

    /**
     * Defines a rule that a {@link UIElement} with the given name should be
     * rendered as an instance of the given subclass of {@link Component}.
     * Instantiation is done by calling the String-arg or (if that fails)
     * no-args constructor of the given class.
     * <p>
     * Rules defined with this method take precedence over those defined with
     * {@link #define(Class, Class)}.
     * 
     * @param contentName
     *            The content's name.
     * @param renditionClass
     *            The class to use for rendering.
     */
    public void define(String contentName,
            Class<? extends Component> renditionClass) {
        nameClasses.put(contentName, renditionClass);
    }

    /**
     * Defines a rule that a {@link UIElement} of the given type should be
     * rendered as an instance of the given subclass of {@link Component}.
     * Instantiation is done by calling the String-arg or (if that fails)
     * no-args constructor of the given class.
     * <p>
     * Rules defined with {@link #define(String, Class)} take precedence over
     * those defined with this method.
     * 
     * @param contentClass
     *            The content's class.
     * @param renditionClass
     *            The class to use for rendering.
     */
    public void define(Class<? extends UIElement<?>> contentClass,
            Class<? extends Component> renditionClass) {
        typeClasses.put(contentClass, renditionClass);
    }

    /**
     * Returns the method in this presentation that has the signature
     * <code>name(UIElement, Component)</code>. Convenience method for use with
     * {@link #define(Class, Method)} and {@link #define(String, Method)}.
     * 
     * @param name
     *            The name of the method to return.
     * @return A method in this presentation.
     */
    protected Method method(String name) {
        Method method = ReflectTools.findMethod(getClass(), name,
                UIElement.class, Component.class);
        return AccessController.doPrivileged(new AccessibleMethod(method));
    }

    /**
     * Defines a rule that a {@link UIElement} with the given name should be
     * initialized by a method with the given name in this presentation. The
     * actual method should be defined in this presentation by sub-classing and
     * should have the signature <code>methodName(UIElement, Component)</code>.
     * <p>
     * Rules defined by this method are applied <i>after</i> those defined by
     * {@link #define(Class, String)}.
     * 
     * @param contentName
     *            The content's name.
     * @param method
     *            A method in this presentation.
     * @see #method(String)
     */
    public void define(String contentName, Method method) {
        nameMethods.put(contentName, method);
    }

    /**
     * Defines a rule that a {@link UIElement} of the given type should be
     * initialized by a method with the given name in this presentation. The
     * actual method should be defined in this presentation by sub-classing and
     * should have the signature <code>methodName(UIElement, Component)</code>.
     * <p>
     * Rules defined by this method are applied <i>before</i> those defined by
     * {@link #define(Class, String)}.
     * 
     * @param contentClass
     *            The content's class.
     * @param method
     *            A method in this presentation.
     * @see #method(String)
     */
    public void define(Class<? extends UIElement<?>> contentClass, Method method) {
        typeMethods.put(contentClass, method);
    }

    /**
     * Renders the given {@link UIElement} according to the rules defined in
     * this presentation.
     * 
     * @param <T>
     *            The type of the rendition component.
     * @param content
     *            The content hierarchy to render.
     * @return The top-level component of the hierarchy.
     * @throws InstantiationException
     *             if a rule-defined class cannot be instantiated by this
     *             presentation
     * @throws IllegalAccessException
     *             if a rule-defined constructor or method cannot be called by
     *             this presentation
     * @throws InvocationTargetException
     *             when a rule-defined method throws an exception
     * @throws NoSuchMethodException
     *             if a rule-defined class has neither a no-args nor a
     *             String-arg constructor
     */
    public <T extends Component> T render(UIElement<T> content)
            throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {
        String name = content.getName();

        @SuppressWarnings("rawtypes")
        Class<? extends UIElement> type = content.getClass();

        T component = create(content);

        component.addStyleName(name.toLowerCase().replace(' ', '-'));
        component.setSizeUndefined();

        init(content, component, typeMethods.get(type));
        init(content, component, nameMethods.get(name));

        if (content instanceof View) {
            for (UIElement<?> child : ((View) content).getChildren()) {
                ((ComponentContainer) component).addComponent(render(child));
            }
        }

        content.setRendition(component);
        presenter.register(content);

        return component;
    }

    /**
     * Instantiates a rendition for the given content. If any rules are defined
     * for the given content, it will use those rules to instantiate the
     * rendition. If not, it will return the content's default rendition.
     * 
     * @param <T>
     * 
     * @param content
     *            The content node to create a rendering for.
     * @return A uninitialized rendering for the given content node.
     * @throws InstantiationException
     *             if any rule-defined class cannot be instantiated by this
     *             presentation
     * @throws IllegalAccessException
     *             if any rule-defined constructor cannot be accessed by this
     *             presentation
     * @throws InvocationTargetException
     *             if the constructor throws an exception
     * @throws NoSuchMethodException
     *             if the rule-defined class has neither a no-args nor a
     *             String-arg constructor
     */
    @SuppressWarnings("unchecked")
    protected <T extends Component> T create(UIElement<T> content)
            throws InstantiationException, IllegalAccessException,
            InvocationTargetException, NoSuchMethodException {

        Class<T> rendition = (Class<T>) nameClasses.get(content.getName());
        if (rendition == null) {
            rendition = (Class<T>) typeClasses.get(content.getClass());
            if (rendition == null) {
                rendition = (Class<T>) content.getDefaultRenditionClass();
            }
        }

        try {
            return rendition.getConstructor(String.class).newInstance(
                    content.getName());
        } catch (NoSuchMethodException e) {
            return rendition.getConstructor().newInstance();
        }
    }

    /**
     * Initializes the given component by calling
     * 
     * <pre>
     * Presentation.this.methodName(content, component)
     * </pre>
     * 
     * @param content
     *            The content for which the given component was rendered.
     * @param component
     *            The component used to render the given content.
     * @param methodName
     *            The name of the method with which to initialize the given
     *            component.
     * @throws IllegalAccessException
     *             if the given method is inaccessible from this presentation
     * @throws InvocationTargetException
     *             when the given method throws an exception
     */
    protected void init(UIElement<?> content, Component component, Method method)
            throws IllegalAccessException, InvocationTargetException {
        if (method != null) {
            method.invoke(Presentation.this, content, component);
        }
    }

    private final static class AccessibleMethod implements
            PrivilegedAction<Method> {
        private final Method method;

        private AccessibleMethod(Method method) {
            this.method = method;
        }

        public Method run() {
            method.setAccessible(true);
            return method;
        }
    }
}

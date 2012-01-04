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

import java.util.LinkedHashSet;
import java.util.Set;

import com.vaadin.ui.ComponentContainer;

/**
 * A {@link View} with child views, rendered as a subclass of
 * {@link com.vaadin.ui.ComponentContainer}.
 * 
 * @param <T>
 *            This view's base rendering class.
 * @author Marlon Richert @ Vaadin
 */
public class Composite<T extends ComponentContainer> extends View<T> {
    private Set<View<?>> children = new LinkedHashSet<View<?>>();

    public Composite(String name, Class<T> base, Class<? extends T> impl) {
        super(name, base, impl);
    }

    public Composite(String name, Class<T> base, Class<? extends T> impl,
            View<?>... children) {
        super(name, base, impl);
        setChildren(children);
    }

    /**
     * Sets this view's children.
     * 
     * @param children
     *            This view's child content nodes.
     */
    public Composite<T> setChildren(View<?>... children) {
        T rendition = getRendition();
        if (rendition != null) {
            rendition.removeAllComponents();
        }
        for (View<?> child : children) {
            this.children.add(child);
            child.setParent(this);
        }
        return this;
    }

    /**
     * Sets this view's rendering component and calls
     * {@link Presentation#visit(View)} for each of its children.
     */
    @Override
    protected void accept(Presentation presentation, T rendition) {
        super.accept(presentation, rendition);
        visitChildren();
    }

    protected void visitChildren() {
        T rendition = getRendition();
        rendition.removeAllComponents();
        for (View<?> child : children) {
            rendition.addComponent(getPresentation().visit(child));
        }
    }

    /**
     * Gets this view's children.
     * 
     * @return This view's child content nodes.
     */
    public View<?>[] getChildren() {
        return children.toArray(new View<?>[children.size()]);
    }

    public boolean replaceChild(View<?> existing, View<?> replacement) {
        if (!children.contains(existing)) {
            return false;
        }
        existing.setParent(null);
        replacement.setParent(this);
        children.remove(existing);
        children.add(replacement);
        getRendition().replaceComponent(
                existing.getRendition(), getPresentation().visit(replacement));
        return true;
    }
}

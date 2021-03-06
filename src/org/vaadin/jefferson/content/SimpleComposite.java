/*
 * Copyright 2011 Vaadin Ltd.
 * 
 * Licensed under the GNU Affero General Public License, Version 3 (the 
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
package org.vaadin.jefferson.content;

import org.vaadin.jefferson.Composite;
import org.vaadin.jefferson.View;

import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CssLayout;

public class SimpleComposite extends Composite<ComponentContainer> {

    public SimpleComposite(String name) {
        super(name, ComponentContainer.class);
    }

    public SimpleComposite(String name, View<?>... children) {
        super(name, ComponentContainer.class, children);
    }

    @Override
    public ComponentContainer createFallback() {
        return new CssLayout();
    }
}

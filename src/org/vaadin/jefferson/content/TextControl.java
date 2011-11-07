package org.vaadin.jefferson.content;

import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.TextField;

public class TextControl extends Control<AbstractTextField> {

    public TextControl(String name) {
        super(name);
    }

    @Override
    public Class<? extends AbstractTextField> getDefaultRenderingClass() {
        return TextField.class;
    }

    @Override
    public Class<AbstractTextField> getRenderingInterface() {
        return AbstractTextField.class;
    }
}

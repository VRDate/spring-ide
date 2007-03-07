/*
 * Copyright 2002-2007 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */ 

package org.springframework.ide.eclipse.beans.ui.graph.figures;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.ToolbarLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.ide.eclipse.beans.ui.BeansUIPlugin;
import org.springframework.ide.eclipse.beans.ui.graph.model.Bean;
import org.springframework.ide.eclipse.beans.ui.graph.model.ConstructorArgument;
import org.springframework.ide.eclipse.beans.ui.graph.model.Property;

/**
 * @author Torsten Juergeleit
 */
public class BeanFigure extends Figure {

	public static final Color COLOR = new Color(null, 255, 255, 206);
	public static final int MAX_NAME_LENGTH = 20;

	protected Bean bean;

	public BeanFigure(Bean bean) {
		this.bean = bean;
		ToolbarLayout layout = new ToolbarLayout();
		setLayoutManager(layout);
		setBorder(new ShadowedLineBorder(ColorConstants.black, 1, 4));
		setBackgroundColor(COLOR);
		setOpaque(true);

		// Prepare child figures
		add(createLabel(bean));
		if (bean.hasConstructorArguments()) {
			add(createConstructorArguments(bean));
		}
		if (bean.hasProperties()) {
			add(createProperties(bean));
		}
	}

	protected Label createLabel(Bean bean) {
		Label label = new Label();
		label.setText(bean.getName());
		if (bean.isRootBean()) {
			label.setIcon(BeansUIPlugin.getLabelProvider().getImage(
					bean.getBean()));
			if (bean.getClassName() != null) {
				label.setToolTip(new Label("Class: " + bean.getClassName()));
			}
			else {
				// TODO set tooltip for abstract beans, bean factories, ...
				label.setToolTip(new Label("Class: <no class specified>"));
			}
		}
		else if (bean.isChildBean()) {
			label.setIcon(BeansUIPlugin.getLabelProvider().getImage(
					bean.getBean()));
			label.setToolTip(new Label("Parent: " + bean.getParentName()));
		}
		else {
			// FIXME Handle factory beans
//			label.setIcon(BeansGraphImages.getImage(
//										 BeansGraphImages.IMG_OBJS_CHILD_BEAN));
//			label.setToolTip(new Label("Parent: " + bean.getParentName()));
		}
		return label;
	}

	protected ConstructorArgumentFigure createConstructorArguments(Bean bean) {
		ConstructorArgument[] cargs = bean.getConstructorArguments();
		ConstructorArgumentFigure figure = new ConstructorArgumentFigure();
		for (int i = 0; i < cargs.length; i++) {
			ConstructorArgument carg = cargs[i];
			String name = carg.getName();
			Label label = new Label();

			// Display a truncated element name if necessary
			if (name.length() > MAX_NAME_LENGTH) {
				label.setText(name.substring(0, MAX_NAME_LENGTH) + "...");
				label.setToolTip(new Label(name));
			} else {
				label.setText(name);
				Object value = carg.getBeanConstructorArgument().getValue();
				label.setToolTip(new Label(createToolTipForValue(value)));
			}
			label.setIcon(BeansUIPlugin.getLabelProvider().getImage(
					carg.getBeanConstructorArgument()));
			figure.add(label);
		}
		return figure;
	}

	protected PropertiesFigure createProperties(Bean bean) {
		Property[] props = bean.getProperties();
		PropertiesFigure properties = new PropertiesFigure();
		for (int i = 0; i < props.length; i++) {
			Property prop = props[i];
			Label label = new Label(prop.getName());
			label.setIcon(BeansUIPlugin.getLabelProvider().getImage(
					prop.getBeanProperty()));
			Object value = prop.getBeanProperty().getValue();
			label.setToolTip(new Label(createToolTipForValue(value)));
			properties.add(label);
		}
		return properties;
	}

	private String createToolTipForValue(Object value) {
		StringBuffer toolTip = new StringBuffer("Value: ");
		if (value == null) {
			toolTip.append("NULL");
		} else if (value instanceof RuntimeBeanReference) {
			toolTip.append('<');
			toolTip.append(((RuntimeBeanReference) value).getBeanName());
			toolTip.append('>');
		} else if (value instanceof BeanDefinitionHolder) {
			toolTip.append('{');
			toolTip.append(((BeanDefinitionHolder) value).getBeanName());
			toolTip.append('}');
		} else {
			toolTip.append(value.toString());
		}
		return toolTip.toString();
	}

	protected void paintFigure(Graphics graphics) {
		if (isOpaque()) {
			graphics.fillRectangle(getClientArea());
		}
	}

	public String toString() {
		Rectangle rect = getBounds();
		return "BeanFigure '" + bean.getName() + "': x=" + rect.x + ", y=" +
			   rect.y + ", width=" + rect.width + ", height=" + rect.height;
	}
}

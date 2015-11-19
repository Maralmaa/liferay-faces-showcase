/**
 * Copyright (c) 2000-2015 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
package com.liferay.faces.showcase.component.accordion.internal;

import java.io.IOException;
import java.util.List;

import javax.faces.application.ResourceDependencies;
import javax.faces.application.ResourceDependency;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.context.ResponseWriter;
import javax.faces.render.FacesRenderer;

import com.liferay.faces.showcase.component.accordion.Accordion;
import com.liferay.faces.showcase.component.tab.Tab;
import com.liferay.faces.showcase.component.tab.TabUtil;
import com.liferay.faces.showcase.util.ShowcaseUtil;
import com.liferay.faces.util.client.Script;
import com.liferay.faces.util.client.ScriptFactory;
import com.liferay.faces.util.component.Styleable;
import com.liferay.faces.util.context.FacesRequestContext;
import com.liferay.faces.util.factory.FactoryExtensionFinder;
import com.liferay.faces.util.logging.Logger;
import com.liferay.faces.util.logging.LoggerFactory;
import com.liferay.faces.util.render.RendererUtil;


/**
 * @author  Vernon Singleton
 */
//J-
@FacesRenderer(componentFamily = Accordion.COMPONENT_FAMILY, rendererType = Accordion.RENDERER_TYPE)
@ResourceDependencies(
	{
		@ResourceDependency(library = "bootstrap", name = "css/bootstrap.min.css"),
		@ResourceDependency(library = "bootstrap", name = "css/bootstrap-responsive.min.css"),
		@ResourceDependency(library = "bootstrap", name = "js/jquery.min.js"),
		@ResourceDependency(library = "bootstrap", name = "js/bootstrap.min.js"),
		@ResourceDependency(library = "bootstrap", name = "js/bootstrap-collapse.js")
	}
)
//J+
public class AccordionRenderer extends AccordionRendererBase {

	// Private Constants

	// Logger
	private static final Logger logger = LoggerFactory.getLogger(AccordionRenderer.class);

	@Override
	public void encodeBegin(FacesContext facesContext, UIComponent uiComponent) throws IOException {

		// Encode the starting <div> element that represents the accordion.
		ResponseWriter responseWriter = facesContext.getResponseWriter();
		responseWriter.startElement("div", uiComponent);
		responseWriter.writeAttribute("id", uiComponent.getClientId(facesContext), "id");
		RendererUtil.encodeStyleable(responseWriter, (Styleable) uiComponent, "accordion");
	}

	@Override
	public void encodeChildren(FacesContext facesContext, UIComponent uiComponent) throws IOException {

		// If iteration should take place over a data-model, then
		Accordion accordion = (Accordion) uiComponent;
		String accordionClientId = accordion.getClientId();
		Integer selectedIndex = accordion.getSelectedIndex();
		Object value = accordion.getValue();
		String var = accordion.getVar();
		boolean iterateOverDataModel = ((value != null) && (var != null));
		ResponseWriter responseWriter = facesContext.getResponseWriter();

		if (iterateOverDataModel) {

			// Get the first child tab and use it as a prototype tab.
			Tab prototypeChildTab = TabUtil.getFirstChildTab(accordion);

			if (prototypeChildTab != null) {

				// Encode a header <div> and content <div> for each row in the data-model.
				int rowCount = accordion.getRowCount();

				for (int i = 0; i < rowCount; i++) {
					accordion.setRowIndex(i);
					String accordionIteratedClientId = accordion.getClientId();

					boolean selected = ((selectedIndex != null) && (i == selectedIndex));
					responseWriter.startElement("div", null);
					responseWriter.writeAttribute("class", "accordion-group", null);
					encodeHeader(facesContext, responseWriter, accordionClientId, accordionIteratedClientId, prototypeChildTab);
					encodeContent(facesContext, responseWriter, accordionIteratedClientId, prototypeChildTab, selected);
					responseWriter.endElement("div");
				}

				accordion.setRowIndex(-1);
			}
			else {
				logger.warn("Unable to iterate because alloy:accordion does not have an alloy:tab child element.");
			}
		}

		// Otherwise, encode a header <div> and content <div> for each child tab of the specified accordion.
		else {
			List<UIComponent> children = uiComponent.getChildren();
			int childCount = children.size();

			for (int i = 0; i < childCount; i++) {

				UIComponent child = children.get(i);

				if ((child instanceof Tab) && child.isRendered()) {
					Tab childTab = (Tab) child;
					String accordionIteratedClientId = accordion.getClientId().concat("_").concat(Integer.toString(i));
					boolean selected = ((selectedIndex != null) && (i == selectedIndex));
					encodeHeader(facesContext, responseWriter, accordionClientId, accordionIteratedClientId, childTab);
					encodeContent(facesContext, responseWriter, accordionIteratedClientId, childTab, selected);
				}
				else {
					logger.warn("Unable to render child element of alloy:accordion since it is not alloy:tab");
				}
			}
		}

		accordion.setRowIndex(-1);
	}

	@Override
	public void encodeEnd(FacesContext facesContext, UIComponent uiComponent) throws IOException {

		// Encode the closing </div> element for the accordion.
		ResponseWriter responseWriter = facesContext.getResponseWriter();
		responseWriter.endElement("div");

		String escapedClientId = ShowcaseUtil.singleEscapeClientId(uiComponent.getClientId());
		String scriptSource = "$('#".concat(escapedClientId.concat("').collapse();"));
		ScriptFactory scriptFactory = (ScriptFactory) FactoryExtensionFinder.getFactory(ScriptFactory.class);
		Script script = scriptFactory.getScript(scriptSource);
		FacesRequestContext facesRequestContext = FacesRequestContext.getCurrentInstance();
		facesRequestContext.addScript(script);
	}

	protected void encodeContent(FacesContext facesContext, ResponseWriter responseWriter, String accordionIteratedClientId, Tab tab, boolean selected) throws IOException {

		// Encode the starting <div> element that represents the specified tab's content.
		responseWriter.startElement("div", tab);
		responseWriter.writeAttribute("id", accordionIteratedClientId, null);

		// Encode the div's class attribute according to the specified tab's collapsed/expanded state.
		String contentClass = "accordion-body";

		if (selected) {
			contentClass = contentClass.concat(" in collapse");
		}
		else {
			contentClass = contentClass.concat(" collapse");
		}

		// If the specified tab has a contentClass, then append it to the class attribute before encoding.
		String tabContentClass = tab.getContentClass();

		if (tabContentClass != null) {
			contentClass = contentClass.concat(" ").concat(tabContentClass);
		}

		responseWriter.writeAttribute("class", contentClass, Styleable.STYLE_CLASS);

		responseWriter.startElement("div", null);
		responseWriter.writeAttribute("class", "accordion-inner", null);

		// Encode the children of the specified tab as the actual content.
		tab.encodeAll(facesContext);

		responseWriter.endElement("div");

		// Encode the closing </div> element for the specified tab.
		responseWriter.endElement("div");
	}

	protected void encodeHeader(FacesContext facesContext, ResponseWriter responseWriter, String accordionClientId, String accordionIteratedClientId, Tab tab) throws IOException {

		// Encode the starting <div> element that represents the specified tab's header.
		responseWriter.startElement("div", tab);

		// Encode the div's class attribute according to the specified tab's collapsed/expanded state.
		String headerClass = "accordion-heading";

		// If the specified tab has a headerClass, then append it to the class attribute before encoding.
		String tabHeaderClass = tab.getHeaderClass();

		if (tabHeaderClass != null) {
			headerClass += " " + tabHeaderClass;
		}

		responseWriter.writeAttribute("class", headerClass, Styleable.STYLE_CLASS);

		responseWriter.startElement("a", null);
		responseWriter.writeAttribute("class", "accordion-toggle collapsed", null);
		String escapedAccordionClientId = "#".concat(ShowcaseUtil.singleEscapeClientId(accordionClientId));
		responseWriter.writeAttribute("data-parent", escapedAccordionClientId, null);
		responseWriter.writeAttribute("data-toggle", "collapse", null);
		String escapedTabClientId = "#".concat(ShowcaseUtil.singleEscapeClientId(accordionIteratedClientId));
		responseWriter.writeAttribute("href", escapedTabClientId, null);

		// If the header facet exists for the specified tab, then encode the header facet.
		UIComponent headerFacet = tab.getFacet("header");

		if (headerFacet != null) {
			headerFacet.encodeAll(facesContext);
		}

		// Otherwise, render the label of the specified tab.
		else {
			String headerText = tab.getHeaderText();

			if (headerText != null) {
				responseWriter.write(headerText);
			}
		}

		responseWriter.endElement("a");

		// Encode the closing </div> element for the specified tab.
		responseWriter.endElement("div");
	}

	@Override
	public boolean getRendersChildren() {
		return true;
	}
}

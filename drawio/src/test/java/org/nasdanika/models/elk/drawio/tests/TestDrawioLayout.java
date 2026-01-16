package org.nasdanika.models.elk.drawio.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.elk.core.RecursiveGraphLayoutEngine;
import org.eclipse.elk.core.options.CoreOptions;
import org.eclipse.elk.core.util.BasicProgressMonitor;
import org.eclipse.elk.graph.ElkGraphElement;
import org.eclipse.elk.graph.ElkNode;
import org.junit.jupiter.api.Test;
import org.nasdanika.common.PrintStreamProgressMonitor;
import org.nasdanika.common.ProgressMonitor;
import org.nasdanika.common.Transformer;
import org.nasdanika.drawio.Document;
import org.nasdanika.drawio.Page;
import org.nasdanika.graph.Element;
import org.nasdanika.models.elk.drawio.DrawioElkGraphFactory;

public class TestDrawioLayout {
	
	@Test
	public void testDrawioLayout() throws Exception {
		Document document = Document.load(new File("elk.drawio"));
		DrawioElkGraphFactory factory = new DrawioElkGraphFactory();		
		Transformer<Element,ElkGraphElement> transformer = new Transformer<>(factory);
		ProgressMonitor progressMonitor = new PrintStreamProgressMonitor();
		Map<Element, ElkGraphElement> graphElements = transformer.transform(document.stream().toList(), false, progressMonitor);
		System.out.println(graphElements.size());

		for (Entry<Element, ElkGraphElement> e: graphElements.entrySet()) {
			if (e.getKey() instanceof Page) {
				ElkNode pageGraph = (ElkNode) e.getValue();
		        pageGraph.setProperty(CoreOptions.ALGORITHM, "org.eclipse.elk.box");
		        RecursiveGraphLayoutEngine engine = new RecursiveGraphLayoutEngine();
		        engine.layout(pageGraph, new BasicProgressMonitor());
		        
		        assertTrue(pageGraph.getWidth() > 0);
		        assertTrue(pageGraph.getHeight() > 0);
		        
		        System.out.println(pageGraph.getChildren().size());
		        System.out.println(pageGraph.getWidth() + " : " + pageGraph.getHeight());
			}
		}
	}
				
}

package org.nasdanika.models.elk.drawio.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.elk.alg.layered.options.LayeredOptions;
import org.eclipse.elk.alg.layered.options.LayeringStrategy;
import org.eclipse.elk.alg.layered.options.NodePlacementStrategy;
import org.eclipse.elk.core.RecursiveGraphLayoutEngine;
import org.eclipse.elk.core.data.LayoutMetaDataService;
import org.eclipse.elk.core.data.LayoutOptionData;
import org.eclipse.elk.core.options.CoreOptions;
import org.eclipse.elk.core.util.BasicProgressMonitor;
import org.eclipse.elk.graph.ElkBendPoint;
import org.eclipse.elk.graph.ElkConnectableShape;
import org.eclipse.elk.graph.ElkEdge;
import org.eclipse.elk.graph.ElkEdgeSection;
import org.eclipse.elk.graph.ElkGraphElement;
import org.eclipse.elk.graph.ElkNode;
import org.eclipse.elk.graph.properties.IProperty;
import org.eclipse.elk.graph.util.ElkGraphUtil;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.json.JSONObject;
import org.junit.jupiter.api.Test;
import org.nasdanika.common.PrintStreamProgressMonitor;
import org.nasdanika.common.ProgressMonitor;
import org.nasdanika.common.Transformer;
import org.nasdanika.drawio.Connection;
import org.nasdanika.drawio.Document;
import org.nasdanika.drawio.Node;
import org.nasdanika.drawio.Page;
import org.nasdanika.drawio.PointList;
import org.nasdanika.graph.Element;
import org.nasdanika.models.elk.drawio.DrawioElkGraphFactory;

public class TestDrawioLayout {
	
	public static JSONObject TREE_CONFIG = new JSONObject("""
	        {
	        	  "algorithm": "org.eclipse.elk.mrtree",

	        	  "direction": "UP",

	        	  "spacing.nodeNode": 40,
	        	  "spacing.edgeNode": 20,
	        	  "spacing.edgeEdge": 20,

	        	  "mrtree.layoutStrategy": "BUCHHEIM", 
	        	  "mrtree.nodePlacement.strategy": "SIMPLE",
	        	  "mrtree.edgeRouting": "ORTHOGONAL",

	        	  "hierarchyHandling": "INCLUDE_CHILDREN",

	        	  "edgeLabels.inline": false,
	        	  "edgeLabels.placement": "CENTER"
	        }			
			""");
	
	@Test
	public void testDrawioLayout() throws Exception {
		Document document = Document.load(new File("elk.drawio"));
		DrawioElkGraphFactory factory = new DrawioElkGraphFactory();		
		Transformer<Element,ElkGraphElement> transformer = new Transformer<>(factory);
		ProgressMonitor progressMonitor = new PrintStreamProgressMonitor();
		Map<Element, ElkGraphElement> graphElements = transformer.transform(document.stream().toList(), false, progressMonitor);
		System.out.println(graphElements.size());
		
		// Wiring connections
		Map<ElkEdge, Connection> connectionMap = new HashMap<>();
		
		document
			.stream()
			.filter(Connection.class::isInstance)
			.map(Connection.class::cast)
			.forEach(connection -> {
				ElkConnectableShape elkSource = (ElkConnectableShape) graphElements.get(connection.getSource());
				ElkConnectableShape elkTarget = (ElkConnectableShape) graphElements.get(connection.getTarget());
				ElkEdge elkEdge = ElkGraphUtil.createSimpleEdge(elkSource, elkTarget);
				elkEdge.setIdentifier(connection.getId());
				connectionMap.put(elkEdge, connection);
			});					

		for (Entry<Element, ElkGraphElement> e: graphElements.entrySet()) {
			if (e.getKey() instanceof Page) {
				ElkNode pageGraph = (ElkNode) e.getValue();
				applyLayoutOptions(pageGraph, TREE_CONFIG.toMap());
				
//		        pageGraph.setProperty(CoreOptions.ALGORITHM, "org.eclipse.elk.layered");
//		        pageGraph.setProperty(LayeredOptions.LAYERING_STRATEGY, LayeringStrategy.NETWORK_SIMPLEX);
//		        pageGraph.setProperty(LayeredOptions.NODE_PLACEMENT_STRATEGY, NodePlacementStrategy.BRANDES_KOEPF);
//		        pageGraph.setProperty(LayeredOptions.NODE_PLACEMENT_FAVOR_STRAIGHT_EDGES, true);
		        
		        
//		        {
//		        	  "elk.algorithm": "org.eclipse.elk.layered",
//
//		        	  // Hierarchical layout
//		        	  "elk.hierarchyHandling": "INCLUDE_CHILDREN",
//
//		        	  // Node sizing & spacing
//		        	  "elk.spacing.nodeNode": 30,
//		        	  "elk.spacing.nodeNodeBetweenLayers": 40,
//		        	  "elk.spacing.edgeNode": 15,
//		        	  "elk.spacing.edgeEdge": 15,
//		        	  "elk.layered.spacing.nodeNodeBetweenLayers": 40,
//
//		        	  // Ports
//		        	  "elk.portConstraints": "FIXED_ORDER",
//		        	  "elk.portAlignment.default": "CENTER",
//		        	  "elk.portAlignment.direction": "CENTER",
//
//		        	  // Edge routing
//		        	  "elk.edgeRouting": "ORTHOGONAL",
//		        	  "elk.layered.edgeRouting": "ORTHOGONAL",
//		        	  "elk.layered.mergeEdges": false,
//
//		        	  // Layering strategy
//		        	  "elk.layered.layering.strategy": "NETWORK_SIMPLEX",
//
//		        	  // Node placement
//		        	  "elk.layered.nodePlacement.strategy": "BRANDES_KOEPF",
//		        	  "elk.layered.nodePlacement.favorStraightEdges": true,
//
//		        	  // Edge labeling
//		        	  "elk.edgeLabels.inline": false,
//		        	  "elk.edgeLabels.placement": "CENTER",
//
//		        	  // Crossings
//		        	  "elk.layered.crossingMinimization.strategy": "LAYER_SWEEP",
//		        	  "elk.layered.crossingMinimization.semiInteractive": false
//		        
//		        	}
		        
		        
		        
		        
		        RecursiveGraphLayoutEngine engine = new RecursiveGraphLayoutEngine();
		        engine.layout(pageGraph, new BasicProgressMonitor());
		        
		        assertTrue(pageGraph.getWidth() > 0);
		        assertTrue(pageGraph.getHeight() > 0);
		        
		        System.out.println(pageGraph.getChildren().size());
		        System.out.println(pageGraph.getWidth() + " : " + pageGraph.getHeight());
			}
		}
		
		graphElements
			.entrySet()
			.stream()
			.filter(e -> e.getKey() instanceof Node)
			.forEach(e -> {
				Node node = (Node) e.getKey();
				ElkNode elkNode = (ElkNode) e.getValue();
				
				node.getGeometry().setHeight(elkNode.getHeight());
				node.getGeometry().setWidth(elkNode.getWidth());
				node.getGeometry().setLocation(elkNode.getX(), elkNode.getY());
			});				
		
		for (Entry<ElkEdge, Connection> ce: connectionMap.entrySet()) {
			PointList cPoints = ce.getValue().getPoints();
			for (ElkEdgeSection section: ce.getKey().getSections()) {
				for (ElkBendPoint bp: section.getBendPoints()) {
					cPoints.add(bp.getX(), bp.getY());
				}
			}
		}
		
		String docStr = document.save(false);
		Files.writeString(new File("target/elk.drawio").toPath(), docStr);
		
		// Saving to a file
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet
			.getResourceFactoryRegistry()
			.getExtensionToFactoryMap()
			.put(
				Resource.Factory.Registry.DEFAULT_EXTENSION, 
				new	XMIResourceFactoryImpl());		
		URI resourceURI = URI.createFileURI(new File("target/elk.xmi").getCanonicalPath());
		Resource resource = resourceSet.createResource(resourceURI);
		for (Page page: document.getPages()) {
			resource.getContents().add(graphElements.get(page));
		}
		resource.save(null);		
		resource.save(System.out, null);
	}
	
	public static void applyLayoutOptions(ElkNode graph, Map<String,Object> config) {
	    for (Map.Entry<String, Object> configEntry : config.entrySet()) {
	        String key = "org.eclipse.elk." + configEntry.getKey();
	        LayoutOptionData optionData = LayoutMetaDataService.getInstance().getOptionData(key);
	        if (optionData == null) {
	            System.err.println("Unknown ELK option: " + key);
	            continue;
	        }
	        
	        Object typedValue = optionData.parseValue(String.valueOf(configEntry.getValue()));
	        graph.setProperty((IProperty<Object>) optionData, typedValue);
	    }
	}
	
				
}


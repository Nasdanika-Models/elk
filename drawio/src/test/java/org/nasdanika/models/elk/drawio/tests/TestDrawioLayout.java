package org.nasdanika.models.elk.drawio.tests;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.elk.core.RecursiveGraphLayoutEngine;
import org.eclipse.elk.core.data.LayoutMetaDataService;
import org.eclipse.elk.core.data.LayoutOptionData;
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
import org.nasdanika.drawio.ConnectionPoint;
import org.nasdanika.drawio.Document;
import org.nasdanika.drawio.Geometry;
import org.nasdanika.drawio.Layer;
import org.nasdanika.drawio.Model;
import org.nasdanika.drawio.Node;
import org.nasdanika.drawio.Page;
import org.nasdanika.drawio.PointList;
import org.nasdanika.drawio.Rectangle;
import org.nasdanika.drawio.Root;
import org.nasdanika.drawio.Tag;
import org.nasdanika.drawio.style.ConnectionStyle;
import org.nasdanika.graph.Element;
import org.nasdanika.models.elk.drawio.DrawioElkGraphFactory;
import org.yaml.snakeyaml.Yaml;

public class TestDrawioLayout {
	
//	{
//		  "elk.algorithm": "org.eclipse.elk.layered",
//
//		  "elk.direction": "RIGHT",
//		  "elk.hierarchyHandling": "INCLUDE_CHILDREN",
//
//		  "elk.spacing.nodeNode": 30,
//		  "elk.spacing.nodeNodeBetweenLayers": 40,
//		  "elk.spacing.edgeNode": 15,
//		  "elk.spacing.edgeEdge": 15,
//
//		  "elk.portConstraints": "FIXED_ORDER",
//		  "elk.portAlignment.default": "CENTER",
//
//		  "elk.edgeRouting": "ORTHOGONAL",
//		  "elk.layered.edgeRouting": "ORTHOGONAL",
//		  "elk.layered.mergeEdges": false,
//
//		  "elk.layered.layering.strategy": "NETWORK_SIMPLEX",
//
//		  "elk.layered.nodePlacement.strategy": "BRANDES_KOEPF",
//		  "elk.layered.nodePlacement.favorStraightEdges": true,
//
//		  "elk.layered.crossingMinimization.strategy": "LAYER_SWEEP",
//		  "elk.layered.crossingMinimization.semiInteractive": false,
//
//		  "elk.edgeLabels.inline": false,
//		  "elk.edgeLabels.placement": "CENTER"
//		}	
	
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
	
	public static JSONObject LAYERED_CONFIG = new JSONObject("""
	{
	  "algorithm": "org.eclipse.elk.layered",
	  "direction": "DOWN",

	  "hierarchyHandling": "INCLUDE_CHILDREN",
	  "portConstraints": "FIXED_SIDE",

	  "layered.spacing.nodeNodeBetweenLayers": 50,
	  "spacing.nodeNode": 150,
	  "spacing.edgeNode": 40,
	  "spacing.edgeEdge": 20,

	  "layered.layering.strategy": "NETWORK_SIMPLEX",
	  "layered.nodePlacement.strategy": "BRANDES_KOEPF",
	  "layered.nodePlacement.favorStraightEdges": true,

	  "edgeRouting": "ORTHOGONAL",
	  "edgeLabels.inline": false,
	  "edgeLabels.placement": "CENTER"
	}
	""");
	
	public static Map<String,Object> LAYERED_CONFIG_MAP = new Yaml().load("""
	  algorithm: org.eclipse.elk.layered
	  direction: RIGHT

	  hierarchyHandling: INCLUDE_CHILDREN

	  layered.spacing.nodeNodeBetweenLayers: 150
	  spacing.nodeNode: 150
	  spacing.edgeNode: 40
	  spacing.edgeEdge: 20

	  layered.layering.strategy: NETWORK_SIMPLEX
	  layered.nodePlacement.strategy: BRANDES_KOEPF
	  layered.nodePlacement.favorStraightEdges: true

	  edgeRouting: ORTHOGONAL
	  edgeLabels.inline: false
	  edgeLabels.placement: CENTER
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
				
//	        pageGraph.setProperty(CoreOptions.ALGORITHM, "org.eclipse.elk.layered");
//	        pageGraph.setProperty(LayeredOptions.LAYERING_STRATEGY, LayeringStrategy.NETWORK_SIMPLEX);
//	        pageGraph.setProperty(LayeredOptions.NODE_PLACEMENT_STRATEGY, NodePlacementStrategy.BRANDES_KOEPF);
//	        pageGraph.setProperty(LayeredOptions.NODE_PLACEMENT_FAVOR_STRAIGHT_EDGES, true);
		        
		        
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
	
	@Test
	public void testGenerateAndLayout() throws Exception {
		Document document = Document.create(false, null);
		Page page = document.createPage();
		page.setName("My first new page");
		
		Model model = page.getModel();
		Root root = model.getRoot();
		
		// Background layer
		Layer<?> layer = root.getLayers().getFirst();
				
		// Add nodes
		Node source = layer.createNode();
		source.setLabel("My source node");
		Rectangle sourceGeometry = source.getGeometry();
		sourceGeometry.setWidth(120);
		sourceGeometry.setHeight(30);
		
		Node target = layer.createNode();
		target.setLabel("My target node");
		Geometry targetGeometry = target.getGeometry();
		targetGeometry.setWidth(120);
		targetGeometry.setHeight(30);
		
		// Add connection
				
		Connection connection = layer.createConnection(source, target);
		connection.setLabel("My connection");
		ConnectionStyle connectionStyle = connection.getStyle();
		connectionStyle.put("orthogonalLoop", "1");
		connectionStyle.put("jettySize", "auto");
		connectionStyle.put("html", "1");
		
		connectionStyle
			.edgeStyle("orthogonalEdgeStyle")
			.color("#0077ff");
						
		// Layout
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
			.forEach(conn -> {
				ElkConnectableShape elkSource = (ElkConnectableShape) graphElements.get(conn.getSource());
				ElkConnectableShape elkTarget = (ElkConnectableShape) graphElements.get(conn.getTarget());
				ElkEdge elkEdge = ElkGraphUtil.createSimpleEdge(elkSource, elkTarget);
				elkEdge.setIdentifier(conn.getId());
				connectionMap.put(elkEdge, conn);
			});					

		for (Entry<Element, ElkGraphElement> e: graphElements.entrySet()) {
			if (e.getKey() instanceof Page) {
				ElkNode pageGraph = (ElkNode) e.getValue();
				
				applyLayoutOptions(pageGraph, LAYERED_CONFIG_MAP);
				RecursiveGraphLayoutEngine engine = new RecursiveGraphLayoutEngine();
				engine.layout(pageGraph, new BasicProgressMonitor());
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
		Files.writeString(new File("target/generated.drawio").toPath(), docStr);
		
		// Saving to a file
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet
			.getResourceFactoryRegistry()
			.getExtensionToFactoryMap()
			.put(
				Resource.Factory.Registry.DEFAULT_EXTENSION, 
				new	XMIResourceFactoryImpl());
		URI resourceURI = URI.createFileURI(new File("target/generated.xmi").getCanonicalPath());
		Resource resource = resourceSet.createResource(resourceURI);
		for (Page pg: document.getPages()) {
			resource.getContents().add(graphElements.get(pg));
		}
		resource.save(null);		
		resource.save(System.out, null);
	}
	
	@Test
	public void testGenerateAndLayoutConnectionPoints() throws Exception {
		Document document = Document.create(false, null);
		Page page = document.createPage();
		page.setName("My first new page");
		
		Model model = page.getModel();
		Root root = model.getRoot();
		
		// Add layer
		Layer<?> newLayer = root.createLayer();
		newLayer.setLabel("My new layer");
				
		// Add nodes
		Node source = newLayer.createNode();
		source.setLabel("My source node");
		Rectangle sourceGeometry = source.getGeometry();
//		sourceGeometry.setX(200);
//		sourceGeometry.setX(100);
		sourceGeometry.setWidth(120);
		sourceGeometry.setHeight(30);
		source.getTags().add(page.createTag("aws"));		
		
		Node target = newLayer.createNode();
		target.setLabel("My target node");
		Geometry targetGeometry = target.getGeometry();
		targetGeometry.setWidth(120);
		targetGeometry.setHeight(30);
		Set<Tag> targetTags = target.getTags();
		targetTags.add(page.createTag("aws"));
		targetTags.add(page.createTag("azure"));
		
		// Add connection
		
		ConnectionPoint sourceConnectionPoint = source.createConnectionPoint(0.5, 1);				
		ConnectionPoint targetConnectionPoint = target.createConnectionPoint();
		
		Connection connection = newLayer.createConnection(
				sourceConnectionPoint, 
				targetConnectionPoint);
		connection.setLabel("My connection");
		connection.setOffset(100, -15);
		ConnectionStyle connectionStyle = connection.getStyle();
		connectionStyle.put("orthogonalLoop", "1");
		connectionStyle.put("jettySize", "auto");
		connectionStyle.put("html", "1");
		
		connectionStyle
			.edgeStyle("orthogonalEdgeStyle")
			.rounded(true)
			.dashed("1")
			.width("2")
			.color("#0077ff");
		
		targetConnectionPoint.setLocation(0, 0.33);		
		
		Node connectionLabel = connection.createNode();
		connectionLabel.setLabel("Connection label");
		Geometry clg = connectionLabel.getGeometry();
		clg.setX(0.9);
		clg.setRelative(true);
		
		org.nasdanika.drawio.Point clgp = clg.getPoints().add();
		clgp.setRole("offset");
		clgp.setY(20);		
				
		// Layout
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
			.forEach(conn -> {
				ElkConnectableShape elkSource = (ElkConnectableShape) graphElements.get(conn.getSource());
				ElkConnectableShape elkTarget = (ElkConnectableShape) graphElements.get(conn.getTarget());
				ElkEdge elkEdge = ElkGraphUtil.createSimpleEdge(elkSource, elkTarget);
				elkEdge.setIdentifier(conn.getId());
				connectionMap.put(elkEdge, conn);
			});					

		for (Entry<Element, ElkGraphElement> e: graphElements.entrySet()) {
			if (e.getKey() instanceof Page) {
				ElkNode pageGraph = (ElkNode) e.getValue();
				
				// Apply two-node tuned layout options instead of setting properties individually
				applyLayoutOptions(pageGraph, LAYERED_CONFIG.toMap());
				
				RecursiveGraphLayoutEngine engine = new RecursiveGraphLayoutEngine();
				engine.layout(pageGraph, new BasicProgressMonitor());
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
		Files.writeString(new File("target/generated-connection-points.drawio").toPath(), docStr);
		
		// Saving to a file
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet
			.getResourceFactoryRegistry()
			.getExtensionToFactoryMap()
			.put(
				Resource.Factory.Registry.DEFAULT_EXTENSION, 
				new	XMIResourceFactoryImpl());
		URI resourceURI = URI.createFileURI(new File("target/generated-connection-points.xmi").getCanonicalPath());
		Resource resource = resourceSet.createResource(resourceURI);
		for (Page pg: document.getPages()) {
			resource.getContents().add(graphElements.get(pg));
		}
		resource.save(null);		
		resource.save(System.out, null);
	}
	
}

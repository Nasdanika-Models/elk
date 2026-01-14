package org.nasdanika.models.elk.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.elk.core.RecursiveGraphLayoutEngine;
import org.eclipse.elk.core.UnsupportedConfigurationException;
import org.eclipse.elk.core.data.LayoutAlgorithmData;
import org.eclipse.elk.core.data.LayoutMetaDataService;
import org.eclipse.elk.core.options.CoreOptions;
import org.eclipse.elk.core.util.BasicProgressMonitor;
import org.eclipse.elk.graph.ElkNode;
import org.eclipse.elk.graph.util.ElkGraphUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Tests for {@link RecursiveGraphLayoutEngine}.
 */
public class RecursiveGraphLayoutEngineTest {
    
    @BeforeAll
    public static void initPlainJavaLayout() {
        PlainJavaInitialization.initializePlainJavaLayout();
    }
    
    @Test
    public void testUnresolvedGraph() {
        Graph graph = new Graph();
        graph.root.setProperty(CoreOptions.ALGORITHM, "org.eclipse.elk.box");
        RecursiveGraphLayoutEngine engine = new RecursiveGraphLayoutEngine();
        engine.layout(graph.root, new BasicProgressMonitor());
        
        assertTrue(graph.root.getWidth() > 0);
        assertTrue(graph.root.getHeight() > 0);
    }
    
    @Test
    public void testResolvedGraph() {
        Graph graph = new Graph();
        LayoutAlgorithmData algorithmData = LayoutMetaDataService.getInstance().getAlgorithmData("org.eclipse.elk.box");
        graph.root.setProperty(CoreOptions.RESOLVED_ALGORITHM, algorithmData);
        RecursiveGraphLayoutEngine engine = new RecursiveGraphLayoutEngine();
        engine.layout(graph.root, new BasicProgressMonitor());
        
        assertTrue(graph.root.getWidth() > 0);
        assertTrue(graph.root.getHeight() > 0);
    }
    
    @Test
    public void testUnknownAlgorithmId() {
    	Assertions.assertThrows(UnsupportedConfigurationException.class, () -> {
	        Graph graph = new Graph();
	        graph.root.setProperty(CoreOptions.ALGORITHM, "foo.Bar");
	        RecursiveGraphLayoutEngine engine = new RecursiveGraphLayoutEngine();
	        engine.layout(graph.root, new BasicProgressMonitor());
    	});
    }
    
    @Test
    public void testEmptyAlgorithmId() {
        Graph graph = new Graph();
        RecursiveGraphLayoutEngine engine = new RecursiveGraphLayoutEngine();
        engine.layout(graph.root, new BasicProgressMonitor());
        
        assertEquals("org.eclipse.elk.layered", graph.root.getProperty(CoreOptions.RESOLVED_ALGORITHM).getId());
    }
    
    private class Graph {
        ElkNode root;
        private ElkNode n1;
        private ElkNode n2;
        public Graph() {
            root = ElkGraphUtil.createGraph();
            n1 = ElkGraphUtil.createNode(root);
            n1.setDimensions(10, 10);
            n2 = ElkGraphUtil.createNode(root);
            n2.setDimensions(10, 10);
        }
    }
    
}

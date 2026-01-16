package org.nasdanika.models.elk.drawio;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.eclipse.elk.graph.ElkGraphElement;
import org.eclipse.elk.graph.ElkNode;
import org.eclipse.elk.graph.util.ElkGraphUtil;
import org.eclipse.emf.ecore.EObject;
import org.nasdanika.common.ProgressMonitor;
import org.nasdanika.drawio.Document;
import org.nasdanika.drawio.Geometry;

public class DrawioElkGraphFactory {
	
	/**
	 * Creates a document element from {@link Document}
	 * @param document
	 * @param parallel
	 * @param elementProvider
	 * @param registry
	 * @param progressMonitor
	 * @return
	 */
	@org.nasdanika.common.Transformer.Factory
	public ElkNode createElkNodeElement(
			org.nasdanika.drawio.Page page,
			boolean parallel,
			BiConsumer<org.nasdanika.drawio.Element<?>, BiConsumer<ElkGraphElement,ProgressMonitor>> elementProvider, 
			Consumer<BiConsumer<Map<EObject, EObject>,ProgressMonitor>> registry,
			ProgressMonitor progressMonitor) {
				
		ElkNode graph = ElkGraphUtil.createGraph();
		
		page
			.getModel()
			.getRoot()
			.getLayers()
			.stream()
			.flatMap(l -> l.getChildren().stream())
			.filter(org.nasdanika.drawio.Node.class::isInstance)
			.forEach(n -> {
				elementProvider.accept(n, (childElkNode, pm) -> {
					graph.getChildren().add((ElkNode) childElkNode);
				});
			});		
		
		return graph;
	}
		
	/**
	 * Creates a document element from {@link Document}
	 * @param document
	 * @param parallel
	 * @param elementProvider
	 * @param registry
	 * @param progressMonitor
	 * @return
	 */
	@org.nasdanika.common.Transformer.Factory
	public ElkNode createElkNodeElement(
			org.nasdanika.drawio.Node node,
			boolean parallel,
			BiConsumer<org.nasdanika.drawio.Element<?>, BiConsumer<ElkGraphElement,ProgressMonitor>> elementProvider, 
			Consumer<BiConsumer<Map<EObject, EObject>,ProgressMonitor>> registry,
			ProgressMonitor progressMonitor) {
				
		ElkNode elkNode = ElkGraphUtil.createGraph();
		Geometry geometry = node.getGeometry();
		elkNode.setDimensions(geometry.getWidth(), geometry.getHeight());
		elkNode.setLocation(geometry.getX(), geometry.getY());
		
		return elkNode;
	}
	
}

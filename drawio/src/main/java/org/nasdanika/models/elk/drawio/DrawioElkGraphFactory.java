package org.nasdanika.models.elk.drawio;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.eclipse.elk.graph.ElkGraphElement;
import org.eclipse.elk.graph.ElkNode;
import org.eclipse.elk.graph.ElkPort;
import org.eclipse.elk.graph.util.ElkGraphUtil;
import org.eclipse.emf.ecore.EObject;
import org.nasdanika.common.ProgressMonitor;
import org.nasdanika.drawio.Connection;
import org.nasdanika.drawio.ConnectionPoint;
import org.nasdanika.drawio.Document;
import org.nasdanika.drawio.Geometry;
import org.nasdanika.drawio.LayerElement;
import org.eclipse.elk.core.options.CoreOptions;
import org.eclipse.elk.core.options.PortSide;

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
	public ElkNode createElkNode(
			org.nasdanika.drawio.Page page,
			boolean parallel,
			BiConsumer<org.nasdanika.drawio.Element<?>, BiConsumer<ElkGraphElement,ProgressMonitor>> elementProvider, 
			Consumer<BiConsumer<Map<EObject, EObject>,ProgressMonitor>> registry,
			ProgressMonitor progressMonitor) {
				
		ElkNode graph = ElkGraphUtil.createGraph();
		graph.setIdentifier(page.getId());
		
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
		
	@org.nasdanika.common.Transformer.Factory
	public ElkNode createElkNode(
			org.nasdanika.drawio.Node node,
			boolean parallel,
			BiConsumer<org.nasdanika.drawio.Element<?>, BiConsumer<ElkGraphElement,ProgressMonitor>> elementProvider, 
			Consumer<BiConsumer<Map<EObject, EObject>,ProgressMonitor>> registry,
			ProgressMonitor progressMonitor) {
				
		ElkNode elkNode = ElkGraphUtil.createGraph();
		Geometry geometry = node.getGeometry();
		elkNode.setDimensions(geometry.getWidth(), geometry.getHeight());
		elkNode.setLocation(geometry.getX(), geometry.getY());
		elkNode.setIdentifier(node.getId());
		
		for (LayerElement<?> child: node.getChildren()) {
			if (child instanceof org.nasdanika.drawio.Node) {
				elementProvider.accept(child, (childElkNode, pm) -> elkNode.getChildren().add((ElkNode) childElkNode));
			}
		}
		
		for (ConnectionPoint connectionPoint: node.getConnectionPoints()) {
			ElkPort elkPort = ElkGraphUtil.createPort(elkNode);
			elkPort.setIdentifier("%f,%f,%f,%f,%b".formatted(
					connectionPoint.getX(), 
					connectionPoint.getY(),
					connectionPoint.getDx(), 
					connectionPoint.getDy(),
					connectionPoint.isPerimeter()));
			
			elkPort.setDimensions(getPortSize(), getPortSize());
			double portX = elkNode.getWidth() * connectionPoint.getX() + connectionPoint.getDx() - getPortSize() / 2;
			double portY = elkNode.getHeight() * connectionPoint.getY() + connectionPoint.getDy() - getPortSize() / 2;
			elkPort.setLocation(portX, portY);
			PortSide portSide = PortSide.NORTH;
			if (connectionPoint.getY() == 0.0) {
				portSide = PortSide.NORTH;
			} else if (connectionPoint.getY() == 1.0) {
				portSide = PortSide.SOUTH;
			} else if (connectionPoint.getX() == 0.0) {
				portSide = PortSide.WEST;
			} else if (connectionPoint.getX() == 1.0) {
				portSide = PortSide.EAST;
			}
			elkPort.setProperty(CoreOptions.PORT_SIDE, portSide);
			
		}
		
		return elkNode;
	}

	protected double getPortSize() {
		return 3;
	}
	
	
//	@org.nasdanika.common.Transformer.Factory
//	public ElkEdge createElkEdge(
//			org.nasdanika.drawio.Connection connection,
//			boolean parallel,
//			BiConsumer<org.nasdanika.drawio.Element<?>, BiConsumer<ElkGraphElement,ProgressMonitor>> elementProvider, 
//			Consumer<BiConsumer<Map<EObject, EObject>,ProgressMonitor>> registry,
//			ProgressMonitor progressMonitor) {
//				
//		ElkEdge elkEdge = ElkGraphUtil.createEdge(null);
//		
//		return elkEdge;
//	}
		
//	@org.nasdanika.common.Transformer.Factory
//	public ElkPort createElkPort(
//			org.nasdanika.drawio.ConnectionPoint connectionPoint,
//			boolean parallel,
//			BiConsumer<org.nasdanika.drawio.Element<?>, BiConsumer<ElkGraphElement,ProgressMonitor>> elementProvider, 
//			Consumer<BiConsumer<Map<EObject, EObject>,ProgressMonitor>> registry,
//			ProgressMonitor progressMonitor) {
//				
//		ElkPort elkPort = ElkGraphUtil.createPort(null);
//		
//		return elkPort;
//	}		
	
}

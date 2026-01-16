module org.nasdanika.models.elk.drawio {
		
	requires transitive org.nasdanika.drawio;
	requires transitive org.eclipse.elk.graph;
	
	exports org.nasdanika.models.elk.drawio;
	opens org.nasdanika.models.elk.drawio to org.nasdanika.core; // For transformer reflection

}

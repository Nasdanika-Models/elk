import org.nasdanika.capability.CapabilityFactory;
import org.nasdanika.models.elk.ecore.ECoreGenElkProcessorsCapabilityFactory;

module org.nasdanika.models.elk.ecore {
		
	requires transitive org.nasdanika.models.ecore.graph;
	requires org.eclipse.elk.graph;
	
	exports org.nasdanika.models.elk.ecore;
	opens org.nasdanika.models.elk.ecore; // For loading resources

	provides CapabilityFactory with	ECoreGenElkProcessorsCapabilityFactory; 		
	
}

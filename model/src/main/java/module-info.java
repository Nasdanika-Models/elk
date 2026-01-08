import org.nasdanika.capability.CapabilityFactory;
import org.nasdanika.models.compare.CompareEPackageResourceSetCapabilityFactory;

module org.nasdanika.models.compare {
	
	exports org.nasdanika.models.compare;
	
	exports org.eclipse.emf.compare;
	exports org.eclipse.emf.compare.conflict;
	exports org.eclipse.emf.compare.diff;
	exports org.eclipse.emf.compare.equi;
	exports org.eclipse.emf.compare.graph;
	exports org.eclipse.emf.compare.internal;
	exports org.eclipse.emf.compare.internal.conflict;
	exports org.eclipse.emf.compare.internal.dmp;
	exports org.eclipse.emf.compare.internal.merge;
	exports org.eclipse.emf.compare.internal.postprocessor.factories;
	exports org.eclipse.emf.compare.internal.spec;
	exports org.eclipse.emf.compare.internal.utils;
	exports org.eclipse.emf.compare.match;
	exports org.eclipse.emf.compare.match.eobject;
	exports org.eclipse.emf.compare.match.eobject.internal;
	exports org.eclipse.emf.compare.match.impl;
	exports org.eclipse.emf.compare.match.resource;
	exports org.eclipse.emf.compare.merge;
	exports org.eclipse.emf.compare.postprocessor;
	exports org.eclipse.emf.compare.req;
	exports org.eclipse.emf.compare.scope;
	exports org.eclipse.emf.compare.utils;
	exports org.eclipse.emf.compare.impl;
	exports org.eclipse.emf.compare.util;
	
	requires org.nasdanika.capability;
	requires com.google.common;
	requires transitive org.eclipse.emf.common;
	requires transitive org.eclipse.emf.ecore;
	requires transitive org.eclipse.emf.ecore.xmi;
//	requires org.nasdanika.models.maven;
//	requires org.nasdanika.emf;	
	
	provides CapabilityFactory with CompareEPackageResourceSetCapabilityFactory;
	
}
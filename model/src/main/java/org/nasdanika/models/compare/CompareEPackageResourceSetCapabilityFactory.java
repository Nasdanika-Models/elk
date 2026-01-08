package org.nasdanika.models.compare;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.ComparePackage;
import org.eclipse.emf.ecore.EPackage;
import org.nasdanika.capability.emf.EPackageCapabilityFactory;

public class CompareEPackageResourceSetCapabilityFactory extends EPackageCapabilityFactory {

	@Override
	protected EPackage getEPackage() {
		return ComparePackage.eINSTANCE;
	}

	@Override
	protected URI getDocumentationURI() {
		return URI.createURI("https://crew-ai.models.nasdanika.org/");
	}

}

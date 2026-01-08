package org.nasdanika.models.compare.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.compare.AttributeChange;
import org.eclipse.emf.compare.CompareFactory;
import org.eclipse.emf.compare.ComparePackage;
import org.eclipse.emf.compare.Comparison;
import org.eclipse.emf.compare.EMFCompare;
import org.eclipse.emf.compare.scope.DefaultComparisonScope;
import org.eclipse.emf.compare.scope.IComparisonScope;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.junit.jupiter.api.Test;
import org.nasdanika.capability.CapabilityLoader;
import org.nasdanika.capability.ServiceCapabilityFactory;
import org.nasdanika.capability.ServiceCapabilityFactory.Requirement;
import org.nasdanika.capability.emf.ResourceSetRequirement;
import org.nasdanika.common.PrintStreamProgressMonitor;
import org.nasdanika.common.ProgressMonitor;
//import org.nasdanika.drawio.Document;
//import org.nasdanika.emf.GitURIHandler;
//import org.nasdanika.models.maven.MavenFactory;

public class CompareTests {

	@Test
	public void testCompare() throws Exception {
		AttributeChange attrChange = CompareFactory.eINSTANCE.createAttributeChange();
		attrChange.setAttribute(ComparePackage.Literals.ATTRIBUTE_CHANGE__VALUE);
		
		AttributeChange oAttrChange = CompareFactory.eINSTANCE.createAttributeChange();
		oAttrChange.setAttribute(ComparePackage.Literals.COMPARISON__THREE_WAY);
		
		IComparisonScope scope = new DefaultComparisonScope(attrChange, oAttrChange, null);
		Comparison comparison = EMFCompare.builder().build().compare(scope);
		comparison.getDifferences().forEach(System.out::println);		
	}
	
//	@Test
//	public void testPomGitCompare() throws Exception {
//		org.nasdanika.models.maven.Model model = MavenFactory.eINSTANCE.createModel();
//		File pomFile = new File("..\\..\\excel\\pom.xml").getCanonicalFile();
//		try (InputStream in = new FileInputStream(pomFile)) {
//			model.load(in);
//		}
//		System.out.println(model.getName());
//		System.out.println(model.getGroupId());
//		System.out.println(model.getArtifactId());
//		System.out.println(model.getVersion());
//		
//		GitURIHandler gitURIHander = new GitURIHandler(pomFile);
//		
//		URI pomURI = URI.createURI("git://maven-2025.5.0/pom.xml");
//		org.nasdanika.models.maven.Model gitModel = MavenFactory.eINSTANCE.createModel();
//		try (InputStream in = gitURIHander.createInputStream(pomURI, null)) {
//			gitModel.load(in);
//		}
//		System.out.println(gitModel.getName());
//		System.out.println(gitModel.getGroupId());
//		System.out.println(gitModel.getArtifactId());
//		System.out.println(gitModel.getVersion());
//
//		IComparisonScope scope = new DefaultComparisonScope(model, gitModel, null);
//		Comparison comparison = EMFCompare.builder().build().compare(scope);
//		comparison.getDifferences().forEach(System.out::println);	
//		
//		CapabilityLoader capabilityLoader = new CapabilityLoader();
//		ProgressMonitor progressMonitor = new PrintStreamProgressMonitor();
//		Requirement<ResourceSetRequirement, ResourceSet> requirement = ServiceCapabilityFactory.createRequirement(ResourceSet.class);		
//		ResourceSet resourceSet = capabilityLoader.loadOne(requirement, progressMonitor);
//		
//		Resource resource = resourceSet.createResource(URI.createFileURI("target/compare.xml"));
//		resource.getContents().add(comparison);
//		resource.getContents().add(model);
//		resource.getContents().add(gitModel);
//		resource.save(null);		
//	}
//	
//	@Test
//	public void testDiagramGitCompare() throws Exception {		
//		GitURIHandler gitURIHander = new GitURIHandler();
//		URI diagramURI = URI.createURI("git://5bfe5731bbdf10b742a3db53ca5e4dad0844732b/model/src/test/resources/org/nasdanika/models/compare/tests/test.drawio");
//		URL resourceURL = getClass().getResource("test.drawio");
//		try (InputStream in = resourceURL.openStream(); InputStream gitIn = gitURIHander.createInputStream(diagramURI, null)) {
//			Document gitDocument = Document.load(gitIn, diagramURI);
//			Document document = Document.load(in, URI.createURI(resourceURL.toString()));
//
//			org.nasdanika.drawio.model.Document modelDocument = document.toModelDocument();
//			org.nasdanika.drawio.model.Document gitModelDocument = gitDocument.toModelDocument();
//			IComparisonScope scope = new DefaultComparisonScope(modelDocument, gitModelDocument, null);
//			Comparison comparison = EMFCompare.builder().build().compare(scope);
//			comparison.getDifferences().forEach(System.out::println);	
//			
//			CapabilityLoader capabilityLoader = new CapabilityLoader();
//			ProgressMonitor progressMonitor = new PrintStreamProgressMonitor();
//			Requirement<ResourceSetRequirement, ResourceSet> requirement = ServiceCapabilityFactory.createRequirement(ResourceSet.class);		
//			ResourceSet resourceSet = capabilityLoader.loadOne(requirement, progressMonitor);
//			
//			Resource resource = resourceSet.createResource(URI.createFileURI("target/diagram-compare.xml"));
//			resource.getContents().add(comparison);
//			resource.getContents().add(modelDocument);
//			resource.getContents().add(gitModelDocument);
//			resource.save(null);
//		}		
//	}
	
}

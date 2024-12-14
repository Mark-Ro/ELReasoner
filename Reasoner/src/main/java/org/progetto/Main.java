package org.progetto;

import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.model.parameters.Imports;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.io.File;
import java.util.Set;

public class Main {
    private static final String baseIRI = "http://www.co-ode.org/marcoromano/ontologies/pizza#";
    private static final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    private static final OWLDataFactory dataFactory = manager.getOWLDataFactory();

    public static void main(String[] args) {
        // Creazione di un manager per gestire le ontologie
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLOntology ontology;
        String resultCR1, resultCR2, resultCR3, resultCR4, resultCR5, resultCR6, resultQuery1, resultQuery2, resultQuery3, resultQuery4, resultQuery5, resultQuery6, resultQuery7, resultQuery8, resultQuery9;
        boolean result, resultelk;
        try {
            normalizationTest();
            File file = new File("data\\PizzaOntology.owx");
            ontology = manager.loadOntologyFromOntologyDocument(file);
            resultCR1 = testCR1(ontology);
            resultCR2 = testCR2(ontology);
            resultCR3 = testCR3(ontology);
            resultCR4 = testCR4(ontology);
            resultCR5 = testCR5(ontology);
            resultCR6 = testCR6(ontology);
            resultQuery1 = query1(ontology);
            resultQuery2 = query2(ontology);
            resultQuery3 = query3(ontology);
            resultQuery4 = query4(ontology);
            resultQuery5 = query5(ontology);
            resultQuery6 = query6(ontology);
            resultQuery7 = query7(ontology);
            resultQuery8 = query8(ontology);
            resultQuery9 = query9(ontology);
            System.out.println("CR1: " + resultCR1);
            System.out.println("CR2: " + resultCR2);
            System.out.println("CR3: " + resultCR3);
            System.out.println("CR4: " + resultCR4);
            System.out.println("CR5: " + resultCR5);
            System.out.println("CR6: " + resultCR6);
            System.out.println("Query1: " + resultQuery1);
            System.out.println("Query2: " + resultQuery2);
            System.out.println("Query3: " + resultQuery3);
            System.out.println("Query4: " + resultQuery4);
            System.out.println("Query5: " + resultQuery5);
            System.out.println("Query6: " + resultQuery6);
            System.out.println("Query7: " + resultQuery7);
            System.out.println("Query8: " + resultQuery8);
            System.out.println("Query9: " + resultQuery9);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void normalizationTest() {
        File file = new File("data\\Normalization Test 1.owx");
        //File file = new File("data\\Normalization Test 2.owx");
        OWLOntology ontology = null;
        try {
            ontology = manager.loadOntologyFromOntologyDocument(file);
        } catch (OWLOntologyCreationException e) {
            throw new RuntimeException(e);
        }
        Normalizer normalizer = new Normalizer();
        Set<OWLSubClassOfAxiom> result = normalizer.normalizeAxioms(ontology.getTBoxAxioms(Imports.EXCLUDED));
        System.out.println("\n\nNormalizzazione: ");
        printAxioms(result);
    }

    public static boolean queryReasoner(OWLOntology ontology, OWLSubClassOfAxiom queryAxiom) {
        Reasoner reasoner = new Reasoner(ontology);
        return reasoner.isSubsumptionSatisfied(queryAxiom);
    }

    public static boolean queryReasonerELK(OWLOntology ontology, OWLSubClassOfAxiom queryAxiom){
        OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
        OWLDataFactory dataFactory = manager.getOWLDataFactory();
        OWLReasonerFactory owlReasonerFactory = new ElkReasonerFactory();
        OWLReasoner reasoner = owlReasonerFactory.createReasoner(ontology);
        reasoner.precomputeInferences();
        OWLObjectComplementOf complementOf = dataFactory.getOWLObjectComplementOf(queryAxiom.getSuperClass());
        OWLObjectIntersectionOf negIntersection = dataFactory.getOWLObjectIntersectionOf(queryAxiom.getSubClass(),complementOf);
        return !reasoner.isSatisfiable(negIntersection);
    }

    //VegetableTopping ⊑ Food
    public static String testCR1(OWLOntology ontology) {

        boolean result, resultelk;
        OWLClass vegetableTopping = dataFactory.getOWLClass(IRI.create(baseIRI + "VegetableTopping"));
        OWLClass food = dataFactory.getOWLClass(IRI.create(baseIRI + "Food"));
        OWLSubClassOfAxiom axiom1 = dataFactory.getOWLSubClassOfAxiom(vegetableTopping, food);
        result = queryReasoner(ontology,axiom1);
        resultelk = queryReasonerELK(ontology,axiom1);
        return result + "-" + resultelk;
    }

    //VeganSauce ⊑ SauceVegetableTopping
    public static String testCR2(OWLOntology ontology) {

        boolean result, resultelk;
        OWLClass siciliana = dataFactory.getOWLClass(IRI.create(baseIRI + "VeganSauce"));
        OWLClass pizza = dataFactory.getOWLClass(IRI.create(baseIRI + "SauceVegetableTopping"));
        OWLSubClassOfAxiom axiom1 = dataFactory.getOWLSubClassOfAxiom(siciliana, pizza);
        result = queryReasoner(ontology,axiom1);
        resultelk = queryReasonerELK(ontology,axiom1);
        return result + "-" + resultelk;
    }

    //SpicyPizza ⊑ ∃hasTopping.SpicyTopping
    public static String testCR3(OWLOntology ontology) {

        boolean result, resultelk;
        OWLObjectProperty hasTopping = dataFactory.getOWLObjectProperty(IRI.create(baseIRI + "hasTopping"));
        OWLClass spicyPizza = dataFactory.getOWLClass(IRI.create(baseIRI + "SpicyPizza"));
        OWLClass spicyTopping = dataFactory.getOWLClass(IRI.create(baseIRI + "SpicyTopping"));
        OWLSubClassOfAxiom axiom1 = dataFactory.getOWLSubClassOfAxiom(spicyPizza, dataFactory.getOWLObjectSomeValuesFrom(hasTopping, spicyTopping));
        result = queryReasoner(ontology,axiom1);
        resultelk = queryReasonerELK(ontology,axiom1);
        return result + "-" + resultelk;
    }

    //AmericanHot ⊑ PepperPizza
    public static String testCR4(OWLOntology ontology) {

        boolean result, resultelk;
        OWLClass siciliana = dataFactory.getOWLClass(IRI.create(baseIRI + "AmericanHot"));
        OWLClass pizza = dataFactory.getOWLClass(IRI.create(baseIRI + "PepperPizza"));
        OWLSubClassOfAxiom axiom1 = dataFactory.getOWLSubClassOfAxiom(siciliana, pizza);
        result = queryReasoner(ontology,axiom1);
        resultelk = queryReasonerELK(ontology,axiom1);
        return result + "-" + resultelk;
    }

    //InvalidVegetableTopping ⊑ ⊥
    public static String testCR5(OWLOntology ontology) {
        boolean result, resultelk;
        OWLClass cheeseyVegetableTopping = dataFactory.getOWLClass(IRI.create(baseIRI + "InvalidVegetableTopping"));
        OWLClass nothing = dataFactory.getOWLNothing();
        OWLSubClassOfAxiom axiom = dataFactory.getOWLSubClassOfAxiom(cheeseyVegetableTopping, nothing);
        result = queryReasoner(ontology, axiom);
        resultelk = queryReasonerELK(ontology, axiom);

        return result + "-" + resultelk;
    }

    //{PizzaIndividual} ⊑ DayPizza
    public static String testCR6(OWLOntology ontology) {
        boolean result, resultelk;
        OWLClass pizzaIndividual = dataFactory.getOWLClass(IRI.create(baseIRI + "PizzaIndividual"));
        OWLClass dayPizza = dataFactory.getOWLClass(IRI.create(baseIRI + "DayPizza"));
        OWLSubClassOfAxiom axiom = dataFactory.getOWLSubClassOfAxiom(pizzaIndividual, dayPizza);
        result = queryReasoner(ontology,axiom);
        resultelk = queryReasonerELK(ontology,axiom);
        return result + "-" + resultelk;
    }

    //Margherita ⊓ Siciliana ⊑ Pizza
    public static String query1(OWLOntology ontology) {
        boolean result, resultelk;

        OWLClass M = dataFactory.getOWLClass(IRI.create(baseIRI + "Margherita"));
        OWLClass N = dataFactory.getOWLClass(IRI.create(baseIRI + "Siciliana"));
        OWLClass B = dataFactory.getOWLClass(IRI.create(baseIRI + "Pizza"));
        OWLObjectIntersectionOf intersection = dataFactory.getOWLObjectIntersectionOf(M, N);
        OWLSubClassOfAxiom axiom = dataFactory.getOWLSubClassOfAxiom(intersection, B);
        result = queryReasoner(ontology, axiom);
        resultelk = queryReasonerELK(ontology,axiom);
        return result + "-" + resultelk;
    }

    //Margherita ⊓ VegetarianPizza ⊑ ⊤
    public static String query2(OWLOntology ontology) {
        boolean result, resultelk;

        OWLClass M = dataFactory.getOWLClass(IRI.create(baseIRI + "Margherita"));
        OWLClass N = dataFactory.getOWLClass(IRI.create(baseIRI + "VegetarianPizza"));
        OWLClass topClass = dataFactory.getOWLThing();
        OWLObjectIntersectionOf intersection = dataFactory.getOWLObjectIntersectionOf(M, N);
        OWLSubClassOfAxiom axiom = dataFactory.getOWLSubClassOfAxiom(intersection, topClass);
        result = queryReasoner(ontology,axiom);
        resultelk = queryReasonerELK(ontology,axiom);
        return result + "-" + resultelk;
    }

    //⊤ ⊑ Margherita ⊓ VegetarianPizza
    public static String query3(OWLOntology ontology) {
        boolean result, resultelk;
        OWLClass topClass = dataFactory.getOWLThing();
        OWLClass M = dataFactory.getOWLClass(IRI.create(baseIRI + "Margherita"));
        OWLClass N = dataFactory.getOWLClass(IRI.create(baseIRI + "VegetarianPizza"));
        OWLObjectIntersectionOf intersection = dataFactory.getOWLObjectIntersectionOf(M, N);
        OWLSubClassOfAxiom axiom = dataFactory.getOWLSubClassOfAxiom(topClass, intersection);
        result = queryReasoner(ontology,axiom);
        resultelk = queryReasonerELK(ontology,axiom);
        return result + "-" + resultelk;
    }

    //Siciliana ⊑ MeatyPizza ⊓ SeaPizza
    public static String query4(OWLOntology ontology) {
        boolean result, resultelk;

        OWLClass Siciliana = dataFactory.getOWLClass(IRI.create(baseIRI + "Siciliana"));
        OWLClass MeatyPizza = dataFactory.getOWLClass(IRI.create(baseIRI + "MeatyPizza"));
        OWLClass SeaPizza = dataFactory.getOWLClass(IRI.create(baseIRI + "SeaPizza"));
        OWLObjectIntersectionOf conjunction = dataFactory.getOWLObjectIntersectionOf(MeatyPizza, SeaPizza);
        OWLSubClassOfAxiom subclassAxiom = dataFactory.getOWLSubClassOfAxiom(Siciliana, conjunction);
        result = queryReasoner(ontology, subclassAxiom);
        resultelk = queryReasonerELK(ontology, subclassAxiom);

        return result + "-" + resultelk;
    }

    //Margherita ⊓ Siciliana ⊓ American ⊓ Giardiniera ⊑ Pizza
    public static String query5(OWLOntology ontology) {
        boolean result, resultelk;
        OWLClass M = dataFactory.getOWLClass(IRI.create(baseIRI + "Margherita"));
        OWLClass N = dataFactory.getOWLClass(IRI.create(baseIRI + "Siciliana"));
        OWLClass I = dataFactory.getOWLClass(IRI.create(baseIRI + "American"));
        OWLClass P = dataFactory.getOWLClass(IRI.create(baseIRI + "Giardiniera"));
        OWLClass B = dataFactory.getOWLClass(IRI.create(baseIRI + "Pizza"));
        OWLObjectIntersectionOf intersection = dataFactory.getOWLObjectIntersectionOf(M, N, I, P);
        OWLSubClassOfAxiom axiom = dataFactory.getOWLSubClassOfAxiom(intersection, B);
        result = queryReasoner(ontology,axiom);
        resultelk = queryReasonerELK(ontology,axiom);
        return result + "-" + resultelk;
    }

    //American ⊓ Parmense ⊓ Capricciosa ⊑ Pizza ⊓ MeatyPizza
    public static String query6(OWLOntology ontology) {
        boolean result, resultelk;
        OWLClass M = dataFactory.getOWLClass(IRI.create(baseIRI + "American"));
        OWLClass N = dataFactory.getOWLClass(IRI.create(baseIRI + "Parmense"));
        OWLClass I = dataFactory.getOWLClass(IRI.create(baseIRI + "Capricciosa"));
        OWLClass B = dataFactory.getOWLClass(IRI.create(baseIRI + "Pizza"));
        OWLClass P = dataFactory.getOWLClass(IRI.create(baseIRI + "MeatyPizza"));
        OWLObjectIntersectionOf conjunctionLeft = dataFactory.getOWLObjectIntersectionOf(M, N, I);
        OWLObjectIntersectionOf conjunctionRight = dataFactory.getOWLObjectIntersectionOf(B, P);
        OWLSubClassOfAxiom subclassAxiom = dataFactory.getOWLSubClassOfAxiom(conjunctionLeft, conjunctionRight);
        result = queryReasoner(ontology,subclassAxiom);
        resultelk = queryReasonerELK(ontology,subclassAxiom);
        return result + "-" + resultelk;
    }

    //Margherita ⊑ ∃hasTopping.(MozzarellaTopping ⊓ TomatoTopping)
    public static String query7(OWLOntology ontology) {
        boolean result, resultelk;
        // Define the classes A and B (replace with PizzaOntology class names)
        OWLClass classA = dataFactory.getOWLClass(IRI.create(baseIRI + "MozzarellaTopping"));
        OWLClass classB = dataFactory.getOWLClass(IRI.create(baseIRI + "TomatoTopping"));
        OWLObjectProperty propertyR = dataFactory.getOWLObjectProperty(IRI.create(baseIRI + "hasTopping"));
        OWLObjectIntersectionOf intersection = dataFactory.getOWLObjectIntersectionOf(classA, classB);
        OWLObjectSomeValuesFrom existsR = dataFactory.getOWLObjectSomeValuesFrom(propertyR, intersection);
        OWLClass classX = dataFactory.getOWLClass(IRI.create(baseIRI + "Margherita"));
        OWLSubClassOfAxiom axiom = dataFactory.getOWLSubClassOfAxiom(classX, existsR);
        result = queryReasoner(ontology,axiom);
        resultelk = queryReasonerELK(ontology,axiom);
        return result + "-" + resultelk;
    }

    //QuattroFormaggi ⊑ ∃hasTopping.(MozzarellaTopping ⊓ TomatoTopping)
    public static String query8(OWLOntology ontology) {
        boolean result, resultelk;
        OWLClass classA = dataFactory.getOWLClass(IRI.create(baseIRI + "MozzarellaTopping"));
        OWLClass classB = dataFactory.getOWLClass(IRI.create(baseIRI + "TomatoTopping"));
        OWLObjectProperty propertyR = dataFactory.getOWLObjectProperty(IRI.create(baseIRI + "hasTopping"));
        OWLObjectIntersectionOf intersection = dataFactory.getOWLObjectIntersectionOf(classA, classB);
        OWLObjectSomeValuesFrom existsR = dataFactory.getOWLObjectSomeValuesFrom(propertyR, intersection);
        OWLClass classX = dataFactory.getOWLClass(IRI.create(baseIRI + "QuattroFormaggi"));
        OWLSubClassOfAxiom axiom = dataFactory.getOWLSubClassOfAxiom(classX, existsR);
        result = queryReasoner(ontology,axiom);
        resultelk = queryReasonerELK(ontology,axiom);
        return result + "-" + resultelk;
    }

    //(Pizza ⊓ ∃hasTopping.MozzarellaTopping) ⊑ (Pizza ⊓ ∃hasTopping.PizzaTopping)
    public static String query9(OWLOntology ontology) {
        boolean result, resultelk;
        // Concetto C: Pizza ⊓ ∃hasTopping.(MozzarellaTopping)
        OWLClass pizza = dataFactory.getOWLClass(IRI.create(baseIRI + "Pizza"));
        OWLObjectProperty hasTopping = dataFactory.getOWLObjectProperty(IRI.create(baseIRI + "hasTopping"));
        OWLClass mozzarellaTopping = dataFactory.getOWLClass(IRI.create(baseIRI + "MozzarellaTopping"));
        OWLObjectSomeValuesFrom existsHasToppingMozzarella = dataFactory.getOWLObjectSomeValuesFrom(hasTopping, mozzarellaTopping);
        OWLObjectIntersectionOf conceptC = dataFactory.getOWLObjectIntersectionOf(pizza, existsHasToppingMozzarella);

        // Concetto D: Pizza ⊓ ∃hasTopping.PizzaTopping
        OWLClass pizzaTopping = dataFactory.getOWLClass(IRI.create(baseIRI + "PizzaTopping"));
        OWLObjectSomeValuesFrom existsHasToppingTopping = dataFactory.getOWLObjectSomeValuesFrom(hasTopping, pizzaTopping);
        OWLObjectIntersectionOf conceptD = dataFactory.getOWLObjectIntersectionOf(pizza, existsHasToppingTopping);
        OWLSubClassOfAxiom queryAxiom = dataFactory.getOWLSubClassOfAxiom(conceptC, conceptD);
        result = queryReasoner(ontology, queryAxiom); // Reasoner generico
        resultelk = queryReasonerELK(ontology, queryAxiom); // Reasoner ELK
        return result + "-" + resultelk;
    }



    public static void print_ontology(OWLOntology ontology) {
        ontology.axioms().forEach(System.out::println);
    }

    public static void printAxioms(Set<OWLSubClassOfAxiom> axioms) {
        if (axioms.isEmpty()) {
            System.out.println("Il set di assiomi è vuoto.");
        } else {
            for (OWLAxiom axiom : axioms) {
                System.out.println(axiom.toString());
            }
        }
    }
}
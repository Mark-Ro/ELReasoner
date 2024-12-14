import org.junit.jupiter.api.*;
import org.progetto.Reasoner;
import org.semanticweb.elk.owlapi.ElkReasonerFactory;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.reasoner.OWLReasonerFactory;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

public class ReasonerTest {

    private final String baseIRI = "http://www.co-ode.org/marcoromano/ontologies/pizza#";
    private final OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
    private final OWLDataFactory dataFactory = manager.getOWLDataFactory();
    private OWLOntology ontology = loadOntology();

    private OWLOntology loadOntology() {
        try {
            String ONTOLOGY_PATH = "data\\PizzaOntology.owx";
            return manager.loadOntologyFromOntologyDocument(new File(ONTOLOGY_PATH));
        } catch (Exception e) {
            throw new RuntimeException("Failed to load ontology", e);
        }
    }

    private boolean queryReasoner(OWLOntology ontology, OWLSubClassOfAxiom queryAxiom) {
        Reasoner reasoner = new Reasoner(ontology);
        return reasoner.isSubsumptionSatisfied(queryAxiom);
    }

    private boolean queryReasonerELK(OWLOntology ontology, OWLSubClassOfAxiom queryAxiom) {
        OWLReasonerFactory owlReasonerFactory = new ElkReasonerFactory();
        OWLReasoner reasoner = owlReasonerFactory.createReasoner(ontology);
        reasoner.precomputeInferences();
        OWLObjectComplementOf complementOf = dataFactory.getOWLObjectComplementOf(queryAxiom.getSuperClass());
        OWLObjectIntersectionOf negIntersection = dataFactory.getOWLObjectIntersectionOf(queryAxiom.getSubClass(), complementOf);
        return !reasoner.isSatisfiable(negIntersection);
    }

    //VegetableTopping ⊑ Food
    @Test
    void test1() {
        OWLClass vegetableTopping = dataFactory.getOWLClass(IRI.create(baseIRI + "VegetableTopping"));
        OWLClass food = dataFactory.getOWLClass(IRI.create(baseIRI + "Food"));
        OWLSubClassOfAxiom axiom = dataFactory.getOWLSubClassOfAxiom(vegetableTopping, food);
        assertEquals(queryReasonerELK(ontology,axiom),queryReasoner(ontology,axiom));
    }

    //VeganSauce ⊑ SauceVegetableTopping
    @Test
    void test2() {
        OWLClass veganSauce = dataFactory.getOWLClass(IRI.create(baseIRI + "VeganSauce"));
        OWLClass sauceVegetableTopping = dataFactory.getOWLClass(IRI.create(baseIRI + "SauceVegetableTopping"));
        OWLSubClassOfAxiom axiom = dataFactory.getOWLSubClassOfAxiom(veganSauce, sauceVegetableTopping);
        assertEquals(queryReasonerELK(ontology,axiom),queryReasoner(ontology,axiom));
    }

    //SpicyPizza ⊑ ∃hasTopping.SpicyTopping
    @Test
    void test3() {
        OWLObjectProperty hasTopping = dataFactory.getOWLObjectProperty(IRI.create(baseIRI + "hasTopping"));
        OWLClass spicyPizza = dataFactory.getOWLClass(IRI.create(baseIRI + "SpicyPizza"));
        OWLClass spicyTopping = dataFactory.getOWLClass(IRI.create(baseIRI + "SpicyTopping"));
        OWLSubClassOfAxiom axiom = dataFactory.getOWLSubClassOfAxiom(spicyPizza, dataFactory.getOWLObjectSomeValuesFrom(hasTopping, spicyTopping));
        assertEquals(queryReasonerELK(ontology,axiom),queryReasoner(ontology,axiom));
    }

    //AmericanHot ⊑ PepperPizza
    @Test
    void test4() {
        OWLClass americanHot = dataFactory.getOWLClass(IRI.create(baseIRI + "AmericanHot"));
        OWLClass pepperPizza = dataFactory.getOWLClass(IRI.create(baseIRI + "PepperPizza"));
        OWLSubClassOfAxiom axiom = dataFactory.getOWLSubClassOfAxiom(americanHot, pepperPizza);
        assertEquals(queryReasonerELK(ontology,axiom),queryReasoner(ontology,axiom));
    }

    //InvalidVegetableTopping ⊑ ⊥
    @Test
    void test5() {
        OWLClass invalidVegetableTopping = dataFactory.getOWLClass(IRI.create(baseIRI + "InvalidVegetableTopping"));
        OWLSubClassOfAxiom axiom = dataFactory.getOWLSubClassOfAxiom(invalidVegetableTopping, dataFactory.getOWLNothing());
        assertEquals(queryReasonerELK(ontology,axiom),queryReasoner(ontology,axiom));
    }

    //{PizzaIndividual} ⊑ DayPizza
    @Test
    void test6() {
        OWLClass pizzaIndividual = dataFactory.getOWLClass(IRI.create(baseIRI + "PizzaIndividual"));
        OWLClass dayPizza = dataFactory.getOWLClass(IRI.create(baseIRI + "DayPizza"));
        OWLSubClassOfAxiom axiom = dataFactory.getOWLSubClassOfAxiom(pizzaIndividual, dayPizza);
        assertEquals(queryReasonerELK(ontology,axiom),queryReasoner(ontology,axiom));
    }

    //Margherita ⊓ Siciliana ⊑ Pizza
    @Test
    void test7() {
        OWLClass margherita = dataFactory.getOWLClass(IRI.create(baseIRI + "Margherita"));
        OWLClass siciliana = dataFactory.getOWLClass(IRI.create(baseIRI + "Siciliana"));
        OWLClass pizza = dataFactory.getOWLClass(IRI.create(baseIRI + "Pizza"));
        OWLObjectIntersectionOf intersection = dataFactory.getOWLObjectIntersectionOf(margherita, siciliana);
        OWLSubClassOfAxiom axiom = dataFactory.getOWLSubClassOfAxiom(intersection, pizza);
        assertEquals(queryReasonerELK(ontology,axiom),queryReasoner(ontology,axiom));
    }

    //Margherita ⊓ VegetarianPizza ⊑ ⊤
    @Test
    void test8() {
        OWLClass margherita = dataFactory.getOWLClass(IRI.create(baseIRI + "Margherita"));
        OWLClass vegetarianPizza = dataFactory.getOWLClass(IRI.create(baseIRI + "VegetarianPizza"));
        OWLObjectIntersectionOf intersection = dataFactory.getOWLObjectIntersectionOf(margherita, vegetarianPizza);
        OWLSubClassOfAxiom axiom = dataFactory.getOWLSubClassOfAxiom(dataFactory.getOWLThing(), intersection);
        assertEquals(queryReasonerELK(ontology,axiom),queryReasoner(ontology,axiom));
    }

    //⊤ ⊑ Margherita ⊓ VegetarianPizza
    @Test
    void test9() {
        OWLClass topClass = dataFactory.getOWLThing();
        OWLClass margherita = dataFactory.getOWLClass(IRI.create(baseIRI + "Margherita"));
        OWLClass vegetarianPizza = dataFactory.getOWLClass(IRI.create(baseIRI + "VegetarianPizza"));
        OWLObjectIntersectionOf intersection = dataFactory.getOWLObjectIntersectionOf(margherita, vegetarianPizza);
        OWLSubClassOfAxiom axiom = dataFactory.getOWLSubClassOfAxiom(topClass, intersection);
        assertEquals(queryReasonerELK(ontology,axiom),queryReasoner(ontology,axiom));
    }
    
    //Siciliana ⊑ MeatyPizza ⊓ SeaPizza
    @Test
    void test10() {
        OWLClass Siciliana = dataFactory.getOWLClass(IRI.create(baseIRI + "Siciliana"));
        OWLClass MeatyPizza = dataFactory.getOWLClass(IRI.create(baseIRI + "MeatyPizza"));
        OWLClass SeaPizza = dataFactory.getOWLClass(IRI.create(baseIRI + "SeaPizza"));
        OWLObjectIntersectionOf conjunction = dataFactory.getOWLObjectIntersectionOf(MeatyPizza, SeaPizza);
        OWLSubClassOfAxiom axiom = dataFactory.getOWLSubClassOfAxiom(Siciliana, conjunction);
        assertEquals(queryReasonerELK(ontology,axiom),queryReasoner(ontology,axiom));
    }

    //Margherita ⊓ Siciliana ⊓ American ⊓ Giardiniera ⊑ Pizza
    @Test
    void test11() {
        OWLClass margherita = dataFactory.getOWLClass(IRI.create(baseIRI + "Margherita"));
        OWLClass siciliana = dataFactory.getOWLClass(IRI.create(baseIRI + "Siciliana"));
        OWLClass american = dataFactory.getOWLClass(IRI.create(baseIRI + "American"));
        OWLClass giardiniera = dataFactory.getOWLClass(IRI.create(baseIRI + "Giardiniera"));
        OWLClass pizza = dataFactory.getOWLClass(IRI.create(baseIRI + "Pizza"));
        OWLObjectIntersectionOf intersection = dataFactory.getOWLObjectIntersectionOf(margherita, siciliana, american, giardiniera);
        OWLSubClassOfAxiom axiom = dataFactory.getOWLSubClassOfAxiom(intersection, pizza);
        assertEquals(queryReasonerELK(ontology,axiom),queryReasoner(ontology,axiom));
    }

    //American ⊓ Parmense ⊓ Capricciosa ⊑ Pizza ⊓ MeatyPizza
    @Test
    void test12() {
        OWLClass american = dataFactory.getOWLClass(IRI.create(baseIRI + "American"));
        OWLClass parmense = dataFactory.getOWLClass(IRI.create(baseIRI + "Parmense"));
        OWLClass capricciosa = dataFactory.getOWLClass(IRI.create(baseIRI + "Capricciosa"));
        OWLClass pizza = dataFactory.getOWLClass(IRI.create(baseIRI + "Pizza"));
        OWLClass meatyPizza = dataFactory.getOWLClass(IRI.create(baseIRI + "MeatyPizza"));
        OWLObjectIntersectionOf conjunctionLeft = dataFactory.getOWLObjectIntersectionOf(american, parmense, capricciosa);
        OWLObjectIntersectionOf conjunctionRight = dataFactory.getOWLObjectIntersectionOf(pizza, meatyPizza);
        OWLSubClassOfAxiom axiom = dataFactory.getOWLSubClassOfAxiom(conjunctionLeft, conjunctionRight);
        assertEquals(queryReasonerELK(ontology,axiom),queryReasoner(ontology,axiom));
    }

    //Margherita ⊑ ∃hasTopping.(MozzarellaTopping ⊓ TomatoTopping)
    @Test
    void test13() {
        OWLClass margherita = dataFactory.getOWLClass(IRI.create(baseIRI + "Margherita"));
        OWLClass mozzarellaTopping = dataFactory.getOWLClass(IRI.create(baseIRI + "MozzarellaTopping"));
        OWLClass tomatoTopping = dataFactory.getOWLClass(IRI.create(baseIRI + "TomatoTopping"));

        OWLObjectProperty propertyR = dataFactory.getOWLObjectProperty(IRI.create(baseIRI + "hasTopping"));

        OWLObjectIntersectionOf intersection = dataFactory.getOWLObjectIntersectionOf(mozzarellaTopping, tomatoTopping);

        OWLObjectSomeValuesFrom existsR = dataFactory.getOWLObjectSomeValuesFrom(propertyR, intersection);

        OWLSubClassOfAxiom axiom = dataFactory.getOWLSubClassOfAxiom(margherita, existsR);

        assertEquals(queryReasonerELK(ontology,axiom),queryReasoner(ontology,axiom));
    }

    //QuattroFormaggi ⊑ ∃hasTopping.(MozzarellaTopping ⊓ TomatoTopping)
    @Test
    void test14() {
        OWLClass mozzarellaTopping = dataFactory.getOWLClass(IRI.create(baseIRI + "MozzarellaTopping"));
        OWLClass tomatoTopping = dataFactory.getOWLClass(IRI.create(baseIRI + "TomatoTopping"));
        OWLObjectProperty hasTopping = dataFactory.getOWLObjectProperty(IRI.create(baseIRI + "hasTopping"));
        OWLObjectIntersectionOf intersection = dataFactory.getOWLObjectIntersectionOf(mozzarellaTopping, tomatoTopping);
        OWLObjectSomeValuesFrom existsR = dataFactory.getOWLObjectSomeValuesFrom(hasTopping, intersection);
        OWLClass quattroFormaggi = dataFactory.getOWLClass(IRI.create(baseIRI + "QuattroFormaggi"));
        OWLSubClassOfAxiom axiom = dataFactory.getOWLSubClassOfAxiom(quattroFormaggi, existsR);
        assertEquals(queryReasonerELK(ontology,axiom),queryReasoner(ontology,axiom));
    }

    //(Pizza ⊓ ∃hasTopping.MozzarellaTopping) ⊑ (Pizza ⊓ ∃hasTopping.PizzaTopping)
    @Test
    void test15() {
        OWLClass pizza = dataFactory.getOWLClass(IRI.create(baseIRI + "Pizza"));
        OWLObjectProperty hasTopping = dataFactory.getOWLObjectProperty(IRI.create(baseIRI + "hasTopping"));
        OWLClass mozzarellaTopping = dataFactory.getOWLClass(IRI.create(baseIRI + "MozzarellaTopping"));
        OWLObjectSomeValuesFrom existsHasToppingMozzarella = dataFactory.getOWLObjectSomeValuesFrom(hasTopping, mozzarellaTopping);
        OWLObjectIntersectionOf conceptC = dataFactory.getOWLObjectIntersectionOf(pizza, existsHasToppingMozzarella);
        OWLClass pizzaTopping = dataFactory.getOWLClass(IRI.create(baseIRI + "PizzaTopping"));
        OWLObjectSomeValuesFrom existsHasToppingTopping = dataFactory.getOWLObjectSomeValuesFrom(hasTopping, pizzaTopping);
        OWLObjectIntersectionOf conceptD = dataFactory.getOWLObjectIntersectionOf(pizza, existsHasToppingTopping);
        OWLSubClassOfAxiom axiom = dataFactory.getOWLSubClassOfAxiom(conceptC, conceptD);
        assertEquals(queryReasonerELK(ontology,axiom),queryReasoner(ontology,axiom));
    }

}

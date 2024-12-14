import org.junit.jupiter.api.*;
import org.progetto.Reasoner;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class ReasonerTest2 {

    private OWLOntologyManager manager;
    private OWLDataFactory factory;
    private OWLOntology ontology;
    private Reasoner reasoner;

    @BeforeEach
    void setUp() throws Exception {
        manager = OWLManager.createOWLOntologyManager();
        factory = manager.getOWLDataFactory();
        // Load your ontology from the provided file
        File file = new File("data\\ONTOLOGY_TEST.owx"); // Update to your ontology file path
        ontology = manager.loadOntologyFromOntologyDocument(file);
        reasoner = new Reasoner(ontology);
    }

    @AfterEach
    void tearDown() throws Exception {
        manager.removeOntology(ontology);
    }

    @Test
    void test1() {
        OWLClass classM = factory.getOWLClass(IRI.create("http://myOntology.com#M"));
        OWLClass classN = factory.getOWLClass(IRI.create("http://myOntology.com#N"));
        OWLClass classB = factory.getOWLClass(IRI.create("http://myOntology.com#B"));
        OWLObjectIntersectionOf intersectionMN = factory.getOWLObjectIntersectionOf(classM, classN);
        OWLSubClassOfAxiom queryAxiom = factory.getOWLSubClassOfAxiom(intersectionMN, classB);

        boolean result = reasoner.isSubsumptionSatisfied(queryAxiom);
        assertTrue(result, "Test1 failed");
    }

    @Test
    void test2() {
        OWLClass classM = factory.getOWLClass(IRI.create("http://myOntology.com#M"));
        OWLClass classN = factory.getOWLClass(IRI.create("http://myOntology.com#N"));
        OWLClass classTop = factory.getOWLThing();
        OWLObjectIntersectionOf intersectionMN = factory.getOWLObjectIntersectionOf(classM, classN);
        OWLSubClassOfAxiom queryAxiom = factory.getOWLSubClassOfAxiom(intersectionMN, classTop);

        boolean result = reasoner.isSubsumptionSatisfied(queryAxiom);
        assertTrue(result, "Test2 failed");
    }

    @Test
    void test3() {
        OWLClass classTop = factory.getOWLThing();
        OWLClass classM = factory.getOWLClass(IRI.create("http://myOntology.com#M"));
        OWLClass classN = factory.getOWLClass(IRI.create("http://myOntology.com#N"));
        OWLObjectIntersectionOf intersectionMN = factory.getOWLObjectIntersectionOf(classM, classN);
        OWLSubClassOfAxiom queryAxiom = factory.getOWLSubClassOfAxiom(classTop, intersectionMN);

        boolean result = reasoner.isSubsumptionSatisfied(queryAxiom);
        assertFalse(result, "Test3 failed");
    }

    @Test
    void test4() {
        OWLClass classM = factory.getOWLClass(IRI.create("http://myOntology.com#M"));
        OWLClass classN = factory.getOWLClass(IRI.create("http://myOntology.com#N"));
        OWLClass classP = factory.getOWLClass(IRI.create("http://myOntology.com#P"));
        OWLClass classB = factory.getOWLClass(IRI.create("http://myOntology.com#B"));
        OWLObjectIntersectionOf intersectionMNP = factory.getOWLObjectIntersectionOf(classM, classN, classP);
        OWLSubClassOfAxiom queryAxiom = factory.getOWLSubClassOfAxiom(intersectionMNP, classB);

        boolean result = reasoner.isSubsumptionSatisfied(queryAxiom);
        assertTrue(result, "Test4 failed");
    }

    @Test
    void test5() {
        // Definisci le classi M, N, I, B, e P utilizzando l'IRI appropriato
        OWLClass classM = factory.getOWLClass(IRI.create("http://myOntology.com#M"));
        OWLClass classN = factory.getOWLClass(IRI.create("http://myOntology.com#N"));
        OWLClass classI = factory.getOWLClass(IRI.create("http://myOntology.com#I"));
        OWLClass classB = factory.getOWLClass(IRI.create("http://myOntology.com#B"));
        OWLClass classP = factory.getOWLClass(IRI.create("http://myOntology.com#P"));

        // Crea l'intersezione M ⊓ N ⊓ I
        OWLObjectIntersectionOf intersectionMNI = factory.getOWLObjectIntersectionOf(classM, classN, classI);

        // Crea l'intersezione B ⊓ P
        OWLObjectIntersectionOf intersectionBP = factory.getOWLObjectIntersectionOf(classB, classP);

        // Crea l'assioma della query: M ⊓ N ⊓ I ⊑ B ⊓ P
        OWLSubClassOfAxiom queryAxiom = factory.getOWLSubClassOfAxiom(intersectionMNI, intersectionBP);

        // Esegui la query usando il reasoner e verifica che il risultato sia true
        boolean result = reasoner.isSubsumptionSatisfied(queryAxiom);
        assertTrue(result, "Test5 failed: M ⊓ N ⊓ I ⊑ B ⊓ P dovrebbe restituire true");
    }


    @Test
    void test6() {
        OWLClass classI = factory.getOWLClass(IRI.create("http://myOntology.com#I"));
        OWLClassExpression someInstance = factory.getOWLObjectOneOf(factory.getOWLNamedIndividual(IRI.create("http://myOntology.com#x")));
        OWLSubClassOfAxiom queryAxiom = factory.getOWLSubClassOfAxiom(someInstance, classI);

        boolean result = reasoner.isSubsumptionSatisfied(queryAxiom);
        assertTrue(result, "Test6 failed");
    }

    @Test
    void test7() {
        OWLClass classA = factory.getOWLClass(IRI.create("http://myOntology.com#A"));
        OWLClassExpression someInstance = factory.getOWLObjectOneOf(factory.getOWLNamedIndividual(IRI.create("http://myOntology.com#x")));
        OWLSubClassOfAxiom queryAxiom = factory.getOWLSubClassOfAxiom(someInstance, classA);

        boolean result = reasoner.isSubsumptionSatisfied(queryAxiom);
        assertFalse(result, "Test7 failed");
    }

    @Test
    void test8() {
        // Definisci le classi A, B, l'individuo x e la proprietà r3
        OWLClass classA = factory.getOWLClass(IRI.create("http://myOntology.com#A"));
        OWLClass classB = factory.getOWLClass(IRI.create("http://myOntology.com#B"));
        OWLNamedIndividual individualX = factory.getOWLNamedIndividual(IRI.create("http://myOntology.com#x"));
        OWLObjectProperty propertyR3 = factory.getOWLObjectProperty(IRI.create("http://myOntology.com#r3"));

        // Crea l'intersezione A ⊓ B
        OWLObjectIntersectionOf intersectionAB = factory.getOWLObjectIntersectionOf(classA, classB);

        // Crea la restrizione ∃r3.(A ⊓ B)
        OWLObjectSomeValuesFrom restriction = factory.getOWLObjectSomeValuesFrom(propertyR3, intersectionAB);

        // Crea l'assioma di sottoclasse: {x} ⊑ ∃r3.(A ⊓ B)
        // Nota che `individualX` deve essere trattato come una classe, quindi usiamo l'espressione `individualX.getTypes(ontology)`
        OWLSubClassOfAxiom subclassAxiom = factory.getOWLSubClassOfAxiom(
                factory.getOWLObjectOneOf(individualX),  // Trattiamo {x} come una "classe" in questo caso
                restriction  // La restrizione ∃r3.(A ⊓ B)
        );

        // Esegui la query usando il reasoner e verifica che il risultato sia true
        boolean result = reasoner.isSubsumptionSatisfied(subclassAxiom);
        assertTrue(result, "Test8 failed: {x} ⊑ ∃r3.(A ⊓ B) should return true");
    }

    @Test
    void test9() {
        // Definisci le classi A, B, l'individuo x e la proprietà r2
        OWLClass classA = factory.getOWLClass(IRI.create("http://myOntology.com#A"));
        OWLClass classB = factory.getOWLClass(IRI.create("http://myOntology.com#B"));
        OWLNamedIndividual individualX = factory.getOWLNamedIndividual(IRI.create("http://myOntology.com#x"));
        OWLObjectProperty propertyR2 = factory.getOWLObjectProperty(IRI.create("http://myOntology.com#r2"));

        // Crea l'intersezione A ⊓ B
        OWLObjectIntersectionOf intersectionAB = factory.getOWLObjectIntersectionOf(classA, classB);

        // Crea la restrizione ∃r2.(A ⊓ B)
        OWLObjectSomeValuesFrom restriction = factory.getOWLObjectSomeValuesFrom(propertyR2, intersectionAB);

        // Crea l'assioma di sottoclasse: {x} ⊑ ∃r2.(A ⊓ B)
        OWLSubClassOfAxiom subclassAxiom = factory.getOWLSubClassOfAxiom(
                factory.getOWLObjectOneOf(individualX),  // {x} come una "classe"
                restriction  // La restrizione ∃r2.(A ⊓ B)
        );

        // Esegui la query usando il reasoner e verifica che il risultato sia false
        boolean result = reasoner.isSubsumptionSatisfied(subclassAxiom);
        assertFalse(result, "Test9 failed: {x} ⊑ ∃r2.(A ⊓ B) should return false");
    }

    @Test
    void test10() {
        // Definisci le classi A, B, I, l'individuo x e la proprietà r2
        OWLClass classA = factory.getOWLClass(IRI.create("http://myOntology.com#A"));
        OWLClass classB = factory.getOWLClass(IRI.create("http://myOntology.com#B"));
        OWLClass classI = factory.getOWLClass(IRI.create("http://myOntology.com#I"));
        OWLNamedIndividual individualX = factory.getOWLNamedIndividual(IRI.create("http://myOntology.com#x"));
        OWLObjectProperty propertyR2 = factory.getOWLObjectProperty(IRI.create("http://myOntology.com#r2"));

        // Crea l'intersezione A ⊓ B ⊓ I
        OWLObjectIntersectionOf intersectionABI = factory.getOWLObjectIntersectionOf(classA, classB, classI);

        // Crea la restrizione ∃r2.(A ⊓ B ⊓ I)
        OWLObjectSomeValuesFrom restriction = factory.getOWLObjectSomeValuesFrom(propertyR2, intersectionABI);

        // Crea l'assioma di sottoclasse: {x} ⊑ ∃r2.(A ⊓ B ⊓ I)
        OWLSubClassOfAxiom subclassAxiom = factory.getOWLSubClassOfAxiom(
                factory.getOWLObjectOneOf(individualX),  // {x} come una "classe"
                restriction  // La restrizione ∃r2.(A ⊓ B ⊓ I)
        );

        // Esegui la query usando il reasoner e verifica che il risultato sia false
        boolean result = reasoner.isSubsumptionSatisfied(subclassAxiom);
        assertFalse(result, "Test10 failed: {x} ⊑ ∃r2.(A ⊓ B ⊓ I) should return false");
    }

    @Test
    void test11() {
        // Definisci le classi A, B, G, gli individui x e y e la proprietà r4
        OWLClass classA = factory.getOWLClass(IRI.create("http://myOntology.com#A"));
        OWLClass classB = factory.getOWLClass(IRI.create("http://myOntology.com#B"));
        OWLClass classG = factory.getOWLClass(IRI.create("http://myOntology.com#G"));
        OWLNamedIndividual individualX = factory.getOWLNamedIndividual(IRI.create("http://myOntology.com#x"));
        OWLNamedIndividual individualY = factory.getOWLNamedIndividual(IRI.create("http://myOntology.com#y"));
        OWLObjectProperty propertyR4 = factory.getOWLObjectProperty(IRI.create("http://myOntology.com#r4"));

        // Crea l'intersezione A ⊓ B ⊓ G
        OWLObjectIntersectionOf intersectionABG = factory.getOWLObjectIntersectionOf(classA, classB, classG);

        // Crea la restrizione ∃r4.(A ⊓ B ⊓ G)
        OWLObjectSomeValuesFrom restriction = factory.getOWLObjectSomeValuesFrom(propertyR4, intersectionABG);

        // Crea l'assioma di sottoclasse: {x} ⊓ {y} ⊑ ∃r4.(A ⊓ B ⊓ G)
        OWLSubClassOfAxiom subclassAxiom = factory.getOWLSubClassOfAxiom(
                factory.getOWLObjectIntersectionOf(factory.getOWLObjectOneOf(individualX), factory.getOWLObjectOneOf(individualY)),  // {x} ⊓ {y}
                restriction  // La restrizione ∃r4.(A ⊓ B ⊓ G)
        );

        // Esegui la query usando il reasoner e verifica che il risultato sia true
        boolean result = reasoner.isSubsumptionSatisfied(subclassAxiom);
        assertTrue(result, "Test11 failed: {x} ⊓ {y} ⊑ ∃r4.(A ⊓ B ⊓ G) should return true");
    }

    @Test
    void test12() {
        // Definisci le classi A, B, gli individui x e y e la proprietà r4
        OWLClass classA = factory.getOWLClass(IRI.create("http://myOntology.com#A"));
        OWLClass classB = factory.getOWLClass(IRI.create("http://myOntology.com#B"));
        OWLNamedIndividual individualX = factory.getOWLNamedIndividual(IRI.create("http://myOntology.com#x"));
        OWLNamedIndividual individualY = factory.getOWLNamedIndividual(IRI.create("http://myOntology.com#y"));
        OWLObjectProperty propertyR4 = factory.getOWLObjectProperty(IRI.create("http://myOntology.com#r4"));

        // Crea l'intersezione A ⊓ B
        OWLObjectIntersectionOf intersectionAB = factory.getOWLObjectIntersectionOf(classA, classB);

        // Crea la restrizione ∃r4.(A ⊓ B)
        OWLObjectSomeValuesFrom restriction = factory.getOWLObjectSomeValuesFrom(propertyR4, intersectionAB);

        // Crea l'assioma di sottoclasse: {x} ⊓ {y} ⊑ ∃r4.(A ⊓ B)
        OWLSubClassOfAxiom subclassAxiom = factory.getOWLSubClassOfAxiom(
                factory.getOWLObjectIntersectionOf(factory.getOWLObjectOneOf(individualX), factory.getOWLObjectOneOf(individualY)),  // {x} ⊓ {y}
                restriction  // La restrizione ∃r4.(A ⊓ B)
        );

        // Esegui la query usando il reasoner e verifica che il risultato sia true
        boolean result = reasoner.isSubsumptionSatisfied(subclassAxiom);
        assertTrue(result, "Test12 failed: {x} ⊓ {y} ⊑ ∃r4.(A ⊓ B) should return true");
    }

    @Test
    void test13() {
        // Definisci le classi A, B, l'individuo y e la proprietà r4
        OWLClass classA = factory.getOWLClass(IRI.create("http://myOntology.com#A"));
        OWLClass classB = factory.getOWLClass(IRI.create("http://myOntology.com#B"));
        OWLNamedIndividual individualY = factory.getOWLNamedIndividual(IRI.create("http://myOntology.com#y"));
        OWLObjectProperty propertyR4 = factory.getOWLObjectProperty(IRI.create("http://myOntology.com#r4"));

        // Crea l'intersezione A ⊓ B
        OWLObjectIntersectionOf intersectionAB = factory.getOWLObjectIntersectionOf(classA, classB);

        // Crea la restrizione ∃r4.(A ⊓ B)
        OWLObjectSomeValuesFrom restriction = factory.getOWLObjectSomeValuesFrom(propertyR4, intersectionAB);

        // Crea l'assioma di sottoclasse: {y} ⊑ ∃r4.(A ⊓ B)
        OWLSubClassOfAxiom subclassAxiom = factory.getOWLSubClassOfAxiom(
                factory.getOWLObjectOneOf(individualY),  // {y}
                restriction  // La restrizione ∃r4.(A ⊓ B)
        );

        // Esegui la query usando il reasoner e verifica che il risultato sia false
        boolean result = reasoner.isSubsumptionSatisfied(subclassAxiom);
        assertFalse(result, "Test13 failed: {y} ⊑ ∃r4.(A ⊓ B) should return false");
    }

    @Test
    void test14() {
        // Definisci le classi F, I, L e la proprietà r3, r4
        OWLClass classF = factory.getOWLClass(IRI.create("http://myOntology.com#F"));
        OWLClass classI = factory.getOWLClass(IRI.create("http://myOntology.com#I"));
        OWLClass classL = factory.getOWLClass(IRI.create("http://myOntology.com#L"));
        OWLObjectProperty propertyR3 = factory.getOWLObjectProperty(IRI.create("http://myOntology.com#r3"));
        OWLObjectProperty propertyR4 = factory.getOWLObjectProperty(IRI.create("http://myOntology.com#r4"));

        // Crea la restrizione ∃r4.(L) - Esiste un individuo che è in relazione con un individuo di L tramite r4
        OWLObjectSomeValuesFrom restrictionR4L = factory.getOWLObjectSomeValuesFrom(propertyR4, classL);

        // Crea l'intersezione I ⊓ ∃r4.(L)
        OWLObjectIntersectionOf intersectionI_R4_L = factory.getOWLObjectIntersectionOf(classI, restrictionR4L);

        // Crea la restrizione ∃r3.(I ⊓ ∃r4.(L))
        OWLObjectSomeValuesFrom restrictionR3_intersection = factory.getOWLObjectSomeValuesFrom(propertyR3, intersectionI_R4_L);

        // Crea l'assioma di sottoclasse: F ⊑ ∃r3.(I ⊓ ∃r4.(L))
        OWLSubClassOfAxiom subclassAxiom = factory.getOWLSubClassOfAxiom(classF, restrictionR3_intersection);

        // Esegui la query usando il reasoner e verifica che il risultato sia true
        boolean result = reasoner.isSubsumptionSatisfied(subclassAxiom);
        assertTrue(result, "Test14 failed: F ⊑ ∃r3.(I ⊓ ∃r4.(L)) should return true");
    }

    @Test
    void test15() {
        // Definisci le classi C, D, S e la proprietà r2
        OWLClass classC = factory.getOWLClass(IRI.create("http://myOntology.com#C"));
        OWLClass classD = factory.getOWLClass(IRI.create("http://myOntology.com#D"));
        OWLClass classS = factory.getOWLClass(IRI.create("http://myOntology.com#S"));
        OWLObjectProperty propertyR2 = factory.getOWLObjectProperty(IRI.create("http://myOntology.com#r2"));

        // Crea l'intersezione C ⊓ D
        OWLObjectIntersectionOf intersectionCD = factory.getOWLObjectIntersectionOf(classC, classD);

        // Crea la restrizione ∃r2.(C ⊓ D)
        OWLObjectSomeValuesFrom restrictionR2_CD = factory.getOWLObjectSomeValuesFrom(propertyR2, intersectionCD);

        // Crea l'assioma di sottoclasse: ∃r2.(C ⊓ D) ⊑ S
        OWLSubClassOfAxiom subclassAxiom = factory.getOWLSubClassOfAxiom(restrictionR2_CD, classS);

        // Esegui la query usando il reasoner e verifica che il risultato sia true
        boolean result = reasoner.isSubsumptionSatisfied(subclassAxiom);
        assertTrue(result, "Test15 failed: ∃r2.(C ⊓ D) ⊑ S should return true");
    }

    @Test
    void test16() {
        // Definisci le classi C, D, E, F e la proprietà r2, r4
        OWLClass classC = factory.getOWLClass(IRI.create("http://myOntology.com#C"));
        OWLClass classD = factory.getOWLClass(IRI.create("http://myOntology.com#D"));
        OWLClass classE = factory.getOWLClass(IRI.create("http://myOntology.com#E"));
        OWLClass classF = factory.getOWLClass(IRI.create("http://myOntology.com#F"));
        OWLObjectProperty propertyR2 = factory.getOWLObjectProperty(IRI.create("http://myOntology.com#r2"));
        OWLObjectProperty propertyR4 = factory.getOWLObjectProperty(IRI.create("http://myOntology.com#r4"));

        // Crea l'intersezione C ⊓ D
        OWLObjectIntersectionOf intersectionCD = factory.getOWLObjectIntersectionOf(classC, classD);

        // Crea la restrizione ∃r2.(C ⊓ D)
        OWLObjectSomeValuesFrom restrictionR2_CD = factory.getOWLObjectSomeValuesFrom(propertyR2, intersectionCD);

        // Crea l'intersezione E ⊓ F
        OWLObjectIntersectionOf intersectionEF = factory.getOWLObjectIntersectionOf(classE, classF);

        // Crea la restrizione ∃r4.(E ⊓ F)
        OWLObjectSomeValuesFrom restrictionR4_EF = factory.getOWLObjectSomeValuesFrom(propertyR4, intersectionEF);

        // Crea l'assioma di sottoclasse: ∃r2.(C ⊓ D) ⊑ ∃r4.(E ⊓ F)
        OWLSubClassOfAxiom subclassAxiom = factory.getOWLSubClassOfAxiom(restrictionR2_CD, restrictionR4_EF);

        // Esegui la query usando il reasoner e verifica che il risultato sia true
        boolean result = reasoner.isSubsumptionSatisfied(subclassAxiom);
        assertTrue(result, "Test16 failed: ∃r2.(C ⊓ D) ⊑ ∃r4.(E ⊓ F) should return true");
    }

    @Test
    void test17() {
        // Definisci le classi C, D, E, F e la proprietà r3, r4
        OWLClass classC = factory.getOWLClass(IRI.create("http://myOntology.com#C"));
        OWLClass classD = factory.getOWLClass(IRI.create("http://myOntology.com#D"));
        OWLClass classE = factory.getOWLClass(IRI.create("http://myOntology.com#E"));
        OWLClass classF = factory.getOWLClass(IRI.create("http://myOntology.com#F"));
        OWLObjectProperty propertyR3 = factory.getOWLObjectProperty(IRI.create("http://myOntology.com#r3"));
        OWLObjectProperty propertyR4 = factory.getOWLObjectProperty(IRI.create("http://myOntology.com#r4"));

        // Crea l'intersezione C ⊓ D
        OWLObjectIntersectionOf intersectionCD = factory.getOWLObjectIntersectionOf(classC, classD);

        // Crea la restrizione ∃r3.(C ⊓ D)
        OWLObjectSomeValuesFrom restrictionR3_CD = factory.getOWLObjectSomeValuesFrom(propertyR3, intersectionCD);

        // Crea l'intersezione E ⊓ F
        OWLObjectIntersectionOf intersectionEF = factory.getOWLObjectIntersectionOf(classE, classF);

        // Crea la restrizione ∃r4.(E ⊓ F)
        OWLObjectSomeValuesFrom restrictionR4_EF = factory.getOWLObjectSomeValuesFrom(propertyR4, intersectionEF);

        // Crea l'assioma di sottoclasse: ∃r3.(C ⊓ D) ⊑ ∃r4.(E ⊓ F)
        OWLSubClassOfAxiom subclassAxiom = factory.getOWLSubClassOfAxiom(restrictionR3_CD, restrictionR4_EF);

        // Esegui la query usando il reasoner e verifica che il risultato sia false
        boolean result = reasoner.isSubsumptionSatisfied(subclassAxiom);
        assertFalse(result, "Test17 failed: ∃r3.(C ⊓ D) ⊑ ∃r4.(E ⊓ F) should return false");
    }

    @Test
    void test18() {
        // Definisci le classi A, D, E, F e le proprietà r2, r4
        OWLClass classA = factory.getOWLClass(IRI.create("http://myOntology.com#A"));
        OWLClass classD = factory.getOWLClass(IRI.create("http://myOntology.com#D"));
        OWLClass classE = factory.getOWLClass(IRI.create("http://myOntology.com#E"));
        OWLClass classF = factory.getOWLClass(IRI.create("http://myOntology.com#F"));
        OWLObjectProperty propertyR2 = factory.getOWLObjectProperty(IRI.create("http://myOntology.com#r2"));
        OWLObjectProperty propertyR4 = factory.getOWLObjectProperty(IRI.create("http://myOntology.com#r4"));

        // Crea l'intersezione A ⊓ D
        OWLObjectIntersectionOf intersectionAD = factory.getOWLObjectIntersectionOf(classA, classD);

        // Crea la restrizione ∃r2.(A ⊓ D)
        OWLObjectSomeValuesFrom restrictionR2_AD = factory.getOWLObjectSomeValuesFrom(propertyR2, intersectionAD);

        // Crea l'intersezione E ⊓ F
        OWLObjectIntersectionOf intersectionEF = factory.getOWLObjectIntersectionOf(classE, classF);

        // Crea la restrizione ∃r4.(E ⊓ F)
        OWLObjectSomeValuesFrom restrictionR4_EF = factory.getOWLObjectSomeValuesFrom(propertyR4, intersectionEF);

        // Crea l'assioma di sottoclasse: ∃r2.(A ⊓ D) ⊑ ∃r4.(E ⊓ F)
        OWLSubClassOfAxiom subclassAxiom = factory.getOWLSubClassOfAxiom(restrictionR2_AD, restrictionR4_EF);

        // Esegui la query usando il reasoner e verifica che il risultato sia false
        boolean result = reasoner.isSubsumptionSatisfied(subclassAxiom);
        assertFalse(result, "Test18 failed: ∃r2.(A ⊓ D) ⊑ ∃r4.(E ⊓ F) should return false");
    }

    @Test
    void test19() {
        // Definisci le classi C, D, e E utilizzando l'IRI appropriato
        OWLClass classC = factory.getOWLClass(IRI.create("http://myOntology.com#C"));
        OWLClass classD = factory.getOWLClass(IRI.create("http://myOntology.com#D"));
        OWLClass classE = factory.getOWLClass(IRI.create("http://myOntology.com#E"));

        // Definisci le proprietà oggetto r2 e r4
        OWLObjectProperty propertyR2 = factory.getOWLObjectProperty(IRI.create("http://myOntology.com#r2"));
        OWLObjectProperty propertyR4 = factory.getOWLObjectProperty(IRI.create("http://myOntology.com#r4"));

        // Crea l'intersezione C ⊓ D
        OWLObjectIntersectionOf intersectionCD = factory.getOWLObjectIntersectionOf(classC, classD);

        // Costruisci \exists r2.(C ⊓ D)
        OWLClassExpression existsR2_CD = factory.getOWLObjectSomeValuesFrom(propertyR2, intersectionCD);

        // Costruisci \exists r4.(E)
        OWLClassExpression existsR4_E = factory.getOWLObjectSomeValuesFrom(propertyR4, classE);

        // Crea l'assioma della query: \exists r2.(C ⊓ D) ⊑ \exists r4.(E)
        OWLSubClassOfAxiom queryAxiom = factory.getOWLSubClassOfAxiom(existsR2_CD, existsR4_E);

        // Esegui la query usando il reasoner e verifica che il risultato sia true
        boolean result = reasoner.isSubsumptionSatisfied(queryAxiom);
        assertTrue(result, "Test19 failed: \\exists r2.(C ⊓ D) ⊑ \\exists r4.(E) dovrebbe restituire true");
    }

    @Test
    void test20() {
        // Definisci le classi D, E, B e le proprietà r2, r5
        OWLClass classD = factory.getOWLClass(IRI.create("http://myOntology.com#D"));
        OWLClass classE = factory.getOWLClass(IRI.create("http://myOntology.com#E"));
        OWLClass classB = factory.getOWLClass(IRI.create("http://myOntology.com#B"));
        OWLObjectProperty propertyR2 = factory.getOWLObjectProperty(IRI.create("http://myOntology.com#r2"));
        OWLObjectProperty propertyR5 = factory.getOWLObjectProperty(IRI.create("http://myOntology.com#r5"));

        // Crea l'intersezione D ⊓ E
        OWLObjectIntersectionOf intersectionDE = factory.getOWLObjectIntersectionOf(classD, classE);

        // Crea la restrizione ∃r2.(D ⊓ E)
        OWLObjectSomeValuesFrom restrictionR2_DE = factory.getOWLObjectSomeValuesFrom(propertyR2, intersectionDE);

        // Crea la restrizione ∃r5.(∃r2.(D ⊓ E))
        OWLObjectSomeValuesFrom restrictionR5_R2_DE = factory.getOWLObjectSomeValuesFrom(propertyR5, restrictionR2_DE);

        // Crea l'assioma di sottoclasse: ∃r5.(∃r2.(D ⊓ E)) ⊑ B
        OWLSubClassOfAxiom subclassAxiom = factory.getOWLSubClassOfAxiom(restrictionR5_R2_DE, classB);

        // Esegui la query usando il reasoner e verifica che il risultato sia false
        boolean result = reasoner.isSubsumptionSatisfied(subclassAxiom);
        assertFalse(result, "Test20 failed: ∃r5.(∃r2.(D ⊓ E)) ⊑ B should return false");
    }

    @Test
    void test21() {
        // Definisci le classi A, B, E e le proprietà r1, r2
        OWLClass classA = factory.getOWLClass(IRI.create("http://myOntology.com#A"));
        OWLClass classB = factory.getOWLClass(IRI.create("http://myOntology.com#B"));
        OWLClass classE = factory.getOWLClass(IRI.create("http://myOntology.com#E"));
        OWLObjectProperty propertyR1 = factory.getOWLObjectProperty(IRI.create("http://myOntology.com#r1"));
        OWLObjectProperty propertyR2 = factory.getOWLObjectProperty(IRI.create("http://myOntology.com#r2"));

        // Crea l'intersezione A ⊓ B
        OWLObjectIntersectionOf intersectionAB = factory.getOWLObjectIntersectionOf(classA, classB);

        // Crea la restrizione ∃r2.(A ⊓ B)
        OWLObjectSomeValuesFrom restrictionR2_AB = factory.getOWLObjectSomeValuesFrom(propertyR2, intersectionAB);

        // Crea la restrizione ∃r1.(∃r2.(A ⊓ B))
        OWLObjectSomeValuesFrom restrictionR1_R2_AB = factory.getOWLObjectSomeValuesFrom(propertyR1, restrictionR2_AB);

        // Crea l'assioma di sottoclasse: ∃r1.(∃r2.(A ⊓ B)) ⊑ E
        OWLSubClassOfAxiom subclassAxiom = factory.getOWLSubClassOfAxiom(restrictionR1_R2_AB, classE);

        // Esegui la query usando il reasoner e verifica che il risultato sia true
        boolean result = reasoner.isSubsumptionSatisfied(subclassAxiom);
        assertTrue(result, "Test21 failed: ∃r1.(∃r2.(A ⊓ B)) ⊑ E should return true");
    }

    @Test
    void test22() {
        // Definisci le classi D, E, P, O e le proprietà r5, r2
        OWLClass classD = factory.getOWLClass(IRI.create("http://myOntology.com#D"));
        OWLClass classE = factory.getOWLClass(IRI.create("http://myOntology.com#E"));
        OWLClass classP = factory.getOWLClass(IRI.create("http://myOntology.com#P"));
        OWLClass classO = factory.getOWLClass(IRI.create("http://myOntology.com#O"));
        OWLObjectProperty propertyR5 = factory.getOWLObjectProperty(IRI.create("http://myOntology.com#r5"));
        OWLObjectProperty propertyR2 = factory.getOWLObjectProperty(IRI.create("http://myOntology.com#r2"));

        // Crea l'intersezione D ⊓ E
        OWLObjectIntersectionOf intersectionDE = factory.getOWLObjectIntersectionOf(classD, classE);

        // Crea la restrizione ∃r2.(D ⊓ E)
        OWLObjectSomeValuesFrom restrictionR2_DE = factory.getOWLObjectSomeValuesFrom(propertyR2, intersectionDE);

        // Crea la restrizione ∃r5.(∃r2.(D ⊓ E))
        OWLObjectSomeValuesFrom restrictionR5_R2_DE = factory.getOWLObjectSomeValuesFrom(propertyR5, restrictionR2_DE);

        // Crea l'intersezione P ⊓ O
        OWLObjectIntersectionOf intersectionPO = factory.getOWLObjectIntersectionOf(classP, classO);

        // Crea l'assioma di sottoclasse: ∃r5.(∃r2.(D ⊓ E)) ⊑ P ⊓ O
        OWLSubClassOfAxiom subclassAxiom = factory.getOWLSubClassOfAxiom(restrictionR5_R2_DE, intersectionPO);

        // Esegui la query usando il reasoner e verifica che il risultato sia true
        boolean result = reasoner.isSubsumptionSatisfied(subclassAxiom);
        assertTrue(result, "Test22 failed: ∃r5.(∃r2.(D ⊓ E)) ⊑ P ⊓ O should return true");
    }

    @Test
    void test23() {
        // Definisci le classi U, V, T e le proprietà r1, r2
        OWLClass classU = factory.getOWLClass(IRI.create("http://myOntology.com#U"));
        OWLClass classV = factory.getOWLClass(IRI.create("http://myOntology.com#V"));
        OWLClass classT = factory.getOWLClass(IRI.create("http://myOntology.com#T"));
        OWLObjectProperty propertyR1 = factory.getOWLObjectProperty(IRI.create("http://myOntology.com#r1"));
        OWLObjectProperty propertyR2 = factory.getOWLObjectProperty(IRI.create("http://myOntology.com#r2"));

        // Crea l'intersezione U ⊓ V
        OWLObjectIntersectionOf intersectionUV = factory.getOWLObjectIntersectionOf(classU, classV);

        // Crea la restrizione R1.(U ⊓ V)
        OWLObjectSomeValuesFrom restrictionR1_UV = factory.getOWLObjectSomeValuesFrom(propertyR1, intersectionUV);

        // Crea la restrizione ∃r2.T
        OWLObjectSomeValuesFrom restrictionR2_T = factory.getOWLObjectSomeValuesFrom(propertyR2, classT);

        // Crea l'assioma di sottoclasse: R1.(U ⊓ V) ⊑ ∃r2.T
        OWLSubClassOfAxiom subclassAxiom = factory.getOWLSubClassOfAxiom(restrictionR1_UV, restrictionR2_T);

        // Esegui la query usando il reasoner e verifica che il risultato sia true
        boolean result = reasoner.isSubsumptionSatisfied(subclassAxiom);
        assertTrue(result, "Test23 failed: R1.(U ⊓ V) ⊑ ∃r2.T should return true");
    }

    @Test
    void test24() {
        // Definisci le classi U, V, T e le proprietà r1, r3
        OWLClass classU = factory.getOWLClass(IRI.create("http://myOntology.com#U"));
        OWLClass classV = factory.getOWLClass(IRI.create("http://myOntology.com#V"));
        OWLClass classT = factory.getOWLClass(IRI.create("http://myOntology.com#T"));
        OWLObjectProperty propertyR1 = factory.getOWLObjectProperty(IRI.create("http://myOntology.com#r1"));
        OWLObjectProperty propertyR3 = factory.getOWLObjectProperty(IRI.create("http://myOntology.com#r3"));

        // Crea l'intersezione U ⊓ V
        OWLObjectIntersectionOf intersectionUV = factory.getOWLObjectIntersectionOf(classU, classV);

        // Crea la restrizione R1.(U ⊓ V)
        OWLObjectSomeValuesFrom restrictionR1_UV = factory.getOWLObjectSomeValuesFrom(propertyR1, intersectionUV);

        // Crea la restrizione ∃r3.T
        OWLObjectSomeValuesFrom restrictionR3_T = factory.getOWLObjectSomeValuesFrom(propertyR3, classT);

        // Crea l'assioma di sottoclasse: R1.(U ⊓ V) ⊑ ∃r3.T
        OWLSubClassOfAxiom subclassAxiom = factory.getOWLSubClassOfAxiom(restrictionR1_UV, restrictionR3_T);

        // Esegui la query usando il reasoner e verifica che il risultato sia true
        boolean result = reasoner.isSubsumptionSatisfied(subclassAxiom);
        assertTrue(result, "Test24 failed: R1.(U ⊓ V) ⊑ ∃r3.T should return true");
    }

    @Test
    void test25() {
        // Definisci le classi U, V, T e le proprietà r1, r4
        OWLClass classU = factory.getOWLClass(IRI.create("http://myOntology.com#U"));
        OWLClass classV = factory.getOWLClass(IRI.create("http://myOntology.com#V"));
        OWLClass classT = factory.getOWLClass(IRI.create("http://myOntology.com#T"));
        OWLObjectProperty propertyR1 = factory.getOWLObjectProperty(IRI.create("http://myOntology.com#r1"));
        OWLObjectProperty propertyR4 = factory.getOWLObjectProperty(IRI.create("http://myOntology.com#r4"));

        // Crea l'intersezione U ⊓ V
        OWLObjectIntersectionOf intersectionUV = factory.getOWLObjectIntersectionOf(classU, classV);

        // Crea la restrizione R1.(U ⊓ V)
        OWLObjectSomeValuesFrom restrictionR1_UV = factory.getOWLObjectSomeValuesFrom(propertyR1, intersectionUV);

        // Crea la restrizione ∃r4.T
        OWLObjectSomeValuesFrom restrictionR4_T = factory.getOWLObjectSomeValuesFrom(propertyR4, classT);

        // Crea l'assioma di sottoclasse: R1.(U ⊓ V) ⊑ ∃r4.T
        OWLSubClassOfAxiom subclassAxiom = factory.getOWLSubClassOfAxiom(restrictionR1_UV, restrictionR4_T);

        // Esegui la query usando il reasoner e verifica che il risultato sia true
        boolean result = reasoner.isSubsumptionSatisfied(subclassAxiom);
        assertFalse(result, "Test25 failed: R1.(U ⊓ V) ⊑ ∃r4.T should return true");
    }
}

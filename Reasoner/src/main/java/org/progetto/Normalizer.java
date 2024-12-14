package org.progetto;

import java.util.AbstractMap.SimpleEntry;
import java.util.*;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;

public class Normalizer {
    private final OWLDataFactory dataFactory;
    private static int auxClassCounter = 0;

    // Costruttore senza parametri
    public Normalizer() {
        this.dataFactory = OWLManager.createOWLOntologyManager().getOWLDataFactory();
    }

    public Set<OWLSubClassOfAxiom> normalizeAxioms(Set<OWLAxiom> axioms) {
        Set<OWLSubClassOfAxiom> normalizedAxioms = new HashSet<>();
        for (OWLAxiom axiom : axioms) { // Itera su ogni assioma nel set di input
            if (axiom instanceof OWLSubClassOfAxiom subClassAxiom) {
                OWLClassExpression subclassExpression = subClassAxiom.getSubClass(); // Ottiene l'espressione della sottoclasse dall'assioma
                OWLClassExpression superclassExpression = subClassAxiom.getSuperClass(); // Ottiene l'espressione della superclasse dall'assioma

                SimpleEntry<Set<OWLSubClassOfAxiom>, OWLClassExpression> normalizedSubclass = normalizeClassExpression(subclassExpression, false); // Normalizza l'espressione della sottoclasse; il secondo parametro (false) indica che questa non è una superclasse
                SimpleEntry<Set<OWLSubClassOfAxiom>, OWLClassExpression> normalizedSuperclass = normalizeClassExpression(superclassExpression, true); // Normalizza l'espressione della superclasse; il secondo parametro (true) indica che questa è una superclasse

                normalizedAxioms.addAll(normalizedSubclass.getKey()); // Aggiunge gli assiomi normalizzati della sottoclasse al set finale
                normalizedAxioms.addAll(normalizedSuperclass.getKey()); // Aggiunge gli assiomi normalizzati della superclasse al set finale

                if ((normalizedSubclass.getValue() instanceof OWLObjectSomeValuesFrom  || normalizedSubclass.getValue() instanceof OWLObjectIntersectionOf) && normalizedSuperclass.getValue() instanceof OWLObjectSomeValuesFrom) { // Se la sottoclasse è un'espressione complessa come OWLObjectSomeValuesFrom o OWLObjectIntersectionOf e la superclasse è un OWLObjectSomeValuesFrom, riduce la sottoclasse a una classe temporanea
                    normalizedSubclass = reduceExpressionToAuxiliaryClass(normalizedSubclass.getValue()); // Riduce l'espressione complessa della sottoclasse in una classe temporanea
                    normalizedAxioms.addAll(normalizedSubclass.getKey()); // Aggiunge gli assiomi derivanti dalla riduzione al set finale
                }
                OWLSubClassOfAxiom finalNormalizedAxiom = this.dataFactory.getOWLSubClassOfAxiom(normalizedSubclass.getValue(), normalizedSuperclass.getValue()); // Dopo aver normalizzato sia la sottoclasse che la superclasse, crea un nuovo assioma del tipo: sottoclasse ⊑ superclasse. Se la sottoclasse è stata ridotta ad una classe temporanea, questa verrà usata qui.
                normalizedAxioms.add(finalNormalizedAxiom); // Aggiunge l'assioma normalizzato al set finale
            }
            else
                throw new IllegalArgumentException("The axiom is not of type OWLSubClassOfAxiom");
        }
        return normalizedAxioms;
    }


    private SimpleEntry<Set<OWLSubClassOfAxiom>, OWLClassExpression> normalizeClassExpression(OWLClassExpression classExpression, boolean isSuperclass) {
        Set<OWLSubClassOfAxiom> axiomSet = new HashSet<>();
        SimpleEntry<Set<OWLSubClassOfAxiom>, OWLClassExpression> result; //Una variabile per memorizzare il risultato finale, che includerà il set di assiomi e l'espressione normalizzata.

        ClassExpressionType expressionType = classExpression.getClassExpressionType();

        if (expressionType.equals(ClassExpressionType.OWL_CLASS) || expressionType.equals(ClassExpressionType.OBJECT_ONE_OF))
            result = new SimpleEntry<>(axiomSet, classExpression);
        else if (expressionType.equals(ClassExpressionType.OBJECT_INTERSECTION_OF)) {
            OWLObjectIntersectionOf intersectionExpression = (OWLObjectIntersectionOf) classExpression; //Converte l'espressione in un oggetto di tipo OWLObjectIntersectionOf per accedere agli operandi.
            SimpleEntry<Set<OWLSubClassOfAxiom>, OWLClassExpression> tempResult = normalizeIntersection(intersectionExpression); //Chiama: Il metodo normalizeIntersection per normalizzare l'intersezione. Restituisce: - nuovi assiomi creati dalla normalizzazione; - la versione normalizzata dell'intersezione.
            if (isSuperclass) {
                result = reduceExpressionToAuxiliaryClass(tempResult.getValue()); //Converte l'espressione normalizzata in una classe temporanea per semplificarla.
                result.getKey().addAll(tempResult.getKey()); //Unisce i nuovi assiomi generati durante la normalizzazione dell'intersezione al risultato.
            }
            else
                result = tempResult; //Se l'intersezione non è una superclasse, utilizza direttamente il risultato della normalizzazione.
        }
        else if (expressionType.equals(ClassExpressionType.OBJECT_SOME_VALUES_FROM)) {
            OWLObjectSomeValuesFrom someValuesExpression = (OWLObjectSomeValuesFrom) classExpression; //Se l'espressione è una restrizione esistenziale, converte l'espressione in un oggetto OWLObjectSomeValuesFrom.
            result = normalizeExistentialRestriction(someValuesExpression); //Chiama il metodo normalizeExistentialRestriction per elaborare la restrizione esistenziale.
        }
        else {
            System.out.println("Unsupported expression type: " + expressionType);
            System.out.println(classExpression);
            result = new SimpleEntry<>(axiomSet, classExpression);
        }
        return result;
    }

    //Metodo che normalizza un'espressione di intersezione OWL (A ⊓ B ⊓ ... ⊓ N). Se gli operandi includono restrizioni esistenziali o sono troppo numerosi, utilizza classi ausiliarie per semplificare le espressioni e creare assiomi equivalenti.
    private SimpleEntry<Set<OWLSubClassOfAxiom>, OWLClassExpression> normalizeIntersection(OWLObjectIntersectionOf intersectionExpression) {
        ArrayList<OWLClassExpression> operands = new ArrayList<>(intersectionExpression.getOperandsAsList()); // Ottiene gli operandi dell'intersezione come una lista di espressioni.
        int operandCount = operands.size();

        List<OWLClassExpression> auxiliaryClasses = new ArrayList<>(); // Lista che raccoglierà classi ausiliarie generate durante la normalizzazione.

        Set<OWLSubClassOfAxiom> normalizedAxioms = new HashSet<>();

        if (operandCount == 2) {  // Caso semplice: intersezione con esattamente due operandi. Si normalizza ciascun operando.
            for (int i = 0; i < 2; i++) {
                if (operands.get(i).getClassExpressionType().equals(ClassExpressionType.OBJECT_SOME_VALUES_FROM)) { // Se un operando è una restrizione esistenziale, lo normalizza in una classe ausiliaria.
                    SimpleEntry<Set<OWLSubClassOfAxiom>, OWLClassExpression> tempResult = convertExistentialToClass((OWLObjectSomeValuesFrom) operands.get(i));
                    normalizedAxioms.addAll(tempResult.getKey()); // Aggiunge gli assiomi generati al set normalizedAxioms.
                    operands.set(i, tempResult.getValue()); // Aggiorna l'operando con la versione normalizzata.
                }
            }
            OWLObjectIntersectionOf newIntersection = this.dataFactory.getOWLObjectIntersectionOf(operands.get(0), operands.get(1)); // Crea una nuova intersezione con i due operandi (normalizzati se necessario).
            return new SimpleEntry<>(normalizedAxioms, newIntersection);
        }

        // Caso generale: intersezione con più di due operandi.
        for (int i = 0; i < operandCount; i++) {
            if (operands.get(i).getClassExpressionType().equals(ClassExpressionType.OBJECT_SOME_VALUES_FROM)) { // Normalizza operandi che sono restrizioni esistenziali.
                SimpleEntry<Set<OWLSubClassOfAxiom>, OWLClassExpression> tempResult = convertExistentialToClass((OWLObjectSomeValuesFrom) operands.get(i));
                normalizedAxioms.addAll(tempResult.getKey());
                operands.set(i, tempResult.getValue());
            }

            if (i % 2 != 0) { // Ogni coppia di operandi adiacenti viene ridotta ad una classe ausiliaria.
                OWLClass auxClass = createAuxiliaryClass(); // Per ogni coppia di operandi, si crea una classe temporanea per rappresentare l'intersezione,
                auxiliaryClasses.add(auxClass); // Si aggiunge la classe temporanea alla lista auxiliaryClasses
                normalizedAxioms.addAll(intersectionToAuxiliaryClass(operands.get(i - 1), operands.get(i), auxClass)); // Si generano assiomi che collegano la classe ausiliaria ai due operandi.
            }
        }

        if (operandCount % 2 != 0) // Se il numero di operandi è dispari, aggiunge l'ultimo operando direttamente.
            auxiliaryClasses.add(operands.get(operandCount - 1));

        // Ricorsivamente, si normalizza l'intersezione delle classi ausiliarie generate.
        OWLObjectIntersectionOf recursiveIntersection = this.dataFactory.getOWLObjectIntersectionOf(auxiliaryClasses);
        SimpleEntry<Set<OWLSubClassOfAxiom>, OWLClassExpression> recursiveResult = normalizeIntersection(recursiveIntersection);
        normalizedAxioms.addAll(recursiveResult.getKey());

        return new SimpleEntry<>(normalizedAxioms, recursiveResult.getValue());
    }

    private SimpleEntry<Set<OWLSubClassOfAxiom>, OWLClassExpression> convertExistentialToClass(OWLObjectSomeValuesFrom someValuesExpression) {
        SimpleEntry<Set<OWLSubClassOfAxiom>, OWLClassExpression> tempResult;

        tempResult = normalizeExistentialRestriction(someValuesExpression); // Si normalizza l'esistenziale
        Set<OWLSubClassOfAxiom> normalizedAxioms = new HashSet<>(tempResult.getKey()); // Estrae il set di assiomi generati durante la normalizzazione dell'esistenziale.

        tempResult = reduceExpressionToAuxiliaryClass(tempResult.getValue()); // Riduce ulteriormente l'espressione normalizzata ad una classe ausiliaria (se necessario), aggiungendo nuovi assiomi per mantenere l'equivalenza semantica tra l'espressione originale e la classe temporanea.
        normalizedAxioms.addAll(tempResult.getKey());

        return new SimpleEntry<>(normalizedAxioms, tempResult.getValue());
    }

    private OWLClass createAuxiliaryClass() {
        OWLClass auxClass = this.dataFactory.getOWLClass(IRI.create("#AUX" + auxClassCounter));
        auxClassCounter++;
        return auxClass;
    }

    // Normalizza una restrizione esistenziale del tipo ∃R.C.
    private SimpleEntry<Set<OWLSubClassOfAxiom>, OWLClassExpression> normalizeExistentialRestriction(OWLObjectSomeValuesFrom someValuesExpression) {
        OWLObjectPropertyExpression property = someValuesExpression.getProperty(); // Ottiene il ruolo R dell'espressione esistenziale
        OWLClassExpression filler = someValuesExpression.getFiller(); // Ottiene il riempitivo (filler) della restrizione esistenziale (es. C in ∃R.C)

        Set<OWLSubClassOfAxiom> axioms = new HashSet<>(); // Inizializza un set vuoto per raccogliere gli assiomi generati durante la normalizzazione

        // Caso 1: Se il riempitivo è una classe semplice o un individuo, non è necessario normalizzarlo. Si restituisce l'espressione originale senza modifiche.
        if (filler.getClassExpressionType().equals(ClassExpressionType.OWL_CLASS) || filler.getClassExpressionType().equals(ClassExpressionType.OBJECT_ONE_OF))
            return new SimpleEntry<>(axioms, someValuesExpression);

        // Caso 2: Il riempitivo è un'intersezione
        if (filler.getClassExpressionType().equals(ClassExpressionType.OBJECT_INTERSECTION_OF)) {
            SimpleEntry<Set<OWLSubClassOfAxiom>, OWLClassExpression> tempResult = normalizeIntersection((OWLObjectIntersectionOf) filler); // Normalizza l'intersezione
            SimpleEntry<Set<OWLSubClassOfAxiom>, OWLClassExpression> reducedResult = reduceExpressionToAuxiliaryClass(tempResult.getValue()); // Riduce l'intersezione normalizzata ad una classe temporanea
            reducedResult.getKey().addAll(tempResult.getKey()); // Aggiunge al set di assiomi quelli generati durante la normalizzazione e la riduzione
            OWLObjectSomeValuesFrom normalizedSomeValues = this.dataFactory.getOWLObjectSomeValuesFrom(property, reducedResult.getValue()); // Crea una nuova restrizione esistenziale utilizzando la classe temporanea come riempitivo
            return new SimpleEntry<>(reducedResult.getKey(), normalizedSomeValues); // Restituisce la coppia: assiomi generati + espressione normalizzata
        }

        // Caso 3: Se il riempitivo è un'altra restrizione esistenziale
        if (filler.getClassExpressionType().equals(ClassExpressionType.OBJECT_SOME_VALUES_FROM)) {
            SimpleEntry<Set<OWLSubClassOfAxiom>, OWLClassExpression> tempResult = normalizeExistentialRestriction((OWLObjectSomeValuesFrom) filler); // Normalizza ricorsivamente la restrizione esistenziale interna

            OWLClass auxClass = createAuxiliaryClass(); // Crea una nuova classe ausiliaria per rappresentare la restrizione interna

            axioms.addAll(tempResult.getKey()); // Aggiunge al set di assiomi quelli generati durante la normalizzazione ricorsiva

            // Crea due assiomi che stabiliscono una relazione di equivalenza tra la classe temporanea e l'espressione normalizzata
            axioms.add(this.dataFactory.getOWLSubClassOfAxiom(auxClass, tempResult.getValue()));
            axioms.add(this.dataFactory.getOWLSubClassOfAxiom(tempResult.getValue(), auxClass));

            OWLObjectSomeValuesFrom newSomeValues = this.dataFactory.getOWLObjectSomeValuesFrom(property, auxClass); // Crea una nuova restrizione esistenziale con la classe temporanea come riempitivo

            return new SimpleEntry<>(axioms, newSomeValues); // Restituisce la coppia: assiomi generati + espressione normalizzata
        }

        return new SimpleEntry<>(axioms, someValuesExpression); // Caso di fallback: se il riempitivo non corrisponde a nessuno dei tipi gestiti sopra, restituisce l'espressione originale senza modifiche e senza generare nuovi assiomi
    }

    private SimpleEntry<Set<OWLSubClassOfAxiom>, OWLClassExpression> reduceExpressionToAuxiliaryClass(OWLClassExpression expression) {
        OWLClass auxClass = createAuxiliaryClass(); // Questa classe rappresenterà l'espressione ridotta.
        ArrayList<OWLClassExpression> expressions; // Sarà utilizzata per memorizzare gli operandi di un'intersezione, se l'espressione fornita è un'intersezione.
        SimpleEntry<Set<OWLSubClassOfAxiom>, OWLClassExpression> result = null; //Restituisce una collezione di assiomi e la classe ausiliaria rappresentante l'espressione ridotta

        if (expression.getClassExpressionType().equals(ClassExpressionType.OBJECT_INTERSECTION_OF)) { // Se l'espressione è un'intersezione
            OWLObjectIntersectionOf intersectionExpression = (OWLObjectIntersectionOf) expression; // Casting per accedere agli operandi dell'intersezione
            expressions = new ArrayList<>(intersectionExpression.getOperandsAsList()); // Ottiene i singoli operandi dell'intersezione e li memorizza in una lista
            result = new SimpleEntry<>(intersectionToAuxiliaryClass(expressions.get(0), expressions.get(1), auxClass), auxClass); // Passa i due operandi e la classe ausiliaria al metodo normalizeSingleIntersection, che genera un set di assiomi che collegano la classe ausiliaria ai due operandi. Crea una coppia con gli assiomi generati e la classe ausiliaria
        }
        else if (expression.getClassExpressionType().equals(ClassExpressionType.OBJECT_SOME_VALUES_FROM)) { // Se l'espressione è una restrizione esistenziale
            OWLObjectSomeValuesFrom someValuesExpression = (OWLObjectSomeValuesFrom) expression; // Casting per accedere a ruolo e riempitivo
            result = new SimpleEntry<>(existentialToAuxiliaryClass(someValuesExpression, auxClass), auxClass); // Passa la restrizione esistenziale e la classe ausiliaria al metodo normalizeExistentialRestrictionAsSingleAxiom, che crea due assiomi che rappresentano la relazione bidirezionale tra l'esistenziale e la classe ausiliaria. Crea una coppia con gli assiomi generati e la classe ausiliaria
        }

        return result;
    }

    private Set<OWLSubClassOfAxiom> existentialToAuxiliaryClass(OWLObjectSomeValuesFrom someValuesExpression, OWLClass auxClass) {
        Set<OWLSubClassOfAxiom> axioms = new HashSet<>();
        OWLSubClassOfAxiom axiom1 = this.dataFactory.getOWLSubClassOfAxiom(auxClass, someValuesExpression); // Crea il primo assioma: auxClass ⊑ existential
        OWLSubClassOfAxiom axiom2 = this.dataFactory.getOWLSubClassOfAxiom(someValuesExpression, auxClass); // Crea il secondo assioma: existential ⊑ auxClass
        axioms.add(axiom1);
        axioms.add(axiom2);
        return axioms;
    }

    private Set<OWLSubClassOfAxiom> intersectionToAuxiliaryClass(OWLClassExpression firstOperand, OWLClassExpression secondOperand, OWLClass auxClass) {
        Set<OWLSubClassOfAxiom> axioms = new HashSet<>();
        OWLSubClassOfAxiom axiom1 = this.dataFactory.getOWLSubClassOfAxiom(auxClass, firstOperand); // auxClass ⊑ A
        OWLSubClassOfAxiom axiom2 = this.dataFactory.getOWLSubClassOfAxiom(auxClass, secondOperand); // auxClass ⊑ B
        OWLObjectIntersectionOf combinedIntersection = this.dataFactory.getOWLObjectIntersectionOf(firstOperand, secondOperand);
        OWLSubClassOfAxiom axiom3 = this.dataFactory.getOWLSubClassOfAxiom(combinedIntersection, auxClass); // A ⊓ B ⊑ auxClass
        axioms.add(axiom1);
        axioms.add(axiom2);
        axioms.add(axiom3);
        return axioms;
    }
}

package org.progetto;

import org.jgrapht.graph.*;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.*;
import java.util.AbstractMap.SimpleEntry;
import org.semanticweb.owlapi.model.parameters.Imports;

import java.util.*;
import java.util.stream.Collectors;

public class Reasoner {
    private OWLOntology ontology;
    private final OWLDataFactory dataFactory;
    private Map<OWLClassExpression, Set<OWLClassExpression>> subSumptionMap;
    private Map<OWLObjectPropertyExpression, Set<SimpleEntry<OWLClassExpression, OWLClassExpression>>> relationships;

    public Reasoner(OWLOntology ontology) {
        this.ontology = ontology;
        this.dataFactory = OWLManager.createOWLOntologyManager().getOWLDataFactory();
        this.subSumptionMap = new HashMap<>();
        this.relationships = new HashMap<>();
    }

    public boolean isSubsumptionSatisfied(OWLAxiom owlAxiom) {
        Normalizer normalizer = new Normalizer();
        if (!(owlAxiom instanceof OWLSubClassOfAxiom queryAxiom))
            throw new IllegalArgumentException("The axiom is not of type OWLSubClassOfAxiom");

        OWLClassExpression subclassExpression = queryAxiom.getSubClass(); // Estrae la sottoclasse dall'assioma
        OWLClassExpression superclassExpression = queryAxiom.getSuperClass(); // Estrae la superclasse dall'assioma

        Set<OWLAxiom> helperAxioms = createInferenceHelperClasses(subclassExpression, superclassExpression); // Crea assiomi di supporto che mappano la sottoclasse e la superclasse su #InferenceHelperClass1 e #InferenceHelperClass2

        Set<OWLSubClassOfAxiom> normalizedAxioms = new HashSet<>(); // Inizializza un set vuoto per raccogliere assiomi normalizzati
        normalizedAxioms.addAll(normalizer.normalizeAxioms(this.ontology.getTBoxAxioms(Imports.EXCLUDED)));  // Aggiunge a normalizedAxioms gli assiomi dell'ontologia, normalizzati tramite il Normalizer
        normalizedAxioms.addAll(normalizer.normalizeAxioms(helperAxioms));  // Aggiunge al set anche gli assiomi di supporto (normalizzati tramite il Normalizer).

        initializeClassMappings(normalizedAxioms); // Inizializza le mappature di sottoclasse basandosi sugli assiomi normalizzati

        applyCompletionRules(normalizedAxioms); // Applica le regole di completamento per inferire nuove relazioni di sottoclasse

        return subSumptionMap.get(dataFactory.getOWLClass("#InferenceHelperClass1")).contains(dataFactory.getOWLClass("#InferenceHelperClass2")); // Verifica se InferenceHelperClass1 è una sottoclasse di InferenceHelperClass2
    }

    private Set<OWLAxiom> createInferenceHelperClasses(OWLClassExpression subclass, OWLClassExpression superclass) {
        Set<OWLAxiom> helperAxioms = new HashSet<>();
        OWLClass helperClass1 = this.dataFactory.getOWLClass(IRI.create("#InferenceHelperClass1")); // Crea una classe ausiliaria denominata InferenceHelperClass1
        OWLClass helperClass2 = this.dataFactory.getOWLClass(IRI.create("#InferenceHelperClass2")); // Crea una classe ausiliaria denominata InferenceHelperClass2
        OWLSubClassOfAxiom subClassAxiom = this.dataFactory.getOWLSubClassOfAxiom(helperClass1, subclass); // InferenceHelperClass1 ⊑ subclass
        OWLSubClassOfAxiom superClassAxiom = this.dataFactory.getOWLSubClassOfAxiom(superclass, helperClass2); // superclass ⊑ InferenceHelperClass2
        helperAxioms.add(subClassAxiom); // Aggiunge l'assioma InferenceHelperClass1 ⊑ subclass al set helperAxioms.
        helperAxioms.add(superClassAxiom); // Aggiunge l'assioma superclass ⊑ InferenceHelperClass2 al set helperAxioms.
        return helperAxioms;
    }

    private void initializeClassMappings(Set<OWLSubClassOfAxiom> normalizedAxioms) {
        for (OWLSubClassOfAxiom axiom : normalizedAxioms) { // Itera su ciascun assioma di sottoclasse presente in normalizedAxioms
            OWLClassExpression subclass = axiom.getSubClass(); // Estrae la sottoclasse dall'assioma corrente
            OWLClassExpression superclass = axiom.getSuperClass(); // Estrae la superclasse dall'assioma corrente
            mapClassToHierarchy(subclass); // Mappa la sottoclasse nella gerarchia. Questo aggiorna le strutture interne per tenere traccia delle relazioni della classe `subclass`.
            mapClassToHierarchy(superclass); // Mappa la superclasse nella gerarchia
        }
    }

    // Metodo che mappa un'espressione di classe OWL in una gerarchia di sottoclassi (in subSumptionMap) o relazioni (relationships), a seconda del tipo di espressione.
    private void mapClassToHierarchy(OWLClassExpression expression) {
        Set<OWLClassExpression> hierarchySet = new HashSet<>(); // Crea un set vuoto (hierarchySet) per memorizzare le classi o espressioni OWL che saranno considerate come superclassi di expression.
        if (expression.getClassExpressionType().equals(ClassExpressionType.OWL_CLASS) || expression.getClassExpressionType().equals(ClassExpressionType.OBJECT_ONE_OF)) { //Se l'espressione è una classe semplice o un singleton
            hierarchySet.add(expression); // Aggiunge l'espressione stessa come elemento della gerarchia (superclasse)
            hierarchySet.add(this.dataFactory.getOWLThing()); // Aggiunge il top (la classe più generale) come superclasse, dato che ogni classe è una sottoclasse di top
            subSumptionMap.put(expression, hierarchySet); // Aggiunge l'espressione e il relativo hierarchySet in subSumptionMap
        }
        else if (expression.getClassExpressionType().equals(ClassExpressionType.OBJECT_INTERSECTION_OF)) { // Se l'espressione è un'intersezione
            OWLObjectIntersectionOf intersection = (OWLObjectIntersectionOf) expression; // Cast dell'espressione a OWLObjectIntersectionOf per accedere ai suoi operandi
            List<OWLClassExpression> operands = intersection.getOperandsAsList(); // Ottiene gli operandi dell'intersezione come lista di espressioni di classe
            hierarchySet.add(operands.getFirst()); // Aggiunge il primo operando dell'intersezione al hierarchySet
            hierarchySet.add(this.dataFactory.getOWLThing()); // Aggiunge top come superconcetto del primo operando
            subSumptionMap.put(operands.get(0), hierarchySet); // Aggiunge il primo operando e il relativo hierarchySet a subSumptionMap
            hierarchySet = new HashSet<>(); // Reinizializza hierarchySet per memorizzare i superconcetti del secondo operando
            hierarchySet.add(operands.get(1)); // Aggiunge il secondo operando dell'intersezione al hierarchySet
            hierarchySet.add(this.dataFactory.getOWLThing()); // Aggiunge top come superconcetto del secondo operando
            subSumptionMap.put(operands.get(1), hierarchySet); // Aggiunge il secondo operando e il relativo hierarchySet a subSumptionMap
        }
        else if (expression.getClassExpressionType().equals(ClassExpressionType.OBJECT_SOME_VALUES_FROM)) { // Se l'espressione è una restrizione esistenziale del tipo ∃R.C
            Set<SimpleEntry<OWLClassExpression, OWLClassExpression>> existentialSet = new HashSet<>(); // Crea un set vuoto per memorizzare coppie di espressioni legate dal ruolo. Si ricordi che (C,D) ∈ relationships(R) significa che, dalla base di conoscenza, si può derivare che C ⊑ ∃R.D
            OWLObjectSomeValuesFrom existentialExpression = (OWLObjectSomeValuesFrom) expression; // Effettua un cast  per accedere al ruolo e al riempitivo.
            relationships.put(existentialExpression.getProperty(), existentialSet); // Aggiunge il ruolo R come chiave nella mappa relationships, associandola al set di coppie vuoto existentialSet
            hierarchySet.add(existentialExpression.getFiller()); // Aggiunge il riempitivo della restrizione esistenziale al hierarchySet
            hierarchySet.add(this.dataFactory.getOWLThing()); // Aggiunge top come superconcetto del riempitivo
            subSumptionMap.put(existentialExpression.getFiller(), hierarchySet); // Aggiunge il riempitivo e il relativo hierarchySet a subSumptionMap
        }
    }

    private void applyCompletionRules(Set<OWLSubClassOfAxiom> normalizedAxioms) {
        boolean hasChanges = true; // Serve a controllare se ci sono stati cambiamenti durante l'applicazione delle regole, determinando se il ciclo deve continuare
        while (hasChanges) {
            hasChanges = false;
            for (OWLClassExpression classExpression : subSumptionMap.keySet()) { // Itera su tutte le espressioni di classe presenti in subSumptionMap
                //L'operatore |= viene utilizzato per aggiornare il valore di hasChanges in modo cumulativo durante l'iterazione, senza perdere informazioni sui cambiamenti precedenti
                hasChanges |= applyCompletionRule1(classExpression, normalizedAxioms);
                hasChanges |= applyCompletionRule2(classExpression, normalizedAxioms);
                hasChanges |= applyCompletionRule3(classExpression, normalizedAxioms);
            }
            for (OWLObjectPropertyExpression property : relationships.keySet()) { // Itera su tutti i ruoli relationships.
                hasChanges |= applyCompletionRule4(property, normalizedAxioms);
                hasChanges |= applyCompletionRule5(property);
            }
            DefaultDirectedGraph<OWLClassExpression, DefaultEdge> classHierarchyGraph = createHierarchyGraph(); // Crea un grafo diretto che rappresenta la gerarchia attuale delle classi. Serve per calcolare la raggiungibilità transitiva tra classi, necessaria per CR6
            for (OWLClassExpression startClass : subSumptionMap.keySet()) { // Itera su tutte le classi conosciute come nodi di partenza
                for (OWLClassExpression endClass : subSumptionMap.keySet()) { // Itera su tutte le classi conosciute come nodi di destinazione
                    hasChanges |= applyCompletionRule6(startClass, endClass, classHierarchyGraph);
                }
            }
        }
    }

    private boolean applyCompletionRule1(OWLClassExpression classExpression, Set<OWLSubClassOfAxiom> normalizedAxioms) {
        Set<OWLClassExpression> currentSuperclasses = new HashSet<>(subSumptionMap.get(classExpression)); // Ottiene i superconcetti attuali di classExpression da subSumptionMap. Copia i superconcetti correnti in un nuovo set per elaborazioni successive
        boolean changesMade = false;

        for (OWLClassExpression currentSuperclass : currentSuperclasses) { // Itera su ogni superconcetto corrente di classExpression
            for (OWLSubClassOfAxiom axiom : normalizedAxioms) { // Itera su tutti gli assiomi normalizzati
                if (axiom.getSubClass().equals(currentSuperclass)) { // Se il superconcetto corrente è la sottoclasse nell'assioma considerato, si applica la regola di transitività
                    OWLClassExpression newSuperclass = axiom.getSuperClass(); // Ottiene il superconcetto dall'assioma considerato
                    if (subSumptionMap.get(classExpression).add(newSuperclass)) // Aggiorna subSumptionMap aggiungendo il nuovo superconcetto alla gerarchia di classExpression.
                        changesMade = true;
                }
            }
        }

        return changesMade;
    }

    private boolean applyCompletionRule2(OWLClassExpression classExpression, Set<OWLSubClassOfAxiom> normalizedAxioms) {
        List<OWLClassExpression> hierarchyMembers = new ArrayList<>(subSumptionMap.get(classExpression)); // Estrae i superconcetti noti per classExpression da subSumptionMap. Converte il set dei superconcetti in una lista ordinata per facilitare il confronto tra coppie di elementi
        boolean changesMade = false;

        for (int i = 0; i < hierarchyMembers.size(); i++) { // Scorre la lista dei superconcetti noti per selezionare il primo operando della coppia
            for (int j = i + 1; j < hierarchyMembers.size(); j++) { // Seleziona il secondo operando della coppia, partendo dall'elemento successivo a quello del ciclo esterno (i + 1). Questo assicura che ogni coppia venga considerata una sola volta
                OWLObjectIntersectionOf intersection = dataFactory.getOWLObjectIntersectionOf(hierarchyMembers.get(i), hierarchyMembers.get(j)); // Crea l'intersezione tra i due superconcetti selezionati
                for (OWLSubClassOfAxiom axiom : normalizedAxioms) { // Scorre tutti i normalizedAxioms per trovare eventuali assiomi che coinvolgono l'intersezione
                    if (axiom.getSubClass().equals(intersection)) { // Controlla se l'intersezione generata è presente come sottoclasse in uno degli assiomi normalizzati
                        OWLClassExpression newSuperclass = axiom.getSuperClass(); // Se la condizione precedente è vera, estrae il superconcetto dall'assioma
                        if (subSumptionMap.get(classExpression).add(newSuperclass))// Aggiunge il nuovo superconcetto a subSumptionMap
                            changesMade = true;
                    }
                }
            }
        }
        return changesMade;
    }



    private boolean applyCompletionRule3(OWLClassExpression classExpression, Set<OWLSubClassOfAxiom> normalizedAxioms) {
        boolean changesMade = false;

        Map<OWLClassExpression, List<OWLSubClassOfAxiom>> existentialRestrictionMap = new HashMap<>(); // Crea una mappa per associare ciascuna classe ad un OWLSubClassOfAxiom in cui a sinistra c'è la classe stessa e a destra una  restrizion esistenziale: {A, A ⊑ ∃R.B}

        for (OWLSubClassOfAxiom axiom : normalizedAxioms) { // Itera su ogni assioma normalizzato
            OWLClassExpression superclass = axiom.getSuperClass();  // Estrae la superclasse dell'assioma
            if (superclass.getClassExpressionType().equals(ClassExpressionType.OBJECT_SOME_VALUES_FROM)) // Verifica se la superclasse è una restrizione esistenziale
                existentialRestrictionMap.computeIfAbsent(axiom.getSubClass(), k -> new ArrayList<>()).add(axiom); // Aggiunge l'assioma alla mappa, associandolo alla sua sottoclasse: {A, A ⊑ ∃R.B}
        }

        Set<OWLClassExpression> currentHierarchy = new HashSet<>(subSumptionMap.get(classExpression)); // Ottiene tutte le superclassi C' di classExpression (C) da subSumptionMap
        for (OWLClassExpression member : currentHierarchy) { // Itera su ogni classe nella gerarchia corrente
            List<OWLSubClassOfAxiom> relevantAxioms = existentialRestrictionMap.getOrDefault(member, List.of()); // Ottiene tutti gli assiomi rilevanti, ovvero del tipo C' ⊑ ∃R.D

            for (OWLSubClassOfAxiom axiom : relevantAxioms) { // Itera sugli assiomi rilevanti per elaborare le restrizioni esistenziali
                OWLObjectSomeValuesFrom existential = (OWLObjectSomeValuesFrom) axiom.getSuperClass(); // Converte la superclasse in una restrizione esistenziale
                OWLObjectPropertyExpression property = existential.getProperty(); // Estrae il ruolo dalla restrizione esistenziale
                OWLClassExpression filler = existential.getFiller(); // Estrae il riempitivo della restrizione esistenziale

                SimpleEntry<OWLClassExpression, OWLClassExpression> pair = new SimpleEntry<>(classExpression, filler); // Crea una coppia che collega la classe corrente (sottoclasse) al riempitivo della restrizione
                changesMade |= relationships.computeIfAbsent(property, k -> new HashSet<>()).add(pair); // Aggiunge la coppia a relationships per il ruolo. Segna come "modificato" se viene aggiunta
            }
        }

        return changesMade;
    }

    private boolean applyCompletionRule4(OWLObjectPropertyExpression property, Set<OWLSubClassOfAxiom> normalizedAxioms) {
        boolean changesMade = false;
        Set<SimpleEntry<OWLClassExpression, OWLClassExpression>> propertyPairs = new HashSet<>(relationships.getOrDefault(property, new HashSet<>())); // Recupera tutte le coppie ⟨C, D⟩ associate al ruolo property da relationships
        Map<OWLClassExpression, Set<OWLClassExpression>> toAdd = new HashMap<>(); // Inizializza una mappa per raccogliere nuove relazioni di sottoclasse da aggiungere a subSumptionMap
        Map<OWLClassExpression, List<OWLSubClassOfAxiom>> relevantAxiomsByFiller = new HashMap<>();  // Crea una mappa per raggruppare gli assiomi in base al filler delle restrizioni esistenziali. Questo aiuta a trovare rapidamente gli assiomi pertinenti per un dato filler
        for (OWLSubClassOfAxiom axiom : normalizedAxioms) { // Itera su tutti gli assiomi normalizzati
            OWLClassExpression subclass = axiom.getSubClass(); //Estrae la sottoclasse
            if (subclass.getClassExpressionType().equals(ClassExpressionType.OBJECT_SOME_VALUES_FROM)) { // Verifica se la sottoclasse è una restrizione esistenziale
                OWLObjectSomeValuesFrom existential = (OWLObjectSomeValuesFrom) subclass; // Casting per accedere ai dettagli della restrizione esistenziale
                if (existential.getProperty().equals(property)) // Se l'esistenziale utilizza il ruolo property
                    relevantAxiomsByFiller.computeIfAbsent(existential.getFiller(), k -> new ArrayList<>()).add(axiom); // Viene aggiunto l'assioma corrente alla lista associata al filler in relevantAxiomsByFiller.
            }
        }
        for (SimpleEntry<OWLClassExpression, OWLClassExpression> pair : propertyPairs) { // Scorre tutte le coppie (C, D) associate al ruolo property
            OWLClassExpression leftNode = pair.getKey();  // Estrae la prima classe della coppia (C)
            OWLClassExpression rightNode = pair.getValue(); // Estrae la seconda classe della coppia (D)

            Set<OWLClassExpression> rightNodeSuperclasses = subSumptionMap.getOrDefault(rightNode, Set.of()); // Recupera tutte le superclassi attualmente conosciute di rightNode (D)
            for (OWLClassExpression superclass : rightNodeSuperclasses) { // Scorre ogni superclasse di rightNode (D)
                List<OWLSubClassOfAxiom> relevantAxioms = relevantAxiomsByFiller.getOrDefault(superclass, List.of()); // Recupera gli assiomi rilevanti il cui filler corrisponde alla superclasse corrente (D') di rightNode (D)
                for (OWLSubClassOfAxiom axiom : relevantAxioms) { // Per ogni assioma rilevante
                    OWLClassExpression newSuperclass = axiom.getSuperClass(); // Estrae la superclasse dell'assioma (E)
                    toAdd.computeIfAbsent(leftNode, k -> new HashSet<>()).add(newSuperclass); // Pianifica di aggiungere questa superclasse alla gerarchia di leftNode (C)
                }
            }
        }
        for (Map.Entry<OWLClassExpression, Set<OWLClassExpression>> entry : toAdd.entrySet()) { // Scorre le nuove relazioni da aggiungere alla gerarchia
            OWLClassExpression key = entry.getKey(); // Estrae il leftNode (C)
            for (OWLClassExpression value : entry.getValue()) { // Per ogni superclasse (di C) da aggiungere
                if (subSumptionMap.get(key).add(value)) // Aggiunge ogni nuovo superconcetto alla gerarchia del nodo di partenza.
                    changesMade = true;
            }
        }
        return changesMade;
    }


    private boolean applyCompletionRule5(OWLObjectPropertyExpression property) {
        boolean changesMade = false;
        Set<SimpleEntry<OWLClassExpression, OWLClassExpression>> propertyPairs = relationships.get(property); // Recupera tutte le coppie ⟨C, D⟩ associate al ruolo property da relationships

        // bottomNodes contiene tutte le classi che hanno come superclasse ⊥
        Set<OWLClassExpression> bottomNodes = subSumptionMap.entrySet().stream() // Accede a subSumptionMap
                .filter(entry -> entry.getValue().contains(dataFactory.getOWLNothing())) // Filtra le classi con ⊥ come superclasse
                .map(Map.Entry::getKey) // Recupera la sottoclasse associata
                .collect(Collectors.toSet()); // Converte il risultato in un Set per rapide verifiche


        for (SimpleEntry<OWLClassExpression, OWLClassExpression> pair : propertyPairs) { // Itera su tutte le coppie ⟨C, D⟩ associate al ruolo property da relationships
            OWLClassExpression leftNode = pair.getKey(); // Estrae il dominio (C) della coppia
            OWLClassExpression rightNode = pair.getValue(); // Estrae il riempitivo (D) della coppia
            if (bottomNodes.contains(rightNode)) // Se il nodo destro (riempitivo) è in bottomNodes (il che implica una contraddizione logica)
                changesMade |= subSumptionMap.get(leftNode).add(dataFactory.getOWLNothing()); // Aggiunge ⊥ come superclasse del nodo sinistro, se non già presente
        }

        return changesMade;
    }

    private boolean applyCompletionRule6(OWLClassExpression startClass, OWLClassExpression endClass, DefaultDirectedGraph<OWLClassExpression, DefaultEdge> classHierarchyGraph) {
        boolean changesMade = false;

        Set<OWLClassExpression> reachableClasses = computeReachability(classHierarchyGraph, startClass); // Calcola tutte le classi raggiungibili da startClass nel grafo gerarchico classHierarchyGraph

        if (!startClass.equals(endClass) && !startClass.isOWLNothing() && reachableClasses.contains(endClass)) { // Controlla se startClass e endClass non sono uguali, startClass non è ⊥ e se endClass è raggiungibile da startClass nel grafo
            // Recupera l'insieme delle superclassi di startClass e endClass, e calcola l'intersezione tra i due insiemi
            Set<OWLClassExpression> intersection = new HashSet<>(subSumptionMap.get(startClass));
            intersection.retainAll(subSumptionMap.get(endClass));

            for (OWLClassExpression member : intersection) {  // Itera sugli elementi dell'intersezione (padri comuni di startClass e endClass)
                if (member.getClassExpressionType().equals(ClassExpressionType.OBJECT_ONE_OF)) // Se l'espressione di classe è di tipo `OBJECT_ONE_OF` (singleton)
                    changesMade = subSumptionMap.get(startClass).addAll(subSumptionMap.get(endClass)); // Aggiunge tutte le superclassi di endClass all'insieme delle superclassi di startClass
            }
        }
        return changesMade;
    }

    // Metodo per calcolare tutti i nodi raggiungibili da un nodo di partenza in un grafo orientato
    private Set<OWLClassExpression> computeReachability(DefaultDirectedGraph<OWLClassExpression, DefaultEdge> graph, OWLClassExpression source) {
        Set<OWLClassExpression> reachable = new HashSet<>(); // Inizializza un set per memorizzare tutti i nodi raggiungibili. Il set garantisce che non ci siano duplicati
        Queue<OWLClassExpression> queue = new LinkedList<>(); // Inizializza una coda per la visita in ampiezza (BFS). La coda conterrà i nodi da esplorare
        queue.add(source); // Aggiunge il nodo di partenza alla coda per iniziare la visita

        // Ciclo principale della visita in ampiezza: continua finché ci sono nodi da esplorare nella coda
        while (!queue.isEmpty()) {
            OWLClassExpression current = queue.poll(); // Rimuove il primo nodo dalla coda e lo assegna a current
            for (DefaultEdge edge : graph.outgoingEdgesOf(current)) { // Per ogni arco uscente dal nodo corrente
                OWLClassExpression target = graph.getEdgeTarget(edge); // Ottiene il nodo di destinazione dell'arco corrente
                if (reachable.add(target)) // Tenta di aggiungere il nodo di destinazione al set dei raggiungibili. L'operazione restituisce true se il nodo non era già presente.
                    queue.add(target); // Se il nodo di destinazione è nuovo (non ancora visitato), lo aggiunge alla coda per futura esplorazione
            }
        }
        return reachable;
    }

    // Metodo per creare un grafo orientato che rappresenta relationships (un arco va da C a D se C ⊑ ∃R.D)
    private DefaultDirectedGraph<OWLClassExpression, DefaultEdge> createHierarchyGraph() {
        DefaultDirectedGraph<OWLClassExpression, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class); // Crea un nuovo grafo orientato con vertici di tipo OWLClassExpression e archi di tipo DefaultEdge (senza ulteriori proprietà associate agli archi)
        for (OWLClassExpression expression : subSumptionMap.keySet()) // Per ogni classe presente in subSumptionMap
            graph.addVertex(expression); // la aggiunge come nodo (vertice) del grafo
        for (OWLObjectPropertyExpression property : relationships.keySet()) { // Per ogni ruolo in relationships
            for (SimpleEntry<OWLClassExpression, OWLClassExpression> pair : relationships.get(property)) { // Per ogni coppia di classi relativa al ruolo (C,D)
                OWLClassExpression source = pair.getKey(); // Ottiene sottoclasse (C)
                OWLClassExpression target = pair.getValue(); //Ottiene il riempitivo della superclasse (D)
                graph.addEdge(source, target); // Aggiunge un arco diretto al grafo, che collega il nodo sorgente (C) al nodo destinazione (D)
            }
        }
        return graph;
    }
}

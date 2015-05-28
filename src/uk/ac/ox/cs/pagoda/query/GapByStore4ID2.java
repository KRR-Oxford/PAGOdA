package uk.ac.ox.cs.pagoda.query;

import uk.ac.ox.cs.JRDFox.JRDFStoreException;
import uk.ac.ox.cs.JRDFox.store.TupleIterator;
import uk.ac.ox.cs.pagoda.reasoner.light.BasicQueryEngine;
import uk.ac.ox.cs.pagoda.util.UFS;

import java.util.*;

public class GapByStore4ID2 extends GapByStore4ID {

    private BasicQueryEngine m_baseEngine;
    private UFS<String> m_equality = null, m_baseEquality = null;
    private LinkedList<String> toAddedIndividuals = null;
    private TupleIterator iter_individual = null;
    private int currentID = -1;

    public GapByStore4ID2(BasicQueryEngine engine, BasicQueryEngine baseEngine) {
        super(engine);
        m_baseEngine = baseEngine;
    }

    @Override
    public boolean hasNext() {
        if(getNewGapTuple(iterator, -1)) return true;
        if(iterator != null) {
            iterator.dispose();
            iterator = null;
        }
        return getNextGapFactAboutEquality();
    }

    private boolean getNewGapTuple(TupleIterator it, int firstElement) {
        if(it == null) return false;
        int firstIndex = 0;
        tuple = new int[3];
        if(firstElement > 0) {
            tuple[0] = firstElement;
            firstIndex = 1;
        }
        Integer predicate;
        try {
            for(; multi != 0; multi = it.getNext()) {
                for(int i = firstIndex; i < 3; ++i)
                    tuple[i] = it.getResourceID(i - firstIndex);

                if(isRDF_TYPE()) {
                    predicate = getGapPredicateID(tuple[2]);
                    if(predicate == null) continue;
                    tuple[2] = predicate;
                } else {
                    predicate = getGapPredicateID(tuple[1]);
                    if(predicate == null) continue;
                    tuple[1] = predicate;
                }
                return true;
            }
        } catch(JRDFStoreException e) {
            e.printStackTrace();
            return false;
        }
        return false;
    }

    private boolean getNextGapFactAboutEquality() {
        if(toAddedIndividuals == null) {
            m_equality = m_engine.getEqualityGroups(false);
            m_baseEquality = m_baseEngine.getEqualityGroups(false);
            toAddedIndividuals = new LinkedList<String>();
            Map<String, Integer> rep2cnt = new HashMap<String, Integer>();
            Map<String, Integer> rep2cnt_base = new HashMap<String, Integer>();
            count(m_engine, m_equality, rep2cnt);
            count(m_baseEngine, m_baseEquality, rep2cnt_base);
            Set<String> visitedrep = new HashSet<String>();
            for(String individual : m_equality.keySet()) {
                String rep = m_equality.find(individual);
                if(visitedrep.contains(rep)) continue;
                visitedrep.add(rep);
                String rep_base = m_baseEquality.find(individual);
                if(!rep2cnt.get(rep).equals(rep2cnt_base.get(rep_base))) {
                    toAddedIndividuals.add(rep);
                }
            }

        }
        while(true) {
            if(getNewGapTuple(iter_individual, currentID)) return true;
            if(iter_individual != null) {
                iter_individual.dispose();
                iter_individual = null;
            }
            if(toAddedIndividuals.isEmpty()) {
                currentID = -1;
                return false;
            }
            String individual = toAddedIndividuals.remove();
            currentID = tripleManager.getResourceID(individual);
            try {
                iter_individual =
                        m_engine.internal_evaluateNotExpanded(String.format("select distinct ?y ?z where { <%s> ?y ?z }", individual));
                multi = iter_individual.open();
            } catch(JRDFStoreException e) {
                e.printStackTrace();
            }
        }
    }

    private void count(BasicQueryEngine engine, UFS<String> equality, Map<String, Integer> map) {
        for(String ind : equality.keySet()) {
            Integer exist = map.get(ind);
            if(exist == null)
                map.put(equality.find(ind), 1);
            else
                map.put(equality.find(ind), ++exist);
        }
    }

    @Override
    public int[] next() {
        try {
            if(iterator != null)
                multi = iterator.getNext();
            else if(iter_individual != null)
                multi = iter_individual.getNext();
            else
                multi = 0;
        } catch(JRDFStoreException e) {
            e.printStackTrace();
        }
        return tuple;
    }

    public void clear() {
        super.clear();
        if(iter_individual != null) {
            iter_individual.dispose();
            iter_individual = null;
        }
    }

}

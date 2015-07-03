package uk.ac.ox.cs.pagoda.rules.clauses;

public class Clause {

//    public static final String IF = ":-";
//    public static final String OR = "|";
//    public static final String AND = ",";
//
//    protected final List<List<Atom>> head;
//    protected final List<Atom> body;
//
//    protected Clause(Atom[] headAtoms, Atom[] bodyAtoms) {
//        this.head = Collections.singletonList(Arrays.asList(headAtoms));
//        this.body= Arrays.asList(bodyAtoms);
//    }
//
//    protected Clause(String s) {
//        this.headAtoms = null;
//        this.bodyAtoms = null;
//    }
//
//    public int getHeadLength() {
//        return headAtoms.length;
//    }
//
//    public Atom getHeadAtom(int atomIndex) {
//        return headAtoms[atomIndex];
//    }
//
//    public Atom[] getHeadAtoms() {
//        return headAtoms.clone();
//    }
//
//    public int getBodyLength() {
//        return bodyAtoms.length;
//    }
//
//    public Atom getBodyAtom(int atomIndex) {
//        return bodyAtoms[atomIndex];
//    }
//
//    public Atom[] getBodyAtoms() {
//        return bodyAtoms.clone();
//    }
//
//    public String toString(Prefixes prefixes) {
//        StringBuilder buffer = new StringBuilder();
//        for(int headIndex = 0; headIndex < headAtoms.length; headIndex++) {
//            if(headIndex != 0)
//                buffer.append(" ").append(OR).append(" ");
//            buffer.append(headAtoms[headIndex].toString(prefixes));
//        }
//        buffer.append(" ").append(IF).append(" ");
//        for(int bodyIndex = 0; bodyIndex < bodyAtoms.length; bodyIndex++) {
//            if(bodyIndex != 0)
//                buffer.append(AND).append(" ");
//            buffer.append(bodyAtoms[bodyIndex].toString(prefixes));
//        }
//        return buffer.toString();
//    }
//
//    public String toString() {
//        return toString(Prefixes.STANDARD_PREFIXES);
//    }
//
//    protected static InterningManager<? extends Clause> s_interningManager = new InterningManager<Clause>() {
//        protected boolean equal(Clause object1, Clause object2) {
//            if(object1.head.length != object2.headAtoms.length
//                    || object1.bodyAtoms.length != object2.bodyAtoms.length)
//                return false;
//            for(int index = object1.headAtoms.length - 1; index >= 0; --index)
//                if(object1.headAtoms[index] != object2.headAtoms[index])
//                    return false;
//            for(int index = object1.bodyAtoms.length - 1; index >= 0; --index)
//                if(object1.bodyAtoms[index] != object2.bodyAtoms[index])
//                    return false;
//            return true;
//        }
//
//        protected int getHashCode(Clause object) {
//            int hashCode = 0;
//            for(int index = object.bodyAtoms.length - 1; index >= 0; --index)
//                hashCode += object.bodyAtoms[index].hashCode();
//            for(int index = object.headAtoms.length - 1; index >= 0; --index)
//                hashCode += object.headAtoms[index].hashCode();
//            return hashCode;
//        }
//    };
//
//    /**
//     * Creates a clause from a string.
//     *
//     * @param s
//     * @return
//     */
//    public static Clause create(String s) {
//        return s_interningManager.intern(new Clause(s));
//    }

}

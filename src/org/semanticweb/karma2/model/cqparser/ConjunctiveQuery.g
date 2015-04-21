grammar ConjunctiveQuery;

options {
  language = Java;
  output = AST;
}

tokens {
  VARIABLE; 
  CONSTANT;
  SCONSTANT;
  ATOM;
  HEADATOM;
  PREDICATE;
  ATOM_LIST;
  TERM_LIST;
  RULE;
  EXPRESSION;
  PREFIX_LIST;
  ID;
  PREFIX;
  PREDICATE;
}

@header {
package org.semanticweb.karma2.model.cqparser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Set;

import org.semanticweb.karma2.model.ConjunctiveQuery;


import org.semanticweb.karma2.model.cqparser.ConjunctiveQueryWalker;
import org.semanticweb.karma2.exception.IllegalInputQueryException;



}

@members{


      
      public ConjunctiveQueryParser(String string)
        throws FileNotFoundException, IOException {
        this(new CommonTokenStream(new ConjunctiveQueryLexer(new ANTLRStringStream(string))));
      }
  
      public ConjunctiveQueryParser(InputStream istream) throws FileNotFoundException, IOException {
        this(new CommonTokenStream(new ConjunctiveQueryLexer(new ANTLRInputStream(istream))));

      }
  
  
      public ConjunctiveQueryParser(File file) throws FileNotFoundException, IOException {
        this(new CommonTokenStream(new ConjunctiveQueryLexer(new ANTLRInputStream(new FileInputStream(file)))));

      }

      public ConjunctiveQuery parse() throws IllegalInputQueryException {
        cq_return r = null;
        try {
          r = cq();
        } catch (RecognitionException e) {
          e.printStackTrace();
        }
        CommonTree t = (CommonTree) r.getTree();

        //System.out.println(t.toStringTree());
        CommonTreeNodeStream nodes = new CommonTreeNodeStream(t);
        // AST nodes have payloads that point into token stream
        nodes.setTokenStream(input);


        ConjunctiveQueryWalker walker = new ConjunctiveQueryWalker();

        ConjunctiveQuery cq = walker.walkExpressionNode(t);
        return cq;
      }
      
      public ConjunctiveQuery parseCQ() throws IllegalInputQueryException {
        return parse();
      }
      
}


@lexer::header{
package org.semanticweb.karma2.model.cqparser;
}

cq : 
  prefixlist rulebody -> ^(EXPRESSION prefixlist rulebody );

prefixlist:
  prefix (',' prefix)* -> ^(PREFIX_LIST prefix*);
  
prefix:
  'prefix' id ':' '<' url '>' -> ^(PREFIX id url);
  

rulebody:
  headatom ('<-'|':') body '.'?  -> ^(RULE headatom body);
 
body:
  atom (',' atom)* -> ^(ATOM_LIST atom*);
   
  
headatom:
  id '(' term (',' term)* ')' -> ^(HEADATOM term*);
  
atom:
  compositeid '(' term (',' term)* ')' -> ^(ATOM compositeid term*);

compositeid:
 (id) ':' (id) -> ^(ID id id);

  
term:
  variable -> ^(VARIABLE variable) 
  | simpleid -> ^(SCONSTANT simpleid)
  | compositeid -> ^(CONSTANT compositeid);

id : (STRING);
simpleid : '<' URLSTRING '>' | '<' STRING '>';

// TODO: FIXIT X1 can be parsed as variable
variable:
  ('?') id -> ^(id);
  
 url : (URLSTRING);

URLSTRING : ('http://'|'file:/') ('a'..'z'|'A'..'Z'|'0'..'9'|'/'|'#'|'.'|'-'|'~'|'_')+; 
STRING  :   ('a'..'z'|'A'..'Z'|'0'..'9'|'/'|'#'|'.'|'-'|'_')+;
WS  : (' '|'\n'|'\r')+ {$channel=HIDDEN;} ;

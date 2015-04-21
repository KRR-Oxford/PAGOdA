// $ANTLR 3.5 /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g 2013-12-13 14:41:34

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





import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

import org.antlr.runtime.tree.*;


@SuppressWarnings("all")
public class ConjunctiveQueryParser extends Parser {
	public static final String[] tokenNames = new String[] {
		"<invalid>", "<EOR>", "<DOWN>", "<UP>", "ATOM", "ATOM_LIST", "CONSTANT", 
		"EXPRESSION", "HEADATOM", "ID", "PREDICATE", "PREFIX", "PREFIX_LIST", 
		"RULE", "SCONSTANT", "STRING", "TERM_LIST", "URLSTRING", "VARIABLE", "WS", 
		"'('", "')'", "','", "'.'", "':'", "'<'", "'<-'", "'>'", "'?'", "'prefix'"
	};
	public static final int EOF=-1;
	public static final int T__20=20;
	public static final int T__21=21;
	public static final int T__22=22;
	public static final int T__23=23;
	public static final int T__24=24;
	public static final int T__25=25;
	public static final int T__26=26;
	public static final int T__27=27;
	public static final int T__28=28;
	public static final int T__29=29;
	public static final int ATOM=4;
	public static final int ATOM_LIST=5;
	public static final int CONSTANT=6;
	public static final int EXPRESSION=7;
	public static final int HEADATOM=8;
	public static final int ID=9;
	public static final int PREDICATE=10;
	public static final int PREFIX=11;
	public static final int PREFIX_LIST=12;
	public static final int RULE=13;
	public static final int SCONSTANT=14;
	public static final int STRING=15;
	public static final int TERM_LIST=16;
	public static final int URLSTRING=17;
	public static final int VARIABLE=18;
	public static final int WS=19;

	// delegates
	public Parser[] getDelegates() {
		return new Parser[] {};
	}

	// delegators


	public ConjunctiveQueryParser(TokenStream input) {
		this(input, new RecognizerSharedState());
	}
	public ConjunctiveQueryParser(TokenStream input, RecognizerSharedState state) {
		super(input, state);
	}

	protected TreeAdaptor adaptor = new CommonTreeAdaptor();

	public void setTreeAdaptor(TreeAdaptor adaptor) {
		this.adaptor = adaptor;
	}
	public TreeAdaptor getTreeAdaptor() {
		return adaptor;
	}
	@Override public String[] getTokenNames() { return ConjunctiveQueryParser.tokenNames; }
	@Override public String getGrammarFileName() { return "/home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g"; }




	      
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
	      


	public static class cq_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "cq"
	// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:97:1: cq : prefixlist rulebody -> ^( EXPRESSION prefixlist rulebody ) ;
	public final ConjunctiveQueryParser.cq_return cq() throws RecognitionException {
		ConjunctiveQueryParser.cq_return retval = new ConjunctiveQueryParser.cq_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope prefixlist1 =null;
		ParserRuleReturnScope rulebody2 =null;

		RewriteRuleSubtreeStream stream_rulebody=new RewriteRuleSubtreeStream(adaptor,"rule rulebody");
		RewriteRuleSubtreeStream stream_prefixlist=new RewriteRuleSubtreeStream(adaptor,"rule prefixlist");

		try {
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:97:4: ( prefixlist rulebody -> ^( EXPRESSION prefixlist rulebody ) )
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:98:3: prefixlist rulebody
			{
			pushFollow(FOLLOW_prefixlist_in_cq132);
			prefixlist1=prefixlist();
			state._fsp--;

			stream_prefixlist.add(prefixlist1.getTree());
			pushFollow(FOLLOW_rulebody_in_cq134);
			rulebody2=rulebody();
			state._fsp--;

			stream_rulebody.add(rulebody2.getTree());
			// AST REWRITE
			// elements: rulebody, prefixlist
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 98:23: -> ^( EXPRESSION prefixlist rulebody )
			{
				// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:98:26: ^( EXPRESSION prefixlist rulebody )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(EXPRESSION, "EXPRESSION"), root_1);
				adaptor.addChild(root_1, stream_prefixlist.nextTree());
				adaptor.addChild(root_1, stream_rulebody.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "cq"


	public static class prefixlist_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "prefixlist"
	// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:100:1: prefixlist : prefix ( ',' prefix )* -> ^( PREFIX_LIST ( prefix )* ) ;
	public final ConjunctiveQueryParser.prefixlist_return prefixlist() throws RecognitionException {
		ConjunctiveQueryParser.prefixlist_return retval = new ConjunctiveQueryParser.prefixlist_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token char_literal4=null;
		ParserRuleReturnScope prefix3 =null;
		ParserRuleReturnScope prefix5 =null;

		Object char_literal4_tree=null;
		RewriteRuleTokenStream stream_22=new RewriteRuleTokenStream(adaptor,"token 22");
		RewriteRuleSubtreeStream stream_prefix=new RewriteRuleSubtreeStream(adaptor,"rule prefix");

		try {
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:100:11: ( prefix ( ',' prefix )* -> ^( PREFIX_LIST ( prefix )* ) )
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:101:3: prefix ( ',' prefix )*
			{
			pushFollow(FOLLOW_prefix_in_prefixlist154);
			prefix3=prefix();
			state._fsp--;

			stream_prefix.add(prefix3.getTree());
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:101:10: ( ',' prefix )*
			loop1:
			while (true) {
				int alt1=2;
				int LA1_0 = input.LA(1);
				if ( (LA1_0==22) ) {
					alt1=1;
				}

				switch (alt1) {
				case 1 :
					// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:101:11: ',' prefix
					{
					char_literal4=(Token)match(input,22,FOLLOW_22_in_prefixlist157);  
					stream_22.add(char_literal4);

					pushFollow(FOLLOW_prefix_in_prefixlist159);
					prefix5=prefix();
					state._fsp--;

					stream_prefix.add(prefix5.getTree());
					}
					break;

				default :
					break loop1;
				}
			}

			// AST REWRITE
			// elements: prefix
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 101:24: -> ^( PREFIX_LIST ( prefix )* )
			{
				// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:101:27: ^( PREFIX_LIST ( prefix )* )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PREFIX_LIST, "PREFIX_LIST"), root_1);
				// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:101:41: ( prefix )*
				while ( stream_prefix.hasNext() ) {
					adaptor.addChild(root_1, stream_prefix.nextTree());
				}
				stream_prefix.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "prefixlist"


	public static class prefix_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "prefix"
	// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:103:1: prefix : 'prefix' id ':' '<' url '>' -> ^( PREFIX id url ) ;
	public final ConjunctiveQueryParser.prefix_return prefix() throws RecognitionException {
		ConjunctiveQueryParser.prefix_return retval = new ConjunctiveQueryParser.prefix_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal6=null;
		Token char_literal8=null;
		Token char_literal9=null;
		Token char_literal11=null;
		ParserRuleReturnScope id7 =null;
		ParserRuleReturnScope url10 =null;

		Object string_literal6_tree=null;
		Object char_literal8_tree=null;
		Object char_literal9_tree=null;
		Object char_literal11_tree=null;
		RewriteRuleTokenStream stream_24=new RewriteRuleTokenStream(adaptor,"token 24");
		RewriteRuleTokenStream stream_25=new RewriteRuleTokenStream(adaptor,"token 25");
		RewriteRuleTokenStream stream_27=new RewriteRuleTokenStream(adaptor,"token 27");
		RewriteRuleTokenStream stream_29=new RewriteRuleTokenStream(adaptor,"token 29");
		RewriteRuleSubtreeStream stream_id=new RewriteRuleSubtreeStream(adaptor,"rule id");
		RewriteRuleSubtreeStream stream_url=new RewriteRuleSubtreeStream(adaptor,"rule url");

		try {
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:103:7: ( 'prefix' id ':' '<' url '>' -> ^( PREFIX id url ) )
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:104:3: 'prefix' id ':' '<' url '>'
			{
			string_literal6=(Token)match(input,29,FOLLOW_29_in_prefix181);  
			stream_29.add(string_literal6);

			pushFollow(FOLLOW_id_in_prefix183);
			id7=id();
			state._fsp--;

			stream_id.add(id7.getTree());
			char_literal8=(Token)match(input,24,FOLLOW_24_in_prefix185);  
			stream_24.add(char_literal8);

			char_literal9=(Token)match(input,25,FOLLOW_25_in_prefix187);  
			stream_25.add(char_literal9);

			pushFollow(FOLLOW_url_in_prefix189);
			url10=url();
			state._fsp--;

			stream_url.add(url10.getTree());
			char_literal11=(Token)match(input,27,FOLLOW_27_in_prefix191);  
			stream_27.add(char_literal11);

			// AST REWRITE
			// elements: id, url
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 104:31: -> ^( PREFIX id url )
			{
				// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:104:34: ^( PREFIX id url )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(PREFIX, "PREFIX"), root_1);
				adaptor.addChild(root_1, stream_id.nextTree());
				adaptor.addChild(root_1, stream_url.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "prefix"


	public static class rulebody_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "rulebody"
	// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:107:1: rulebody : headatom ( '<-' | ':' ) body ( '.' )? -> ^( RULE headatom body ) ;
	public final ConjunctiveQueryParser.rulebody_return rulebody() throws RecognitionException {
		ConjunctiveQueryParser.rulebody_return retval = new ConjunctiveQueryParser.rulebody_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token string_literal13=null;
		Token char_literal14=null;
		Token char_literal16=null;
		ParserRuleReturnScope headatom12 =null;
		ParserRuleReturnScope body15 =null;

		Object string_literal13_tree=null;
		Object char_literal14_tree=null;
		Object char_literal16_tree=null;
		RewriteRuleTokenStream stream_23=new RewriteRuleTokenStream(adaptor,"token 23");
		RewriteRuleTokenStream stream_24=new RewriteRuleTokenStream(adaptor,"token 24");
		RewriteRuleTokenStream stream_26=new RewriteRuleTokenStream(adaptor,"token 26");
		RewriteRuleSubtreeStream stream_headatom=new RewriteRuleSubtreeStream(adaptor,"rule headatom");
		RewriteRuleSubtreeStream stream_body=new RewriteRuleSubtreeStream(adaptor,"rule body");

		try {
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:107:9: ( headatom ( '<-' | ':' ) body ( '.' )? -> ^( RULE headatom body ) )
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:108:3: headatom ( '<-' | ':' ) body ( '.' )?
			{
			pushFollow(FOLLOW_headatom_in_rulebody213);
			headatom12=headatom();
			state._fsp--;

			stream_headatom.add(headatom12.getTree());
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:108:12: ( '<-' | ':' )
			int alt2=2;
			int LA2_0 = input.LA(1);
			if ( (LA2_0==26) ) {
				alt2=1;
			}
			else if ( (LA2_0==24) ) {
				alt2=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 2, 0, input);
				throw nvae;
			}

			switch (alt2) {
				case 1 :
					// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:108:13: '<-'
					{
					string_literal13=(Token)match(input,26,FOLLOW_26_in_rulebody216);  
					stream_26.add(string_literal13);

					}
					break;
				case 2 :
					// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:108:18: ':'
					{
					char_literal14=(Token)match(input,24,FOLLOW_24_in_rulebody218);  
					stream_24.add(char_literal14);

					}
					break;

			}

			pushFollow(FOLLOW_body_in_rulebody221);
			body15=body();
			state._fsp--;

			stream_body.add(body15.getTree());
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:108:28: ( '.' )?
			int alt3=2;
			int LA3_0 = input.LA(1);
			if ( (LA3_0==23) ) {
				alt3=1;
			}
			switch (alt3) {
				case 1 :
					// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:108:28: '.'
					{
					char_literal16=(Token)match(input,23,FOLLOW_23_in_rulebody223);  
					stream_23.add(char_literal16);

					}
					break;

			}

			// AST REWRITE
			// elements: headatom, body
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 108:34: -> ^( RULE headatom body )
			{
				// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:108:37: ^( RULE headatom body )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(RULE, "RULE"), root_1);
				adaptor.addChild(root_1, stream_headatom.nextTree());
				adaptor.addChild(root_1, stream_body.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "rulebody"


	public static class body_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "body"
	// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:110:1: body : atom ( ',' atom )* -> ^( ATOM_LIST ( atom )* ) ;
	public final ConjunctiveQueryParser.body_return body() throws RecognitionException {
		ConjunctiveQueryParser.body_return retval = new ConjunctiveQueryParser.body_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token char_literal18=null;
		ParserRuleReturnScope atom17 =null;
		ParserRuleReturnScope atom19 =null;

		Object char_literal18_tree=null;
		RewriteRuleTokenStream stream_22=new RewriteRuleTokenStream(adaptor,"token 22");
		RewriteRuleSubtreeStream stream_atom=new RewriteRuleSubtreeStream(adaptor,"rule atom");

		try {
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:110:5: ( atom ( ',' atom )* -> ^( ATOM_LIST ( atom )* ) )
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:111:3: atom ( ',' atom )*
			{
			pushFollow(FOLLOW_atom_in_body245);
			atom17=atom();
			state._fsp--;

			stream_atom.add(atom17.getTree());
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:111:8: ( ',' atom )*
			loop4:
			while (true) {
				int alt4=2;
				int LA4_0 = input.LA(1);
				if ( (LA4_0==22) ) {
					alt4=1;
				}

				switch (alt4) {
				case 1 :
					// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:111:9: ',' atom
					{
					char_literal18=(Token)match(input,22,FOLLOW_22_in_body248);  
					stream_22.add(char_literal18);

					pushFollow(FOLLOW_atom_in_body250);
					atom19=atom();
					state._fsp--;

					stream_atom.add(atom19.getTree());
					}
					break;

				default :
					break loop4;
				}
			}

			// AST REWRITE
			// elements: atom
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 111:20: -> ^( ATOM_LIST ( atom )* )
			{
				// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:111:23: ^( ATOM_LIST ( atom )* )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(ATOM_LIST, "ATOM_LIST"), root_1);
				// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:111:35: ( atom )*
				while ( stream_atom.hasNext() ) {
					adaptor.addChild(root_1, stream_atom.nextTree());
				}
				stream_atom.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "body"


	public static class headatom_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "headatom"
	// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:114:1: headatom : id '(' term ( ',' term )* ')' -> ^( HEADATOM ( term )* ) ;
	public final ConjunctiveQueryParser.headatom_return headatom() throws RecognitionException {
		ConjunctiveQueryParser.headatom_return retval = new ConjunctiveQueryParser.headatom_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token char_literal21=null;
		Token char_literal23=null;
		Token char_literal25=null;
		ParserRuleReturnScope id20 =null;
		ParserRuleReturnScope term22 =null;
		ParserRuleReturnScope term24 =null;

		Object char_literal21_tree=null;
		Object char_literal23_tree=null;
		Object char_literal25_tree=null;
		RewriteRuleTokenStream stream_21=new RewriteRuleTokenStream(adaptor,"token 21");
		RewriteRuleTokenStream stream_20=new RewriteRuleTokenStream(adaptor,"token 20");
		RewriteRuleTokenStream stream_22=new RewriteRuleTokenStream(adaptor,"token 22");
		RewriteRuleSubtreeStream stream_id=new RewriteRuleSubtreeStream(adaptor,"rule id");
		RewriteRuleSubtreeStream stream_term=new RewriteRuleSubtreeStream(adaptor,"rule term");

		try {
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:114:9: ( id '(' term ( ',' term )* ')' -> ^( HEADATOM ( term )* ) )
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:115:3: id '(' term ( ',' term )* ')'
			{
			pushFollow(FOLLOW_id_in_headatom276);
			id20=id();
			state._fsp--;

			stream_id.add(id20.getTree());
			char_literal21=(Token)match(input,20,FOLLOW_20_in_headatom278);  
			stream_20.add(char_literal21);

			pushFollow(FOLLOW_term_in_headatom280);
			term22=term();
			state._fsp--;

			stream_term.add(term22.getTree());
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:115:15: ( ',' term )*
			loop5:
			while (true) {
				int alt5=2;
				int LA5_0 = input.LA(1);
				if ( (LA5_0==22) ) {
					alt5=1;
				}

				switch (alt5) {
				case 1 :
					// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:115:16: ',' term
					{
					char_literal23=(Token)match(input,22,FOLLOW_22_in_headatom283);  
					stream_22.add(char_literal23);

					pushFollow(FOLLOW_term_in_headatom285);
					term24=term();
					state._fsp--;

					stream_term.add(term24.getTree());
					}
					break;

				default :
					break loop5;
				}
			}

			char_literal25=(Token)match(input,21,FOLLOW_21_in_headatom289);  
			stream_21.add(char_literal25);

			// AST REWRITE
			// elements: term
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 115:31: -> ^( HEADATOM ( term )* )
			{
				// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:115:34: ^( HEADATOM ( term )* )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(HEADATOM, "HEADATOM"), root_1);
				// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:115:45: ( term )*
				while ( stream_term.hasNext() ) {
					adaptor.addChild(root_1, stream_term.nextTree());
				}
				stream_term.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "headatom"


	public static class atom_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "atom"
	// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:117:1: atom : compositeid '(' term ( ',' term )* ')' -> ^( ATOM compositeid ( term )* ) ;
	public final ConjunctiveQueryParser.atom_return atom() throws RecognitionException {
		ConjunctiveQueryParser.atom_return retval = new ConjunctiveQueryParser.atom_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token char_literal27=null;
		Token char_literal29=null;
		Token char_literal31=null;
		ParserRuleReturnScope compositeid26 =null;
		ParserRuleReturnScope term28 =null;
		ParserRuleReturnScope term30 =null;

		Object char_literal27_tree=null;
		Object char_literal29_tree=null;
		Object char_literal31_tree=null;
		RewriteRuleTokenStream stream_21=new RewriteRuleTokenStream(adaptor,"token 21");
		RewriteRuleTokenStream stream_20=new RewriteRuleTokenStream(adaptor,"token 20");
		RewriteRuleTokenStream stream_22=new RewriteRuleTokenStream(adaptor,"token 22");
		RewriteRuleSubtreeStream stream_term=new RewriteRuleSubtreeStream(adaptor,"rule term");
		RewriteRuleSubtreeStream stream_compositeid=new RewriteRuleSubtreeStream(adaptor,"rule compositeid");

		try {
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:117:5: ( compositeid '(' term ( ',' term )* ')' -> ^( ATOM compositeid ( term )* ) )
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:118:3: compositeid '(' term ( ',' term )* ')'
			{
			pushFollow(FOLLOW_compositeid_in_atom309);
			compositeid26=compositeid();
			state._fsp--;

			stream_compositeid.add(compositeid26.getTree());
			char_literal27=(Token)match(input,20,FOLLOW_20_in_atom311);  
			stream_20.add(char_literal27);

			pushFollow(FOLLOW_term_in_atom313);
			term28=term();
			state._fsp--;

			stream_term.add(term28.getTree());
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:118:24: ( ',' term )*
			loop6:
			while (true) {
				int alt6=2;
				int LA6_0 = input.LA(1);
				if ( (LA6_0==22) ) {
					alt6=1;
				}

				switch (alt6) {
				case 1 :
					// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:118:25: ',' term
					{
					char_literal29=(Token)match(input,22,FOLLOW_22_in_atom316);  
					stream_22.add(char_literal29);

					pushFollow(FOLLOW_term_in_atom318);
					term30=term();
					state._fsp--;

					stream_term.add(term30.getTree());
					}
					break;

				default :
					break loop6;
				}
			}

			char_literal31=(Token)match(input,21,FOLLOW_21_in_atom322);  
			stream_21.add(char_literal31);

			// AST REWRITE
			// elements: term, compositeid
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 118:40: -> ^( ATOM compositeid ( term )* )
			{
				// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:118:43: ^( ATOM compositeid ( term )* )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(ATOM, "ATOM"), root_1);
				adaptor.addChild(root_1, stream_compositeid.nextTree());
				// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:118:62: ( term )*
				while ( stream_term.hasNext() ) {
					adaptor.addChild(root_1, stream_term.nextTree());
				}
				stream_term.reset();

				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "atom"


	public static class compositeid_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "compositeid"
	// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:120:1: compositeid : ( id ) ':' ( id ) -> ^( ID id id ) ;
	public final ConjunctiveQueryParser.compositeid_return compositeid() throws RecognitionException {
		ConjunctiveQueryParser.compositeid_return retval = new ConjunctiveQueryParser.compositeid_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token char_literal33=null;
		ParserRuleReturnScope id32 =null;
		ParserRuleReturnScope id34 =null;

		Object char_literal33_tree=null;
		RewriteRuleTokenStream stream_24=new RewriteRuleTokenStream(adaptor,"token 24");
		RewriteRuleSubtreeStream stream_id=new RewriteRuleSubtreeStream(adaptor,"rule id");

		try {
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:120:12: ( ( id ) ':' ( id ) -> ^( ID id id ) )
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:121:2: ( id ) ':' ( id )
			{
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:121:2: ( id )
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:121:3: id
			{
			pushFollow(FOLLOW_id_in_compositeid342);
			id32=id();
			state._fsp--;

			stream_id.add(id32.getTree());
			}

			char_literal33=(Token)match(input,24,FOLLOW_24_in_compositeid345);  
			stream_24.add(char_literal33);

			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:121:11: ( id )
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:121:12: id
			{
			pushFollow(FOLLOW_id_in_compositeid348);
			id34=id();
			state._fsp--;

			stream_id.add(id34.getTree());
			}

			// AST REWRITE
			// elements: id, id
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 121:16: -> ^( ID id id )
			{
				// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:121:19: ^( ID id id )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(ID, "ID"), root_1);
				adaptor.addChild(root_1, stream_id.nextTree());
				adaptor.addChild(root_1, stream_id.nextTree());
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "compositeid"


	public static class term_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "term"
	// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:124:1: term : ( variable -> ^( VARIABLE variable ) | simpleid -> ^( SCONSTANT simpleid ) | compositeid -> ^( CONSTANT compositeid ) );
	public final ConjunctiveQueryParser.term_return term() throws RecognitionException {
		ConjunctiveQueryParser.term_return retval = new ConjunctiveQueryParser.term_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		ParserRuleReturnScope variable35 =null;
		ParserRuleReturnScope simpleid36 =null;
		ParserRuleReturnScope compositeid37 =null;

		RewriteRuleSubtreeStream stream_simpleid=new RewriteRuleSubtreeStream(adaptor,"rule simpleid");
		RewriteRuleSubtreeStream stream_compositeid=new RewriteRuleSubtreeStream(adaptor,"rule compositeid");
		RewriteRuleSubtreeStream stream_variable=new RewriteRuleSubtreeStream(adaptor,"rule variable");

		try {
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:124:5: ( variable -> ^( VARIABLE variable ) | simpleid -> ^( SCONSTANT simpleid ) | compositeid -> ^( CONSTANT compositeid ) )
			int alt7=3;
			switch ( input.LA(1) ) {
			case 28:
				{
				alt7=1;
				}
				break;
			case 25:
				{
				alt7=2;
				}
				break;
			case STRING:
				{
				alt7=3;
				}
				break;
			default:
				NoViableAltException nvae =
					new NoViableAltException("", 7, 0, input);
				throw nvae;
			}
			switch (alt7) {
				case 1 :
					// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:125:3: variable
					{
					pushFollow(FOLLOW_variable_in_term371);
					variable35=variable();
					state._fsp--;

					stream_variable.add(variable35.getTree());
					// AST REWRITE
					// elements: variable
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 125:12: -> ^( VARIABLE variable )
					{
						// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:125:15: ^( VARIABLE variable )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(VARIABLE, "VARIABLE"), root_1);
						adaptor.addChild(root_1, stream_variable.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 2 :
					// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:126:5: simpleid
					{
					pushFollow(FOLLOW_simpleid_in_term386);
					simpleid36=simpleid();
					state._fsp--;

					stream_simpleid.add(simpleid36.getTree());
					// AST REWRITE
					// elements: simpleid
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 126:14: -> ^( SCONSTANT simpleid )
					{
						// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:126:17: ^( SCONSTANT simpleid )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(SCONSTANT, "SCONSTANT"), root_1);
						adaptor.addChild(root_1, stream_simpleid.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;
				case 3 :
					// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:127:5: compositeid
					{
					pushFollow(FOLLOW_compositeid_in_term400);
					compositeid37=compositeid();
					state._fsp--;

					stream_compositeid.add(compositeid37.getTree());
					// AST REWRITE
					// elements: compositeid
					// token labels: 
					// rule labels: retval
					// token list labels: 
					// rule list labels: 
					// wildcard labels: 
					retval.tree = root_0;
					RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

					root_0 = (Object)adaptor.nil();
					// 127:17: -> ^( CONSTANT compositeid )
					{
						// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:127:20: ^( CONSTANT compositeid )
						{
						Object root_1 = (Object)adaptor.nil();
						root_1 = (Object)adaptor.becomeRoot((Object)adaptor.create(CONSTANT, "CONSTANT"), root_1);
						adaptor.addChild(root_1, stream_compositeid.nextTree());
						adaptor.addChild(root_0, root_1);
						}

					}


					retval.tree = root_0;

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "term"


	public static class id_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "id"
	// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:129:1: id : ( STRING ) ;
	public final ConjunctiveQueryParser.id_return id() throws RecognitionException {
		ConjunctiveQueryParser.id_return retval = new ConjunctiveQueryParser.id_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token STRING38=null;

		Object STRING38_tree=null;

		try {
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:129:4: ( ( STRING ) )
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:129:6: ( STRING )
			{
			root_0 = (Object)adaptor.nil();


			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:129:6: ( STRING )
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:129:7: STRING
			{
			STRING38=(Token)match(input,STRING,FOLLOW_STRING_in_id417); 
			STRING38_tree = (Object)adaptor.create(STRING38);
			adaptor.addChild(root_0, STRING38_tree);

			}

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "id"


	public static class simpleid_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "simpleid"
	// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:130:1: simpleid : ( '<' URLSTRING '>' | '<' STRING '>' );
	public final ConjunctiveQueryParser.simpleid_return simpleid() throws RecognitionException {
		ConjunctiveQueryParser.simpleid_return retval = new ConjunctiveQueryParser.simpleid_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token char_literal39=null;
		Token URLSTRING40=null;
		Token char_literal41=null;
		Token char_literal42=null;
		Token STRING43=null;
		Token char_literal44=null;

		Object char_literal39_tree=null;
		Object URLSTRING40_tree=null;
		Object char_literal41_tree=null;
		Object char_literal42_tree=null;
		Object STRING43_tree=null;
		Object char_literal44_tree=null;

		try {
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:130:10: ( '<' URLSTRING '>' | '<' STRING '>' )
			int alt8=2;
			int LA8_0 = input.LA(1);
			if ( (LA8_0==25) ) {
				int LA8_1 = input.LA(2);
				if ( (LA8_1==URLSTRING) ) {
					alt8=1;
				}
				else if ( (LA8_1==STRING) ) {
					alt8=2;
				}

				else {
					int nvaeMark = input.mark();
					try {
						input.consume();
						NoViableAltException nvae =
							new NoViableAltException("", 8, 1, input);
						throw nvae;
					} finally {
						input.rewind(nvaeMark);
					}
				}

			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 8, 0, input);
				throw nvae;
			}

			switch (alt8) {
				case 1 :
					// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:130:12: '<' URLSTRING '>'
					{
					root_0 = (Object)adaptor.nil();


					char_literal39=(Token)match(input,25,FOLLOW_25_in_simpleid425); 
					char_literal39_tree = (Object)adaptor.create(char_literal39);
					adaptor.addChild(root_0, char_literal39_tree);

					URLSTRING40=(Token)match(input,URLSTRING,FOLLOW_URLSTRING_in_simpleid427); 
					URLSTRING40_tree = (Object)adaptor.create(URLSTRING40);
					adaptor.addChild(root_0, URLSTRING40_tree);

					char_literal41=(Token)match(input,27,FOLLOW_27_in_simpleid429); 
					char_literal41_tree = (Object)adaptor.create(char_literal41);
					adaptor.addChild(root_0, char_literal41_tree);

					}
					break;
				case 2 :
					// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:130:32: '<' STRING '>'
					{
					root_0 = (Object)adaptor.nil();


					char_literal42=(Token)match(input,25,FOLLOW_25_in_simpleid433); 
					char_literal42_tree = (Object)adaptor.create(char_literal42);
					adaptor.addChild(root_0, char_literal42_tree);

					STRING43=(Token)match(input,STRING,FOLLOW_STRING_in_simpleid435); 
					STRING43_tree = (Object)adaptor.create(STRING43);
					adaptor.addChild(root_0, STRING43_tree);

					char_literal44=(Token)match(input,27,FOLLOW_27_in_simpleid437); 
					char_literal44_tree = (Object)adaptor.create(char_literal44);
					adaptor.addChild(root_0, char_literal44_tree);

					}
					break;

			}
			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "simpleid"


	public static class variable_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "variable"
	// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:133:1: variable : ( '?' ) id -> ^( id ) ;
	public final ConjunctiveQueryParser.variable_return variable() throws RecognitionException {
		ConjunctiveQueryParser.variable_return retval = new ConjunctiveQueryParser.variable_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token char_literal45=null;
		ParserRuleReturnScope id46 =null;

		Object char_literal45_tree=null;
		RewriteRuleTokenStream stream_28=new RewriteRuleTokenStream(adaptor,"token 28");
		RewriteRuleSubtreeStream stream_id=new RewriteRuleSubtreeStream(adaptor,"rule id");

		try {
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:133:9: ( ( '?' ) id -> ^( id ) )
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:134:3: ( '?' ) id
			{
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:134:3: ( '?' )
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:134:4: '?'
			{
			char_literal45=(Token)match(input,28,FOLLOW_28_in_variable448);  
			stream_28.add(char_literal45);

			}

			pushFollow(FOLLOW_id_in_variable451);
			id46=id();
			state._fsp--;

			stream_id.add(id46.getTree());
			// AST REWRITE
			// elements: id
			// token labels: 
			// rule labels: retval
			// token list labels: 
			// rule list labels: 
			// wildcard labels: 
			retval.tree = root_0;
			RewriteRuleSubtreeStream stream_retval=new RewriteRuleSubtreeStream(adaptor,"rule retval",retval!=null?retval.getTree():null);

			root_0 = (Object)adaptor.nil();
			// 134:12: -> ^( id )
			{
				// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:134:15: ^( id )
				{
				Object root_1 = (Object)adaptor.nil();
				root_1 = (Object)adaptor.becomeRoot(stream_id.nextNode(), root_1);
				adaptor.addChild(root_0, root_1);
				}

			}


			retval.tree = root_0;

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "variable"


	public static class url_return extends ParserRuleReturnScope {
		Object tree;
		@Override
		public Object getTree() { return tree; }
	};


	// $ANTLR start "url"
	// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:136:2: url : ( URLSTRING ) ;
	public final ConjunctiveQueryParser.url_return url() throws RecognitionException {
		ConjunctiveQueryParser.url_return retval = new ConjunctiveQueryParser.url_return();
		retval.start = input.LT(1);

		Object root_0 = null;

		Token URLSTRING47=null;

		Object URLSTRING47_tree=null;

		try {
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:136:6: ( ( URLSTRING ) )
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:136:8: ( URLSTRING )
			{
			root_0 = (Object)adaptor.nil();


			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:136:8: ( URLSTRING )
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:136:9: URLSTRING
			{
			URLSTRING47=(Token)match(input,URLSTRING,FOLLOW_URLSTRING_in_url469); 
			URLSTRING47_tree = (Object)adaptor.create(URLSTRING47);
			adaptor.addChild(root_0, URLSTRING47_tree);

			}

			}

			retval.stop = input.LT(-1);

			retval.tree = (Object)adaptor.rulePostProcessing(root_0);
			adaptor.setTokenBoundaries(retval.tree, retval.start, retval.stop);

		}
		catch (RecognitionException re) {
			reportError(re);
			recover(input,re);
			retval.tree = (Object)adaptor.errorNode(input, retval.start, input.LT(-1), re);
		}
		finally {
			// do for sure before leaving
		}
		return retval;
	}
	// $ANTLR end "url"

	// Delegated rules



	public static final BitSet FOLLOW_prefixlist_in_cq132 = new BitSet(new long[]{0x0000000000008000L});
	public static final BitSet FOLLOW_rulebody_in_cq134 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_prefix_in_prefixlist154 = new BitSet(new long[]{0x0000000000400002L});
	public static final BitSet FOLLOW_22_in_prefixlist157 = new BitSet(new long[]{0x0000000020000000L});
	public static final BitSet FOLLOW_prefix_in_prefixlist159 = new BitSet(new long[]{0x0000000000400002L});
	public static final BitSet FOLLOW_29_in_prefix181 = new BitSet(new long[]{0x0000000000008000L});
	public static final BitSet FOLLOW_id_in_prefix183 = new BitSet(new long[]{0x0000000001000000L});
	public static final BitSet FOLLOW_24_in_prefix185 = new BitSet(new long[]{0x0000000002000000L});
	public static final BitSet FOLLOW_25_in_prefix187 = new BitSet(new long[]{0x0000000000020000L});
	public static final BitSet FOLLOW_url_in_prefix189 = new BitSet(new long[]{0x0000000008000000L});
	public static final BitSet FOLLOW_27_in_prefix191 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_headatom_in_rulebody213 = new BitSet(new long[]{0x0000000005000000L});
	public static final BitSet FOLLOW_26_in_rulebody216 = new BitSet(new long[]{0x0000000000008000L});
	public static final BitSet FOLLOW_24_in_rulebody218 = new BitSet(new long[]{0x0000000000008000L});
	public static final BitSet FOLLOW_body_in_rulebody221 = new BitSet(new long[]{0x0000000000800002L});
	public static final BitSet FOLLOW_23_in_rulebody223 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_atom_in_body245 = new BitSet(new long[]{0x0000000000400002L});
	public static final BitSet FOLLOW_22_in_body248 = new BitSet(new long[]{0x0000000000008000L});
	public static final BitSet FOLLOW_atom_in_body250 = new BitSet(new long[]{0x0000000000400002L});
	public static final BitSet FOLLOW_id_in_headatom276 = new BitSet(new long[]{0x0000000000100000L});
	public static final BitSet FOLLOW_20_in_headatom278 = new BitSet(new long[]{0x0000000012008000L});
	public static final BitSet FOLLOW_term_in_headatom280 = new BitSet(new long[]{0x0000000000600000L});
	public static final BitSet FOLLOW_22_in_headatom283 = new BitSet(new long[]{0x0000000012008000L});
	public static final BitSet FOLLOW_term_in_headatom285 = new BitSet(new long[]{0x0000000000600000L});
	public static final BitSet FOLLOW_21_in_headatom289 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_compositeid_in_atom309 = new BitSet(new long[]{0x0000000000100000L});
	public static final BitSet FOLLOW_20_in_atom311 = new BitSet(new long[]{0x0000000012008000L});
	public static final BitSet FOLLOW_term_in_atom313 = new BitSet(new long[]{0x0000000000600000L});
	public static final BitSet FOLLOW_22_in_atom316 = new BitSet(new long[]{0x0000000012008000L});
	public static final BitSet FOLLOW_term_in_atom318 = new BitSet(new long[]{0x0000000000600000L});
	public static final BitSet FOLLOW_21_in_atom322 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_id_in_compositeid342 = new BitSet(new long[]{0x0000000001000000L});
	public static final BitSet FOLLOW_24_in_compositeid345 = new BitSet(new long[]{0x0000000000008000L});
	public static final BitSet FOLLOW_id_in_compositeid348 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_variable_in_term371 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_simpleid_in_term386 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_compositeid_in_term400 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_STRING_in_id417 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_25_in_simpleid425 = new BitSet(new long[]{0x0000000000020000L});
	public static final BitSet FOLLOW_URLSTRING_in_simpleid427 = new BitSet(new long[]{0x0000000008000000L});
	public static final BitSet FOLLOW_27_in_simpleid429 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_25_in_simpleid433 = new BitSet(new long[]{0x0000000000008000L});
	public static final BitSet FOLLOW_STRING_in_simpleid435 = new BitSet(new long[]{0x0000000008000000L});
	public static final BitSet FOLLOW_27_in_simpleid437 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_28_in_variable448 = new BitSet(new long[]{0x0000000000008000L});
	public static final BitSet FOLLOW_id_in_variable451 = new BitSet(new long[]{0x0000000000000002L});
	public static final BitSet FOLLOW_URLSTRING_in_url469 = new BitSet(new long[]{0x0000000000000002L});
}

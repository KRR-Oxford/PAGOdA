// $ANTLR 3.5 /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g 2013-12-13 14:41:34

package org.semanticweb.karma2.model.cqparser;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

@SuppressWarnings("all")
public class ConjunctiveQueryLexer extends Lexer {
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
	// delegators
	public Lexer[] getDelegates() {
		return new Lexer[] {};
	}

	public ConjunctiveQueryLexer() {} 
	public ConjunctiveQueryLexer(CharStream input) {
		this(input, new RecognizerSharedState());
	}
	public ConjunctiveQueryLexer(CharStream input, RecognizerSharedState state) {
		super(input,state);
	}
	@Override public String getGrammarFileName() { return "/home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g"; }

	// $ANTLR start "T__20"
	public final void mT__20() throws RecognitionException {
		try {
			int _type = T__20;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:11:7: ( '(' )
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:11:9: '('
			{
			match('('); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__20"

	// $ANTLR start "T__21"
	public final void mT__21() throws RecognitionException {
		try {
			int _type = T__21;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:12:7: ( ')' )
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:12:9: ')'
			{
			match(')'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__21"

	// $ANTLR start "T__22"
	public final void mT__22() throws RecognitionException {
		try {
			int _type = T__22;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:13:7: ( ',' )
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:13:9: ','
			{
			match(','); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__22"

	// $ANTLR start "T__23"
	public final void mT__23() throws RecognitionException {
		try {
			int _type = T__23;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:14:7: ( '.' )
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:14:9: '.'
			{
			match('.'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__23"

	// $ANTLR start "T__24"
	public final void mT__24() throws RecognitionException {
		try {
			int _type = T__24;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:15:7: ( ':' )
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:15:9: ':'
			{
			match(':'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__24"

	// $ANTLR start "T__25"
	public final void mT__25() throws RecognitionException {
		try {
			int _type = T__25;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:16:7: ( '<' )
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:16:9: '<'
			{
			match('<'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__25"

	// $ANTLR start "T__26"
	public final void mT__26() throws RecognitionException {
		try {
			int _type = T__26;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:17:7: ( '<-' )
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:17:9: '<-'
			{
			match("<-"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__26"

	// $ANTLR start "T__27"
	public final void mT__27() throws RecognitionException {
		try {
			int _type = T__27;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:18:7: ( '>' )
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:18:9: '>'
			{
			match('>'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__27"

	// $ANTLR start "T__28"
	public final void mT__28() throws RecognitionException {
		try {
			int _type = T__28;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:19:7: ( '?' )
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:19:9: '?'
			{
			match('?'); 
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__28"

	// $ANTLR start "T__29"
	public final void mT__29() throws RecognitionException {
		try {
			int _type = T__29;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:20:7: ( 'prefix' )
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:20:9: 'prefix'
			{
			match("prefix"); 

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "T__29"

	// $ANTLR start "URLSTRING"
	public final void mURLSTRING() throws RecognitionException {
		try {
			int _type = URLSTRING;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:138:11: ( ( 'http://' | 'file:/' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '/' | '#' | '.' | '-' | '~' | '_' )+ )
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:138:13: ( 'http://' | 'file:/' ) ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '/' | '#' | '.' | '-' | '~' | '_' )+
			{
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:138:13: ( 'http://' | 'file:/' )
			int alt1=2;
			int LA1_0 = input.LA(1);
			if ( (LA1_0=='h') ) {
				alt1=1;
			}
			else if ( (LA1_0=='f') ) {
				alt1=2;
			}

			else {
				NoViableAltException nvae =
					new NoViableAltException("", 1, 0, input);
				throw nvae;
			}

			switch (alt1) {
				case 1 :
					// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:138:14: 'http://'
					{
					match("http://"); 

					}
					break;
				case 2 :
					// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:138:24: 'file:/'
					{
					match("file:/"); 

					}
					break;

			}

			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:138:34: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '/' | '#' | '.' | '-' | '~' | '_' )+
			int cnt2=0;
			loop2:
			while (true) {
				int alt2=2;
				int LA2_0 = input.LA(1);
				if ( (LA2_0=='#'||(LA2_0 >= '-' && LA2_0 <= '9')||(LA2_0 >= 'A' && LA2_0 <= 'Z')||LA2_0=='_'||(LA2_0 >= 'a' && LA2_0 <= 'z')||LA2_0=='~') ) {
					alt2=1;
				}

				switch (alt2) {
				case 1 :
					// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:
					{
					if ( input.LA(1)=='#'||(input.LA(1) >= '-' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z')||input.LA(1)=='~' ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					if ( cnt2 >= 1 ) break loop2;
					EarlyExitException eee = new EarlyExitException(2, input);
					throw eee;
				}
				cnt2++;
			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "URLSTRING"

	// $ANTLR start "STRING"
	public final void mSTRING() throws RecognitionException {
		try {
			int _type = STRING;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:139:9: ( ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '/' | '#' | '.' | '-' | '_' )+ )
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:139:13: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '/' | '#' | '.' | '-' | '_' )+
			{
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:139:13: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '/' | '#' | '.' | '-' | '_' )+
			int cnt3=0;
			loop3:
			while (true) {
				int alt3=2;
				int LA3_0 = input.LA(1);
				if ( (LA3_0=='#'||(LA3_0 >= '-' && LA3_0 <= '9')||(LA3_0 >= 'A' && LA3_0 <= 'Z')||LA3_0=='_'||(LA3_0 >= 'a' && LA3_0 <= 'z')) ) {
					alt3=1;
				}

				switch (alt3) {
				case 1 :
					// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:
					{
					if ( input.LA(1)=='#'||(input.LA(1) >= '-' && input.LA(1) <= '9')||(input.LA(1) >= 'A' && input.LA(1) <= 'Z')||input.LA(1)=='_'||(input.LA(1) >= 'a' && input.LA(1) <= 'z') ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					if ( cnt3 >= 1 ) break loop3;
					EarlyExitException eee = new EarlyExitException(3, input);
					throw eee;
				}
				cnt3++;
			}

			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "STRING"

	// $ANTLR start "WS"
	public final void mWS() throws RecognitionException {
		try {
			int _type = WS;
			int _channel = DEFAULT_TOKEN_CHANNEL;
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:140:5: ( ( ' ' | '\\n' | '\\r' )+ )
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:140:7: ( ' ' | '\\n' | '\\r' )+
			{
			// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:140:7: ( ' ' | '\\n' | '\\r' )+
			int cnt4=0;
			loop4:
			while (true) {
				int alt4=2;
				int LA4_0 = input.LA(1);
				if ( (LA4_0=='\n'||LA4_0=='\r'||LA4_0==' ') ) {
					alt4=1;
				}

				switch (alt4) {
				case 1 :
					// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:
					{
					if ( input.LA(1)=='\n'||input.LA(1)=='\r'||input.LA(1)==' ' ) {
						input.consume();
					}
					else {
						MismatchedSetException mse = new MismatchedSetException(null,input);
						recover(mse);
						throw mse;
					}
					}
					break;

				default :
					if ( cnt4 >= 1 ) break loop4;
					EarlyExitException eee = new EarlyExitException(4, input);
					throw eee;
				}
				cnt4++;
			}

			_channel=HIDDEN;
			}

			state.type = _type;
			state.channel = _channel;
		}
		finally {
			// do for sure before leaving
		}
	}
	// $ANTLR end "WS"

	@Override
	public void mTokens() throws RecognitionException {
		// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:1:8: ( T__20 | T__21 | T__22 | T__23 | T__24 | T__25 | T__26 | T__27 | T__28 | T__29 | URLSTRING | STRING | WS )
		int alt5=13;
		switch ( input.LA(1) ) {
		case '(':
			{
			alt5=1;
			}
			break;
		case ')':
			{
			alt5=2;
			}
			break;
		case ',':
			{
			alt5=3;
			}
			break;
		case '.':
			{
			int LA5_4 = input.LA(2);
			if ( (LA5_4=='#'||(LA5_4 >= '-' && LA5_4 <= '9')||(LA5_4 >= 'A' && LA5_4 <= 'Z')||LA5_4=='_'||(LA5_4 >= 'a' && LA5_4 <= 'z')) ) {
				alt5=12;
			}

			else {
				alt5=4;
			}

			}
			break;
		case ':':
			{
			alt5=5;
			}
			break;
		case '<':
			{
			int LA5_6 = input.LA(2);
			if ( (LA5_6=='-') ) {
				alt5=7;
			}

			else {
				alt5=6;
			}

			}
			break;
		case '>':
			{
			alt5=8;
			}
			break;
		case '?':
			{
			alt5=9;
			}
			break;
		case 'p':
			{
			int LA5_9 = input.LA(2);
			if ( (LA5_9=='r') ) {
				int LA5_17 = input.LA(3);
				if ( (LA5_17=='e') ) {
					int LA5_20 = input.LA(4);
					if ( (LA5_20=='f') ) {
						int LA5_23 = input.LA(5);
						if ( (LA5_23=='i') ) {
							int LA5_26 = input.LA(6);
							if ( (LA5_26=='x') ) {
								int LA5_28 = input.LA(7);
								if ( (LA5_28=='#'||(LA5_28 >= '-' && LA5_28 <= '9')||(LA5_28 >= 'A' && LA5_28 <= 'Z')||LA5_28=='_'||(LA5_28 >= 'a' && LA5_28 <= 'z')) ) {
									alt5=12;
								}

								else {
									alt5=10;
								}

							}

							else {
								alt5=12;
							}

						}

						else {
							alt5=12;
						}

					}

					else {
						alt5=12;
					}

				}

				else {
					alt5=12;
				}

			}

			else {
				alt5=12;
			}

			}
			break;
		case 'h':
			{
			int LA5_10 = input.LA(2);
			if ( (LA5_10=='t') ) {
				int LA5_18 = input.LA(3);
				if ( (LA5_18=='t') ) {
					int LA5_21 = input.LA(4);
					if ( (LA5_21=='p') ) {
						int LA5_24 = input.LA(5);
						if ( (LA5_24==':') ) {
							alt5=11;
						}

						else {
							alt5=12;
						}

					}

					else {
						alt5=12;
					}

				}

				else {
					alt5=12;
				}

			}

			else {
				alt5=12;
			}

			}
			break;
		case 'f':
			{
			int LA5_11 = input.LA(2);
			if ( (LA5_11=='i') ) {
				int LA5_19 = input.LA(3);
				if ( (LA5_19=='l') ) {
					int LA5_22 = input.LA(4);
					if ( (LA5_22=='e') ) {
						int LA5_25 = input.LA(5);
						if ( (LA5_25==':') ) {
							alt5=11;
						}

						else {
							alt5=12;
						}

					}

					else {
						alt5=12;
					}

				}

				else {
					alt5=12;
				}

			}

			else {
				alt5=12;
			}

			}
			break;
		case '#':
		case '-':
		case '/':
		case '0':
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9':
		case 'A':
		case 'B':
		case 'C':
		case 'D':
		case 'E':
		case 'F':
		case 'G':
		case 'H':
		case 'I':
		case 'J':
		case 'K':
		case 'L':
		case 'M':
		case 'N':
		case 'O':
		case 'P':
		case 'Q':
		case 'R':
		case 'S':
		case 'T':
		case 'U':
		case 'V':
		case 'W':
		case 'X':
		case 'Y':
		case 'Z':
		case '_':
		case 'a':
		case 'b':
		case 'c':
		case 'd':
		case 'e':
		case 'g':
		case 'i':
		case 'j':
		case 'k':
		case 'l':
		case 'm':
		case 'n':
		case 'o':
		case 'q':
		case 'r':
		case 's':
		case 't':
		case 'u':
		case 'v':
		case 'w':
		case 'x':
		case 'y':
		case 'z':
			{
			alt5=12;
			}
			break;
		case '\n':
		case '\r':
		case ' ':
			{
			alt5=13;
			}
			break;
		default:
			NoViableAltException nvae =
				new NoViableAltException("", 5, 0, input);
			throw nvae;
		}
		switch (alt5) {
			case 1 :
				// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:1:10: T__20
				{
				mT__20(); 

				}
				break;
			case 2 :
				// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:1:16: T__21
				{
				mT__21(); 

				}
				break;
			case 3 :
				// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:1:22: T__22
				{
				mT__22(); 

				}
				break;
			case 4 :
				// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:1:28: T__23
				{
				mT__23(); 

				}
				break;
			case 5 :
				// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:1:34: T__24
				{
				mT__24(); 

				}
				break;
			case 6 :
				// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:1:40: T__25
				{
				mT__25(); 

				}
				break;
			case 7 :
				// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:1:46: T__26
				{
				mT__26(); 

				}
				break;
			case 8 :
				// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:1:52: T__27
				{
				mT__27(); 

				}
				break;
			case 9 :
				// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:1:58: T__28
				{
				mT__28(); 

				}
				break;
			case 10 :
				// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:1:64: T__29
				{
				mT__29(); 

				}
				break;
			case 11 :
				// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:1:70: URLSTRING
				{
				mURLSTRING(); 

				}
				break;
			case 12 :
				// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:1:80: STRING
				{
				mSTRING(); 

				}
				break;
			case 13 :
				// /home/yzhou/workspace/KARMA/src/org/semanticweb/karma2/model/cqparser/ConjunctiveQuery.g:1:87: WS
				{
				mWS(); 

				}
				break;

		}
	}



}

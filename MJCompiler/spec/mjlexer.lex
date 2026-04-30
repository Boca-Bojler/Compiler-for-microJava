
package rs.ac.bg.etf.pp1;

import java_cup.runtime.Symbol;



%%

%{

	// ukljucivanje informacije o poziciji tokena
	private Symbol new_symbol(int type) {
		return new Symbol(type, yyline+1, yycolumn);
	}
	
	// ukljucivanje informacije o poziciji tokena
	private Symbol new_symbol(int type, Object value) {
		return new Symbol(type, yyline+1, yycolumn, value);
	}

%}

%cup
%line
%column

%xstate COMMENT

%eofval{
	return new_symbol(sym.EOF);
%eofval}


%%

" " 	{ }
"\b" 	{ }
"\t" 	{ }
"\r\n" 	{ }
"\f" 	{ }


"program"   { return new_symbol(sym.PROG, yytext()); }
"return" 	{ return new_symbol(sym.RETURN, yytext()); }
"print" 	{ return new_symbol(sym.PRINT, yytext()); }
"void" 		{ return new_symbol(sym.VOID, yytext()); }
"const" 	{ return new_symbol(sym.CONST, yytext()); }
"new"		{ return new_symbol(sym.NEW, yytext()); } 
"read"		{ return new_symbol(sym.READ, yytext()); }
"enum"		{ return new_symbol(sym.ENUM, yytext()); }
"length"	{ return new_symbol(sym.LENGTH, yytext()); }
"final"     { return new_symbol(sym.FINAL, yytext()); }

"+" 		{ return new_symbol(sym.PLUS, yytext()); }
"-" 		{ return new_symbol(sym.MINUS, yytext()); }
"*"			{ return new_symbol(sym.MUL, yytext()); }
"/"			{ return new_symbol(sym.DIV, yytext()); }
"%"			{ return new_symbol(sym.REM, yytext()); }
"=" 		{ return new_symbol(sym.ASSIGN, yytext()); }
";" 		{ return new_symbol(sym.SEMI, yytext()); }
"," 		{ return new_symbol(sym.COMMA, yytext()); }
"."			{ return new_symbol(sym.DOT, yytext()); }
":"			{ return new_symbol(sym.COLON, yytext()); }
"(" 		{ return new_symbol(sym.LPAREN, yytext()); }
")" 		{ return new_symbol(sym.RPAREN, yytext()); }
"{" 		{ return new_symbol(sym.LBRACE, yytext()); }
"}"			{ return new_symbol(sym.RBRACE, yytext()); }
"["			{ return new_symbol(sym.LBRACKET, yytext()); }
"]"         { return new_symbol(sym.RBRACKET, yytext()); }
"++"        { return new_symbol(sym.INC, yytext()); }
"--"		{ return new_symbol(sym.DEC, yytext()); }
"=="		{ return new_symbol(sym.EQUAL, yytext()); }
"!="		{ return new_symbol(sym.NOTEQUAL, yytext()); }
">"			{ return new_symbol(sym.GREATER, yytext()); }
">="		{ return new_symbol(sym.GREATEREQUAL, yytext()); }
"<"			{ return new_symbol(sym.LOWER, yytext()); }
"<="		{ return new_symbol(sym.LOWEREQUAL, yytext()); }
"?" 		{ return new_symbol(sym.QUESTION, yytext()); }


"//" 				{yybegin(COMMENT);}
<COMMENT> . 		{yybegin(COMMENT);}
<COMMENT> "\r\n"	{yybegin(YYINITIAL);}

[0-9]+							{ return new_symbol(sym.NUMBER, Integer.parseInt(yytext())); }
"'"."'" 						{ return new_symbol(sym.CHARACTER, new Character (yytext().charAt(1))); }
("true"|"false") 				{ return new_symbol(sym.BOOL, yytext().equals("true") ? 1 : 0); }
([a-zA-Z])[a-zA-Z0-9_]* 	        { return new_symbol(sym.IDENT, yytext()); }

. 								{ System.err.println("Leksicka greska ("+yytext()+") na liniji "+ (yyline + 1) + "u koloni" + (yycolumn + 1) + "\n"); }


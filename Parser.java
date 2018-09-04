import java.util.Stack;
import java.util.ArrayList;
//import java.util.Collections;

public class Parser {

    private Symtab symtab = new Symtab();
    
    // the first sets.
    // note: we cheat sometimes:
    // when there is only a single token in the set,
    // we generally just compare tkrep with the first token.
    TK f_declarations[] = {TK.VAR, TK.none};
    TK f_statement[] = {TK.ID, TK.PRINT, TK.IF, TK.DO, TK.FA, TK.SKIP, TK.STOP, TK.BREAK, 
    TK.DUMP,TK.EXCNT, TK.none};
    TK f_print[] = {TK.PRINT, TK.none};
    TK f_assignment[] = {TK.ID, TK.none};
    TK f_if[] = {TK.IF, TK.none};
    TK f_do[] = {TK.DO, TK.none};
    TK f_fa[] = {TK.FA, TK.none};
    TK f_expression[] = {TK.ID, TK.NUM, TK.LPAREN, TK.none};
    TK f_skip[] = {TK.SKIP, TK.none};
    TK f_stop[] = {TK.STOP, TK.none};
    TK f_break[] = {TK.BREAK, TK.none};
    TK f_dump[] = {TK.DUMP, TK.none};
    TK f_excnt[] = {TK.EXCNT, TK.none};
    TK f_factor[] = {TK.MAX, TK.LPAREN, TK.ID, TK.NUM, TK.MODULO, TK.none}; 
    TK f_predef[] = {TK.MAX, TK.MODULO, TK.none};


    // tok is global to all these parsing methods;
    // scan just calls the scanner's scan method and saves the result in tok.
    private Token tok; // the current token
    private void scan() {
        tok = scanner.scan();
    }

    private Scan scanner;
    Parser(Scan scanner) {
        this.scanner = scanner;
        scan();
        program();
        if( tok.kind != TK.EOF )
            parse_error("junk after logical end of program");
        symtab.reportVariables();
    }

    // print something in the generated code
    private void gcprint(String str) {
        System.out.println(str);
    }
    // print identifier in the generated code
    // it prefixes x_ in case id conflicts with C keyword.
    private void gcprintid(String str) {
        System.out.println("x_"+str);
    }

    private void program() {
        // generate the E math functions:
        gcprint("int esquare(int x){ return x*x;}");
        gcprint("#include <math.h>");
        gcprint("int esqrt(int x){ double y; if (x < 0) return 0; y = sqrt((double)x); return (int)y;}");

        gcprint("#include <stdio.h>");
        gcprint("#include <stdlib.h>");
        gcprint("#define MAX(a,b) (((a)>(b))?(a):(b))");

        gcprint("int emod(int a, int b){ if (b == 0){ printf(\"\\nmod(a,b) with b=0\\n\"); exit(1);} int rem = a%b; if (b > 0) return (rem >= 0) ? rem : rem + b; else return -emod(-a, -b);}");

        gcprint("int main() {");
        
        gcprint("int excnt[101];");
        gcprint("for(int i = 0; i <= 100; i++) excnt[i] = 0;");

        block();
        gcprint("return 0; }");
    }

    private void block() {
        symtab.begin_st_block();
	gcprint("{");
        if( first(f_declarations) ) {
            declarations();
        }
        statement_list();
        symtab.end_st_block();
	gcprint("}");
    }

    private void declarations() {
        mustbe(TK.VAR);
        while( is(TK.ID) ) {
            if( symtab.add_var_entry(tok.string,tok.lineNumber) ) {
                gcprint("int");
                gcprintid(tok.string);
                gcprint("= -12345;");
            }
            scan();
        }
        mustbe(TK.RAV);
    }

    private int excnt_count = 0;
    private int[] excnt_lineNumber = new int[101];
    private int loop_id = 0;
    private int loop_level = 0;
    private boolean break_check = false;
    private Stack<String> label_stack = new Stack<String>();

    private void statement_list(){
        boolean stop_check = false;
        
        while( first(f_statement) ) {
            if (stop_check) {
                System.err.println("warning: on line " + tok.lineNumber + " statement(s) follows stop statement");
                stop_check = false;
            }

            if (break_check && symtab.get_level() > 0) {
                System.err.println("warning: on line " + tok.lineNumber + " statement(s) follows break statement");
                break_check = false;
            }

            if (first(f_stop))
                stop_check = true;

            statement();
        }

        if (symtab.get_level() == 0) {
            print_excnt();
        }

        break_check = false;
    }

    private void statement(){
        if( first(f_assignment) )
            assignment();
        else if( first(f_print) )
            print();
        else if( first(f_if) )
            ifproc();
        else if( first(f_do) ){
            loop_id++;
            loop_level++;
            label_stack.push("LOOP" + Integer.toString(loop_id) + "LEVEL" + Integer.toString(loop_level));
            doproc();
            loop_level--;
            String label = label_stack.pop();
            gcprint(label + ":");
            gcprint("if (0) printf(\"Stupid\");");
        }
        else if( first(f_fa) ){
            loop_id++;
            loop_level++;
            label_stack.push("LOOP" + Integer.toString(loop_id) + "LEVEL" + Integer.toString(loop_level));
            fa();
            loop_level--;
            String label = label_stack.pop();
            gcprint(label + ":");
            gcprint("if (0) printf(\"Stupid\");");
        }
        else if( first(f_skip))
            skip();
        else if (first(f_stop)){
            stop();
        }
        else if (first(f_break)) {
            breakStmt();
        }
        else if (first(f_dump)) {
            dump();
        }
        else if (first(f_excnt)) {
            excnt();
        }

        else
            parse_error("statement");
    }

    private void print_excnt(){
        gcprint("EXCNT_TABLE:");
        gcprint("if (0) printf(\"Stupid\");");
        if (excnt_count > 0){
            gcprint("printf(\"---- Execution Counts ----\\n\");");
            gcprint("printf(\" num line    count\\n\");");
            for(int i = 1; i <= excnt_count; i++)
                gcprint("printf(\"%4d%5d%9d\\n\","+ i + "," + excnt_lineNumber[i] + " , excnt[" + i +"]);");
        }
    }

    private void assignment(){
        String id = tok.string;
        int lno = tok.lineNumber; // save it too before mustbe!
        mustbe(TK.ID);
        referenced_id(id, true, lno);
        gcprintid(id);
        mustbe(TK.ASSIGN);
        gcprint("=");
        expression();
        gcprint(";");
    }



    private void skip() {
        mustbe(TK.SKIP);
    }

    private void stop() {
        mustbe(TK.STOP);
        gcprint("goto EXCNT_TABLE;");
        gcprint("exit(0);");
    }


    private void excnt() {
        int no = tok.lineNumber;
        mustbe(TK.EXCNT);
        excnt_count++;
        if (excnt_count > 100) {
            System.err.println("can't parse: line " + no + " too many EXCNT (more than 100)");
            System.exit(1);
        }
        gcprint("excnt[" + excnt_count + "] ++ ;");
        excnt_lineNumber[excnt_count] = no;    
    }


    private void dump() {
        int no = tok.lineNumber;

        mustbe(TK.DUMP);

        if (is(TK.NUM)) {
            // handle number
            int k = Integer.parseInt(tok.string);
            if (k > symtab.get_level()){
                System.err.println("warning: on line " + tok.lineNumber + " dump statement level (" + tok.string + ") exceeds block nesting level (" + Integer.toString(symtab.get_level()) + "). using " + Integer.toString(symtab.get_level()));
                k = symtab.get_level();
            }
            gcprint("printf(\"+++ dump on line " + tok.lineNumber + " of level " + k + " begin +++\\n\");");
            ArrayList<ArrayList<Entry>> list = new ArrayList<ArrayList<Entry>>(symtab.get_stack());
            for( ArrayList<Entry> entry_list : list){
                for( Entry e : entry_list) {
                    if (e.getLevel() == k){
                        gcprint("printf(\"%12d \", x_" + e.getName() + ");");
                        gcprint("printf(\"%3d \", " + e.getLineNumber()+ ");");
                        gcprint("printf(\"%3d \", " + e.getLevel()+ ");");
                        gcprint("printf(\"" + e.getName() + "\\n\");");
                    }
                }
            }
            gcprint("printf(\"--- dump on line " + tok.lineNumber + " of level " + k + " end ---\\n\");");
            scan();
        }
        else {
            gcprint("printf(\"+++ dump on line " + no + " of all levels begin +++\\n\");");
            ArrayList<ArrayList<Entry>> list = new ArrayList<ArrayList<Entry>>(symtab.get_stack());
            for( ArrayList<Entry> entry_list : list){
                for( Entry e : entry_list) {
                    gcprint("printf(\"%12d \", x_" + e.getName() + ");");
                    gcprint("printf(\"%3d \", " + e.getLineNumber()+ ");");
                    gcprint("printf(\"%3d \", " + e.getLevel()+ ");");
                    gcprint("printf(\"" + e.getName() + "\\n\");");
                }
            }
            gcprint("printf(\"--- dump on line " + no + " of all levels end ---\\n\");");
        }
    }

    private void breakStmt() {
        if (symtab.get_level() == 0){   // if level is not in block it is 0
            System.err.println("warning: on line " + tok.lineNumber + " break statement outside of loop ignored");
            mustbe(TK.BREAK);
        }
        else {
            mustbe(TK.BREAK);
            if (is(TK.NUM)) {
                int k = Integer.parseInt(tok.string);
                if (k == 0) {
                    System.err.println("warning: on line " + tok.lineNumber + " break 0 statement ignored");
                }
                else if (k > loop_level){
                    System.err.println("warning: on line " + tok.lineNumber + " break statement exceeding loop nesting ignored");   
                }
                else {
                    ArrayList<String> labels = new ArrayList<String>(label_stack);
                    gcprint("goto "+ labels.get(labels.size() - k) + ";");
                    break_check = true;
                }
                scan();
            }
            else{
                gcprint("break;");
                break_check = true;
            }
        }
        
    }


    private void print(){
        mustbe(TK.PRINT);
        gcprint("printf(\"%d\\n\", ");
        expression();
        gcprint(");");
    }

    private void ifproc(){
        mustbe(TK.IF);
        guarded_commands(TK.IF);
        mustbe(TK.FI);
    }

    private void doproc(){
        mustbe(TK.DO);
        gcprint("while(1){");
        guarded_commands(TK.DO);
        gcprint("}");
        mustbe(TK.OD);
    }

    private void fa(){
        mustbe(TK.FA);
        gcprint("for(");
        String id = tok.string;
        int lno = tok.lineNumber; // save it too before mustbe!
        mustbe(TK.ID);
        referenced_id(id, true, lno);
        gcprintid(id);
        mustbe(TK.ASSIGN);
        gcprint("=");
        expression();
        gcprint(";");
        mustbe(TK.TO);
        gcprintid(id);
        gcprint("<=");
        expression();
        gcprint(";");
        gcprintid(id);
        gcprint("++)");
        if( is(TK.ST) ) {
            gcprint("if( ");
            scan();
            expression();
            gcprint(")");
        }
        commands();
        mustbe(TK.AF);
    }

    private void guarded_commands(TK which){
        guarded_command();
        while( is(TK.BOX) ) {
            scan();
            gcprint("else");
            guarded_command();
        }
        if( is(TK.ELSE) ) {
            gcprint("else");
            scan();
            commands();
        }
        else if( which == TK.DO )
            gcprint("else break;");
    }

    private void guarded_command(){
        gcprint("if(");
        expression();
        gcprint(")");
        commands();
    }

    private void commands(){
        mustbe(TK.ARROW);
        gcprint("{");/* note: generate {} to simplify, e.g., st in fa. */
        block();
        gcprint("}");
    }

    private int expression(){
        int max_deep = 0;
        max_deep = Math.max(simple(), max_deep);
        while( is(TK.EQ) || is(TK.LT) || is(TK.GT) ||
               is(TK.NE) || is(TK.LE) || is(TK.GE)) {
            if( is(TK.EQ) ) gcprint("==");
            else if( is(TK.NE) ) gcprint("!=");
            else gcprint(tok.string);
            scan();
            max_deep = Math.max(simple(), max_deep);
        }
        return max_deep;
    }

    private int simple(){
        int max_deep = 0;
        max_deep = Math.max(term(), max_deep);
        while( is(TK.PLUS) || is(TK.MINUS) ) {
            gcprint(tok.string);
            scan();
            max_deep = Math.max(term(), max_deep);
        }
        return max_deep;
    }

    private int term(){
        int max_deep = 0;
        max_deep = Math.max(factor(), max_deep);
        while(  is(TK.TIMES) || is(TK.DIVIDE) || is(TK.REM)) {
            gcprint(tok.string);
            scan();
            max_deep = Math.max(factor(), max_deep);
        }
        return max_deep;
    }

    private int factor(){
        int max_deep = 0;
        
        if( is(TK.LPAREN) ) {
            gcprint("(");
            scan();
            expression();
            mustbe(TK.RPAREN);
            gcprint(")");
        }
        else if( is(TK.SQUARE) ) {
            gcprint("esquare(");
            scan();
            expression();
            gcprint(")");
        }
        else if( is(TK.SQRT) ) {
            gcprint("esqrt(");
            scan();
            expression();
            gcprint(")");
        }
        else if( is(TK.ID) ) {
            referenced_id(tok.string, false, tok.lineNumber);
            gcprintid(tok.string);
            scan();
        }
        else if( is(TK.NUM) ) {
            gcprint(tok.string);
            scan();
        }
        
        else if(is (TK.MODULO)) {
            gcprint("emod(");
            scan();
            mustbe(TK.LPAREN);
            expression();
            mustbe(TK.COMMA);
            gcprint(",");
            expression();
            mustbe(TK.RPAREN);
            gcprint(")");
        }
        else if (is (TK.MAX)) {
            gcprint("MAX(");
            scan();
            mustbe(TK.LPAREN);
            int max_deep1 = expression();
            mustbe(TK.COMMA);
            gcprint(",");
            int max_deep2 = expression();
            max_deep = Math.max(max_deep1, max_deep2) + 1;
            if (max_deep > 5) {
                System.err.println("can't parse: line " + tok.lineNumber + " max expressions nested too deeply (> 5 deep)");
                System.exit(1);
            }
            mustbe(TK.RPAREN);
            gcprint(")");
        }
        else
            parse_error("factor");
        return max_deep;
    }

    // be careful: use lno here, not tok.lineNumber
    // (which may have been advanced by now)
    private void referenced_id(String id, boolean assigned, int lno) {
        Entry e = symtab.search(id);
        if( e == null) {
            System.err.println("undeclared variable "+ id + " on line "
                               + lno);
            System.exit(1);
        }
        e.ref(assigned, lno);
    }

    // is current token what we want?
    private boolean is(TK tk) {
        return tk == tok.kind;
    }

    // ensure current token is tk and skip over it.
    private void mustbe(TK tk) {
        if( ! is(tk) ) {
            System.err.println( "mustbe: want " + tk + ", got " +
                                    tok);
            parse_error( "missing token (mustbe)" );
        }
        scan();
    }
    boolean first(TK [] set) {
        int k = 0;
        while(set[k] != TK.none && set[k] != tok.kind) {
            k++;
        }
        return set[k] != TK.none;
    }

    private void parse_error(String msg) {
        System.err.println( "can't parse: line "
                            + tok.lineNumber + " " + msg );
        System.exit(1);
    }
}

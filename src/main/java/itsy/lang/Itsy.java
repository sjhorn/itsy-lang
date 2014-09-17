package itsy.lang;

import itsy.antlr4.ItsyLexer;
import itsy.antlr4.ItsyParser;

import java.util.HashMap;
import java.util.Map;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class Itsy {
	Scope globalScope = new Scope();
	
	public void run(String file) {
		try {
            ItsyLexer lexer = new ItsyLexer(new ANTLRFileStream(file));
            ItsyParser parser = new ItsyParser(new CommonTokenStream(lexer));
            parser.setBuildParseTree(true);
            ParseTree tree = parser.parse();
            
            Map<String, Function> functions = new HashMap<String, Function>();
            SymbolVisitor symbolVisitor = new SymbolVisitor(functions);
            symbolVisitor.visit(tree);
            EvalVisitor visitor = new EvalVisitor(globalScope, functions);
            visitor.visit(tree);
        } catch (Exception e) {
            if ( e.getMessage() != null) {
                System.err.println(e.getMessage());
            } else {
                e.printStackTrace();
            }
        }
	}
	
    public static void main(String[] args) {
    	String main = args.length == 0 ? "src/main/itsy/test.it" : args[0];
        new Itsy().run(main);
    }
}

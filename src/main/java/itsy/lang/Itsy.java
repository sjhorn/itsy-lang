package itsy.lang;

import itsy.antlr4.ItsyLexer;
import itsy.antlr4.ItsyParser;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;

public class Itsy {
	Scope globalScope = new Scope();
	
	public void run(CharStream source) {
		try {
            ItsyLexer lexer = new ItsyLexer(source);
            ItsyParser parser = new ItsyParser(new CommonTokenStream(lexer));
            parser.setBuildParseTree(true);
            ParseTree tree = parser.parse();
            if (parser.getNumberOfSyntaxErrors() != 0) {
            	return;
            }
            
            Map<String, Function> functions = new HashMap<String, Function>();
            SymbolVisitor symbolVisitor = new SymbolVisitor(functions);
            symbolVisitor.visit(tree);
            EvalVisitor visitor = new EvalVisitor(globalScope, functions);
            visitor.visit(tree);
        } catch (AssertionError ae) {
        	System.err.println(ae.getMessage());
        } catch (EvalException ee) {
        	System.err.println(ee.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
        }
	}
	
	public static ANTLRInputStream resourceToString(String resourcePath) throws Exception {
        java.net.URL url = Itsy.class.getResource(resourcePath);
        java.net.URI uri = url.toURI();
        Path resPath;
        if (uri.toString().contains("!")) {
            final Map<String, String> env = new HashMap<>();
            final String[] array = uri.toString().split("!");
            final FileSystem fs = FileSystems.newFileSystem(URI.create(array[0]), env);
            resPath = fs.getPath(array[1]);
        } else {
            resPath = java.nio.file.Paths.get(uri);
        }
        return new ANTLRInputStream(new String(java.nio.file.Files.readAllBytes(resPath), "UTF8"));
	}
	
    public static void main(String[] args) throws Exception{
        CharStream main = args.length == 0 ? resourceToString("/itsy/test.it") : new ANTLRFileStream(args[0]);
        new Itsy().run(main);
    }
}

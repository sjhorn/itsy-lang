package itsy.lang;

import itsy.antlr4.ItsyBaseVisitor;
import itsy.antlr4.ItsyParser.FunctionDeclContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;


public class SymbolVisitor extends ItsyBaseVisitor<ItsyValue> {
    Map<String, Function> functions;
    
    public SymbolVisitor(Map<String, Function> functions) {
        this.functions = functions;
    }
    
    @Override
    public ItsyValue visitFunctionDecl(FunctionDeclContext ctx) {
        List<TerminalNode> params = ctx.idList() != null ? ctx.idList().IDENTIFIER() : new ArrayList<TerminalNode>(); 
        ParseTree block = ctx.block();
        String id = ctx.IDENTIFIER().getText() + params.size();
        functions.put(id, new Function(id, params, block));
        return ItsyValue.VOID;
    }
}

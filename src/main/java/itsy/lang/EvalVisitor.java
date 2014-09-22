package itsy.lang;

import itsy.antlr4.ItsyBaseVisitor;
import itsy.antlr4.ItsyParser;
import itsy.antlr4.ItsyParser.AndExpressionContext;
import itsy.antlr4.ItsyParser.AssertFunctionCallContext;
import itsy.antlr4.ItsyParser.BlockContext;
import itsy.antlr4.ItsyParser.DivideExpressionContext;
import itsy.antlr4.ItsyParser.ExpressionContext;
import itsy.antlr4.ItsyParser.ExpressionExpressionContext;
import itsy.antlr4.ItsyParser.FileExpressionContext;
import itsy.antlr4.ItsyParser.ForInStatementContext;
import itsy.antlr4.ItsyParser.ForStatementContext;
import itsy.antlr4.ItsyParser.FunctionCallExpressionContext;
import itsy.antlr4.ItsyParser.FunctionDeclContext;
import itsy.antlr4.ItsyParser.GtEqExpressionContext;
import itsy.antlr4.ItsyParser.GtExpressionContext;
import itsy.antlr4.ItsyParser.IdentifierFunctionCallContext;
import itsy.antlr4.ItsyParser.ImportDeclarationContext;
import itsy.antlr4.ItsyParser.InExpressionContext;
import itsy.antlr4.ItsyParser.InputExpressionContext;
import itsy.antlr4.ItsyParser.ListContext;
import itsy.antlr4.ItsyParser.ListExpressionContext;
import itsy.antlr4.ItsyParser.LtEqExpressionContext;
import itsy.antlr4.ItsyParser.LtExpressionContext;
import itsy.antlr4.ItsyParser.MapContext;
import itsy.antlr4.ItsyParser.MapExpressionContext;
import itsy.antlr4.ItsyParser.ModulusExpressionContext;
import itsy.antlr4.ItsyParser.MultiplyExpressionContext;
import itsy.antlr4.ItsyParser.NotExpressionContext;
import itsy.antlr4.ItsyParser.OrExpressionContext;
import itsy.antlr4.ItsyParser.PowerExpressionContext;
import itsy.antlr4.ItsyParser.SizeFunctionCallContext;
import itsy.antlr4.ItsyParser.StatementContext;
import itsy.antlr4.ItsyParser.SubtractExpressionContext;
import itsy.antlr4.ItsyParser.TernaryExpressionContext;
import itsy.antlr4.ItsyParser.UnaryMinusExpressionContext;
import itsy.antlr4.ItsyParser.WhileStatementContext;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.ANTLRFileStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.misc.NotNull;
import org.antlr.v4.runtime.tree.TerminalNode;

public class EvalVisitor extends ItsyBaseVisitor<ItsyValue> {
	private static ReturnValue returnValue = new ReturnValue();
    private Scope scope;
    private Map<String, Function> functions;
    private File parentPath;
    
    public EvalVisitor(Scope scope, Map<String, Function> functions, File parentPath) {
        this.scope = scope;
        this.functions = functions;
        this.parentPath = parentPath;
    }
    // TODO complete this
    /*
    @Override
    public ItsyValue visitImportDeclaration(ImportDeclarationContext ctx) {
        String filePath = ctx.STRING().getText();
        try {
            new Itsy(scope, filePath).run(new ANTLRFileStream(filePath));
        } catch (IOException e) {
            throw new EvalException(e.getMessage(), ctx);
        }
        return ItsyValue.VOID; 
    }
    */
    
    // functionDecl
    @Override
    public ItsyValue visitFunctionDecl(FunctionDeclContext ctx) {
        return ItsyValue.VOID;
    }
    
    // list: '[' exprList? ']'
    @Override
    public ItsyValue visitList(ListContext ctx) {
        List<ItsyValue> list = new ArrayList<ItsyValue>();
        if (ctx.exprList() != null) {
	        for (ExpressionContext ex: ctx.exprList().expression()) {
	            list.add(this.visit(ex));
	        }
        }
        return new ItsyValue(list);
    }
    
    // map: '{' exprMap? '}' | '[' exprMap? ']'
    // exprMap: expression ':' expression (',' expression ':' expression )*
    @Override
    public ItsyValue visitMap(MapContext ctx) {
        Map<ItsyValue, ItsyValue> map = new LinkedHashMap<ItsyValue, ItsyValue>();
        if (ctx.exprMap() != null) {
            List<ExpressionContext> expressions = ctx.exprMap().expression();
            for (int i = 0; i < expressions.size(); i += 2) {
                map.put(this.visit(expressions.get(i)), this.visit(expressions.get(i+1)));
            }
        }
        return new ItsyValue(map);
    }
    
    // '-' expression                           #unaryMinusExpression
    @Override
    public ItsyValue visitUnaryMinusExpression(UnaryMinusExpressionContext ctx) {
    	ItsyValue v = this.visit(ctx.expression());
    	if (!v.isNumber()) {
    	    throw new EvalException(ctx);
        }
    	return new ItsyValue(-1 * v.asDouble());
    }

    // '!' expression                           #notExpression
    @Override
    public ItsyValue visitNotExpression(NotExpressionContext ctx) {
    	ItsyValue v = this.visit(ctx.expression());
    	if(!v.isBoolean()) {
    	    throw new EvalException(ctx);
        }
    	return new ItsyValue(!v.asBoolean());
    }

    // expression '^' expression                #powerExpression
    @Override
    public ItsyValue visitPowerExpression(PowerExpressionContext ctx) {
    	ItsyValue lhs = this.visit(ctx.expression(0));
    	ItsyValue rhs = this.visit(ctx.expression(1));
    	if (lhs.isNumber() && rhs.isNumber()) {
    		return new ItsyValue(Math.pow(lhs.asDouble(), rhs.asDouble()));
    	}
    	throw new EvalException(ctx);
    }

    // expression '*' expression                #multiplyExpression
    @Override
    public ItsyValue visitMultiplyExpression(MultiplyExpressionContext ctx) {
    	ItsyValue lhs = this.visit(ctx.expression(0));
    	ItsyValue rhs = this.visit(ctx.expression(1));
    	if(lhs == null || rhs == null) {
    	    throw new EvalException(ctx);
    	}
    	
    	// number * number
        if(lhs.isNumber() && rhs.isNumber()) {
            return new ItsyValue(lhs.asDouble() * rhs.asDouble());
        }

        // string * number
        if(lhs.isString() && rhs.isNumber()) {
            StringBuilder str = new StringBuilder();
            int stop = rhs.asDouble().intValue();
            for(int i = 0; i < stop; i++) {
                str.append(lhs.asString());
            }
            return new ItsyValue(str.toString());
        }

        // list * number
        if(lhs.isList() && rhs.isNumber()) {
            List<ItsyValue> total = new ArrayList<ItsyValue>();
            int stop = rhs.asDouble().intValue();
            for(int i = 0; i < stop; i++) {
                total.addAll(lhs.asList());
            }
            return new ItsyValue(total);
        }    	
    	throw new EvalException(ctx);
    }

    // expression '/' expression                #divideExpression
    @Override
    public ItsyValue visitDivideExpression(DivideExpressionContext ctx) {
    	ItsyValue lhs = this.visit(ctx.expression(0));
    	ItsyValue rhs = this.visit(ctx.expression(1));
    	if (lhs.isNumber() && rhs.isNumber()) {
    		return new ItsyValue(lhs.asDouble() / rhs.asDouble());
    	}
    	throw new EvalException(ctx);
    }

    // expression '%' expression                #modulusExpression
	@Override
	public ItsyValue visitModulusExpression(ModulusExpressionContext ctx) {
		ItsyValue lhs = this.visit(ctx.expression(0));
    	ItsyValue rhs = this.visit(ctx.expression(1));
    	if (lhs.isNumber() && rhs.isNumber()) {
    		return new ItsyValue(lhs.asDouble() % rhs.asDouble());
    	}
    	throw new EvalException(ctx);
	}
	
    // expression '+' expression                #addExpression
    @Override
    public ItsyValue visitAddExpression(@NotNull ItsyParser.AddExpressionContext ctx) {
        ItsyValue lhs = this.visit(ctx.expression(0));
        ItsyValue rhs = this.visit(ctx.expression(1));
        
        if(lhs == null || rhs == null) {
            throw new EvalException(ctx);
        }
        
        // number + number
        if(lhs.isNumber() && rhs.isNumber()) {
            return new ItsyValue(lhs.asDouble() + rhs.asDouble());
        }
        
        // list + any
        if(lhs.isList()) {
            List<ItsyValue> list = lhs.asList();
            list.add(rhs);
            return new ItsyValue(list);
        }

        // string + any
        if(lhs.isString()) {
            return new ItsyValue(lhs.asString() + "" + rhs.toString());
        }

        // any + string
        if(rhs.isString()) {
            return new ItsyValue(lhs.toString() + "" + rhs.asString());
        }
        
        return new ItsyValue(lhs.toString() + rhs.toString());
    }

    // expression '-' expression                #subtractExpression
    @Override
    public ItsyValue visitSubtractExpression(SubtractExpressionContext ctx) {
    	ItsyValue lhs = this.visit(ctx.expression(0));
    	ItsyValue rhs = this.visit(ctx.expression(1));
    	if (lhs.isNumber() && rhs.isNumber()) {
    		return new ItsyValue(lhs.asDouble() - rhs.asDouble());
    	}
    	if (lhs.isList()) {
            List<ItsyValue> list = lhs.asList();
            list.remove(rhs);
            return new ItsyValue(list);
        }
    	throw new EvalException(ctx);
    }

    // expression '>=' expression               #gtEqExpression
    @Override
    public ItsyValue visitGtEqExpression(GtEqExpressionContext ctx) {
    	ItsyValue lhs = this.visit(ctx.expression(0));
    	ItsyValue rhs = this.visit(ctx.expression(1));
    	if (lhs.isNumber() && rhs.isNumber()) {
    		return new ItsyValue(lhs.asDouble() >= rhs.asDouble());
    	}
    	if(lhs.isString() && rhs.isString()) {
            return new ItsyValue(lhs.asString().compareTo(rhs.asString()) >= 0);
        }
    	throw new EvalException(ctx);
    }

    // expression '<=' expression               #ltEqExpression
    @Override
    public ItsyValue visitLtEqExpression(LtEqExpressionContext ctx) {
    	ItsyValue lhs = this.visit(ctx.expression(0));
    	ItsyValue rhs = this.visit(ctx.expression(1));
    	if (lhs.isNumber() && rhs.isNumber()) {
    		return new ItsyValue(lhs.asDouble() <= rhs.asDouble());
    	}
    	if(lhs.isString() && rhs.isString()) {
            return new ItsyValue(lhs.asString().compareTo(rhs.asString()) <= 0);
        }
    	throw new EvalException(ctx);
    }

    // expression '>' expression                #gtExpression
    @Override
    public ItsyValue visitGtExpression(GtExpressionContext ctx) {
    	ItsyValue lhs = this.visit(ctx.expression(0));
    	ItsyValue rhs = this.visit(ctx.expression(1));
    	if (lhs.isNumber() && rhs.isNumber()) {
    		return new ItsyValue(lhs.asDouble() > rhs.asDouble());
    	}
    	if(lhs.isString() && rhs.isString()) {
            return new ItsyValue(lhs.asString().compareTo(rhs.asString()) > 0);
        }
    	throw new EvalException(ctx);
    }

    // expression '<' expression                #ltExpression
    @Override
    public ItsyValue visitLtExpression(LtExpressionContext ctx) {
    	ItsyValue lhs = this.visit(ctx.expression(0));
    	ItsyValue rhs = this.visit(ctx.expression(1));
    	if (lhs.isNumber() && rhs.isNumber()) {
    		return new ItsyValue(lhs.asDouble() < rhs.asDouble());
    	}
    	if(lhs.isString() && rhs.isString()) {
            return new ItsyValue(lhs.asString().compareTo(rhs.asString()) < 0);
        }
    	throw new EvalException(ctx);
    }

    // expression '==' expression               #eqExpression
    @Override
    public ItsyValue visitEqExpression(@NotNull ItsyParser.EqExpressionContext ctx) {
        ItsyValue lhs = this.visit(ctx.expression(0));
        ItsyValue rhs = this.visit(ctx.expression(1));
        if (lhs == null) {
        	throw new EvalException(ctx);
        }
        return new ItsyValue(lhs.equals(rhs));
    }

    // expression '!=' expression               #notEqExpression
    @Override
    public ItsyValue visitNotEqExpression(@NotNull ItsyParser.NotEqExpressionContext ctx) {
        ItsyValue lhs = this.visit(ctx.expression(0));
        ItsyValue rhs = this.visit(ctx.expression(1));
        return new ItsyValue(!lhs.equals(rhs));
    }

    // expression '&&' expression               #andExpression
    @Override
    public ItsyValue visitAndExpression(AndExpressionContext ctx) {
    	ItsyValue lhs = this.visit(ctx.expression(0));
    	ItsyValue rhs = this.visit(ctx.expression(1));
    	
    	if(!lhs.isBoolean() || !rhs.isBoolean()) {
    	    throw new EvalException(ctx);
        }
		return new ItsyValue(lhs.asBoolean() && rhs.asBoolean());
    }

    // expression '||' expression               #orExpression
    @Override
    public ItsyValue visitOrExpression(OrExpressionContext ctx) {
    	ItsyValue lhs = this.visit(ctx.expression(0));
    	ItsyValue rhs = this.visit(ctx.expression(1));
    	
    	if(!lhs.isBoolean() || !rhs.isBoolean()) {
    	    throw new EvalException(ctx);
        }
		return new ItsyValue(lhs.asBoolean() || rhs.asBoolean());
    }

    // expression '?' expression ':' expression #ternaryExpression
    @Override
    public ItsyValue visitTernaryExpression(TernaryExpressionContext ctx) {
    	ItsyValue condition = this.visit(ctx.expression(0));
    	if (condition.asBoolean()) {
    		return new ItsyValue(this.visit(ctx.expression(1)));
    	} else {
    		return new ItsyValue(this.visit(ctx.expression(2)));
    	}
    }

    // expression In expression                 #inExpression
	@Override
	public ItsyValue visitInExpression(InExpressionContext ctx) {
		ItsyValue lhs = this.visit(ctx.expression(0));
    	ItsyValue rhs = this.visit(ctx.expression(1));
    	
    	if (rhs.isList()) {
    		for(ItsyValue val: rhs.asList()) {
    			if (val.equals(lhs)) {
    				return new ItsyValue(true);
    			}
    		}
    		return new ItsyValue(false);
    	}
    	throw new EvalException(ctx);
	}
	
    // Number                                   #numberExpression
    @Override
    public ItsyValue visitNumberExpression(@NotNull ItsyParser.NumberExpressionContext ctx) {
        return new ItsyValue(Double.valueOf(ctx.getText()));
    }

    // Bool                                     #boolExpression
    @Override
    public ItsyValue visitBoolExpression(@NotNull ItsyParser.BoolExpressionContext ctx) {
        return new ItsyValue(Boolean.valueOf(ctx.getText()));
    }

    // Null                                     #nullExpression
    @Override
    public ItsyValue visitNullExpression(@NotNull ItsyParser.NullExpressionContext ctx) {
        return ItsyValue.NULL;
    }

    private ItsyValue resolveIndexes(ParserRuleContext ctx, ItsyValue val, List<ExpressionContext> indexes) {
    	for (ExpressionContext ec: indexes) {
    		ItsyValue idx = this.visit(ec);
    		if (!val.isList() && !val.isString() && !val.isMap()) {
        		throw new EvalException("Problem resolving indexes on "+val+" at "+idx, ec);
    		}
    		if (idx.isNumber()) {
        		int i = idx.asDouble().intValue();
        		if (val.isString()) {
        			val = new ItsyValue(val.asString().substring(i, i+1));
        		} else {
        			val = val.asList().get(i);
        		}
    		} else if (val.isMap()) {
    		    val = (ItsyValue) val.asMap().get(idx);
    		} else {
    		    throw new EvalException("Problem resolving indexes on "+val+" at "+idx, ec);
    		}
    	}
    	return val;
    }
    
    private void setAtIndex(ParserRuleContext ctx, List<ExpressionContext> indexes, ItsyValue val, ItsyValue newVal) {
    	if (!val.isList()) {
    		throw new EvalException(ctx);
    	}
    	// TODO some more list size checking in here
    	for (int i = 0; i < indexes.size() - 1; i++) {
    		ItsyValue idx = this.visit(indexes.get(i));
    		if (!idx.isNumber()) {
        		throw new EvalException(ctx);
    		}
    		val = val.asList().get(idx.asDouble().intValue());
    	}
    	ItsyValue idx = this.visit(indexes.get(indexes.size() - 1));
		if (!idx.isNumber()) {
    		throw new EvalException(ctx);
		}
    	val.asList().set(idx.asDouble().intValue(), newVal);
    }
    
    // functionCall indexes?                    #functionCallExpression
    @Override
    public ItsyValue visitFunctionCallExpression(FunctionCallExpressionContext ctx) {
    	ItsyValue val = this.visit(ctx.functionCall());
    	if (ctx.indexes() != null) {
        	List<ExpressionContext> exps = ctx.indexes().expression();
        	val = resolveIndexes(ctx, val, exps);
        }
    	return val;
    }

    // list indexes?                            #listExpression
    @Override
    public ItsyValue visitListExpression(ListExpressionContext ctx) {
    	ItsyValue val = this.visit(ctx.list());
    	if (ctx.indexes() != null) {
        	List<ExpressionContext> exps = ctx.indexes().expression();
        	val = resolveIndexes(ctx, val, exps);
        }
    	return val;
    }
    
    // map indexes?                             #mapExpression
    @Override
    public ItsyValue visitMapExpression(MapExpressionContext ctx) {
        ItsyValue val = this.visit(ctx.map());
        if (ctx.indexes() != null) {
            List<ExpressionContext> exps = ctx.indexes().expression();
            val = resolveIndexes(ctx, val, exps);
        }
        return val;
    }
    
    // Identifier indexes?                      #identifierExpression
    @Override
    public ItsyValue visitIdentifierExpression(@NotNull ItsyParser.IdentifierExpressionContext ctx) {
        String id = ctx.IDENTIFIER().getText();
        ItsyValue val = scope.resolve(id);
        
        if (ctx.indexes() != null) {
        	List<ExpressionContext> exps = ctx.indexes().expression();
        	val = resolveIndexes(ctx, val, exps);
        }
        return val;
    }

    // String indexes?                          #stringExpression
    @Override
    public ItsyValue visitStringExpression(@NotNull ItsyParser.StringExpressionContext ctx) {
        String text = ctx.getText();
        text = text.substring(1, text.length() - 1).replaceAll("\\\\(.)", "$1");
        ItsyValue val = new ItsyValue(text);
        if (ctx.indexes() != null) {
        	List<ExpressionContext> exps = ctx.indexes().expression();
        	val = resolveIndexes(ctx, val, exps);
        }
        return val;
    }

    // '(' expression ')' indexes?              #expressionExpression
    @Override
    public ItsyValue visitExpressionExpression(ExpressionExpressionContext ctx) {
        ItsyValue val = this.visit(ctx.expression());
        if (ctx.indexes() != null) {
        	List<ExpressionContext> exps = ctx.indexes().expression();
        	val = resolveIndexes(ctx, val, exps);
        }
        return val;
    }
    
    // FILE '(' String ')' 						#fileExpression
    @Override
    public ItsyValue visitFileExpression(FileExpressionContext ctx) {
    	String filePath = getString(ctx.STRING());
    	try {
			return new ItsyValue(new String(Files.readAllBytes(Paths.get(filePath))));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }
    
    private String getString(TerminalNode inputString) {
    	 String text = inputString.getText();
	     return text.substring(1, text.length() - 1).replaceAll("\\\\(.)", "$1");
    }
    
    // INPUT '(' String? ')'                    #inputExpression
    @Override
    public ItsyValue visitInputExpression(InputExpressionContext ctx) {
    	TerminalNode inputString = ctx.STRING();
		try {
			if (inputString != null) {
				System.out.print(getString(inputString));
			} 
			BufferedReader buffer = new BufferedReader(new InputStreamReader(System.in));
			return new ItsyValue(buffer.readLine());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
    }

    
    // assignment
    // : Identifier indexes? '=' expression
    // ;
    @Override
    public ItsyValue visitAssignment(@NotNull ItsyParser.AssignmentContext ctx) {
        ItsyValue newVal = this.visit(ctx.expression());
        if (ctx.indexes() != null) {
        	ItsyValue val = scope.resolve(ctx.IDENTIFIER().getText());
        	List<ExpressionContext> exps = ctx.indexes().expression();
        	setAtIndex(ctx, exps, val, newVal);
        } else {
        	String id = ctx.IDENTIFIER().getText();        	
        	scope.assign(id, newVal);
        }
        return ItsyValue.VOID;
    }

    // Identifier '(' exprList? ')' #identifierFunctionCall
    @Override
    public ItsyValue visitIdentifierFunctionCall(IdentifierFunctionCallContext ctx) {
        List<ExpressionContext> params = ctx.exprList() != null ? ctx.exprList().expression() : new ArrayList<ExpressionContext>();
        String id = ctx.IDENTIFIER().getText() + params.size();
        Function function;      
        if ((function = functions.get(id)) != null) {
            return function.invoke(params, functions, scope);
        }
        throw new EvalException(ctx);
    }

    // Println '(' expression? ')'  #printlnFunctionCall
    @Override
    public ItsyValue visitPrintlnFunctionCall(@NotNull ItsyParser.PrintlnFunctionCallContext ctx) {
        System.out.println(this.visit(ctx.expression()));
        return ItsyValue.VOID;
    }

    // Print '(' expression ')'     #printFunctionCall
    @Override
    public ItsyValue visitPrintFunctionCall(@NotNull ItsyParser.PrintFunctionCallContext ctx) {
        System.out.print(this.visit(ctx.expression()));
        return ItsyValue.VOID;
    }

    // Assert '(' expression ')'    #assertFunctionCall
    @Override
    public ItsyValue visitAssertFunctionCall(AssertFunctionCallContext ctx) {
    	ItsyValue value = this.visit(ctx.expression());

        if(!value.isBoolean()) {
            throw new EvalException(ctx);
        }

        if(!value.asBoolean()) {
            throw new AssertionError("Failed Assertion "+ctx.expression().getText()+" line:"+ctx.start.getLine());
        }

        return ItsyValue.VOID;
    }

    // Size '(' expression ')'      #sizeFunctionCall
    @Override
    public ItsyValue visitSizeFunctionCall(SizeFunctionCallContext ctx) {
    	ItsyValue value = this.visit(ctx.expression());

        if(value.isString()) {
            return new ItsyValue(value.asString().length());
        }

        if(value.isList()) {
            return new ItsyValue(value.asList().size());
        }

        throw new EvalException(ctx);
    }

    // ifStatement
    //  : ifStat elseIfStat* elseStat? End
    //  ;
    //
    // ifStat
    //  : If expression Do block
    //  ;
    //
    // elseIfStat
    //  : Else If expression Do block
    //  ;
    //
    // elseStat
    //  : Else Do block
    //  ;
    @Override
    public ItsyValue visitIfStatement(@NotNull ItsyParser.IfStatementContext ctx) {

        // if ...
        if(this.visit(ctx.ifStat().expression()).asBoolean()) {
            return this.visit(ctx.ifStat().block());
        }

        // else if ...
        for(int i = 0; i < ctx.elseIfStat().size(); i++) {
            if(this.visit(ctx.elseIfStat(i).expression()).asBoolean()) {
                return this.visit(ctx.elseIfStat(i).block());
            }
        }

        // else ...
        if(ctx.elseStat() != null) {
            return this.visit(ctx.elseStat().block());
        }

        return ItsyValue.VOID;
    }
    
    // block
    // : (statement | functionDecl)* (Return expression)?
    // ;
    @Override
    public ItsyValue visitBlock(BlockContext ctx) {
    		
    	scope = new Scope(scope); // create new local scope
        for (StatementContext sx: ctx.statement()) {
            this.visit(sx);
        }
        ExpressionContext ex;
        if ((ex = ctx.expression()) != null) {
        	returnValue.value = this.visit(ex);
        	scope = scope.parent();
        	throw returnValue;
        }
        scope = scope.parent();
        return ItsyValue.VOID;
    }
    
    // forStatement
    // : For Identifier '=' expression To expression NEWLINE INDENT block DEDENT
    // ;
    @Override
    public ItsyValue visitForStatement(ForStatementContext ctx) {
        int start = this.visit(ctx.expression(0)).asDouble().intValue();
        int stop = this.visit(ctx.expression(1)).asDouble().intValue();
        String varName = ctx.IDENTIFIER().getText();
        for(int i = start; i <= stop; i++) {
            scope.assign(varName, new ItsyValue(i));
            ItsyValue returnValue = this.visit(ctx.block());
            if(returnValue != ItsyValue.VOID) {
                return returnValue;
            }
        }
        return ItsyValue.VOID;
    }
    
    // forInStatement
    // : For Identifier IN expression NEWLINE INDENT block DEDENT
    // ;
    @Override
    public ItsyValue visitForInStatement(ForInStatementContext ctx) {
    	ItsyValue expression = this.visit(ctx.expression());
    	Iterable<ItsyValue> iterable;
    	if (expression.isList()) {
    		iterable = expression.asList();
    	} else if (expression.isMap()) {
    		iterable = expression.asMap().keySet();
    	} else {
    		throw new EvalException("Please use a dict/map or list in a for in for ", ctx);
    	}
    	String varName = ctx.IDENTIFIER().getText();
    	for (ItsyValue val: iterable) {
			scope.assign(varName, val);
            ItsyValue returnValue = this.visit(ctx.block());
            if(returnValue != ItsyValue.VOID) {
                return returnValue;
            }
		}
    	return ItsyValue.VOID;
    }
    
    // whileStatement
    // : While expression NEWLINE INDENT block DEDENT
    // ;
    @Override
    public ItsyValue visitWhileStatement(WhileStatementContext ctx) {
        while( this.visit(ctx.expression()).asBoolean() ) {
            ItsyValue returnValue = this.visit(ctx.block());
            if (returnValue != ItsyValue.VOID) {
                return returnValue;
            }
        }
        return ItsyValue.VOID;
    }
    
}

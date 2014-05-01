/**
 *  Copyright 2013 Douglas Campos, and individual contributors
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.dynjs.parser.ast;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;

import org.dynjs.parser.CodeVisitor;
import org.dynjs.runtime.DynObject;
import org.dynjs.runtime.EnvironmentRecord;
import org.dynjs.runtime.ExecutionContext;
import org.dynjs.runtime.JSObject;
import org.dynjs.runtime.ObjectEnvironmentRecord;
import org.dynjs.runtime.PropertyDescriptor;
import org.dynjs.runtime.builtins.types.array.LinearArray;

/**
 * Access a property with bracket notation
 * 
 * see 11.2.1
 * 
 * @author Douglas Campos
 * @author Bob McWhirter
 */
public class BracketExpression extends AbstractBinaryExpression {

	public static final int MULTIDIM_ARR_FLAG = -1;
	public static Deque<Integer> MultidimensionArrayIndexs = new ArrayDeque<Integer>();

	public BracketExpression(Expression lhs, Expression rhs) {
		super(lhs, rhs, "[]");
	}

	public String toString() {
		return getLhs() + "[" + getRhs() + "]";
	}

	public String dump(String indent) {
		return super.dump(indent) + getLhs().dump(indent + "  ")
				+ getRhs().dump(indent + "  ");
	}
	
	/*
	 *  Hand-crafted method to support non literal inside multidimensional array brackets.
	 */
	private void addIndex(BracketExpression bracketExp, ExecutionContext context){
		Expression exp = bracketExp.getRhs();
		if(!(exp instanceof IntegerNumberExpression)){
			String identifier = ((IdentifierReferenceExpression) exp).getIdentifier();
			EnvironmentRecord env = context.getVariableEnvironment().getRecord();
			JSObject globalObject = ((ObjectEnvironmentRecord) env).getBindingObject();
			Object identifierProperty = globalObject.getProperty(context, identifier, false);
			int value = (int) (long) ((PropertyDescriptor) identifierProperty).getValue();
			BracketExpression.MultidimensionArrayIndexs.addFirst(value);
		} else {
			BracketExpression.MultidimensionArrayIndexs.addFirst((int)((IntegerNumberExpression) exp).getValue());
		}
	}

	@Override
    public void accept(ExecutionContext context, CodeVisitor visitor, boolean strict) {
        if(this.getLhs() instanceof BracketExpression) {
        	try{
        	//System.out.println("@" + BracketExpression.class.getSimpleName() + " Multidimension array detected");            
            Expression lh = this.getLhs();
            BracketExpression be;
            
            addIndex(this, context);
            
            while(lh instanceof BracketExpression) {
                be = (BracketExpression) lh;
                addIndex(be, context);
                lh = be.getLhs();
            }
            
            BracketExpression linearizedExpr = new BracketExpression(lh, new IntegerNumberExpression(this.getPosition(), "", 10, BracketExpression.MULTIDIM_ARR_FLAG));
            
            visitor.visit( context, linearizedExpr, strict );
            
        } catch (Exception e){
        	e.printStackTrace();
        }
        } else {
        	visitor.visit( context, this, strict );
        }
    }
}

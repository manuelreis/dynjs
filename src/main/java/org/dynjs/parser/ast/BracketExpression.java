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
import org.dynjs.runtime.ExecutionContext;

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
        super( lhs, rhs, "[]" );
    }
    
    public String toString() {
        return getLhs() + "[" + getRhs() + "]";
    }
    
    public String dump(String indent) {
        return super.dump(indent) + getLhs().dump( indent + "  " ) + getRhs().dump( indent + "  " );
    }

    @Override
    public void accept(ExecutionContext context, CodeVisitor visitor, boolean strict) {
        if(this.getLhs() instanceof BracketExpression) {
            System.out.println("@" + BracketExpression.class.getSimpleName() + " Multidimension array detected");            
            
            Expression lh = this.getLhs();
            BracketExpression be;
            
            BracketExpression.MultidimensionArrayIndexs.addFirst( (int)((IntegerNumberExpression)this.getRhs()).getValue() );
            while(lh instanceof BracketExpression) {
                be = (BracketExpression) lh;
                BracketExpression.MultidimensionArrayIndexs.addFirst( (int)((IntegerNumberExpression)be.getRhs()).getValue() );
                lh = be.getLhs();
            }
            
            BracketExpression linearizedExpr = new BracketExpression(lh, new IntegerNumberExpression(this.getPosition(), "", 10, BracketExpression.MULTIDIM_ARR_FLAG));
            
            visitor.visit( context, linearizedExpr, strict );
        } else {
//        BracketExpression.LinearizedIndex = String.valueOf( (IntegerNumberExpression)this.getRhs().;
        visitor.visit( context, this, strict );
        }
    }
}

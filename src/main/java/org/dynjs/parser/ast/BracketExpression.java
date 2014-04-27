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

import java.util.ArrayList;
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

    public static final String MULTIDIM_ARR_FLAG = "-1";
    public static String LinearizedIndex;
    
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
            
            String parsedIndex = ((IntegerNumberExpression)this.getRhs()).getText();
            Expression lh = this.getLhs();
            BracketExpression be;
            while(lh instanceof BracketExpression) {
                be = (BracketExpression) lh;
                parsedIndex = ((IntegerNumberExpression)be.getRhs()).getText() + "," + parsedIndex;
                lh = be.getLhs();
            }
            
            BracketExpression linearizedExpr = new BracketExpression(lh, new IntegerNumberExpression(this.getPosition(), parsedIndex, 10, -1));
            BracketExpression.LinearizedIndex = parsedIndex;
            
            System.out.println("@" + BracketExpression.class.getSimpleName() + " Multidimension array linearized index: " + parsedIndex);
            
            visitor.visit( context, linearizedExpr, strict );
        } else {
        visitor.visit( context, this, strict );
        }
    }
}

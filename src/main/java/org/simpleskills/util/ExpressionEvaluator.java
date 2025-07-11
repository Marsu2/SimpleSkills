package org.simpleskills.util;

import java.util.*;

public class ExpressionEvaluator {
    private static final Map<String,Integer> PREC = Map.of(
        "+",1,"-",1,"*",2,"/",2,"^",3
    );
    private static final Set<String> RIGHT = Set.of("^");

    public static double eval(String expr, double level) {
        List<String> tok = tokenize(expr);
        List<String> rpn = toRPN(tok);
        return evalRPN(rpn, level);
    }
    private static List<String> tokenize(String s) {
        List<String> t = new ArrayList<>();
        int i=0,n=s.length();
        while(i<n) {
            char c=s.charAt(i);
            if(Character.isWhitespace(c)){ i++; continue; }
            if(Character.isDigit(c)||c=='.'){
                int j=i; while(j<n&&(Character.isDigit(s.charAt(j))||s.charAt(j)=='.')) j++;
                t.add(s.substring(i,j)); i=j;
            } else if(Character.isLetter(c)){
                int j=i; while(j<n&&Character.isLetterOrDigit(s.charAt(j))) j++;
                t.add(s.substring(i,j)); i=j;
            } else {
                t.add(""+c); i++;
            }
        }
        return t;
    }
    private static List<String> toRPN(List<String> in) {
        List<String> out=new ArrayList<>();
        Deque<String> ops=new ArrayDeque<>();
        for(String tok: in){
            if (tok.matches("\\d+(\\.\\d+)?") || tok.equalsIgnoreCase("level")){
                out.add(tok);
            } else if(PREC.containsKey(tok)){
                while(!ops.isEmpty()&&PREC.containsKey(ops.peek())){
                    String top=ops.peek();
                    int p1=PREC.get(top), p2=PREC.get(tok);
                    if(p1>p2||(p1==p2&&!RIGHT.contains(tok))){ out.add(ops.pop()); continue; }
                    break;
                }
                ops.push(tok);
            } else if(tok.equals("(")){
                ops.push(tok);
            } else if(tok.equals(")")){
                while(!ops.isEmpty()&&!ops.peek().equals("(")) out.add(ops.pop());
                if(ops.isEmpty()||!ops.peek().equals("(")) throw new IllegalArgumentException("Parentheses mismatch");
                ops.pop();
            } else {
                throw new IllegalArgumentException("Invalid token: " + tok);
            }
        }
        while(!ops.isEmpty()){
            String o=ops.pop();
            if(o.equals("(")||o.equals(")")) throw new IllegalArgumentException("Parentheses mismatch");
            out.add(o);
        }
        return out;
    }
    private static double evalRPN(List<String> rpn, double lvl){
        Deque<Double> st=new ArrayDeque<>();
        for(String tok:rpn){
            if((tok.matches("\\d+(\\.\\d+)?"))){
                st.push(Double.parseDouble(tok));
            } else if(tok.equalsIgnoreCase("level")){
                st.push(lvl);
            } else if(PREC.containsKey(tok)){
                if(st.size()<2) throw new IllegalArgumentException("Invalid RPN");
                double b=st.pop(), a=st.pop();
                switch(tok){
                    case "+": st.push(a+b); break;
                    case "-": st.push(a-b); break;
                    case "*": st.push(a*b); break;
                    case "/": st.push(a/b); break;
                    case "^": st.push(Math.pow(a,b)); break;
                }
            } else {
                throw new IllegalArgumentException("Invalid RPN token: " + tok);
            }
        }
        return st.pop();
    }
}

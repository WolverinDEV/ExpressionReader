package dev.wolveringer.jee;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.commons.lang3.Validate;

import dev.wolveringer.jee.ExpressionEvaluator.ExpressionValue.Type;
import dev.wolveringer.jee.ExpressionEvaluatorFunctions.ExpressionFunction;
import dev.wolveringer.jee.ExpressionEvaluatorOperators.Operator;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

public class ExpressionEvaluator {
	@RequiredArgsConstructor
	@ToString
	static class ExpressionValue<VType> {
		public static enum TypeClass {
			TEXT,
			NUMBER,
			DECIMAL_NUMBER,
			NULL
		}
		
		@AllArgsConstructor
		@Getter
		public static enum Type {
			STRING(TypeClass.TEXT),
			FLOAT(TypeClass.DECIMAL_NUMBER),
			DOUBLE(TypeClass.DECIMAL_NUMBER),
			LONG(TypeClass.NUMBER),
			INT(TypeClass.NUMBER),
			SHORT(TypeClass.NUMBER),
			BYTE(TypeClass.NUMBER),
			NULL(TypeClass.NULL);
			
			private final TypeClass root;
		}
		
		@Getter
		private final Type type;
		private final VType val;
		
		public Number asNumber(){
			return (Number) val;
		}
		
		public String asString(){
			return String.valueOf(val);
		}
		
		public float asFloat(){
			return asNumber().floatValue();
		}
		
		public double asDouble(){
			return asNumber().doubleValue();
		}
		
		public long asLong(){
			return asNumber().longValue();
		}
		
		public int asInt(){
			return asNumber().intValue();
		}
		
		public short asShort(){
			return asNumber().shortValue();
		}
		
		public byte asByte(){
			return asNumber().byteValue();
		}
		
		public VType getValue(){
			return val;
		}
		
		public void enshureNumeric(){
			if(!(type.getRoot() == TypeClass.DECIMAL_NUMBER || type.getRoot() == TypeClass.NUMBER))
				throw new RuntimeException("Invalid type! ("+type+" isn't numeric!)");
		}
	}
	
	static interface ExpressionElement {
		public ExpressionValue<?> parse();
	}
	
	@RequiredArgsConstructor
	@Getter
	@ToString
	static class StringExpressionElement implements ExpressionElement{
		private final String elm;
	
		public ExpressionValue<?> parse(){
			String elm = this.elm.trim();
			while(elm.startsWith("(") && elm.endsWith(")")) elm = elm.substring(1, elm.length() - 1);
			
			if(elm.matches("[0-9.flsb]+")){
				if(elm.endsWith("f")){
					return new ExpressionValue<Float>(Type.FLOAT, Float.valueOf(elm.substring(0, elm.length() - 1)));
				} else if(elm.endsWith("l")){
					return new ExpressionValue<Long>(Type.LONG, Long.valueOf(elm.substring(0, elm.length() - 1)));
				} else if(elm.endsWith("s")){
					return new ExpressionValue<Short>(Type.SHORT, Short.valueOf(elm.substring(0, elm.length() - 1)));
				} else if(elm.endsWith("b")){
					return new ExpressionValue<Byte>(Type.BYTE, Byte.valueOf(elm.substring(0, elm.length() - 1)));
				} else if(elm.contains(".")){
					return new ExpressionValue<Double>(Type.DOUBLE, Double.valueOf(elm.substring(0, elm.length() - 1)));
				}
				return new ExpressionValue<Integer>(Type.INT, Integer.valueOf(elm));
			}
			if(elm.contains(" ")) throw new ExpressionException("Invalid variable name '"+elm+"'/'"+elm.trim()+"'");
			throw new ExpressionException("Cant find variable: "+elm);
			//return null;
		}
	}
	
	@RequiredArgsConstructor
	@Getter
	static class EvalatedExpressionElement implements ExpressionElement {
		private final ExpressionValue<?> value;
		
		public ExpressionValue<?> parse() {
			return value;
		};
		
		@Override
		public String toString() {
			return "const "+value.toString();
		}
	}
	
	@RequiredArgsConstructor
	@Getter
	static class FunctionExpressionElement implements ExpressionElement {
		private final String name;
		private final String parm;
		
		@Override
		public String toString() {
			return name + "("+parm+")";
		}
		
		@Override
		public ExpressionValue<?> parse() { //TODO multi args
			System.out.println("name: "+name+" parm: "+parm);
			ExpressionIterator parms = new ExpressionIterator(parm);
			Optional<ExpressionFunction> fn = ExpressionEvaluatorFunctions.getFunction(name);
			if(!fn.isPresent()) throw new ExpressionException("Cant find function '"+name+"'");
			System.out.println("Running fn "+name+" with parm '"+parms.parse()+"'");
			return fn.get().applay(parms.parse());
		}
	}
	
	static class ExpressionIterator implements ExpressionElement{
		private List<ExpressionIteratorElement> elements = new ArrayList<>();
		
		private String expression;
		
		public ExpressionIterator(String exp) {
			this.expression = exp;
			parse(exp);
		}
		
		public boolean isBreaced(){
			return expression.charAt(0) == '(' && expression.charAt(expression.length() - 1) == ')';
		}
		
		private void parse(String elm){
			while(elm.charAt(0) == '(' && elm.charAt(elm.length() - 1) == ')') elm = elm.substring(1, elm.length() -1);
			int braceLevel = 0;
			mainLoop:
				for(int i = 0;i<elm.length();i++){
					
					switch(elm.charAt(i)){
					case '(':
						Validate.isTrue(++braceLevel < 255, "Stack overflow");
						break;
					case ')':
						Validate.isTrue(--braceLevel >= 0, "Stack underflow");
						break;
						default:
							break;
					}
					if(braceLevel == 0)
						for(Operator eop : ExpressionEvaluatorOperators.getAvariableOperators()){
							String op = eop.getOperator();
							if(op.length() < elm.length() - i){
								if(op.equalsIgnoreCase(elm.substring(i, i + op.length()))){
									ExpressionIterator elmFirst;
									if(elm.substring(0, i).trim().isEmpty() && (op.equalsIgnoreCase("-") || op.equalsIgnoreCase("+"))){
										 elmFirst = new ExpressionIterator("0");
									} else if(elm.substring(0, i).trim().isEmpty()) throw new ExpressionException("First element is null!");
									else {
										elmFirst = new ExpressionIterator(elm.substring(0, i));
									}
									ExpressionIterator elmLast = new ExpressionIterator(elm.substring(i + op.length()));
									
									if(elmFirst.elements.size() == 1 || !elmFirst.isBreaced()){
										elements.addAll(elmFirst.elements);
										elmFirst.elements.get(elmFirst.elements.size() - 1).operator = op;
									} else {
										elements.add(new ExpressionIteratorElement(null, null, elmFirst, op));
									}	
									
									if(elmLast.elements.size() == 1 || !elmLast.isBreaced()){
										elements.addAll(elmLast.elements);
									} else 
										elements.add(new ExpressionIteratorElement(null, null, elmLast, op));
									
									break mainLoop;
								}	
							}
						}
				}
			if(elements.size() == 0){
				fnGetter:
				if(expression.contains("(") && expression.contains(")")){
					if(expression.charAt(expression.length() - 1) != ')') throw new ExpressionException("Invalid function parameters '"+expression+"'");
					int sindex = expression.indexOf('(');
					String fnName = expression.substring(0, sindex);
					if(fnName.trim().isEmpty()) break fnGetter;
					
					String parm = expression.substring(sindex + 1, expression.length() - 1);
					System.out.println("Found function '"+fnName+"' with parms '"+parm+"'");
					elements.add(new ExpressionIteratorElement(null, null, new FunctionExpressionElement(fnName, parm), ""));
				}
				if(elements.isEmpty()){
					System.out.println("Found root: "+expression);
					elements.add(new ExpressionIteratorElement(null, null, new StringExpressionElement(expression), ""));
				}
			} else {
				ExpressionIteratorElement[] elms = elements.toArray(new ExpressionIteratorElement[0]);
				for(int i = 0;i<elms.length;i++){
					if(i > 0){
						elms[i].prev = elms[i - 1];
					}
					if(i + 1 < elms.length)
						elms[i].next = elms[i + 1];
				}
			}
		}
		
		@Override
		public ExpressionValue<?> parse() {
			List<ExpressionIteratorElement> evalElements = new ArrayList<>();
			elements.stream().map(ExpressionIteratorElement::clone).forEach(evalElements::add);
			
			HashMap<Integer, List<Operator>> operators = new HashMap<>();
			ExpressionEvaluatorOperators.getAvariableOperators().forEach(e -> {
				if(!operators.containsKey(e.getPriority())) operators.put(e.getPriority(), new ArrayList<>());
				operators.get(e.getPriority()).add(e);
			});
			List<Entry<Integer, List<Operator>>> loperators = new ArrayList<>(operators.entrySet());
			Collections.sort(loperators, (a, b) ->  b.getKey().compareTo(a.getKey()));
			
			for(Entry<Integer, List<Operator>> eop : loperators){
				int i;
				mainLoop:
				for(i = 0; i < evalElements.size() - 1; i++){
					for(Operator op : eop.getValue()){
						if(evalElements.get(i).operator.equalsIgnoreCase(op.getOperator())){
							ExpressionIteratorElement next = evalElements.get(i + 1); //TODO range check!
							ExpressionIteratorElement current = evalElements.get(i);
							ExpressionIteratorElement out = op.applay(current, next);
							evalElements.remove(i + 1);
							evalElements.set(i, out);
							i = i - 1;
							continue mainLoop;
						}
					}
				}
			}
			
			String message = "";
			for(ExpressionIteratorElement e : evalElements){
				if(!e.operator.isEmpty())
					message += "\nCant find operator '"+e.operator+"'";
			}
			if(!message.isEmpty()) throw new ExpressionException(message.substring(1));
			
			if(evalElements.size() != 1){
				System.err.println("Invalid elm size "+evalElements.size());
			}
			return evalElements.get(0).elm.parse();
		}
	}
	
	@AllArgsConstructor
	static class ExpressionIteratorElement {
		static class EndExpressionIteratorElement extends ExpressionIteratorElement {
			public EndExpressionIteratorElement(ExpressionIteratorElement prev, ExpressionElement elm, String operator) {
				super(prev, null, elm, operator);
			}
		}
		
		ExpressionIteratorElement prev;
		ExpressionIteratorElement next;
		ExpressionElement elm;
		String operator;
		
		@Override
		public String toString() {
			return "ExpressionIteratorElement [elm=" + elm + ", operator=" + operator + "]";
		}
		
		public ExpressionIteratorElement clone(){
			return new ExpressionIteratorElement(prev, next, elm, operator);
		}
	}
	
	private StringBuilder print(StringBuilder sb, ExpressionElement elm){
		if(elm instanceof ExpressionIterator){
			boolean start = sb.length() == 0;
			
			if(!start) sb.append("(");
			for(ExpressionIteratorElement e : ((ExpressionIterator) elm).elements){
				print(sb, e.elm);
				sb.append(e.operator);
			}
			if(!start) sb.append(")");
		} else if(elm instanceof StringExpressionElement) {
			sb.append(((StringExpressionElement) elm).elm);
		} else if(elm instanceof FunctionExpressionElement){
			FunctionExpressionElement e = (FunctionExpressionElement) elm;
			sb.append(e.getName()+"("+e.getParm()+")");
		} else {
			sb.append("<"+elm.getClass().getName()+">");
		}
		return sb;
	}
	
	public void evaluate(String expression){
		System.out.println("Evaluate: '"+expression+"'");
		//if(!expression.startsWith("(")) expression = "(" + expression + ")";
		
		ExpressionIterator it = new ExpressionIterator(expression);
		System.out.println(it.elements);
		System.out.println("Parse: "+it.parse());
	
		
		System.out.println(print(new StringBuilder(), it).toString());
		//ExpressionStackElement root = new ExpressionStackElement(expression);
		//System.out.println("Root: "+root);
	}
}

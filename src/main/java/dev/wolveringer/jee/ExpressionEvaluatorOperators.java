package dev.wolveringer.jee;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import dev.wolveringer.jee.ExpressionEvaluator.EvalatedExpressionElement;
import dev.wolveringer.jee.ExpressionEvaluator.ExpressionIteratorElement;
import dev.wolveringer.jee.ExpressionEvaluator.ExpressionValue;
import dev.wolveringer.jee.ExpressionEvaluator.ExpressionValue.Type;

public class ExpressionEvaluatorOperators {
	public static interface Operator {
		String getOperator();
		int getPriority();
		default ExpressionIteratorElement applay(ExpressionIteratorElement first, ExpressionIteratorElement last){
			ExpressionValue<?> val = applay(first.elm.parse(), last.elm.parse());
			return new ExpressionIteratorElement(first.prev, last.next, new EvalatedExpressionElement(val), last.operator);
		}
		ExpressionValue<?> applay(ExpressionValue<?> first, ExpressionValue<?> next);
	}
	
	private static List<Operator> avariableOperators = new ArrayList<>();
	
	public static List<Operator> getAvariableOperators(){
		return Collections.unmodifiableList(avariableOperators);
	}
	
	public static Optional<Operator> getOperator(String op){
		return avariableOperators.stream().filter(e -> e.getOperator().equalsIgnoreCase(op)).findFirst();
	}
	
	public static void registerOperator(Operator op){
		if(getOperator(op.getOperator()).isPresent()) throw new RuntimeException("Operator '"+op.getOperator()+"' alredy registered!");
		synchronized (avariableOperators) {
			avariableOperators.add(op);
		}
	}
	
	public static final int PRIORITY_FUNCTION = 20;
	public static final int PRIORITY_SQUARE = 15;
	public static final int PRIORITY_POINT = 10;
	public static final int PRIORITY_LINE = 5;
	
	public abstract static class BasicNumberOperator implements Operator{
		@Override
		public ExpressionValue<?> applay(ExpressionValue<?> first, ExpressionValue<?> next) {
			Number a = first.asNumber();
			Number b = next.asNumber();
			
			if(a instanceof Double || b instanceof Double)
				return new ExpressionValue<Double>(Type.DOUBLE, applay(a.doubleValue(), b.doubleValue()));
			else if(a instanceof Float || b instanceof Float)
				return new ExpressionValue<Float>(Type.FLOAT, applay(a.floatValue(), b.floatValue()));
			
			else if(a instanceof Long || b instanceof Long)
				return new ExpressionValue<Long>(Type.LONG, applay(a.longValue(), b.longValue()));
			else if(a instanceof Integer || b instanceof Integer)
				return new ExpressionValue<Integer>(Type.INT, applay(a.intValue(), b.intValue()));
			else if(a instanceof Short || b instanceof Short)
				return new ExpressionValue<Short>(Type.SHORT, (short) applay(a.shortValue(), b.shortValue()));
			else if(a instanceof Byte || b instanceof Byte)
				return new ExpressionValue<Byte>(Type.BYTE, applay(a.byteValue(), b.byteValue()));
			
			else throw new UnsupportedOperationException("Class "+a.getClass().getName()+" isnt an number instance!");
		}
		
		public abstract double applay(double first, double next);
		public abstract float applay(float first, float next);
		
		public abstract long applay(long first, long next);
		public abstract int applay(int first, int next);
		public abstract short applay(short first, byte next);
		public abstract byte applay(byte first, byte next);
	}
	
	public static final Operator MATH_PLUS = new BasicNumberOperator() {
		@Override
		public int getPriority() {
			return PRIORITY_LINE;
		}
		
		@Override
		public String getOperator() {
			return "+";
		}
		
		@Override
		public byte applay(byte first, byte next) {
			return (byte) (first + next);
		}
		
		@Override
		public short applay(short first, byte next) {
			return (short) (first + next);
		}
		
		@Override
		public int applay(int first, int next) {
			return first + next;
		}
		
		@Override
		public long applay(long first, long next) {
			return first + next;
		}
		
		@Override
		public float applay(float first, float next) {
			return first + next;
		}
		
		@Override
		public double applay(double first, double next) {
			return first + next;
		}
	};
	
	public static final Operator MATH_MUNUS = new BasicNumberOperator() {
		@Override
		public int getPriority() {
			return PRIORITY_LINE;
		}
		
		@Override
		public String getOperator() {
			return "-";
		}
		
		@Override
		public byte applay(byte first, byte next) {
			return (byte) (first - next);
		}
		
		@Override
		public short applay(short first, byte next) {
			return (short) (first - next);
		}
		
		@Override
		public int applay(int first, int next) {
			return first - next;
		}
		
		@Override
		public long applay(long first, long next) {
			return first - next;
		}
		
		@Override
		public float applay(float first, float next) {
			return first - next;
		}
		
		@Override
		public double applay(double first, double next) {
			return first - next;
		}
	};
	
	public static final Operator MATH_MULTIPLY = new BasicNumberOperator() {
		@Override
		public int getPriority() {
			return PRIORITY_POINT;
		}
		
		@Override
		public String getOperator() {
			return "*";
		}
		
		@Override
		public byte applay(byte first, byte next) {
			return (byte) (first * next);
		}
		
		@Override
		public short applay(short first, byte next) {
			return (short) (first * next);
		}
		
		@Override
		public int applay(int first, int next) {
			return first * next;
		}
		
		@Override
		public long applay(long first, long next) {
			return first * next;
		}
		
		@Override
		public float applay(float first, float next) {
			return first * next;
		}
		
		@Override
		public double applay(double first, double next) {
			return first * next;
		}
	};
	
	public static final Operator MATH_DIVIDIDE = new BasicNumberOperator() {
		@Override
		public int getPriority() {
			return PRIORITY_POINT;
		}
		
		@Override
		public String getOperator() {
			return "/";
		}
		
		@Override
		public byte applay(byte first, byte next) {
			return (byte) (first / next);
		}
		
		@Override
		public short applay(short first, byte next) {
			return (short) (first / next);
		}
		
		@Override
		public int applay(int first, int next) {
			return first / next;
		}
		
		@Override
		public long applay(long first, long next) {
			return first / next;
		}
		
		@Override
		public float applay(float first, float next) {
			return first / next;
		}
		
		@Override
		public double applay(double first, double next) {
			return first / next;
		}
	};
	
	static {
		registerOperator(MATH_PLUS);
		registerOperator(MATH_MUNUS);
		registerOperator(MATH_MULTIPLY);
		registerOperator(MATH_DIVIDIDE);
	}
}

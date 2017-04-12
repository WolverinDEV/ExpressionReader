package dev.wolveringer.jee;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import dev.wolveringer.jee.ExpressionEvaluator.ExpressionValue;
import dev.wolveringer.jee.ExpressionEvaluator.ExpressionValue.Type;
import lombok.RequiredArgsConstructor;
import lombok.val;

public class ExpressionEvaluatorFunctions {
	private static List<ExpressionFunction> avariableFunctions = new ArrayList<>();
	
	public static Optional<ExpressionFunction> getFunction(String name){
		synchronized (avariableFunctions) {
			return avariableFunctions.stream().filter(e -> e.getName().equalsIgnoreCase(name)).findAny();
		}
	}
	
	public static void registerFunction(ExpressionFunction function){
		synchronized (avariableFunctions) {
			if(getFunction(function.getName()).isPresent()) throw new ExpressionException.FunctionExpressionException("Function '"+function.getName()+"' alredy registered!");
			avariableFunctions.add(function);
		}
	}
	
	public static interface ExpressionFunction {
		public ExpressionValue<?> applay(ExpressionValue<?>...values);
		
		public String getName();
	}
	
	public static abstract class SingleParmFunction implements ExpressionFunction{
		@Override
		public ExpressionValue<?> applay(ExpressionValue<?>... values) {
			if(values.length != 1) throw new ExpressionException.FunctionExpressionException("Invalid function parameter count. Required 1!");
			return applay(values[0]);
		}
		
		public abstract ExpressionValue<?> applay(ExpressionValue<?> value);
	}
	
	@RequiredArgsConstructor
	public static abstract class NumberParmFunction implements ExpressionFunction {
		private final int minArgs;
		private final int maxArgs;
		
		@SuppressWarnings("unchecked")
		private <T> T[] mapTo(Class<T> clazz,Function<ExpressionValue<?>, T> mapper, ExpressionValue<?>...values){
			T[] out = (T[]) Array.newInstance(clazz, values.length);
			for(int i = 0;i<values.length;i++)
				out[i] = mapper.apply(values[i]);
			return out;
		}
		
		private <T> boolean isAny(Class<T> clazz, ExpressionValue<?>... values){
			for(int i = 0;i<values.length;i++)
				if(values[i].getValue().getClass().isAssignableFrom(clazz) ||
						clazz.isAssignableFrom(values[i].getValue().getClass()))
					return true;
			return false;
		}
		
		@Override
		public ExpressionValue<?> applay(ExpressionValue<?>... values) {
			if(values.length < minArgs || values.length > maxArgs) throw new ExpressionException.FunctionExpressionException("Function parameter count out of bounds! ("+minArgs+" < "+val.class+" < "+maxArgs+")");
			if(isAny(Float.class, values)){
				return new ExpressionValue<Float>(Type.FLOAT, applay(mapTo(Float.class, a ->  a.asFloat(), values)));
			} else if(isAny(Double.class, values)){
				return new ExpressionValue<Double>(Type.DOUBLE, applay(mapTo(Double.class, a ->  a.asDouble(), values)));
			} else if(isAny(Long.class, values)){
				return new ExpressionValue<Long>(Type.LONG, applay(mapTo(Long.class, a ->  a.asLong(), values)));
			} else if(isAny(Integer.class, values)){
				return new ExpressionValue<Integer>(Type.INT, applay(mapTo(Integer.class, a ->  a.asInt(), values)));
			} else if(isAny(Short.class, values)){
				return new ExpressionValue<Short>(Type.SHORT, applay(mapTo(Short.class, a ->  a.asShort(), values)));
			} else if(isAny(Byte.class, values)){
				return new ExpressionValue<Byte>(Type.BYTE, applay(mapTo(Byte.class, a ->  a.asByte(), values)));
			} else throw new ExpressionException("Cant find value type for "+values.length+"");
		}
		
		abstract float applay(Float...args);
		abstract double applay(Double...args);
		abstract long applay(Long...args);
		abstract int applay(Integer...args);
		abstract short applay(Short...args);
		abstract byte applay(Byte...args);
	}
	
	public static final ExpressionFunction MATH_ABS = new NumberParmFunction(1, 1) {
		@Override
		public String getName() {
			return "abs";
		}
		
		@Override
		byte applay(Byte... args) {
			return (byte) (args[0].byteValue() < 0 ? -args[0].byteValue() : args[0].byteValue());
		}
		
		@Override
		short applay(Short... args) {
			return (short) (args[0].shortValue() < 0 ? -args[0].shortValue() : args[0].shortValue());
		}
		
		@Override
		int applay(Integer... args) {
			return args[0].intValue() < 0 ? -args[0].intValue() : args[0].intValue();
		}
		
		@Override
		long applay(Long... args) {
			return args[0].longValue() < 0 ? -args[0].longValue() : args[0].longValue();
		}
		
		@Override
		double applay(Double... args) {
			return args[0].doubleValue() < 0 ? -args[0].doubleValue() : args[0].doubleValue();
		}
		
		@Override
		float applay(Float... args) {
			return args[0].floatValue() < 0 ? -args[0].floatValue() : args[0].floatValue();
		}
	};
		
	public static final ExpressionFunction MATH_SQR = new NumberParmFunction(1, 1) {
		@Override
		public String getName() {
			return "sqr";
		}
		
		@Override
		byte applay(Byte... args) {
			return (byte) (args[0].byteValue() * args[0].byteValue());
		}
		
		@Override
		short applay(Short... args) {
			return (short) (args[0].shortValue() * args[0].shortValue());
		}
		
		@Override
		int applay(Integer... args) {
			return args[0].intValue() * args[0].intValue();
		}
		
		@Override
		long applay(Long... args) {
			return args[0].longValue() * args[0].longValue();
		}
		
		@Override
		double applay(Double... args) {
			return args[0].doubleValue() * args[0].doubleValue();
		}
		
		@Override
		float applay(Float... args) {
			return args[0].floatValue() * args[0].floatValue();
		}
	};
	
	static {
		registerFunction(MATH_SQR);
		registerFunction(MATH_ABS);
	}
}

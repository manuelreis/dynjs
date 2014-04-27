package org.dynjs.runtime.builtins.types.array;

public class LinearArray {

	
	private final Integer[] dimensions;
	private final Object[] array;
	
	
	public LinearArray(Integer[] dimensions) {
		this.dimensions = dimensions;
		int totalSize = dimensions[0];
		for(int i=1;i<dimensions.length;i++)
			totalSize *= dimensions[i];
		this.array = new Object[totalSize];
	}


	public Integer[] getDimensions() {
		return dimensions;
	}
	
	public int getDimensionsNumber(){
		return dimensions.length;
	}
	
	public int getDimensionElementCount(int dimension){
		return dimensions[dimension];
	}


	public Object[] getArray() {
		return array;
	}
	
	public Object getElement(int... args){
		return array[toLinearIndex(args)];
	}
	
	public void setElement(Object value, int... args){
		array[toLinearIndex(args)] = value;
	}
	
	private int toLinearIndex(int [] indexs){
		int index = 0;
		int i = 0;
		for(; i < dimensions.length - 1; i++){
			index += indexs[i] * dimensions[i];
			
		}	
		index += indexs[i];
		return index;
		
	}
	
}

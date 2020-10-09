package com.knowm.kt_ram_client.business;

public class Activation {

	public int module;
	public int unit;
	public int array;
	public int column;
	public int row;

	public Activation() {// for JSON

	}

	public Activation(int module, int unit, int array, int column, int row) {
		this.module = module;
		this.unit = unit;
		this.array = array;
		this.column = column;
		this.row = row;
	}

	public int getModule() {
		return module;
	}

	public void setModule(int module) {
		this.module = module;
	}

	public int getUnit() {
		return unit;
	}

	public void setUnit(int unit) {
		this.unit = unit;
	}

	public int getArray() {
		return array;
	}

	public void setArray(int array) {
		this.array = array;
	}

	public int getColumn() {
		return column;
	}

	public void setColumn(int column) {
		this.column = column;
	}

	public int getRow() {
		return row;
	}

	public void setRow(int row) {
		this.row = row;
	}

	@Override
	public String toString() {
		return module + ":" + unit + ":" + array + ":" + column + ":" + row;
	}

}

package de.gaalop.tba.generatedTests;

import java.util.HashMap;

public class CircleOneVar implements GAProgram {
	// input variables
	private float x1_0;

	// output variables
	private float m_1;
	private float r_0;
	private float m_2;

	@Override
	public float getValue(String varName) {
		if (varName.equals("m_1")) return m_1;
		if (varName.equals("r_0")) return r_0;
		if (varName.equals("m_2")) return m_2;
		return 0.0f;
	}

	@Override
	public HashMap<String,Float> getValues() {
		HashMap<String,Float> result = new HashMap<String,Float>();
		result.put("m_1",m_1);
		result.put("r_0",r_0);
		result.put("m_2",m_2);
		return result;
	}
	@Override
	public boolean setValue(String varName, float value) {
		if (varName.equals("x1_0")) { x1_0 = value; return true; }
		return false;
	}
	
	@Override
	public void calculate() {
		p1_4 = ((0.5f * (x1_0 * x1_0)) + 2.0f); // einf;
		c_7 = (((-((9.0f * p1_4))) + (4.0f * ((p1_4 - 45.0f)))) + 272.0f); // e1^e3;
		c_10 = ((((-((45.0f * x1_0))) + (26.0f * ((x1_0 - 3.0f)))) + (3.0f * p1_4)) - (6.0f * ((p1_4 - 45.0f)))); // e2^e3;
		c_13 = (((4.0f * (((45.0f * x1_0) - (3.0f * p1_4)))) - (26.0f * (((9.0f * x1_0) - 6.0f)))) - (6.0f * ((90.0f - (9.0f * p1_4))))); // e3^einf;
		c_14 = (((9.0f * x1_0) - (4.0f * ((x1_0 - 3.0f)))) - 48.0f); // e3^e0;
		calculate1();
	}

	public void calculate1() {
		mtmp_1 = ((2.0f * c_14) * c_7); // e1;
		mtmp_2 = ((2.0f * c_10) * c_14); // e2;
		mtmp_5 = (-((2.0f * (c_14 * c_14)))); // e0;
		mtmp_21 = 0.0f; // e1^einf^e0;
		mtmp_24 = 0.0f; // e2^einf^e0;
		m_1 = ((mtmp_21 / mtmp_5) + (mtmp_1 / mtmp_5)); // e1;
		m_2 = ((mtmp_24 / mtmp_5) + (mtmp_2 / mtmp_5)); // e2;
		r_0 = ((float) Math.sqrt(Math.abs((((c_7 * c_7) - ((2.0f * c_13) * c_14)) + (c_10 * c_10)))) / Math.abs(c_14)); // 1.0;
	}

	private float c_13;
	private float c_14;
	private float p1_4;
	private float mtmp_5;
	private float c_10;
	private float c_7;
	private float mtmp_24;
	private float mtmp_21;
	private float mtmp_1;
	private float mtmp_2;

}

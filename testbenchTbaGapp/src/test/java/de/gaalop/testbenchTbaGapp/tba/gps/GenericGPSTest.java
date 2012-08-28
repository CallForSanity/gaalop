package de.gaalop.testbenchTbaGapp.tba.gps;

import de.gaalop.testbenchTbaGapp.tba.GenericTestable;
import de.gaalop.tba.UseAlgebra;

/**
 * Implements a generic gps test
 * @author Christian Steinmetz
 */
public abstract class GenericGPSTest implements GenericTestable {

    protected static final double EPSILON = 10E-4;

    protected abstract String constantDefinition();
    protected Point3D sat1;
    protected Point3D sat2;
    protected Point3D sat3;
    protected double d1;
    protected double d2;
    protected double d3;

    public GenericGPSTest(Point3D sat1, Point3D sat2, Point3D sat3, double d1, double d2, double d3) {
        this.sat1 = sat1;
        this.sat2 = sat2;
        this.sat3 = sat3;
        this.d1 = d1;
        this.d2 = d2;
        this.d3 = d3;
    }

    @Override
    public String getCLUScript() {
        return "//#pragma output rc1N e1 e2 e3" + "\n"
                + "//#pragma output rc2N e1 e2 e3" + "\n"
                + "//#pragma output z11 1.0" + "\n"
                + "//#pragma output z12 1.0" + "\n"
                + "//#pragma output z21 1.0" + "\n"
                + "//#pragma output z22 1.0" + "\n"
                + "//#pragma output z31 1.0" + "\n"
                + "//#pragma output z32 1.0" + "\n"
                + constantDefinition()
                + "sat1 = VecN3(sat1x,sat1y,sat1z);\n"
                + "sat2 = VecN3(sat2x,sat2y,sat2z);\n"
                + "sat3 = VecN3(sat3x,sat3y,sat3z);\n"
                + "// create the three spheres\n"
                + "sph1 = sat1 - 0.5*d1*d1*einf;\n"
                + "sph2 = sat2 - 0.5*d2*d2*einf;\n"
                + "sph3 = sat3 - 0.5*d3*d3*einf;\n"
                + "// calculate the intersection point pair of three spheres\n"
                + "rcPp = sph1^sph2^sph3;\n"
                + "len = sqrt(abs(rcPp.rcPp));\n"
                + "rcPpDual = *rcPp;\n"
                + "nen = einf.rcPpDual;\n"
                + "// get first point\n"
                + "rc1 = (rcPpDual + len) / nen;\n"
                + "?rc1N = - rc1/(rc1.einf);\n"
                + "// get second point\n"
                + "rc2 = (rcPpDual - len) / nen;\n"
                + "?rc2N = - rc2/(rc2.einf);\n"
                + "// the following mv must be zero\n"
                + "?z11 = (sqrt(-2*rc1N.sat1))-d1;\n"
                + "?z12 = (sqrt(-2*rc2N.sat1))-d1;\n"
                + "?z21 = (sqrt(-2*rc1N.sat2))-d2;\n"
                + "?z22 = (sqrt(-2*rc2N.sat2))-d2;\n"
                + "?z31 = (sqrt(-2*rc1N.sat3))-d3;\n"
                + "?z32 = (sqrt(-2*rc2N.sat3))-d3;\n";
    }

    /**
     * Returns the distance of two points
     * @param p1x The x-coordinate from the first point
     * @param p1y The y-coordinate from the first point
     * @param p2x The x-coordinate from the second point
     * @param p2y The y-coordinate from the second point
     * @return The distance
     */
    protected String getDistance(String p1x, String p1y, String p1z, String p2x, String p2y, String p2z) {
        return "Math.sqrt((" + p2x + "-" + p1x + ")*(" + p2x + "-" + p1x + ") + (" + p2y + "-" + p1y + ")*(" + p2y + "-" + p1y + ") + (" + p2z + "-" + p1z + ")*(" + p2z + "-" + p1z + "))";
    }

    protected String getChecksForAllInstances() {
        return "// check containing all outputs\n"
                + "assertTrue(outputs.containsKey(\"rc1N$1\"));\n"
                + "assertTrue(outputs.containsKey(\"rc1N$2\"));\n"
                + "assertTrue(outputs.containsKey(\"rc1N$3\"));\n"
                + "assertTrue(outputs.containsKey(\"rc2N$1\"));\n"
                + "assertTrue(outputs.containsKey(\"rc2N$2\"));\n"
                + "assertTrue(outputs.containsKey(\"rc2N$3\"));\n"
                + "assertTrue(outputs.containsKey(\"z11$0\"));\n"
                + "assertTrue(outputs.containsKey(\"z12$0\"));\n"
                + "assertTrue(outputs.containsKey(\"z21$0\"));\n"
                + "assertTrue(outputs.containsKey(\"z22$0\"));\n"
                + "assertTrue(outputs.containsKey(\"z31$0\"));\n"
                + "assertTrue(outputs.containsKey(\"z32$0\"));\n"
                + "double rc1Nx = outputs.get(\"rc1N$1\");\n"
                + "double rc1Ny = outputs.get(\"rc1N$2\");\n"
                + "double rc1Nz = outputs.get(\"rc1N$3\");\n"
                + "double rc2Nx = outputs.get(\"rc2N$1\");\n"
                + "double rc2Ny = outputs.get(\"rc2N$2\");\n"
                + "double rc2Nz = outputs.get(\"rc2N$3\");\n"
                + "double z11 = outputs.get(\"z11$0\");\n"
                + "double z12 = outputs.get(\"z12$0\");\n"
                + "double z21 = outputs.get(\"z21$0\");\n"
                + "double z22 = outputs.get(\"z22$0\");\n"
                + "double z31 = outputs.get(\"z31$0\");\n"
                + "double z32 = outputs.get(\"z32$0\");\n"
                + "assertEquals(0,z11," + EPSILON + ");\n"
                + "assertEquals(0,z12," + EPSILON + ");\n"
                + "assertEquals(0,z21," + EPSILON + ");\n"
                + "assertEquals(0,z22," + EPSILON + ");\n"
                + "assertEquals(0,z31," + EPSILON + ");\n"
                + "assertEquals(0,z32," + EPSILON + ");\n"
                + "// check number of outputs\n"
                + "assertEquals(12, outputs.size());\n";
    }

    @Override
    public UseAlgebra getUsedAlgebra() {
        return UseAlgebra.get5dConformalGATable();
    }
}
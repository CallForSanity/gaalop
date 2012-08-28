package de.gaalop.clucalc.output;

import de.gaalop.Notifications;
import de.gaalop.cfg.*;
import de.gaalop.clucalc.input.CluCalcFileHeader;
import de.gaalop.dfg.Expression;
import de.gaalop.dfg.MultivectorComponent;
import de.gaalop.dfg.Variable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Visits the control structure of the control dataflow graph.
 */
public class CfgVisitor implements ControlFlowVisitor {
	
	private static final String MAPLE_SUFFIX = "_opt";
	String codeSuffix;
	
	private Log log = LogFactory.getLog(CfgVisitor.class);

	private Map<String, Set<Integer>> assignedComponents = new HashMap<String, Set<Integer>>();

	StringBuilder code = new StringBuilder();

	int indent;
	
	public CfgVisitor(String suffix) {
		codeSuffix = suffix;
	}

	public String getCode() {
		return code.toString();
	}

	@Override
	public void visit(StartNode startNode) {
		CluCalcFileHeader header = CluCalcFileHeader.get(startNode);

		if (header != null) {
			if (header.getNullSpace() != null) {
				switch (header.getNullSpace()) {
				case IPNS:
					code.append(":IPNS;\n");
					break;
				case OPNS:
					code.append(":OPNS;\n");
					break;
				}
			}
			code.append("\n");

		}

		// TODO add "Generated by" header etc.

		// Generate the local variables for all local variables
		for (Variable localVariable : startNode.getGraph().getLocalVariables()) {
			code.append(localVariable.getName() + codeSuffix);
			code.append(" = List(");
			code.append(startNode.getGraph().getAlgebraDefinitionFile().getBladeCount());
			code.append(");\n");
		}

		code.append("\n");

		startNode.getSuccessor().accept(this);
	}

	@Override
	public void visit(EndNode node) {
	}

	@Override
	public void visit(AssignmentNode assignmentNode) {
		Set<Integer> assigned = assignedComponents.get(assignmentNode.getVariable().getName());
		if (assigned != null && assigned.isEmpty()) {
			// in this case the variable is reused and should be reset
			String message = "Variable " + assignmentNode.getVariable().getName() + " has been reset for reuse.";
			log.warn(message);
			Notifications.addWarning(message);
			appendIndent();
			code.append(assignmentNode.getVariable().getName());
			code.append(" = List(32); // reset for reuse\n");
		}

		appendIndent();
		addCode(assignmentNode.getVariable());
		code.append(" = ");
		addCode(assignmentNode.getValue());
		if (assignmentNode.getVariable() instanceof MultivectorComponent) {
			code.append("; // ");

			MultivectorComponent component = (MultivectorComponent) assignmentNode.getVariable();
			code.append(assignmentNode.getGraph().getAlgebraDefinitionFile().getBladeString(component.getBladeIndex()));

			code.append("\n");

			// Record that this component has been set for the multivector
			if (!assignedComponents.containsKey(component.getName())) {
				assignedComponents.put(component.getName(), new HashSet<Integer>());
			}
			assignedComponents.get(component.getName()).add(component.getBladeIndex());
		} else {
			code.append(";\n");
		}

		assignmentNode.getSuccessor().accept(this);
	}
	
	@Override
	public void visit(ExpressionStatement node) {
		appendIndent();
		addCode(node.getExpression());
		code.append(";\n");
		
		node.getSuccessor().accept(this);
	}

	@Override
	public void visit(StoreResultNode node) {
		appendIndent();

		code.append('?');

		// Reassemble all output variables in the value
		Variable outputVariable = (Variable) node.getValue();

		String variableName = outputVariable.getName();
		String opt = variableName + MAPLE_SUFFIX;
		code.append(variableName);
		Set<Integer> var = assignedComponents.get(opt);
		if (var == null) {
			// no assignment for this variable at all -> keep ? operator
		} else {
			code.append(" = ");
                        int bladeCount = node.getGraph().getAlgebraDefinitionFile().getBladeCount();
			for (int i = 0; i < bladeCount; ++i) {
				if (!var.contains(i)) {
					continue;
				}

				Expression blade = node.getGraph().getAlgebraDefinitionFile().getBladeExpression(i);

				code.append(opt.replace(MAPLE_SUFFIX, codeSuffix));
				code.append("(");
				code.append(i + 1);
				code.append(")");
				code.append(" * ");
				addCode(blade);
				code.append(" + ");
			}
			// Remove the last " + "
			code.setLength(code.length() - 3);
		}

		// reset the set of assigned components, so variable can be reused
		assignedComponents.put(opt, new HashSet<Integer>());

		code.append(";\n");

		node.getSuccessor().accept(this);
	}

	@Override
	public void visit(IfThenElseNode node) {
		code.append('\n');
		appendIndent();
		code.append("if (");
		addCode(node.getCondition());
		code.append(") {\n");
		indent++;

		node.getPositive().accept(this);

		indent--;
		appendIndent();
		code.append("}");

		if (node.getNegative() instanceof BlockEndNode) {
			code.append("\n\n");
		} else {
			code.append(" else ");

			boolean isElseIf = false;
			if (node.getNegative() instanceof IfThenElseNode) {
				IfThenElseNode ifthenelse = (IfThenElseNode) node.getNegative();
				isElseIf = ifthenelse.isElseIf();
			}
			if (!isElseIf) {
				code.append("{\n");
				indent++;
			}

			node.getNegative().accept(this);

			if (!isElseIf) {
				indent--;
				appendIndent();
				code.append("}\n\n");
			}
		}

		node.getSuccessor().accept(this);
	}
	
	@Override
	public void visit(LoopNode node) {
		appendIndent();
		code.append("loop {\n");
		indent++;
		node.getBody().accept(this);
		indent--;
		appendIndent();
		code.append("}\n");
		
		node.getSuccessor().accept(this);
	}
	
	@Override
	public void visit(BreakNode breakNode) {
		appendIndent();
		code.append("break;\n");
	}

	@Override
	public void visit(BlockEndNode node) {
		// nothing to do
	}

	void addCode(Expression value) {
		DfgVisitor visitor = new DfgVisitor(codeSuffix, MAPLE_SUFFIX);
		value.accept(visitor);
		code.append(visitor.getCode());
	}

	void appendIndent() {
		for (int i = 0; i < indent; i++) {
			code.append("\t");
		}
	}

	@Override
	public void visit(Macro node) {
		throw new IllegalStateException("Macros should have been inlined.");
	}

	@Override
	public void visit(ColorNode node) {
		appendIndent();
		code.append(":");
		code.append(node);
		code.append(";\n");
		node.getSuccessor().accept(this);
	}
}
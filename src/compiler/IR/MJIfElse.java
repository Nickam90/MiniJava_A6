package compiler.IR;

import java.util.HashSet;

import compiler.PrettyPrinter;
import compiler.CODE.CODE;
import compiler.CODE.LC3.*;
import compiler.Exceptions.CodeGenException;
import compiler.Exceptions.TypeCheckerException;

public class MJIfElse extends MJIf {

	private MJBlock elseblock;

	public MJIfElse(MJExpression condition, MJBlock thenblock, MJBlock elseblock) {
		super(condition, thenblock);
		this.elseblock = elseblock;
	}
	
	public MJBlock getElseblock() {
		return elseblock;
	}

	public void prettyPrint(PrettyPrinter prepri) {
		super.prettyPrint(prepri);
		if (this.elseblock != null) {
			prepri.println("else");
			this.elseblock.prettyPrint(prepri);
		}
	}
	
	public void rewriteTwo() {
		super.rewriteTwo();
		
		this.elseblock.rewriteTwo();
	}

	MJType typeCheck() throws TypeCheckerException {

		super.typeCheck();
		this.elseblock.typeCheck();

		return MJType.getVoidType();
	}

	void variableInit(HashSet<MJVariable> initialized)
			throws TypeCheckerException {
		
		// check the condition
		this.getCondition().variableInit(initialized);

		// check the then block with a new copy of the set
		HashSet<MJVariable> thenset = (HashSet<MJVariable>) initialized.clone();
		this.getThenblock().variableInit(thenset);

		// check it with a new copy of the set
		HashSet<MJVariable> elseset;
		elseset = (HashSet<MJVariable>) initialized.clone();
		this.elseblock.variableInit(elseset);
			
		//compute the intersection of the then and the else set
		thenset.retainAll(elseset);
			
		// and add it to initialized
		initialized.addAll(thenset);
	}

	public int requiredStackSize() {
		
		int maxstack = this.getThenblock().requiredStackSize();
		
		maxstack = Math.max(maxstack, this.elseblock.requiredStackSize());
		
		return maxstack;
	}

	public void generateCode(CODE code) throws CodeGenException {
		LC3label end = code.newLabel();
		LC3label mid = code.newLabel();		
		
		code.comment(" IF/ELSE ");
		super.generateCode(code);
//		super.getCondition().generateCode(code);
		code.pop(CODE.TMP1);
		code.add(new LC3BRNP(end));
		this.elseblock.generateCode(code);
		code.pop(CODE.TMP1);
		code.push(CODE.TMP1);
		code.add(end);
		code.comment(" IF/ELSE END ");
	}
}

package ru.dz.soot;

import java.util.LinkedList;
import java.util.List;

import ru.dz.plc.compiler.Method;
import ru.dz.plc.compiler.ParseState;
import ru.dz.plc.compiler.PhantomClass;
import ru.dz.plc.compiler.PhantomType;
import ru.dz.plc.compiler.PhantomVariable;
import ru.dz.plc.compiler.binode.OpAssignNode;
import ru.dz.plc.compiler.binode.SequenceNode;
import ru.dz.plc.compiler.node.EmptyNode;
import ru.dz.plc.compiler.node.IdentNode;
import ru.dz.plc.compiler.node.JumpNode;
import ru.dz.plc.compiler.node.Node;
import ru.dz.plc.compiler.node.NullNode;
import ru.dz.plc.compiler.node.ThisNode;
import ru.dz.plc.compiler.node.ThrowNode;
import ru.dz.plc.compiler.node.VoidNode;
import ru.dz.plc.compiler.trinode.IfNode;
import ru.dz.plc.util.PlcException;
import soot.Body;
import soot.PatchingChain;
import soot.SootMethod;
import soot.SootMethodRef;
import soot.Type;
import soot.Unit;
import soot.UnitBox;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.BreakpointStmt;
import soot.jimple.EnterMonitorStmt;
import soot.jimple.ExitMonitorStmt;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InvokeExpr;
import soot.jimple.InvokeStmt;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.NopStmt;
import soot.jimple.RetStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.StmtSwitch;
import soot.jimple.TableSwitchStmt;
import soot.jimple.ThrowStmt;
import soot.jimple.internal.AbstractStmt;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JGotoStmt;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JReturnStmt;
import soot.jimple.internal.JReturnVoidStmt;
import soot.tagkit.Tag;
import soot.util.Switch;
import sun.security.action.GetLongAction;

public class SootMethodTranslator {

	private SootLabelMap lmap = new SootLabelMap();
	private SootMethod m;

	private List<PhantomCodeWrapper> statements = new LinkedList<PhantomCodeWrapper>();
	private String mName;
	private Method phantomMethod;
	private PhantomClass pc;
	private ParseState s;
	
	public SootMethodTranslator(SootMethod m, PhantomClass pc) throws PlcException {
		this.m = m;
		this.pc = pc;
		mName = m.getName();

		s = new ParseState(pc);
		
		Type returnType = m.getReturnType();
		
		PhantomType type = SootExpressionTranslator.convertType(returnType);
		phantomMethod = new Method(mName, type); 
		pc.addMethod(phantomMethod);
	
		for( int i = 0; i < m.getParameterCount(); i++ )
		{
			Type parameterType = m.getParameterType(i);
			PhantomType pptype = SootExpressionTranslator.convertType(parameterType);
			String parName = String.format("parm%d", i);
			phantomMethod.addArg(parName, pptype);
		}

	}

	
	
	
	public void process() throws PlcException {
		say("Method "+mName);

		m.retrieveActiveBody();
		
		Body body = m.getActiveBody();
		
		PatchingChain<Unit> units = body.getUnits();
		
		for( Unit u : units )
		{
			doUnit(u);
		}
		
	}

	
	/*public void codegen()
	{
		// TO DO write codegen
	}*/
	
	//static private JimpleToBafContext context = new JimpleToBafContext(0);

	private void doUnit(Unit u) throws PlcException {
		String dump = u.toString(); say("\n  "+dump);
		//say("  "+u.getClass().getName());

		List<UnitBox> boxes = u.getBoxesPointingToThis();
		for( UnitBox ub : boxes )
		{
			if( ub.isBranchTarget() )
			{
				String labelFor = lmap.getLabelFor(ub);
				say(""+labelFor+":");
			}
		}
		
		phantomMethod.code = new SequenceNode(null, null);
		
		if( u instanceof AbstractStmt )
		{
			 AbstractStmt as = (AbstractStmt)u;
			 PhantomCodeWrapper statement = doStatement(as);
			 if( statement != null)
			 {
			 statements.add(statement);
			 phantomMethod.code  =
					 new SequenceNode(
							 phantomMethod.code, 
							 statement.getNode());
			 }
			 else
				 SootMain.say("null statement");
		}
		
		s.set_method(phantomMethod);
		phantomMethod.code.preprocess(s);
		
		List<Tag> tags = u.getTags();
		for( Tag t : tags )
		{
			String ts = t.toString();
			say("     "+ts);
		}

		/*
		List<UnitBox> boxes = u.getUnitBoxes();
		for( UnitBox b : boxes )
		{
			say("     "+b.toString());
		}
		*/
	}

	private PhantomCodeWrapper doStatement(AbstractStmt as) throws PlcException {
		final ww ret = new ww();
		ret.w = null;

		as.apply(new StmtSwitch() {

			@Override
			public void caseAssignStmt(AssignStmt as ) {
				try {
					ret.w = doAssign( as );
				} catch (PlcException e) {
					SootMain.error(e);
				}
			}

			@Override
			public void caseBreakpointStmt(BreakpointStmt arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void caseEnterMonitorStmt(EnterMonitorStmt arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void caseExitMonitorStmt(ExitMonitorStmt arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void caseGotoStmt(GotoStmt as ) {
				ret.w = doGoto( as );
				}

			@Override
			public void caseIdentityStmt(IdentityStmt as ) {
				try {
					ret.w = doIdentity( as );
				} catch (PlcException e) {
					SootMain.error(e);
				}
			}

			@Override
			public void caseIfStmt(IfStmt as) {
				try {
					ret.w = doIf( as );
				} catch (PlcException e) {
					SootMain.error(e);
				}
			}

			@Override
			public void caseInvokeStmt(InvokeStmt as ) {
				ret.w = doInvoke( as );
			}

			@Override
			public void caseNopStmt(NopStmt arg0) {
				ret.w = new PhantomCodeWrapper(new EmptyNode());				
			}

			@Override
			public void caseRetStmt(RetStmt arg0) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void caseReturnStmt(ReturnStmt as ) {
				try {
					ret.w = doReturn( as );
				} catch (PlcException e) {
					SootMain.error(e);
				}
			}

			@Override
			public void caseReturnVoidStmt(ReturnVoidStmt as ) {
				ret.w = doRetVoid( as );				
			}

			
			@Override
			public void caseTableSwitchStmt(TableSwitchStmt arg0) {
				// TODO make switch statement
				
			}
			
			@Override
			public void caseLookupSwitchStmt(LookupSwitchStmt arg0) {
				// TODO make switch statement
				
			}


			
			
			@Override
			public void caseThrowStmt(ThrowStmt as) {
				try {
					PhantomCodeWrapper expression = PhantomCodeWrapper.getExpression( as.getOp(), phantomMethod, pc );
					ret.w = new PhantomCodeWrapper(new ThrowNode(expression.getNode()));				
				} catch (PlcException e) {
					SootMain.error(e);
				}		
				
			}

			@Override
			public void defaultCase(Object arg0) {
				// Intentionally left blank
			}
			
		});
		
		if( ret.w != null ) return ret.w;
		
		//if( as instanceof JIdentityStmt )			return doIdentity( (JIdentityStmt)as );
		//if( as instanceof JReturnVoidStmt )			return doRetVoid( (JReturnVoidStmt)as );		
		//if( as instanceof JReturnStmt )			return doReturn( (JReturnStmt)as );		
		//if( as instanceof JInvokeStmt )			return doInvoke( (JInvokeStmt)as );		
		//if( as instanceof JGotoStmt )			return doGoto( (JGotoStmt)as );
		//if( as instanceof JAssignStmt )			return doAssign( (JAssignStmt)as );
		//if( as instanceof JIfStmt ) 			return doIf( (JIfStmt)as );
		
		
		SootMain.say("s ?? "+as.getClass().getName()+" ("+as.toString()+")");

		return PhantomCodeWrapper.getNullNode();
	}

	
	
	
	
	


	private PhantomCodeWrapper doGoto(GotoStmt as) {
		UnitBox targetBox = as.getTargetBox();
		String label = lmap.getLabelFor(targetBox);
		
		//say("      go to "+targetBox.toString()+" ("+label+")");
		//Unit target = as.getTarget();
		//say("      if "+target.toString());

		return PhantomCodeWrapper.getJumpNode(label);
	}
	
	
	private PhantomCodeWrapper doIf(IfStmt as) throws PlcException {
		UnitBox targetBox = as.getTargetBox();
		String label = lmap.getLabelFor(targetBox);

		//say("      go to "+targetBox.toString()+" ("+label+")");
		//Unit target = as.getTarget();
		//say("      if "+target.toString());
		
		
		PhantomCodeWrapper expression = PhantomCodeWrapper.getExpression( as.getCondition(), phantomMethod, pc );		
		return new PhantomCodeWrapper(new IfNode(expression.getNode(),new JumpNode(label), new EmptyNode()));
	}




	private PhantomCodeWrapper doAssign(AssignStmt as) throws PlcException {
		/*
		ValueBox leftBox = as.leftBox;
		ValueBox rightBox = as.rightBox;
		
		String ls = leftBox.toString();
		String rs = rightBox.toString();
		
		say("      Assign '"+ls+"' = '"+rs+"'");
		*/
		PhantomCodeWrapper expression = PhantomCodeWrapper.getExpression( as.getRightOp(), phantomMethod, pc );		
		return PhantomCodeWrapper.getAssign( as.getLeftOp(), expression, phantomMethod );
	}


	private PhantomCodeWrapper doInvoke(InvokeStmt as) {
		InvokeExpr expr = as.getInvokeExpr();
		say("      Invoke "+expr.toString());
		SootMethodRef methodRef = expr.getMethodRef();
		say("      ."+methodRef.name());
		// TODO make invoke
		return null;
	}

	
	
	private PhantomCodeWrapper doRetVoid(ReturnVoidStmt as) {
		//say("      Return void ");
		return PhantomCodeWrapper.getReturnNode();
	}

	private PhantomCodeWrapper doReturn(ReturnStmt as) throws PlcException {
		Value op = as.getOp();
		PhantomCodeWrapper v = PhantomCodeWrapper.getExpression( op, phantomMethod, pc );
		//say("      Return "+op.toString());
		return PhantomCodeWrapper.getReturnValueNode(v);
	}



	
	/**
	 * Identity - make ident to refer to some stuff, like arg or this.
	 * @param as statement
	 * @return generated code tree
	 * @throws PlcException
	 */
	private PhantomCodeWrapper doIdentity(IdentityStmt as) throws PlcException {
		//Value lv = as.leftBox.getValue();
		//Value rv = as.rightBox.getValue();

		Value lv = as.getLeftOp();
		Value rv = as.getRightOp();
		
		// Arg: we just create var def and stick it to fixed object stack position, which is
		// a correct way to do it.
		if(
				(lv.getClass() == soot.jimple.internal.JimpleLocal.class) &&
				(rv.getClass() == soot.jimple.ParameterRef.class)
				)
		{
			soot.jimple.internal.JimpleLocal jLocal = (soot.jimple.internal.JimpleLocal)lv;
			soot.jimple.ParameterRef parmRef = (soot.jimple.ParameterRef)rv;
			
			String localName = jLocal.getName();
			Type type = jLocal.getType();

			// TODO make sure that parameter position is correct, or else we need here (numPar - parameterPosition - 1)
			int parameterPosition = parmRef.getIndex();
			
			phantomMethod.svars.setParameter(parameterPosition, localName, SootExpressionTranslator.convertType(type));
			return new PhantomCodeWrapper(new NullNode());
		}

		// This: we really should set up a virtual variable that codegens 'summon this',
		// but for simplicity we generate code to load this to some var
		if(
				(lv.getClass() == soot.jimple.internal.JimpleLocal.class) &&
				(rv.getClass() == soot.jimple.ThisRef.class)
				)
		{
			// Create stack var ans load it with 'this'
			
			soot.jimple.internal.JimpleLocal jLocal = (soot.jimple.internal.JimpleLocal)lv;
			//soot.jimple.ThisRef thisRef = (soot.jimple.ThisRef)rv;
			
			//thisRef.
			
			String localName = jLocal.getName();
			Type type = jLocal.getType();

			PhantomVariable v = new PhantomVariable(localName, SootExpressionTranslator.convertType(type));			
			phantomMethod.svars.add_stack_var(v);
			
			OpAssignNode node = new OpAssignNode(new IdentNode(localName) , new ThisNode(pc));
			
			return new PhantomCodeWrapper(node);
		}
		
		
		//String ls = as.leftBox.toString();
		//String rs = as.rightBox.toString();
		
		//say(" ??   Identity '"+ls+"' <- '"+rs+"'");
		
		say(" ??   Identity '"+lv+"' <- '"+rv+"'");
		say("      Identity '"+lv.getClass()+"' <- '"+rv.getClass()+"'");
		
		
		return null;
	}


	private void say(String string) {
		System.err.println(string);
	}
	
	
}

package gov.nih.nlm.semmed.rules;

import gov.nih.nlm.semmed.model.APredication;

import java.util.List;

public class Not implements Operator {

	public Predicate eval(final List<Predicate> operands) {
		
		//System.err.println("Creating NOT with operands "+operands);
		
		return new Predicate(){
			public boolean eval(List<APredication> predications){
				return !operands.get(0).eval(predications);				
			}			
			
			@Override
			public String toString(){
				StringBuffer sb = new StringBuffer();
				sb.append("(NOT ");
				sb.append(" "+operands.get(0).toString());
				sb.append(")");
				return sb.toString();
			}
		};
	}

}

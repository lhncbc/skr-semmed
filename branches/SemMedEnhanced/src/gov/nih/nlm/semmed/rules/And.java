package gov.nih.nlm.semmed.rules;

import gov.nih.nlm.semmed.model.APredication;

import java.util.List;

public class And implements Operator {

	public Predicate eval(final List<Predicate> operands) {
		
		//System.err.println("Creating AND with operands "+operands);
		if (operands.get(0)==null)
			throw new Error();
		
		return new Predicate(){
			public boolean eval(List<APredication> predications){
				for(Predicate p:operands)
					if (!p.eval(predications))
						return false;
				return true;
			}
			
			@Override
			public String toString(){
				StringBuffer sb = new StringBuffer();
				sb.append("(AND");
				for(Predicate p:operands)
					sb.append(" "+p.toString());
				sb.append(")");
				return sb.toString();
			}
		};
	}

}

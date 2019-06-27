package gov.nih.nlm.semmed.rules;

import gov.nih.nlm.semmed.model.APredication;

import java.util.List;

public class Or implements Operator {

	public Predicate eval(final List<Predicate> operands) {
		
		//System.err.println("Creating OR with operands "+operands);
		
		return new Predicate(){
			public boolean eval(List<APredication> predications){
				for(Predicate p:operands)
					if (p.eval(predications))
						return true;
				return false;
			}	
			
			@Override
			public String toString(){
				StringBuffer sb = new StringBuffer();
				sb.append("(OR");
				for(Predicate p:operands)
					sb.append(" "+p.toString());
				sb.append(")");
				return sb.toString();
			}

		};
	}

}

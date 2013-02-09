package ly.dollar.tx.entity;

import java.io.Serializable;
import java.util.Collection;

import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class Ledger implements Serializable {

	private static final long serialVersionUID = 1L;
	private Collection<IouOrder> collect;
	private Collection<IouOrder> pay;
	
	public Collection<IouOrder> getCollect() {
		return collect;
	}
	public void setCollect(Collection<IouOrder> collect) {
		this.collect = collect;
	}
	public Collection<IouOrder> getPay() {
		return pay;
	}
	public void setPay(Collection<IouOrder> pay) {
		this.pay = pay;
	}
}

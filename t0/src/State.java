
public abstract class State{
	private final State parent;
	
	protected State(State parent){
		this.parent= parent;
	}
	
	public abstract void action(Customer customer, VendingMachine vendingMachine);
	
	public abstract State getNextState();
	
	State getParent(){
		return this.parent;
	}
}

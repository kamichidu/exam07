package vendingmachine;

import java.util.Collection;

import com.google.common.collect.ImmutableList;

public class VendingMachineSelection implements Scene{
	private final IOManager iomanager;
	private final ImmutableList<VendingMachine> availables;
	
	public VendingMachineSelection(IOManager iomanager, Collection<VendingMachine> availables){
		this.iomanager= iomanager;
		this.availables= ImmutableList.copyOf(availables);
	}
	
	@Override
	public Scene start(){
		System.out.println();
		VendingMachine selected= this.iomanager.select("購入する自動販売機を選択してください。", this.availables);
		
		return selected;
	}
}

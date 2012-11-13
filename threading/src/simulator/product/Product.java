package simulator.product;

import simulator.io.Formattable;
import simulator.util.Yen;

public enum Product implements Formattable{
	COFFEE("コーヒー", new Yen(120)), 
	BOTTLE_COLA("ボトルのコーラ", new Yen(150)), 
	ENERGY_DRINK("栄養ドリンク", new Yen(200)), 
	TEE_OF_TOKUHO("トクホのお茶", new Yen(190));

	private final String name;
	private final Yen price;
	private Product(String name, Yen price){
		this.name= name;
		this.price= price;
	}
	@Override
	public String getDisplayText(){
		return String.format("%s%s円", this.name, this.price);
	}
}
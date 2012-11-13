package simulator.customer;

import java.util.Collection;

import simulator.currency.Coin;
import simulator.currency.Coins;
import simulator.currency.Wallet;
import simulator.io.Appender;
import simulator.io.ConsoleAppender;
import simulator.io.DefaultLayout;
import simulator.product.Product;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

public class Customer{
	private final Appender appender;
	private final Wallet wallet;
	private final Collection<Product> bucket;
	
	public Customer(){
		this.appender= new ConsoleAppender(new DefaultLayout());
		this.wallet= new Wallet( //
				Coins.fiveHundredYenCoin(), // 500円玉が1枚
				Coins.oneHundredYenCoin(), Coins.oneHundredYenCoin(), // 100円玉が2枚
				Coins.fiftyYenCoin(), // 50円玉が1枚
				Coins.tenYenCoin(), Coins.tenYenCoin(), Coins.tenYenCoin() // 10円玉が3枚
				);
		this.bucket= Lists.newArrayList();
	}
	
	public ImmutableSet<Coin> getUniqueCoinSet(){
		return this.wallet.getUniqueCoinSet();
	}
	
	public Coin pay(Coin paied){
		return this.wallet.get(paied);
	}
	
	public void giveCoins(Collection<Coin> coins){
		System.out.println("giveCoins " + coins);
	}
	
	public void showResult(Appender appender){
		this.appender.writeln("〜お買いもの結果〜");
		
		if(this.bucket.isEmpty()){
			this.appender.writeln("あなたは何も買っていません。");
		}
		else{
			this.appender.writeln("あなたは%sを持っています。", this.formatBoughtProducts());
		}
		
		if(this.wallet.isEmpty()){
			this.appender.writeln("あなたのお財布の中には何もありません。");
		}
		else{
			this.appender.writeln("あなたのお財布の中には%sあります。", this.formatWallet());
		}
	}
	
	private String formatBoughtProducts(){
		return "hogehoge";
	}
	
	private String formatWallet(){
		return "piyopiyo";
	}
}

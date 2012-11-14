package simulator.vendingmachine;

import static com.google.common.base.Preconditions.checkState;

import java.util.Collection;
import java.util.Collections;
import java.util.Formattable;
import java.util.Formatter;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingQueue;

import simulator.currency.Coin;
import simulator.currency.Coins;
import simulator.currency.Wallet;
import simulator.customer.Customer;
import simulator.io.Appender;
import simulator.product.Product;
import simulator.util.Yen;

import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.ImmutableSortedSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Multiset;
import com.google.common.collect.Queues;

public class VendingMachine implements Callable<Customer>{
	private final Appender appender;
	private final BlockingQueue<Customer> customerQueue= new LinkedBlockingQueue<>();
	private final Wallet coinPool;
	private final Wallet safeBox;
	
	public VendingMachine(Appender appender){
		this.appender= appender;
		this.coinPool= new Wallet();
		this.safeBox= new Wallet();
	}
	
	public void comeNewCustomer(Customer newCustomer){
		this.customerQueue.add(newCustomer);
	}
	
	/**
	 * 
	 * @return 買い物が終わったお客さん
	 * @throws Exception
	 */
	@Override
	public Customer call() throws Exception{
		Customer customer= this.customerQueue.take();
		
		Operation op= new SelectOperation();
		while(op != null){
			op= op.perform(customer);
		}
		
		return customer;
	}
	
	public void putInto(Coin coin){
		this.coinPool.add(coin);
	}
	
	public Product sell(Product product){
		// XXX: 在庫を気にしないで良いので、そのまま返す
		return product;
	}
	
	/**
	 * 返却するコインの枚数が最小になるように、おつりを返却する。
	 * 
	 * @return
	 */
	public Collection<Coin> payback(){
		// 金額の高い順にコインを1種類ずつ保持
		ImmutableSortedSet<Coin> coinSet= ImmutableSortedSet.copyOf( //
				Coins.descComparator(), //
				Coins.getOneOfEach() //
				);
		Collection<Coin> payback= Lists.newArrayList();
		
		Yen remain= this.coinPool.getTotalAmount();
		for(Coin coin : coinSet){
			while(remain.compareTo(coin.getAmount()) >= 0){
				remain= remain.subtract(coin.getAmount());
				
				if(remain.compareTo(Yen.zero()) < 0){ throw new AssertionError(); }
				
				payback.add(coin);
				
				if(remain.compareTo(Yen.zero()) == 0){
					this.coinPool.clear();
					this.appender.writeln(this.formatPayback(payback));
					return payback;
				}
			}
		}
		
		// おつりなし
		this.appender.writeln("現在0円なので、返却するコインはありません。");
		return Collections.emptyList();
	}
	
	private String formatPayback(Collection<Coin> payback){
		// コインの種類
		ImmutableSortedSet<Coin> kinds= ImmutableSortedSet.copyOf(Coins.descComparator(), payback);
		// コインの枚数
		ImmutableMultiset<Coin> ncoins= ImmutableMultiset.copyOf(payback);
		// 合計金額
		Yen total= Yen.zero();
		for(Coin coin : payback){
			total= total.add(coin.getAmount());
		}
		// 高い順に表示
		return String.format("%s円の返却です。%sを返却します。", total,
				this.formatPaybackDetails(Queues.newPriorityQueue(kinds), ncoins));
	}
	
	private String formatPaybackDetails(Queue<Coin> kinds, Multiset<Coin> ncoins){
		if(kinds.isEmpty()){ return ""; }
		
		Coin coin= kinds.poll();
		int count= ncoins.count(coin);
		
		return String.format("%s玉%d枚と", coin, count)
				+ this.formatPaybackDetails(kinds, ncoins);
	}
	
	public Yen getTotalAmount(){
		return this.coinPool.getTotalAmount();
	}
	
	private static interface Operation extends Formattable{
		Operation perform(Customer customer);
	}
	
	private class Exit implements Operation{
		private final Operation beforeExit;
		
		Exit(){
			this.beforeExit= null;
		}
		
		Exit(Operation performBeforeExit){
			this.beforeExit= performBeforeExit;
		}
		
		@Override
		public Operation perform(Customer customer){
			// 終了前に呼ぶ操作が指定されていたときには、操作を呼んでから終了する
			if(this.beforeExit != null){
				this.beforeExit.perform(customer);
			}
			return null;
		}
		
		@Override
		public void formatTo(Formatter fmt, int flags, int width, int precision){
			fmt.format("終了");
		}
	}
	
	private class SelectOperation implements Operation{
		@Override
		public Operation perform(Customer customer){
			ImmutableList<Operation> operationList= ImmutableList.of( //
					new SelectCoin(), //
					new SelectProduct(), //
					new Payback(this), //
					new Exit(new Payback()) //
					);
			
			Operation selected= appender.select("操作を選択してください。", operationList);
			
			return selected;
		}
		
		@Override
		public void formatTo(Formatter fmt, int flags, int width, int precision){
			fmt.format("操作の選択");
		}
	}
	
	private class SelectCoin implements Operation{
		@Override
		public Operation perform(Customer customer){
			ImmutableList<Coin> coinList= ImmutableList.copyOf(customer.getUniqueCoinSet());
			
			Coin selected= appender.select("入れるコインを選択してください。", coinList);
			
			putInto(customer.pay(selected));
			appender.writeln("現在%s円入っています。", getTotalAmount());
			
			return new SelectOperation();
		}
		
		@Override
		public void formatTo(Formatter fmt, int flags, int width, int precision){
			fmt.format("コインを入れる");
		}
	}
	
	private class Payback implements Operation{
		private final Operation nextOperation;
		
		Payback(){
			// おつりを出したら、その後は終了がデフォルト
			this.nextOperation= new Exit();
		}
		
		Payback(Operation nextOperation){
			this.nextOperation= nextOperation;
		}
		
		@Override
		public Operation perform(Customer customer){
			// コインの枚数が最小となるようにおつりを出す
			customer.giveCoins(payback());
			
			return this.nextOperation;
		}
		
		@Override
		public void formatTo(Formatter fmt, int flags, int width, int precision){
			fmt.format("コインを戻す");
		}
	}
	
	private class SelectProduct implements Operation{
		@Override
		public Operation perform(Customer customer){
			ImmutableList<Product> productList= ImmutableList.of( //
					Product.COFFEE, //
					Product.BOTTLE_COLA, //
					Product.ENERGY_DRINK, //
					Product.TEE_OF_TOKUHO //
					);
			Product selected= appender.select("商品を選択してください。", productList);
			
			// 売ることができない場合
			if( !this.canSellIt(selected)){
				appender.writeln("現在の投入金額は%#sなので、%#sの商品はお売りできません。", //
						coinPool.getTotalAmount(), //
						selected.getPrice() //
				);
				return new SelectOperation();
			}
			
			// 取引が成立したので、商品価格分を金庫にしまう
			this.move(safeBox, coinPool, selected.getPrice());
			customer.buy(sell(selected));
			
			return new Payback(new SelectOperation());
		}
		
		private void move(Wallet dest, Wallet src, Yen moveAmount){
			final Yen preAmount= dest.getTotalAmount().add(src.getTotalAmount());
			
			// FIXME: 投入金額からお金を崩して、moveAmountちょうどの金額ができるようにする
			
			final Yen postAmount= dest.getTotalAmount().add(src.getTotalAmount());
			checkState( //
					preAmount.compareTo(postAmount) == 0, //
					"投入金額から%#sを金庫にしまった結果、%#sから%#sに投入金額と金庫の金額の合計値が変化しました。", //
					moveAmount, preAmount, postAmount //
			);
		}
		
		private ImmutableCollection<Coin> exchange(Coin orig, Coin want){
			// FIXME: payback()のアルゴリズムを使って、両替する
			return ImmutableList.of(orig);
		}
		
		private boolean canSellIt(Product product){
			// 購入可能条件: 投入金額 >= 商品価格
			return coinPool.getTotalAmount().compareTo(product.getPrice()) >= 0;
		}
		
		@Override
		public void formatTo(Formatter fmt, int flags, int width, int precision){
			fmt.format("買う");
		}
	}
}

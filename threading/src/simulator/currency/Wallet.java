package simulator.currency;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;

import simulator.util.Yen;

import com.google.common.collect.ConcurrentHashMultiset;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multiset;

public class Wallet extends AbstractCollection<Coin>{
	private final Multiset<Coin> impl;
	
	public Wallet(){
		this(ImmutableList.<Coin>of());
	}
	
	public Wallet(Coin... coins){
		this(ImmutableList.copyOf(coins));
	}
	
	public Wallet(Iterable<Coin> coins){
		this.impl= ConcurrentHashMultiset.create(coins);
	}
	
	public Yen getTotalAmount(){
		Yen total= Yen.zero();
		
		for(Coin coin : this.impl){
			total= total.add(coin.getAmount());
		}
		
		return total;
	}
	
	public int count(Coin o){
		return this.impl.count(checkNotNull(o));
	}
	
	public ImmutableSet<Coin> getUniqueCoinSet(){
		return ImmutableSet.copyOf(this.impl.elementSet());
	}
	
	public Coin get(Coin coin){
		if(this.impl.contains(checkNotNull(coin))){
    		this.impl.remove(coin);
    		// TODO: 外部から渡されたオブジェクトを返しているが、イテレートして探すべき
    		return coin;
		}
		// TODO: 例外投げる？
		return null;
	}
	
	@Override
	public boolean add(Coin e){
		return this.impl.add(e);
	}
	
	@Override
	public boolean addAll(Collection<? extends Coin> c){
		return this.impl.addAll(c);
	}
	
	@Override
	public boolean remove(Object o){
		return this.impl.remove(o);
	}
	
	@Override
	public boolean removeAll(Collection<?> c){
		return this.impl.removeAll(c);
	}
	
	@Override
	public Iterator<Coin> iterator(){
		return this.impl.iterator();
	}
	
	@Override
	public int size(){
		return this.impl.size();
	}
}

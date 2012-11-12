package guava.predicate;
import java.util.Arrays;

import org.junit.Test;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class Basic{
	@Test
	public void howTo(){
		Predicate<Integer> assertion= Predicates.notNull();
		ImmutableList<Integer> list= ImmutableList.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 9);
		
		Iterable<Integer> it= Iterables.filter(list, assertion);
		ImmutableList<Integer> filtered= new ImmutableList.Builder<Integer>().addAll(it).build();
		
		Iterables.all(Arrays.asList(1, 2, null), Predicates.notNull());
		Preconditions.checkArgument(Iterables.all(Arrays.asList(1, 2, null), Predicates.notNull()), "not null constraint.");
		
		System.out.println(filtered);
	}
}

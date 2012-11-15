package simulator;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import simulator.customer.Customer;
import simulator.io.IOManager;
import simulator.io.ConsoleIOManager;
import simulator.vendingmachine.DrinkVendingMachine;

public class Simulator{
	private static final IOManager appender= new ConsoleIOManager();
	
	public static void main(String[] args){
		DrinkVendingMachine vendingMachine= new DrinkVendingMachine(appender);
		ExecutorService service= Executors.newCachedThreadPool();
		
		vendingMachine.comeNewCustomer(new Customer());
		
		Future<Customer> future= service.submit(vendingMachine);
		
		try{
			Customer customer= future.get();
			
			customer.showResult(appender);
		}
		catch(InterruptedException | ExecutionException e){
			e.printStackTrace();
		}
		finally{
			service.shutdown();
		}
	}
}

package main;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import main.io.Appender;
import main.io.ConsoleAppender;
import main.io.DefaultLayout;

public class Simulator{
	private static final Appender appender= new ConsoleAppender(new DefaultLayout());
	
	public static void main(String[] args){
		VendingMachine vendingMachine= new VendingMachine(appender);
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

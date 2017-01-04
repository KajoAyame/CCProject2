

import java.io.IOException;
import java.net.ServerSocket;

public class Main {
	private static final int PORT = 80;
	private static DataCenterInstance[] instances;
	private static ServerSocket serverSocket;

	//Update this list with the DNS of your data center instances
	static {
		instances = new DataCenterInstance[3];
		instances[0] = new DataCenterInstance("first_instance", "http://ec2-52-91-201-75.compute-1.amazonaws.com");
		instances[1] = new DataCenterInstance("second_instance", "http://ec2-54-210-82-171.compute-1.amazonaws.com");
		instances[2] = new DataCenterInstance("third_instance", "http://ec2-52-23-228-28.compute-1.amazonaws.com");
	}

	public static void main(String[] args) throws IOException {
		
		initServerSocket();
		LoadBalancer loadBalancer = new LoadBalancer(serverSocket, instances);
		Runnable r1 = new HealthCheck(loadBalancer,"http://ec2-52-91-201-75.compute-1.amazonaws.com/lookup/random",0);
		Runnable r2 = new HealthCheck(loadBalancer,"http://ec2-54-210-82-171.compute-1.amazonaws.com/lookup/random",1);
		Runnable r3 = new HealthCheck(loadBalancer,"http://ec2-52-23-228-28.compute-1.amazonaws.com/lookup/random",2);

		Thread healthCheck1 = new Thread(r1);
		Thread healthCheck2 = new Thread(r2);
		Thread healthCheck3 = new Thread(r3);
		
		healthCheck1.start();
		healthCheck2.start();
		healthCheck3.start();

		loadBalancer.start();
		
	
	}

	/**
	 * Initialize the socket on which the Load Balancer will receive requests from the Load Generator
	 */
	private static void initServerSocket() {
		try {
			serverSocket = new ServerSocket(PORT);
		} catch (IOException e) {
			System.err.println("ERROR: Could not listen on port: " + PORT);
			e.printStackTrace();
			System.exit(-1);
		}
	}
}



import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;



public class LoadBalancer {
	private static final int THREAD_POOL_SIZE = 4;
	private final ServerSocket socket;
	private DataCenterInstance[] instances;
	private int[] isRunning = {1,1,1};
	
	public void setInstance(DataCenterInstance instance, int number) {
		this.instances[number] = instance;
	}
	
	public void setRunning(int number, int signal) {
		this.isRunning[number] = signal;
	}
	
	public LoadBalancer(ServerSocket socket, DataCenterInstance[] instances) {
		this.socket = socket;
		this.instances = instances;
	}

	
	public void start() throws IOException {
		
		ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
		int currentInstance = 0;
		while (true) {
			// By default, it will send all requests to the first instance
			
			if (isRunning[currentInstance] == 0) {
				currentInstance++;
				currentInstance %= 3;
				continue;
			}
			
			Runnable requestHandler = new RequestHandler(socket.accept(), instances[currentInstance]);
			executorService.execute(requestHandler);
			currentInstance++;
			currentInstance %= 3;
		}
	}
	
}

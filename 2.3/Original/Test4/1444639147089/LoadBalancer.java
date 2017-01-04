

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Random;



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
		int cnt = 0;
		Random random = new Random();
		int randomNumber = 0;
		int[][] excuteArray = {{0,1,2},{0,2,1},{1,0,2},{1,2,0},{2,0,1},{2,1,0}};
		Runnable requestHandler = null;
		while (true) {

			// Randomly send the load to the three instances.
			for (cnt = 0; cnt < 100; cnt++)
			{			
				randomNumber = random.nextInt(6);				
				for (int i = 0; i < 3; i++)	{
					if (isRunning[excuteArray[randomNumber][i]] == 0) { // Don't send the request to the bad one.
						continue;
					}
					requestHandler = new RequestHandler(socket.accept(), instances[excuteArray[randomNumber][i]]);
					executorService.execute(requestHandler);
				}
			}
		}
	}
	
}

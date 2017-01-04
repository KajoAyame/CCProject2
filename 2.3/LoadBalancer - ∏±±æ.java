import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import java.util.Random;

public class LoadBalancer {
	private static final int THREAD_POOL_SIZE = 4;
	private final ServerSocket socket;
	private final DataCenterInstance[] instances;

	public LoadBalancer(ServerSocket socket, DataCenterInstance[] instances) {
		this.socket = socket;
		this.instances = instances;
	}

	// Complete this function
	public void start() throws IOException {
		ExecutorService executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);

		Random random = new Random();
		int randomNumber = 0;
		int[][] excuteArray = {{0,1,2,0,2,1,1,0,2,1,2,0,2,0,1,2,1,0},
		
		
		System.out.println("Test Start!");
		while (true) {
			// By default, it will send all requests to the first instance
		
				randomNumber = random.nextInt(6);
				
				for (int i = 0; i < 3; i++)	{
					Runnable requestHandler = new RequestHandler(socket.accept(), instances[excuteArray[randomNumber][i]]);
					executorService.execute(requestHandler);

				}
				
			
			
			
		}
	}
}

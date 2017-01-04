import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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

		int currentInstance = 0;
		while (true) {
			Runnable requestHandler = new RequestHandler(socket.accept(), instances[currentInstance]);
			executorService.execute(requestHandler);

			currentInstance++;
			currentInstance %= 3;
		}
	}
}

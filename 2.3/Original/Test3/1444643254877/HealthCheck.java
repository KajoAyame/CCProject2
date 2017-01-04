
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.Instance;



public class HealthCheck implements Runnable {
	private final LoadBalancer loadBalancer;
	private String address;	
	private final int number;
	HttpURLConnection connection = null;
	URL checkUrl = null;

	BasicAWSCredentials bawsc = new BasicAWSCredentials("AKIAJ3AED3E6NDMANTGQ",
			"pnJGBB/w2qIZk1onOQFXR7/pqiGzBETfQBQ4Mm7b");	// Submission password has been deleted.
	AmazonEC2Client ec2 = new AmazonEC2Client(bawsc);	

	public HealthCheck(LoadBalancer loadBalancer, String address, int number) {
		this.loadBalancer = loadBalancer;
		this.address = address;
		this.number = number;
	}

	public void setAddress(String address) {
		this.address = address;
	}
	
	// Connect the data center lookup/random url.
	public void connect(String address) throws Exception{
		checkUrl = new URL(address);
		System.out.println("Connect address:" + address);
		connection = (HttpURLConnection)checkUrl.openConnection();
		connection.setConnectTimeout(1000);
		connection.setReadTimeout(1000);
	}
	
	@Override
	public void run() {
		System.out.println("intorun");
		try {
			go();
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	public void go() throws Exception {		
		System.out.println("intogo");
		boolean isConnect = false;
		while (!isConnect) {			
			try {
				connect(address);
				isConnect = true;
				System.out.println("connect!");
			} catch (Exception e) {
				System.out.println("no connect!");
			}
		}

		// Check the response
		while (true) {
			checkUrl = new URL(address);
			connection = (HttpURLConnection)checkUrl.openConnection();
			connection.setConnectTimeout(1000);
			connection.setReadTimeout(1000);
			System.out.println("Checking" + address);
			try {
				if (connection.getResponseCode() != 200) {
					System.out.println("Timeout!");
					loadBalancer.setRunning(number, 0);
					addNewInstance();
					System.out.println("inside addNewInstance() 5");
					Thread.sleep(25000);
				}
			} catch (IOException e) {
				System.out.println("kill!!!!!!!!!!!!!!!!!!!!!");
				loadBalancer.setRunning(number, 0);
				addNewInstance();
				System.out.println("inside addNewInstance() 5");
				Thread.sleep(25000);
			}	
			Thread.sleep(2000);
		}
	}
	
public void addNewInstance() {
		
		System.out.println("inside addNewInstance()");
		Instance newDataCenter = Launching.lauchInstance(ec2,
				 "ami-ed80c388", 
				 "m3.medium", 
				 1, 
				 1, 
				 "ccProject", 
				 "alltraffic");	
		System.out.println("inside addNewInstance() 2");

		boolean isRun = false;
		while (isRun == false) {			
			isRun = IsRunning.isRunning(newDataCenter.getInstanceId(), ec2);
			try	{
				Thread.sleep(1000);
			} catch (Exception e) {
				System.out.println("Sleep errer!");
			}
			
		}		
		String Dns = GetInstancePublicDnsName.getInstancePublicDnsName(newDataCenter.getInstanceId() , ec2);	
		
		System.out.println("inside addNewInstance() 3");
		
		
		
		System.out.println("inside addNewInstance() 4" + "http://" + Dns + "/lookup/random");
		
		boolean open = false;
		while (!open) {
			try	{
				System.out.println("sleep 2.5s");
				Thread.sleep(2500);
			} catch (Exception e) {
				System.out.println("Sleep errer!");
			}

			try	{
				System.out.println("connect dns");
				URLConnectionReader.connectURL("http://" + Dns);
				System.out.println("connect dns done");
				open = true;
			}
			catch (Exception e) {
			}
		}

		setAddress("http://" + Dns + "/lookup/random");

		System.out.println("inside addNewInstance() 4 open");

		DataCenterInstance instance = new DataCenterInstance("new_instance", "http://" + Dns);
		System.out.println("http://" + Dns);
		loadBalancer.setInstance(instance, number);
		loadBalancer.setRunning(number, 1);
		System.out.println("setRunning number");
	}

}

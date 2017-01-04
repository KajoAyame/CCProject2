
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

	BasicAWSCredentials bawsc = new BasicAWSCredentials("",
			"");	// AWSCredentials has been deleted.
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
		try {
			go();
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
	
	public void go() throws Exception {		
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
			try {
				if (connection.getResponseCode() != 200) {
					System.out.println("Timeout!");
					loadBalancer.setRunning(number, 0);
					addNewInstance();
					Thread.sleep(25000);
				}
			} catch (IOException e) {
				System.out.println("kill!!!!!!!!!!!!!!!!!!!!!");
				loadBalancer.setRunning(number, 0);
				addNewInstance();
				Thread.sleep(25000);
			}	
			Thread.sleep(2000);
		}
	}
	
public void addNewInstance() {
		
		// Launch new instance, and attach it to the Load Generator.
		Instance newDataCenter = Launching.lauchInstance(ec2,
				 "ami-ed80c388", 
				 "m3.medium", 
				 1, 
				 1, 
				 "ccProject", 
				 "alltraffic");	

		boolean isRun = false;
		while (isRun == false) {			
			isRun = IsRunning.isRunning(newDataCenter.getInstanceId(), ec2);
			try	{
				Thread.sleep(1000);
			} catch (Exception e) {
				System.out.println("Sleep error!");
			}
			
		}		
		String Dns = GetInstancePublicDnsName.getInstancePublicDnsName(newDataCenter.getInstanceId() , ec2);	
		
		boolean open = false;
		while (!open) {
			try	{
				System.out.println("sleep 2.5s");
				Thread.sleep(2500);
			} catch (Exception e) {
				System.out.println("Sleep error!");
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

		DataCenterInstance instance = new DataCenterInstance("new_instance", "http://" + Dns);
		System.out.println("http://" + Dns);
		loadBalancer.setInstance(instance, number);
		loadBalancer.setRunning(number, 1);
		System.out.println("setRunning number");
	}

}

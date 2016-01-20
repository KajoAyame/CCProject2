package part2;
/* Project 0
 * This source code is created by Xinghao Zhou independently.
 * Andrew id: xinghaoz
 * Name: Xinghao Zhou
 * Email: xinghaoz@andrew.cmu.edu
 * 
 * Main method is in this class.
*/

import java.util.Properties;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.auth.BasicAWSCredentials;


public class AdvanceCCProject0 {
	
	public static final String MY_SUBMISSION_PASSWORD = new String(""); // Submission password has been deleted.
	public static final String SECURITY_GROUP_NAME = new String("Project0");
	public static final String KEY_PAIR_NAME = new String("ccProject");
	
	public static void main(String[] args) throws Exception{
		
		// Get the Account Id and Secret Key
		Properties properties = new Properties();
		properties.load(AdvanceCCProject0.class.getResourceAsStream("/AwsCredentials.properties"));
		
		BasicAWSCredentials bawsc = 
				new BasicAWSCredentials(properties.getProperty("accessKey"),
						properties.getProperty("secretKey"));
		
		// Create an Amazon EC2 Client
		AmazonEC2Client ec2 = new AmazonEC2Client(bawsc);
		
		/* 1. Create Security Group.
		 * 
		 */
		System.out.println("///////// 1. Create Security Group. /////////");
		
		CreateSecurityGroupRequest csgr = new CreateSecurityGroupRequest();
		csgr.withGroupName(SECURITY_GROUP_NAME)
		.withDescription("Security Group for Advance CC Project 0");
		
		ec2.createSecurityGroup(csgr);
		
		IpPermission ipPermission = new IpPermission();
		
		ipPermission.withIpRanges("0.0.0.0/0")
		.withIpProtocol("tcp")
		.withFromPort(0)
		.withToPort(65535);
		
		AuthorizeSecurityGroupIngressRequest asgir = new AuthorizeSecurityGroupIngressRequest();
		asgir.withGroupName(SECURITY_GROUP_NAME)
		.withIpPermissions(ipPermission);		
		
		ec2.authorizeSecurityGroupIngress(asgir);
		
		
		
		/* 2. Launch load generator.
		 * 
		 */
		Instance loadGenerator = Launching.lauchInstance(ec2,
														 "ami-76164d1c", 
														 "m3.medium", 
														 1, 
														 1, 
														 KEY_PAIR_NAME, 
														 SECURITY_GROUP_NAME);		
		System.out.println("/n///////// 2. Launch load generator. /////////");

		boolean isRun = false;
		while (isRun == false) {			
			isRun = IsRunning.isRunning(loadGenerator.getInstanceId(), ec2);			
		}		
		String LGDNS = GetInstancePublicDnsName.getInstancePublicDnsName(loadGenerator.getInstanceId() , ec2);
		System.out.println("Public DNS: " + LGDNS);
		String LGurl = new String("http://"
				+ LGDNS
				+ "/password?passwd="
				+ MY_SUBMISSION_PASSWORD);
		
		Thread.sleep(100000);
		URLConnectionReader.connectURL(LGurl);

		
		
		/* 3. Launch the first data center.
		 * 
		 */
		System.out.println("/n///////// 3. Launch the first data center. /////////");
		Instance dataCenter1 = Launching.lauchInstance(ec2,
													   "ami-f4144f9e", 
													   "m3.medium", 
													   1, 
													   1, 	
													   KEY_PAIR_NAME, 
													   SECURITY_GROUP_NAME);		
		Thread.sleep(10000);
		isRun = false;
		while (isRun == false) {			
			isRun = IsRunning.isRunning(dataCenter1.getInstanceId(), ec2);
			Thread.sleep(1000);
		}		
		String DC1DNS = GetInstancePublicDnsName.getInstancePublicDnsName(dataCenter1.getInstanceId() , ec2);
		System.out.println("Public DNS: " + DC1DNS);
		String startURL = new String ("http://"
				+ LGDNS
				+ "/test/horizontal?dns="
				+ DC1DNS);
		
		System.out.println(startURL);
		Thread.sleep(100000);
		
		
		
		/* 4. start the test and get the log.
		 * 
		 */		
		System.out.println("/n///////// 4. start the test and get the log. /////////");
		String logUrlpart = URLConnectionReader.getLog(startURL);
		
		String logUrl = new String("http://" + LGDNS + logUrlpart);
		System.out.println("logUrl = " + logUrl);
		
		///////// Calculate the total rps /////////
		float rps = 0;
		float currentRps = 0;
		String DCDNS = new String(DC1DNS);
		Instance dataCenter = dataCenter1;
		
		while (true) {
			while (currentRps == 0) {				
				currentRps = GetRps.readURL(logUrl, DCDNS);
				Thread.sleep(5000);
			}
			
			
			rps += currentRps;
			System.out.println("Total rps = " + rps);
			
			if (rps > 4000) {
				break;
			}
			
			// Add another data center.
			dataCenter = Launching.lauchInstance(ec2,
					   "ami-f4144f9e", 
					   "m3.medium", 
					   1, 
					   1, 	
					   KEY_PAIR_NAME, 
					   SECURITY_GROUP_NAME);		
			
			Thread.sleep(10000);
			
			isRun = false;
			while (isRun == false) {			
				isRun = IsRunning.isRunning(dataCenter.getInstanceId(), ec2);	
				Thread.sleep(1000);
			}		
			DCDNS = GetInstancePublicDnsName.getInstancePublicDnsName(dataCenter.getInstanceId() , ec2);
			System.out.println("/n///////// Add Instance /////////");
			Adding.addInstance(LGDNS, DCDNS);
			currentRps = 0;
			Thread.sleep(100000);
			
		}
		
		
		
	}
}


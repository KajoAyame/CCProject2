

/* Project 2.1
 * This source code is created by Xinghao Zhou independently.
 * Andrew id: xinghaoz
 * Name: Xinghao Zhou
 * Email: xinghaoz@andrew.cmu.edu
 * 
*/

import java.util.List;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;

public class IsRunning {

//	public static boolean isRunning(String instanceID, AmazonEC2Client ec2) {		
	public static boolean isRunning(String instanceID, AmazonEC2 ec2) {		

		///////// Listing all running instances using the AWS SDK for Java /////////
		List<Reservation> reservations = ec2.describeInstances().getReservations();
		for (Reservation reservation : reservations) {
			for (Instance instance : reservation.getInstances()) {
				if (instance.getInstanceId().equals(instanceID))
					if (instance.getState().getName().equals("running")) {
						System.out.println(instance.getInstanceId() + " is running");
						return true;
					}
			}
		}	
		
	
		return false;
	}

}

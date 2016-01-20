package part2;
/* Project 0
 * This source code is created by Xinghao Zhou independently.
 * Andrew id: xinghaoz
 * Name: Xinghao Zhou
 * Email: xinghaoz@andrew.cmu.edu
 * 
*/

import java.util.List;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;

public class GetInstancePublicDnsName {

	static String getInstancePublicDnsName(String instanceID, AmazonEC2 ec2) {
		
		DescribeInstancesResult describeInstancesRequest = ec2.describeInstances();
		List<Reservation> reservations = describeInstancesRequest.getReservations();
		for (Reservation reservation : reservations) {
			for (Instance instance : reservation.getInstances()) {
				if (instance.getInstanceId().equals(instanceID)) {					
					return instance.getPublicDnsName();
				}
			}
		}		
		return null;
	}
}
	


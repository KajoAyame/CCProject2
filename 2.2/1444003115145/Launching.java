/* Project 2.1
 * This source code is created by Xinghao Zhou independently.
 * Andrew id: xinghaoz
 * Name: Xinghao Zhou
 * Email: xinghaoz@andrew.cmu.edu
 * 
*/

import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.CreateTagsRequest;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.RunInstancesRequest;
import com.amazonaws.services.ec2.model.RunInstancesResult;
import com.amazonaws.services.ec2.model.Tag;

public class Launching {
			
			public static Instance lauchInstance(AmazonEC2Client ec2,
											   String imageID,
											   String instanceType,
											   int minCount,
											   int maxCount,
											   String keyName,
											   String securityGroups) {
			// Create Instance Request
			RunInstancesRequest runInstancesRequest = new RunInstancesRequest();
			
			// Configure Instance Request
			runInstancesRequest.withImageId(imageID)
			.withInstanceType(instanceType)
			.withMinCount(minCount)
			.withMaxCount(maxCount)
			.withKeyName(keyName)
			.withSecurityGroups(securityGroups);
			
			// Launch InstanceId
			RunInstancesResult runInstanceResult = ec2.runInstances(runInstancesRequest);
			
			// Get Instance Id of Instance
			Instance instance = runInstanceResult.getReservation().getInstances().get(0);
			
			
			///////// Add a Tag to the Instance /////////
			CreateTagsRequest createTagsRequest = new CreateTagsRequest();
			createTagsRequest.withResources(instance.getInstanceId())
							 .withTags(new Tag("Project", "2.2"));
			
			ec2.createTags(createTagsRequest);;
			
			// Print InstanceId
			String instanceID = instance.getInstanceId();
			System.out.println("Just launched an Instance with ID: " + instanceID);
			
			return instance;
			}
			
}

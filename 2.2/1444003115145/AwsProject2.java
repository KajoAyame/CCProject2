/* Project 2.2
 * This source code is created by Xinghao Zhou independently.
 * Andrew id: xinghaoz
 * Name: Xinghao Zhou
 * Email: xinghaoz@andrew.cmu.edu
 * 
 * Main method is in this class.
*/

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.amazonaws.services.autoscaling.AmazonAutoScalingClient;
import com.amazonaws.services.autoscaling.model.Alarm;
import com.amazonaws.services.autoscaling.model.CreateAutoScalingGroupRequest;
import com.amazonaws.services.autoscaling.model.CreateLaunchConfigurationRequest;
import com.amazonaws.services.autoscaling.model.DeleteAutoScalingGroupRequest;
import com.amazonaws.services.autoscaling.model.DeleteLaunchConfigurationRequest;
import com.amazonaws.services.autoscaling.model.PutScalingPolicyRequest;
import com.amazonaws.services.autoscaling.model.PutScalingPolicyResult;
import com.amazonaws.services.autoscaling.model.SetDesiredCapacityRequest;
import com.amazonaws.services.autoscaling.model.StepAdjustment;
import com.amazonaws.services.autoscaling.model.Tag;
import com.amazonaws.services.autoscaling.model.UpdateAutoScalingGroupRequest;
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient;
import com.amazonaws.services.cloudwatch.model.ComparisonOperator;
import com.amazonaws.services.cloudwatch.model.Dimension;
import com.amazonaws.services.cloudwatch.model.PutMetricAlarmRequest;
import com.amazonaws.services.cloudwatch.model.StandardUnit;
import com.amazonaws.services.cloudwatch.model.Statistic;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.amazonaws.services.ec2.model.AuthorizeSecurityGroupIngressRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupRequest;
import com.amazonaws.services.ec2.model.CreateSecurityGroupResult;
import com.amazonaws.services.ec2.model.DeleteSecurityGroupRequest;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.IpPermission;
import com.amazonaws.services.elasticloadbalancing.AmazonElasticLoadBalancingClient;
import com.amazonaws.services.elasticloadbalancing.model.ConfigureHealthCheckRequest;
import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerResult;
import com.amazonaws.services.elasticloadbalancing.model.DeleteLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.HealthCheck;
import com.amazonaws.services.elasticloadbalancing.model.Listener;
import com.amazonaws.auth.BasicAWSCredentials;


public class AwsProject2 {
	
	public static final String MY_SUBMISSION_PASSWORD = new String(""); // Submission password has been deleted.
	public static final String LOAD_GENERATOR_AMI = new String("ami-312b5154");
	public static final String LOAD_GENERATOR_INSTANCE_TYPE = new String("m3.medium");
	public static final String DATA_CENTER_AMI = new String("ami-3b2b515e");
	public static final String KEY_PAIR_NAME = new String("Project2");
	public static final String ASG_ELB_SECURITY_GROUP_NAME = new String("AsgElbSecurityGroup");
	public static final String LOAD_GENERATOR_SECURITY_GROUP_NAME = new String("LoadGeneratorSecurityGroup");
	public static final String LOAD_BALANCER_NAME = new String("LoadBlancerProject2");
	public static final String LAUNCH_CONFIGURATION_NAME = new String("Launch Configuration 1");
	public static final String TAG_KEY = new String("Project");
	public static final String TAG_VALUE = new String("2.2");
	
	public static void main(String[] args) throws Exception{
		
		// Get the Account Id and Secret Key
		Properties properties = new Properties();
		properties.load(AwsProject2.class.getResourceAsStream("/AwsCredentials.properties"));
		
		BasicAWSCredentials bawsc = 
				new BasicAWSCredentials(properties.getProperty("accessKey"),
						properties.getProperty("secretKey"));
		
		// Create an Amazon EC2 Client
		AmazonEC2Client ec2 = new AmazonEC2Client(bawsc);
		
		
		
		/* 1. Create two Security Groups.
		 * 
		 */
		// Create the security group of the ASG and ELB.		
		System.out.println("///////// 1. Create two Security Groups. /////////");
		
		CreateSecurityGroupRequest csgr = new CreateSecurityGroupRequest();
		csgr.withGroupName(ASG_ELB_SECURITY_GROUP_NAME)
		.withDescription("Security Group for ASG and ELB");
		
		CreateSecurityGroupResult createSecurityGroupResult = ec2.createSecurityGroup(csgr);
		String groupId= createSecurityGroupResult.getGroupId();
		
		IpPermission ipPermission = new IpPermission();
		
		ipPermission.withIpRanges("0.0.0.0/0")
		.withIpProtocol("tcp")
		.withFromPort(0)
		.withToPort(65535);
		
		AuthorizeSecurityGroupIngressRequest asgir = new AuthorizeSecurityGroupIngressRequest();
		asgir.withGroupName(ASG_ELB_SECURITY_GROUP_NAME)
		.withIpPermissions(ipPermission);		
		
		ec2.authorizeSecurityGroupIngress(asgir);
		
		// Create the security group of the Load Generator.
		CreateSecurityGroupRequest lgCsgr = new CreateSecurityGroupRequest();
		lgCsgr.withGroupName(LOAD_GENERATOR_SECURITY_GROUP_NAME)
		.withDescription("Security Group for Load Generator");
		
		CreateSecurityGroupResult lgCreateSecurityGroupResult = ec2.createSecurityGroup(lgCsgr);
		String lgGroupId = lgCreateSecurityGroupResult.getGroupId();
		
		AuthorizeSecurityGroupIngressRequest lgAsgir = new AuthorizeSecurityGroupIngressRequest();
		lgAsgir.withGroupName(LOAD_GENERATOR_SECURITY_GROUP_NAME)
		.withIpPermissions(ipPermission);		
		
		ec2.authorizeSecurityGroupIngress(lgAsgir);
		
		
		
		/* 2. Create a Load Generator with the security group.
		 * 
		 */		
		System.out.println("///////// 2. Create a Load Generator with the security group. /////////");

		
		Instance loadGenerator = Launching.lauchInstance(ec2,
				 LOAD_GENERATOR_AMI, 
				 LOAD_GENERATOR_INSTANCE_TYPE, 
				 1, 
				 1, 
				 KEY_PAIR_NAME, 
				 LOAD_GENERATOR_SECURITY_GROUP_NAME);	

		boolean isRun = false;
		while (isRun == false) {			
		isRun = IsRunning.isRunning(loadGenerator.getInstanceId(), ec2);			
		}		
		String lgDns = GetInstancePublicDnsName.getInstancePublicDnsName(loadGenerator.getInstanceId() , ec2);			
		
		
		
		/* 3. Create an ELB.
		 * 
		 */		
		System.out.println("///////// 3. Create an ELB. /////////");
		
		Tag elbTag = new Tag();
		elbTag.withKey(TAG_KEY) 
		.withValue(TAG_VALUE);
		
		AmazonElasticLoadBalancingClient elb = 
				new AmazonElasticLoadBalancingClient(bawsc);
		
		CreateLoadBalancerRequest createLoadBalancerRequest =
				new CreateLoadBalancerRequest();
		
		// Create a load balancer and add tag to it.
		createLoadBalancerRequest.withAvailabilityZones("us-east-1b")
		.withLoadBalancerName(LOAD_BALANCER_NAME)
		.withSecurityGroups(groupId);	
		SetElbTag.setElbTag(createLoadBalancerRequest);			
				
		List<Listener> listeners = new ArrayList<Listener>(1);
		listeners.add(new Listener("HTTP", 80, 80));
		createLoadBalancerRequest.setListeners(listeners);
		
		// Configure health check.
		HealthCheck healthCheck = new HealthCheck();
		healthCheck.withHealthyThreshold(2)
		.withUnhealthyThreshold(2)
		.withTimeout(30)
		.withInterval(60)
		.withTarget("HTTP:80/heartbeat?lg=" + lgDns);
		
		ConfigureHealthCheckRequest configureHealthCheckRequest = new ConfigureHealthCheckRequest();
		configureHealthCheckRequest.withHealthCheck(healthCheck)
		.withLoadBalancerName(LOAD_BALANCER_NAME);				
		
		CreateLoadBalancerResult createLoadBalancerResult = elb.createLoadBalancer(createLoadBalancerRequest);
		Thread.sleep(5000);
		elb.configureHealthCheck(configureHealthCheckRequest);
		String elbDns = createLoadBalancerResult.getDNSName();
		System.out.println(elbDns);
		
		
		
		/* 4. Create a Launch Configuration.
		 * 
		 */
		System.out.println("///////// 4. Create a Launch Configuration. /////////");
		
		AmazonAutoScalingClient asClient = new AmazonAutoScalingClient(bawsc);
		
		
		CreateLaunchConfigurationRequest createLaunchConfigurationRequest = 
				new CreateLaunchConfigurationRequest();
		createLaunchConfigurationRequest.withImageId(DATA_CENTER_AMI)
		.withInstanceType("m3.large")
		.withKeyName(KEY_PAIR_NAME)
		.withLaunchConfigurationName(LAUNCH_CONFIGURATION_NAME)
		.withSecurityGroups(groupId)
		.getInstanceMonitoring();
		
		asClient.createLaunchConfiguration(createLaunchConfigurationRequest);
		
		
		
		/* 5. Define good rule for Scale Out and Scale In operations.
		 * 
		 */		
		System.out.println("///////// 5. Define good rule for Scale Out and Scale In operations. /////////");

		Tag asgTag = new Tag();
		asgTag.withKey(TAG_KEY) 
		.withValue(TAG_VALUE)
		.withResourceType("auto-scaling-group")
		.withPropagateAtLaunch(true);		
		
		CreateAutoScalingGroupRequest createAutoScalingGroupRequest =
				new CreateAutoScalingGroupRequest();
		createAutoScalingGroupRequest.withAutoScalingGroupName("autoScalingGroupProject2")
		.withAvailabilityZones("us-east-1b")
		.withDefaultCooldown(60)
		.withDesiredCapacity(0)
		.withHealthCheckGracePeriod(60) 
		.withHealthCheckType("ELB")
		.withLaunchConfigurationName(LAUNCH_CONFIGURATION_NAME)
		.withLoadBalancerNames(LOAD_BALANCER_NAME)
		.withMaxSize(15)  
		.withMinSize(0)
		.withTags(asgTag);
		
		asClient.createAutoScalingGroup(createAutoScalingGroupRequest);
		
		
		
		/* 6. Create Auto Scale Policies.
		 * 
		 */		
		System.out.println("///////// 6. Create Auto Scale Policies. /////////");
		
		Alarm alarms = new Alarm();
		alarms.withAlarmName("alarms");		
		
		StepAdjustment increaseAdjustment = new StepAdjustment();
		increaseAdjustment.withMetricIntervalLowerBound((double) 0)
		.withScalingAdjustment(1);
		
		StepAdjustment decreaseAdjustment = new StepAdjustment();
		decreaseAdjustment.withMetricIntervalUpperBound((double) 0)
		.withScalingAdjustment(-1);
		
		StepAdjustment largelyIncreaseAdjustment = new StepAdjustment();
		largelyIncreaseAdjustment.withMetricIntervalLowerBound((double) 0)
		.withScalingAdjustment(4);		
		
		StepAdjustment largelyDecreaseAdjustment = new StepAdjustment();
		largelyDecreaseAdjustment.withMetricIntervalUpperBound(0d)
		.withScalingAdjustment(-3);		
		
		PutScalingPolicyRequest upScalingPolicyRequest = new PutScalingPolicyRequest(); 
		upScalingPolicyRequest.withAutoScalingGroupName("autoScalingGroupProject2")
		.withPolicyName("Increase Policy")
		.withAdjustmentType("ChangeInCapacity")
		.withPolicyType("StepScaling")
		.withStepAdjustments(increaseAdjustment);
		
		PutScalingPolicyRequest downScalingPolicyRequest = new PutScalingPolicyRequest(); 
		downScalingPolicyRequest.withAutoScalingGroupName("autoScalingGroupProject2")
		.withPolicyName("Decrease Policy")
		.withAdjustmentType("ChangeInCapacity")
		.withPolicyType("StepScaling")
		.withStepAdjustments(decreaseAdjustment);		
		
		PutScalingPolicyRequest largelyUpScalingPolicyRequest = new PutScalingPolicyRequest(); 
		largelyUpScalingPolicyRequest.withAutoScalingGroupName("autoScalingGroupProject2")
		.withPolicyName("Largely Increase Policy")
		.withAdjustmentType("ChangeInCapacity")
		.withPolicyType("StepScaling")
		.withStepAdjustments(largelyIncreaseAdjustment);
		
		PutScalingPolicyRequest largelyDownScalingPolicyRequest = new PutScalingPolicyRequest(); 
		largelyDownScalingPolicyRequest.withAutoScalingGroupName("autoScalingGroupProject2")
		.withPolicyName("Largely Decrease Policy")
		.withAdjustmentType("ChangeInCapacity")
		.withPolicyType("StepScaling")
		.withStepAdjustments(largelyDecreaseAdjustment);	
		
		PutScalingPolicyResult putScalingPolicyResultup   		 = new PutScalingPolicyResult();
		PutScalingPolicyResult putScalingPolicyResultdown 		 = new PutScalingPolicyResult();
		PutScalingPolicyResult putScalingPolicyResultlargelyUp   = new PutScalingPolicyResult();
		PutScalingPolicyResult putScalingPolicyResultlargelyDown = new PutScalingPolicyResult();
		putScalingPolicyResultup   	      = asClient.putScalingPolicy(upScalingPolicyRequest);
		putScalingPolicyResultdown		  = asClient.putScalingPolicy(downScalingPolicyRequest);
		putScalingPolicyResultlargelyUp   = asClient.putScalingPolicy(largelyUpScalingPolicyRequest);
		putScalingPolicyResultlargelyDown = asClient.putScalingPolicy(largelyDownScalingPolicyRequest);
		String upArn   		  = new String(putScalingPolicyResultup.getPolicyARN());
		String downArn 		  = new String(putScalingPolicyResultdown.getPolicyARN());
		String largelyUpArn   = new String(putScalingPolicyResultlargelyUp.getPolicyARN());
		String largelyDownArn = new String(putScalingPolicyResultlargelyDown.getPolicyARN());		
		
		
		
		/* 7. Create CloudWatch Alarms.
		 * 
		 */		
		System.out.println("///////// 7. Create CloudWatch Alarms. /////////");
		
		AmazonCloudWatchClient cwClient = new AmazonCloudWatchClient(bawsc);
		
		List<Dimension> dimensions = new ArrayList<>();
		Dimension dimension = new Dimension();
		dimension.withName("AutoScalingGroupName")
		.withValue("autoScalingGroupProject2");
		dimensions.add(dimension);
		
		List<String> upActions         = new ArrayList<>();
		upActions.add(upArn);		
		List<String>downActions        = new ArrayList<>();
		downActions.add(downArn);		
		List<String>largelyUpActions   = new ArrayList<>();
		largelyUpActions.add(largelyUpArn);		
		List<String>largelyDownActions = new ArrayList<>();
		largelyDownActions.add(largelyDownArn);
				
		PutMetricAlarmRequest upRequest = new PutMetricAlarmRequest();
		upRequest.withAlarmName("upAlarms")
		.withMetricName("CPUUtilization")
		.withComparisonOperator(ComparisonOperator.GreaterThanOrEqualToThreshold)
		.withNamespace("AWS/EC2")
		.withStatistic(Statistic.Average)
		.withUnit(StandardUnit.Percent)
		.withThreshold(75d)
		.withPeriod(60)
		.withEvaluationPeriods(1)
		.withDimensions(dimensions);
				
		PutMetricAlarmRequest downRequest = new PutMetricAlarmRequest();
		downRequest.withAlarmName("downAlarms")
		.withMetricName("CPUUtilization")
		.withComparisonOperator(ComparisonOperator.LessThanOrEqualToThreshold)
		.withNamespace("AWS/EC2")
		.withStatistic(Statistic.Average)
		.withUnit(StandardUnit.Percent)
		.withThreshold(25d)
		.withPeriod(60)
		.withEvaluationPeriods(1)
		.withDimensions(dimensions);
				
		PutMetricAlarmRequest largelyUpRequest = new PutMetricAlarmRequest();
		largelyUpRequest.withAlarmName("largelyUpAlarms")
		.withMetricName("CPUUtilization")
		.withComparisonOperator(ComparisonOperator.GreaterThanOrEqualToThreshold)
		.withNamespace("AWS/EC2")
		.withStatistic(Statistic.Average)
		.withUnit(StandardUnit.Percent)
		.withThreshold(85d)
		.withPeriod(60)
		.withEvaluationPeriods(1)
		.withDimensions(dimensions);
		
		PutMetricAlarmRequest largelyDownRequest = new PutMetricAlarmRequest();
		largelyDownRequest.withAlarmName("largelyDownAlarms")
		.withMetricName("CPUUtilization")
		.withComparisonOperator(ComparisonOperator.LessThanOrEqualToThreshold)
		.withNamespace("AWS/EC2")
		.withStatistic(Statistic.Average)
		.withUnit(StandardUnit.Percent)
		.withThreshold(25d)
		.withPeriod(60)
		.withEvaluationPeriods(1)
		.withDimensions(dimensions);
		
		
		
		/* 8. Link the CloudWatch Alarms to the ScaleOut and ScaleIn rules of the Auto Scaling Group.
		 * 
		 */		
		System.out.println("///////// 8. Link the CloudWatch Alarms to the ScaleOut and ScaleIn rules of the Auto Scaling Group. /////////");
		
		upRequest.setAlarmActions(upActions);
		downRequest.setAlarmActions(downActions);	
		largelyUpRequest.setAlarmActions(largelyUpActions);
		largelyDownRequest.setAlarmActions(largelyDownActions);
		cwClient.putMetricAlarm(upRequest);
		cwClient.putMetricAlarm(downRequest);
		// Later, I realize that it was a bad design to have 4 alarms, 
		// so I didn't use the last two alarms in the later test.
	//	cwClient.putMetricAlarm(largelyUpRequest);		
	//	cwClient.putMetricAlarm(largelyDownRequest);

		
		
		/* 9. Configure correct tags for the Auto Scaling Group.
		 * 
		 */		
		System.out.println("///////// 9. Configure correct tags for the Auto Scaling Group. /////////");
		// Already done in the process of creating the Auto Scaling Group.
		
		
		
		/* 10. Submit submission password.
		 * 
		 */		
		System.out.println("///////// 10. Submit submission password. /////////");
		
		System.out.println("Public DNS: " + lgDns);
		String lGurl = new String("http://"
								  + lgDns
								  + "/password?passwd="
								  + MY_SUBMISSION_PASSWORD);
		System.out.println(lGurl);

		Thread.sleep(60000);
		
		boolean isSubmit = false;
		while (isSubmit == false) {
			Thread.sleep(2500);
			try {
				URLConnectionReader.connectURL(lGurl);
				isSubmit = true;
			} catch (IOException E) {
				
			}
		}
		
		
		
		/* 11. Warm up the Load Balancer.
		 * 
		 */		
		System.out.println("///////// 11. Warm up the Load Balancer. /////////");

		UpdateAutoScalingGroupRequest update1 = new UpdateAutoScalingGroupRequest();
		update1.withAutoScalingGroupName("autoScalingGroupProject2")
		.withDefaultCooldown(0)
		.withMinSize(5)
		.withMaxSize(5)
		.withDesiredCapacity(5);		
		asClient.updateAutoScalingGroup(update1);
		
		SetDesiredCapacityRequest setDesiredCapacityRequest = new SetDesiredCapacityRequest();
		setDesiredCapacityRequest.withAutoScalingGroupName("autoScalingGroupProject2")
		.withDesiredCapacity(5);
		asClient.setDesiredCapacity(setDesiredCapacityRequest);		
	
		String warmUpUrl = new String("http://"
				  + lgDns
				  + "/warmup?dns="
				  + elbDns);
		
		System.out.println("Begining warm up.");
		boolean isConnect = false;
		
		for (int i = 0; i < 18; i++) {	// warm up 18 times: 18 * 5 min = 90(min) = 1.5(hour)
			isConnect = false;
			while (isConnect == false) {
				Thread.sleep(2500);
				try {
					URLConnectionReader.connectURL(warmUpUrl);
					isConnect = true;
				} catch (IOException e) {
					
				}
			}
	
			System.out.println("ELB warm up " + (i + 1) + " time(s).");
			Thread.sleep(300000);
		}		
		
		
		
		/* 12. After the ELB warmed up, start the test.
		 * 
		 */
		System.out.println("///////// 12. After the ELB warmed up, start the test. /////////");
		
		UpdateAutoScalingGroupRequest update2 = new UpdateAutoScalingGroupRequest();
		update2.withAutoScalingGroupName("autoScalingGroupProject2")
		.withDefaultCooldown(0)
		.withMinSize(2)
		.withMaxSize(2)
		.withDesiredCapacity(2);		
		asClient.updateAutoScalingGroup(update2);
		
		String startUrl = new String("http://"
									 + lgDns
									 + "/junior?dns="
									 + elbDns);
		boolean isStart = false;
		while (isStart == false) {
			Thread.sleep(2500);
			try {
				URLConnectionReader.connectURL(startUrl);
				isStart = true;
				System.out.println("Test starts!!");
			} catch (IOException e){
				
			}
		}
		UpdateAutoScalingGroupRequest update3 = new UpdateAutoScalingGroupRequest();
		update3.withAutoScalingGroupName("autoScalingGroupProject2")
		.withDefaultCooldown(60)
		.withMinSize(3)
		.withMaxSize(9)
		.withDesiredCapacity(3);		
		asClient.updateAutoScalingGroup(update3);
		
		Thread.sleep(50*60000);
		
		
		
		/* 13. Terminate all instance except Load Generator
		 * 
		 */		
		UpdateAutoScalingGroupRequest update4 = new UpdateAutoScalingGroupRequest();
		update4.withAutoScalingGroupName("autoScalingGroupProject2")
		.withDefaultCooldown(0)
		.withMinSize(0)
		.withMaxSize(0)
		.withDesiredCapacity(0);		
		asClient.updateAutoScalingGroup(update4);
	
		Thread.sleep(60000);
		
		
		
		/* 14. Delete Auto Scaling Group, Launch Configuration, Elastic Load Balancer and Security groups.
		 * 
		 */		
		DeleteAutoScalingGroupRequest deleteAutoScalingGroupRequest = new DeleteAutoScalingGroupRequest();		
		deleteAutoScalingGroupRequest.withAutoScalingGroupName("autoScalingGroupProject2");
		
		DeleteLaunchConfigurationRequest deleteLaunchConfigurationRequest = new DeleteLaunchConfigurationRequest();
		deleteLaunchConfigurationRequest.withLaunchConfigurationName(LAUNCH_CONFIGURATION_NAME)	;
		
		asClient.deleteAutoScalingGroup(deleteAutoScalingGroupRequest);
		asClient.deleteLaunchConfiguration(deleteLaunchConfigurationRequest);
		
		DeleteLoadBalancerRequest deleteLoadBalancerRequest = new DeleteLoadBalancerRequest();
		deleteLoadBalancerRequest.withLoadBalancerName(LOAD_BALANCER_NAME);
		elb.deleteLoadBalancer(deleteLoadBalancerRequest);
		
		DeleteSecurityGroupRequest deleteSecurityGroupRequest = new DeleteSecurityGroupRequest();
		deleteSecurityGroupRequest.withGroupId(groupId);
		DeleteSecurityGroupRequest deleteSecurityGroupRequest2 = new DeleteSecurityGroupRequest();
		deleteSecurityGroupRequest2.withGroupId(lgGroupId);
		
		ec2.deleteSecurityGroup(deleteSecurityGroupRequest);
		ec2.deleteSecurityGroup(deleteSecurityGroupRequest2);

		
	}
}


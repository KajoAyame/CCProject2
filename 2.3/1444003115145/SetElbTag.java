import com.amazonaws.services.elasticloadbalancing.model.CreateLoadBalancerRequest;
import com.amazonaws.services.elasticloadbalancing.model.Tag;

public class SetElbTag {

	public static void setElbTag(CreateLoadBalancerRequest createLoadBalancerRequest) {
		Tag elbTag = new Tag();
		elbTag.withKey("Project")
		.withValue("2.2"); 
		
		createLoadBalancerRequest.withTags(elbTag);
	}
}

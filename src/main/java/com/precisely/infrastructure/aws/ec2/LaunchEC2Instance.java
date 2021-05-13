package com.precisely.infrastructure.aws.ec2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.Ec2ClientBuilder;
import software.amazon.awssdk.services.ec2.model.*;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.ListInstanceProfilesForRoleRequest;
import software.amazon.awssdk.services.iam.model.ListInstanceProfilesForRoleResponse;

import java.util.ArrayList;
import java.util.List;

public final class LaunchEC2Instance {

    private static final Logger logger = LoggerFactory.getLogger(LaunchEC2Instance.class);

    private final Ec2ClientBuilder ec2Client;

    public LaunchEC2Instance() {
        ec2Client = Ec2Client.builder();
    }

    public static void main(String[] args) {
        LaunchEC2Instance launchEC2Instance = new LaunchEC2Instance();
        launchEC2Instance.createEC2Instance("eksctl-routing-k8s-nodegroup-grp-NodeInstanceRole-1FM2MKRE6QA9D", new ArrayList<>());
    }

    public void createEC2Instance(String role, List<String> securityGrpIds) {

        String instanceProfile = fetchInstanceProfileForRole(role);
        logger.info("Instance Profile : " + instanceProfile);

        RunInstancesRequest.Builder runInstancesRequest = RunInstancesRequest.builder();
        IamInstanceProfileSpecification.Builder iamInstanceProfileSpecification = IamInstanceProfileSpecification.builder();
        iamInstanceProfileSpecification.name(instanceProfile);

        runInstancesRequest.
                imageId("ami-06f6f33114d2db0b1").
                instanceType(InstanceType.T2_XLARGE).
                iamInstanceProfile(iamInstanceProfileSpecification.build()).
                keyName("key-pair-1-north-virginia").
                minCount(1).
                maxCount(1).
                securityGroupIds(securityGrpIds);

        RunInstancesResponse runInstancesResponse = ec2Client.build().runInstances(runInstancesRequest.build());

        String instanceId = runInstancesResponse.instances().get(0).instanceId();
        logger.info("Instance created. Instance Id: " + instanceId);

        Tag tag = Tag.builder()
                .key("Name")
                .value("jmeter")
                .build();

        CreateTagsRequest tagRequest = CreateTagsRequest.builder()
                .resources(instanceId)
                .tags(tag)
                .build();

        ec2Client.build().createTags(tagRequest);
        logger.info("Successfully started EC2 instance %s", instanceId);
    }

    public String fetchInstanceProfileForRole(String roleName) {
        ListInstanceProfilesForRoleRequest.Builder profilesForRoleRequest = ListInstanceProfilesForRoleRequest.builder();
        profilesForRoleRequest.roleName(roleName);

        IamClient iamClient = IamClient.builder().region(Region.AWS_GLOBAL).build();
        ListInstanceProfilesForRoleResponse profileResponse = iamClient.listInstanceProfilesForRole(profilesForRoleRequest.build());

        if (!profileResponse.hasInstanceProfiles()) {
            throw new IllegalStateException("Can't find instance profile for role : " + roleName);
        }

        return profileResponse.instanceProfiles().get(0).instanceProfileName();
    }
}

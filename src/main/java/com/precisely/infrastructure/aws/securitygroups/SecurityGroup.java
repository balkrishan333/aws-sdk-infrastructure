package com.precisely.infrastructure.aws.securitygroups;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

import java.util.Collections;

public final class SecurityGroup {

    private static final Logger logger = LoggerFactory.getLogger(SecurityGroup.class);
    private final Ec2Client ec2Client;

    public SecurityGroup() {
        ec2Client = Ec2Client.builder().region(Region.US_EAST_1).build();
    }

    public static void main(String[] args) {
        SecurityGroup securityGroup = new SecurityGroup();
        //software.amazon.awssdk.services.ec2.model.SecurityGroup sg = securityGroup.fetchDefaultSG("vpc-0f677d49f97cbd51e");
        securityGroup.addIngressSourceSG("tcp", 2049, 2049, "sg-0820252eb287d9d46", "SSH access");

        logger.info("Ingress request complete.");
    }

    public DescribeSecurityGroupsResponse fetchSGByName(String sgName) {
        logger.debug("Fetching Security group with name : " + sgName);

        DescribeSecurityGroupsRequest.Builder describeSecurityGroupsRequest = DescribeSecurityGroupsRequest.builder();
        logger.debug("Creating group-name filter...");
        Filter.Builder filter = Filter.builder();
        filter.name("group-name");
        filter.values(sgName);

        describeSecurityGroupsRequest.filters(filter.build());
        logger.debug("sending request...");
        DescribeSecurityGroupsResponse securityGroupsResponse = ec2Client.describeSecurityGroups(describeSecurityGroupsRequest.build());

        if (securityGroupsResponse.hasSecurityGroups()) {
            logger.info("Found security group. Id : " + securityGroupsResponse.securityGroups().get(0).groupId());
        } else {
            logger.error("Security group not found...");
        }

        return securityGroupsResponse;
    }

    public DescribeSecurityGroupsResponse fetchSG(String vpcId) {
        logger.debug("Fetching Security group with VPC ID : " + vpcId);

        DescribeSecurityGroupsRequest.Builder describeSecurityGroupsRequest = DescribeSecurityGroupsRequest.builder();
        logger.debug("Creating vpc-id filter...");
        Filter.Builder filter = Filter.builder();
        filter.name("vpc-id");
        filter.values(vpcId);

        describeSecurityGroupsRequest.filters(filter.build());
        logger.debug("sending request...");
        DescribeSecurityGroupsResponse securityGroupsResponse = ec2Client.describeSecurityGroups(describeSecurityGroupsRequest.build());

        if (securityGroupsResponse.hasSecurityGroups()) {
            logger.info("Found security groups for VPC ID : " + vpcId);
        } else {
            logger.error("Security groups not found...");
        }

        return securityGroupsResponse;
    }

    public software.amazon.awssdk.services.ec2.model.SecurityGroup fetchDefaultSG(String vpcId) {
        logger.debug("Fetching Security group with VPC ID : " + vpcId);

        DescribeSecurityGroupsRequest.Builder describeSecurityGroupsRequest = DescribeSecurityGroupsRequest.builder();
        logger.debug("Creating vpc-id filter...");
        Filter.Builder filter = Filter.builder();
        filter.name("vpc-id");
        filter.values(vpcId);

        describeSecurityGroupsRequest.filters(filter.build());
        logger.debug("sending request...");
        DescribeSecurityGroupsResponse securityGroupsResponse = ec2Client.describeSecurityGroups(describeSecurityGroupsRequest.build());

        software.amazon.awssdk.services.ec2.model.SecurityGroup securityGroup = null;
        for(software.amazon.awssdk.services.ec2.model.SecurityGroup sg : securityGroupsResponse.securityGroups()) {
            if (sg.groupName().equalsIgnoreCase("default")) {
                logger.info("Found default security group. Id : " + sg.groupId());
                securityGroup = sg;
                break;
            }
        }
        return securityGroup;
    }

    public AuthorizeSecurityGroupIngressResponse addIngressSourceCIDR(String protocol, int fromPort, int toPort,
                                                                      String sourceCIDR, String securityGroupId) {

        logger.debug("Adding ingress rule to security group Id : " + securityGroupId);

        IpRange.Builder ipRange = IpRange.builder();
        logger.debug("CIDR range : " + sourceCIDR);
        ipRange.cidrIp(sourceCIDR);

        IpPermission.Builder ipPermission = IpPermission.builder();
        logger.debug("Protocol : " + protocol);
        logger.debug("From port : " + fromPort);
        logger.debug("To port : " + toPort);

        ipPermission.ipProtocol(protocol).fromPort(fromPort).toPort(toPort).ipRanges(ipRange.build());

        AuthorizeSecurityGroupIngressRequest.Builder sgIngressRequest = AuthorizeSecurityGroupIngressRequest.builder();
        sgIngressRequest.groupId(securityGroupId).ipPermissions(ipPermission.build());

        logger.debug("Sending request to update security group ingress...");
        AuthorizeSecurityGroupIngressResponse sgIngressResponse = ec2Client.authorizeSecurityGroupIngress(sgIngressRequest.build());

        logger.info("Security group ingress updated.");

        return sgIngressResponse;
    }

    public AuthorizeSecurityGroupIngressResponse addIngressSourceSG(String protocol, int fromPort, int toPort,
                                                                    String securityGroupId, String sourceSG) {

        System.out.println("Adding ingress rule to security group Id : " + securityGroupId);

        System.out.println("Source SG : " + sourceSG);
        System.out.println("Destination SG : " + securityGroupId);

        UserIdGroupPair.Builder userIdGroupPair = UserIdGroupPair.builder();
        userIdGroupPair.groupId(sourceSG);

        IpPermission.Builder ipPermission = IpPermission.builder();
        ipPermission.fromPort(fromPort);
        ipPermission.toPort(toPort);
        ipPermission.ipProtocol(protocol);
        ipPermission.userIdGroupPairs(Collections.singleton(userIdGroupPair.build()));


        AuthorizeSecurityGroupIngressRequest.Builder sgIngressRequest = AuthorizeSecurityGroupIngressRequest.builder();
        sgIngressRequest.groupId(securityGroupId).ipPermissions(ipPermission.build());

        System.out.println("Sending request to update security group ingress...");
        AuthorizeSecurityGroupIngressResponse sgIngressResponse = ec2Client.authorizeSecurityGroupIngress(sgIngressRequest.build());

        System.out.println("Security group ingress updated.");

        return sgIngressResponse;
    }
}

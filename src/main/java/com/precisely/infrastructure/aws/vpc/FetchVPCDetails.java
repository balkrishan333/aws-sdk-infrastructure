package com.precisely.infrastructure.aws.vpc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.ec2.Ec2Client;
import software.amazon.awssdk.services.ec2.model.*;

public final class FetchVPCDetails {

    private static final Logger logger = LoggerFactory.getLogger(FetchVPCDetails.class);
    private final Ec2Client ec2Client;

    public FetchVPCDetails() {
        ec2Client = Ec2Client.create();
    }

    public DescribeVpcsResponse fetchVPCDetails(String name) {

        logger.debug("Fetching VPC details for: " + name);
        DescribeVpcsRequest.Builder describeVpcsRequest = DescribeVpcsRequest.builder();
        Filter.Builder vpcNameFilter = Filter.builder();
        vpcNameFilter.name("tag:Name");
        vpcNameFilter.values(name);

        describeVpcsRequest.filters(vpcNameFilter.build());

        logger.debug("Sending DescribeVPC Request... ");
        DescribeVpcsResponse describeVpcsResponse = ec2Client.describeVpcs(describeVpcsRequest.build());

        if (describeVpcsResponse.hasVpcs()) {
            String vpcId = describeVpcsResponse.vpcs().get(0).vpcId();
            logger.info("Found VPC. Id : " + vpcId);
        }
        logger.error("VPC not found...");
        return describeVpcsResponse;
    }

    public DescribeSubnetsResponse fetchSubnetDetails(String vpcId) {
        logger.debug("Fetching subnets in VPC ID :" + vpcId);

        DescribeSubnetsRequest.Builder describeSubnetsRequest = DescribeSubnetsRequest.builder();
        logger.debug("Creating filters...");
        Filter.Builder subnetVPCFilter = Filter.builder();
        subnetVPCFilter.name("vpc-id");
        subnetVPCFilter.values(vpcId);
        describeSubnetsRequest.filters(subnetVPCFilter.build());

        logger.debug("Sending request..");
        DescribeSubnetsResponse describeSubnetsResponse = ec2Client.describeSubnets(describeSubnetsRequest.build());
        if (describeSubnetsResponse.hasSubnets()) {
            logger.info("Found "+ describeSubnetsResponse.subnets().size() + " subnets.");
        } else {
            logger.error("No subnets found.");
        }

       return describeSubnetsResponse;
    }
}

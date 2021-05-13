package com.precisely.infrastructure.aws;

import com.precisely.infrastructure.aws.cloudformation.FetchStackOutput;
import com.precisely.infrastructure.aws.securitygroups.SecurityGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.cloudformation.model.DescribeStacksResponse;
import software.amazon.awssdk.services.cloudformation.model.Output;
import software.amazon.awssdk.services.cloudformation.model.Stack;
import software.amazon.awssdk.services.ec2.model.DescribeSecurityGroupsResponse;

import java.util.Optional;

public final class EKSClusterDetails {

    private static final Logger logger = LoggerFactory.getLogger(EKSClusterDetails.class);

    private final String clusterName;
    private final String vpcId;
    private final String clusterSecurityGrpId;

    public EKSClusterDetails(String clusterName) {
        this.clusterName = clusterName;
        this.vpcId = fetchVPCId();
        this.clusterSecurityGrpId = fetchSecurityGrpId();
    }

    public static void main(String[] args) {
        EKSClusterDetails clusterDetails = new EKSClusterDetails(Constants.CLUSTER_STACK_NAME);
    }

    public String getVPCId() {
        return vpcId;
    }

    public String getClusterSecurityGrpId() {
        return clusterSecurityGrpId;
    }

    private String fetchVPCId() {
        FetchStackOutput stackOutput = new FetchStackOutput(clusterName);
        DescribeStacksResponse describeStacksResponse = stackOutput.fetchStackOutput();
        Stack stack = describeStacksResponse.stacks().get(0);
        Optional<Output> vpc = stack.outputs().stream().filter(output -> output.outputKey().equalsIgnoreCase("VPC")).findAny();

        if (vpc.isPresent()) {
            logger.info("Found VPC. Id : " + vpc.get().outputKey());
            return vpc.get().outputValue();
        }

        logger.error("No VPC found.");
        throw new IllegalStateException("No VPC found for EFS.");
    }

    private String fetchSecurityGrpId() {
        String securityGrpId = null;
        DescribeSecurityGroupsResponse groupsResponse = new SecurityGroup().fetchSG(this.vpcId);

        if (groupsResponse.hasSecurityGroups()) {
            for (software.amazon.awssdk.services.ec2.model.SecurityGroup sg : groupsResponse.securityGroups()) {
                if (sg.groupName().contains(Constants.SECURITY_GROUP_NAME)) {
                    securityGrpId = sg.groupId();
                    break;
                }
            }
        }
        return securityGrpId;
    }
}

package com.precisely.infrastructure.aws.efs;

import com.precisely.infrastructure.aws.Constants;
import com.precisely.infrastructure.aws.EKSClusterDetails;
import com.precisely.infrastructure.aws.EKSClusterFactory;
import com.precisely.infrastructure.aws.securitygroups.SecurityGroup;
import com.precisely.infrastructure.aws.vpc.FetchVPCDetails;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.ec2.model.DescribeSecurityGroupsResponse;
import software.amazon.awssdk.services.ec2.model.DescribeSubnetsResponse;
import software.amazon.awssdk.services.ec2.model.Subnet;
import software.amazon.awssdk.services.efs.EfsClient;
import software.amazon.awssdk.services.efs.model.*;

import java.util.Objects;

public final class EFSVolume {

    private static final Logger logger = LoggerFactory.getLogger(EFSVolume.class);
    private static final int WAIT_TIME = 10000;

    private final EfsClient efsClient;
    private final String efsName;
    private final String vpcId;
    private final EKSClusterDetails clusterDetails;

    public EFSVolume(String efsName) {
        clusterDetails = EKSClusterFactory.getClusterDetails(Constants.CLUSTER_STACK_NAME);
        efsClient = EfsClient.create();

        Objects.requireNonNull(efsName, "Empty EFS Volume name");
        this.efsName = efsName;
        this.vpcId = clusterDetails.getVPCId();
    }

    public CreateFileSystemResponse createEFS() {

        logger.debug("Creating EFS Volume : " + this.efsName);
        Tag tag = Tag.builder().key("Name").value(this.efsName).build();

        CreateFileSystemRequest fileSystemRequest = CreateFileSystemRequest.builder().
                            performanceMode(PerformanceMode.GENERAL_PURPOSE).
                            tags(tag).build();

        logger.debug("Sending request...");
        CreateFileSystemResponse fileSystemResponse = efsClient.createFileSystem(fileSystemRequest);
        String fileSystemId = fileSystemResponse.fileSystemId();
        logger.info("File system created. fileSystemId : " + fileSystemId);

        logger.debug("lifecycle state = " + fileSystemResponse.lifeCycleState());

        try {
            logger.debug("Waiting for " + WAIT_TIME +" milliseconds for EFS to be available...");
            Thread.sleep(WAIT_TIME);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        DescribeSubnetsResponse subnetResponse = fetchVPCDetails();
        SecurityGroup securityGrp = new SecurityGroup();

        if (subnetResponse.hasSubnets()) {
            logger.info("Creating mount points...");

            String securityGrpId = new SecurityGroup().fetchDefaultSG(this.vpcId).groupId();

            logger.debug("Using security group id : " + securityGrpId);

            for (Subnet subnet : subnetResponse.subnets()) {

                boolean publicIPOnLaunch = subnet.mapPublicIpOnLaunch();
                logger.debug("Subnet id : " + subnet.subnetId() +" , Public IP on Launch : " + publicIPOnLaunch);

                if (publicIPOnLaunch) {
                    logger.debug("Creating mount point for subnet id : " + subnet.subnetId());
                    CreateMountTargetRequest.Builder mountTargetBuilder = CreateMountTargetRequest.builder();
                    mountTargetBuilder.fileSystemId(fileSystemId).subnetId(subnet.subnetId()).securityGroups(securityGrpId);

                    logger.debug("sending request for subnet : " + subnet.subnetId());
                    CreateMountTargetResponse mountTarget = efsClient.createMountTarget(mountTargetBuilder.build());
                    logger.debug("Mount target id : " + mountTarget.mountTargetId());
                }
            }

            String srcSecurityGrp = null;
            DescribeSecurityGroupsResponse groupsResponse = new SecurityGroup().fetchSG(this.vpcId);

            if (groupsResponse.hasSecurityGroups()) {
                for (software.amazon.awssdk.services.ec2.model.SecurityGroup sg : groupsResponse.securityGroups()) {
                    if (sg.groupName().contains(Constants.SECURITY_GROUP_NAME)) {
                        srcSecurityGrp = sg.groupId();
                        break;
                    }
                }
            } else {
                throw new IllegalStateException("Security group not found while configuring EFS");
            }
            securityGrp.addIngressSourceSG("tcp", 2049 ,2049, securityGrpId, srcSecurityGrp);
        }
        return fileSystemResponse;
    }

    private DescribeSubnetsResponse fetchVPCDetails() {
        FetchVPCDetails vpc = new FetchVPCDetails();
        return vpc.fetchSubnetDetails(this.vpcId);
    }
}

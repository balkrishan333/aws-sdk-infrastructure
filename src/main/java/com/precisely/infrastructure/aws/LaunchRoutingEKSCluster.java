package com.precisely.infrastructure.aws;

import com.precisely.infrastructure.aws.efs.EFSVolume;
import com.precisely.infrastructure.aws.iam.AttachRolePolicy;
import com.precisely.infrastructure.aws.securitygroups.SecurityGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.efs.model.CreateFileSystemResponse;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static com.precisely.infrastructure.aws.Constants.*;

public final class LaunchRoutingEKSCluster {

    private static final Logger logger = LoggerFactory.getLogger(LaunchRoutingEKSCluster.class);

    public static void main(String[] args) {

        logger.info("Running LaunchRoutingEKSCluster to setup Routing k8s cluster...");

        LaunchRoutingEKSCluster eksCluster = new LaunchRoutingEKSCluster();

        //fetch k8s cluster details.
        EKSClusterDetails clusterDetails = new EKSClusterDetails(CLUSTER_STACK_NAME);
        EKSClusterFactory.addClusterDetails(CLUSTER_STACK_NAME, clusterDetails);

        //add security group ingress to cluster security group
        addIngressSecurityGroup(clusterDetails.getClusterSecurityGrpId());

        //create EFS for Routing Data
        CreateFileSystemResponse efsResponse = eksCluster.createEFSVolume();
        String efsId = efsResponse.fileSystemId();

        //update yaml config with new EFS id
        try {
            logger.info("Updating k8s configuration to use new efs volume...");
            updateK8sConfig(efsId);
        }catch (Exception e) {
            e.printStackTrace();
        }

        //attach policies to eks cluster role
        AttachRolePolicy attachRolePolicy = new AttachRolePolicy();
        List<String> policies = new ArrayList<>();
        policies.add("arn:aws:iam::aws:policy/AmazonS3ReadOnlyAccess");
        policies.add("arn:aws:iam::aws:policy/AmazonEC2ReadOnlyAccess");

        String roleName = attachRolePolicy.attachIAMRolePolicy("NodeInstanceRole", policies);

        //launch ec2 instance for client
       // LaunchEC2Instance launchEC2Instance = new LaunchEC2Instance();
      //  launchEC2Instance.createEC2Instance(roleName, Arrays.asList(clusterDetails.getClusterSecurityGrpId()));
    }

    private CreateFileSystemResponse createEFSVolume() {
        EFSVolume efsVolume = new EFSVolume(EFS_NAME);
        return efsVolume.createEFS();
    }

    private static void updateK8sConfig(String efsId) throws Exception {

        logger.debug("Reading config from : " + DATA_PREP_POD_FILE_PATH);
        logger.debug("Writing config to : " + DATA_PREP_POD_NEW_FILE);

        Reader reader = new FileReader(DATA_PREP_POD_FILE_PATH);
        try(BufferedReader bufferedReader = new BufferedReader(reader)) {
            logger.debug("Reading from file...");
            String line = bufferedReader.readLine();
            logger.debug(line);

            Writer writer = new FileWriter(DATA_PREP_POD_NEW_FILE);
            try(BufferedWriter bufferedWriter = new BufferedWriter(writer)) {
                while (line  != null) {
                    if (line.contains(EFS_VOLUME_ID_PLACEHOLDER)) {
                        logger.debug("Placeholder found. Replacing with efs-id : " + efsId);
                        line = line.replaceAll(EFS_VOLUME_ID_PLACEHOLDER, efsId);
                    }
                    bufferedWriter.write(line);
                    bufferedWriter.newLine();
                    line = bufferedReader.readLine();
                }
            }
        }
    }

    private static void addIngressSecurityGroup(String clusterSecurityGrpId) {
        SecurityGroup securityGroup = new SecurityGroup();
        String myIP = GetMyIP.getMyIPCIDR();

        securityGroup.addIngressSourceCIDR("tcp", 3389, 3389, myIP, clusterSecurityGrpId);
        securityGroup.addIngressSourceCIDR("tcp", 30000, 30000, myIP, clusterSecurityGrpId);
        securityGroup.addIngressSourceCIDR("tcp", 32000, 32000, myIP, clusterSecurityGrpId);
    }
}

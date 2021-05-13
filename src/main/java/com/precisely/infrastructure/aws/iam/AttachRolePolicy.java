package com.precisely.infrastructure.aws.iam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.model.*;

import java.util.ArrayList;
import java.util.List;

public class AttachRolePolicy {

    private static final Logger logger = LoggerFactory.getLogger(AttachRolePolicy.class);

    private IamClient iam;

    public AttachRolePolicy() {
        Region region = Region.AWS_GLOBAL;
        iam = IamClient.builder()
                .region(region)
                .build();
    }

    public static void main(String[] args) {
        AttachRolePolicy attachRolePolicy = new AttachRolePolicy();
        List<String> policies = new ArrayList<>();
        policies.add("arn:aws:iam::aws:policy/AmazonS3ReadOnlyAccess");
        policies.add("arn:aws:iam::aws:policy/AmazonEC2ReadOnlyAccess");

        attachRolePolicy.attachIAMRolePolicy("NodeInstanceRole", policies);
    }

    public String attachIAMRolePolicy(String roleNameContains, List<String> policyArns) {

        logger.info("Attaching policies to role...");
        logger.debug("Role name contains : " + roleNameContains);
        logger.debug("Attaching Policies: " + policyArns);

        String roleName = resolveRoleName(roleNameContains);
        logger.info("Role name : " + roleName);

        try {
            for (String policyArn : policyArns) {
                AttachRolePolicyRequest attachRequest =
                        AttachRolePolicyRequest.builder()
                                .roleName(roleName)
                                .policyArn(policyArn).build();

                iam.attachRolePolicy(attachRequest);

                logger.info("Successfully attached policy " + policyArn +
                        " to role " + roleName);
            }
        } catch (IamException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return roleName;
    }

    private String resolveRoleName(String roleNameContains) {

        ListRolesRequest.Builder listRolesRequest = ListRolesRequest.builder();
        ListRolesResponse listRolesResponse = this.iam.listRoles(listRolesRequest.build());
        for (Role role : listRolesResponse.roles()) {
            if (role.roleName().contains(roleNameContains)) {
                logger.debug("Role found. role.arn() = " + role.arn());
                return role.roleName();
            }
        }
        logger.error("Role Not Found...");
        return null;
       /* Optional<Role> role1 = listRolesResponse.roles().stream().findFirst().filter(role -> role.roleName().contains(roleNameContains));
        return role1.get().roleName();*/
    }
}
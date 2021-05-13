package com.precisely.infrastructure.aws.iam;

import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.iam.IamClient;
import software.amazon.awssdk.services.iam.IamClientBuilder;
import software.amazon.awssdk.services.iam.model.*;

public final class FetchRoleDetails {

    public static void main(String[] args) {
        GetRoleRequest.Builder getRoleRequest = GetRoleRequest.builder();
        getRoleRequest.roleName("routing-container-s3");

        IamClientBuilder iamClient = IamClient.builder();
        iamClient.region(Region.AWS_GLOBAL);
        GetRoleResponse getRoleResponse = iamClient.build().getRole(getRoleRequest.build());

        CreatePolicyRequest.Builder createPolicyRequest = CreatePolicyRequest.builder();
        //createPolicyRequest.policyName()

        System.out.println("getRoleResponse = " + getRoleResponse);

        ListRolesRequest.Builder listRolesRequest = ListRolesRequest.builder();
        ListRolesResponse listRolesResponse = iamClient.build().listRoles(listRolesRequest.build());
        listRolesResponse.roles().forEach(role -> {
            System.out.println("role.roleName() = " + role.roleName());
        });
    }
}

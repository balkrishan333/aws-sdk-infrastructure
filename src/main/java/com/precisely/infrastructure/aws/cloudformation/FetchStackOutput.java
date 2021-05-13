package com.precisely.infrastructure.aws.cloudformation;

import com.precisely.infrastructure.aws.Constants;
import software.amazon.awssdk.services.cloudformation.CloudFormationClient;
import software.amazon.awssdk.services.cloudformation.model.DescribeStacksRequest;
import software.amazon.awssdk.services.cloudformation.model.DescribeStacksResponse;

public final class FetchStackOutput {

    private final String stackName;

    public FetchStackOutput(String stackName) {
        this.stackName = stackName;
    }

    public static void main(String[] args) {

        FetchStackOutput stackOutput = new FetchStackOutput(Constants.CLUSTER_STACK_NAME);
        DescribeStacksResponse describeStacksResponse = stackOutput.fetchStackOutput();

        System.out.println(describeStacksResponse.stacks().get(0).outputs().get(0).outputKey());
        System.out.println(describeStacksResponse.stacks().get(0).outputs().get(0).outputValue());
    }

    public DescribeStacksResponse fetchStackOutput() {
        CloudFormationClient cloudFormationClient = CloudFormationClient.create();
        DescribeStacksRequest.Builder describeStacksRequest = DescribeStacksRequest.builder();
        describeStacksRequest.stackName(this.stackName);

        return cloudFormationClient.describeStacks(describeStacksRequest.build());
    }
}

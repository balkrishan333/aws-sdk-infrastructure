package com.precisely.infrastructure.aws;

public final class Constants {

    private Constants(){}

    //eksctl output variables name
    public static final String EFS_NAME = "routing-efs";
    public static final String CLUSTER_STACK_NAME = "eksctl-routing-k8s-cluster";
    public static final String SECURITY_GROUP_NAME = "eks-cluster-sg-routing-k8s";

    public static final String EFS_VOLUME_ID_PLACEHOLDER = "efs-volume-id-here";
    public static final String DATA_PREP_POD_FILE_PATH = "D:\\work\\routing\\docker\\k8s-gra-data-prep-pod-template.yaml";
    public static final String DATA_PREP_POD_NEW_FILE = "D:\\work\\routing\\docker\\k8s-gra-data-prep-pod.yaml";


}

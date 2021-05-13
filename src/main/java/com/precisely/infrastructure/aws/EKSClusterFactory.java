package com.precisely.infrastructure.aws;

import java.util.HashMap;
import java.util.Map;

public final class EKSClusterFactory {

    private final static Map<String, EKSClusterDetails> clusters = new HashMap<>();

    private EKSClusterFactory(){}

    public static EKSClusterDetails getClusterDetails(String clusterName) {
        return clusters.get(clusterName);
    }

    public static void addClusterDetails(String clusterName, EKSClusterDetails clusterDetails) {
        clusters.put(clusterName, clusterDetails);
    }
}

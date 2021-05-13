package com.precisely.infrastructure.aws;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public final class GetMyIP {

    public static String getMyIPCIDR() {

        URL whatismyip;
        try {
            whatismyip = new URL("http://checkip.amazonaws.com");
        } catch (MalformedURLException e) {
            throw new RuntimeException("Could not get IP address", e);
        }

        BufferedReader in;
        String ip;
        try {
            in = new BufferedReader(new InputStreamReader(
                    whatismyip.openStream()));
            ip = in.readLine(); //you get the IP as a String
        } catch (IOException e) {
            throw new RuntimeException("Could not get IP address", e);
        }

        return ip + "/32";
    }

    public static void main(String[] args) {
        System.out.println(getMyIPCIDR());
    }
}



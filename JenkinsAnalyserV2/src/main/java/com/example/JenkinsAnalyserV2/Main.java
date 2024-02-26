package com.example.JenkinsAnalyserV2;

import java.io.IOException;
import java.util.Scanner;

public class Main {
    public static void main(String [] agrs) throws IOException {
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter Job URL");
        String jobURLFromUser = sc.nextLine().trim();
        JobDetailsExtractor jobDetailsExtractor = new JobDetailsExtractor(jobURLFromUser);
        jobDetailsExtractor.extractor();
    }
}

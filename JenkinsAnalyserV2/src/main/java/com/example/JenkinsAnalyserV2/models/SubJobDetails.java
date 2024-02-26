package com.example.JenkinsAnalyserV2.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class SubJobDetails {
    String jobAlias,parentJobName,result,jobURL,duration;
    int pass,totalFailures,total,newFailures,knownFailure,notRun;
    String failedTags;


    @Override
    public String toString() {
        return jobAlias+","+jobURL+","+result+","+duration+","+total+","+pass+","+knownFailure+","+newFailures+","+totalFailures+","+notRun+","+failedTags+"\n";
    }
}

package com.example.JenkinsAnalyserV2.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter @Setter @AllArgsConstructor @NoArgsConstructor
public class JOBDetails {
    String jobName;
    String jobURL;
    String branchName;
    String testAgentBranch;

    List<SubJobDetails> subJobDetails;

    @Override
    public String toString() {
        String row="";
        for(SubJobDetails job: subJobDetails){
            row+=job.toString();
        };
        return "JobName,"+jobName+"\n"+
                "JobURL,"+jobURL+"\n"+
                "BranchName,"+branchName+"\n"+
                "TestAgentBranch,"+testAgentBranch+"\n\n"+
                "JobName,JobURL,Result,Duration,Total,Pass,KnownFailures,NewFailures,TotalFailures,NotRun,FailedTags\n"+
                row;
    }
}

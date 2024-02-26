package com.example.JenkinsAnalyserV2;

import com.example.JenkinsAnalyserV2.models.JOBDetails;
import com.example.JenkinsAnalyserV2.models.SubJobDetails;
import com.sun.org.apache.xpath.internal.operations.Bool;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JobDetailsExtractor {
    static String  userName = "gaunisha_ganesh";
    static String  authtToken = "111b9cac718afce07e2a69246b6e88e740";
    static String  credentials = Credentials.basic(userName,authtToken);
    private String base_URL;
    private enum FailedTagsPattern{
        CHORUS("[^\\w]FailedTags: \\[.*"),
        TESTNG("The Failed Test Groups are :\\n.*\\[.*]");

        private String failedTagPattern;
        FailedTagsPattern(String failedTagPattern){
            this.failedTagPattern=failedTagPattern;
        }
        public String getPattern(){
            return this.failedTagPattern;
        }

    }

    JobDetailsExtractor(String base_URL){
        this.base_URL = base_URL;
    }

    public JOBDetails extractor() throws IOException {
        Response response = getResponseFromJenkins(credentials,base_URL+ "/api/json?pretty=true");

        JSONObject jenkinsResponse = new JSONObject(response.body().string());

        JOBDetails jobDetails = parseJSON(jenkinsResponse);

        getConsoleOutput(jobDetails.getSubJobDetails(),credentials);
        writeIntoFile(File.separator+"Users"+File.separator+"gaunishagaanavig"+File.separator+"Documents"+File.separator+"JobDetails.csv", jobDetails);
        return jobDetails;
    }

    private JSONObject getTestStatsfromConsoleOutput(String consoleOutputs) {
        Pattern pattern = Pattern.compile("Test Result : \\{(\n.*){6}\n");
        Matcher matcher = pattern.matcher(consoleOutputs);
        JSONObject testStatObj=null;
        while(matcher.find()){
            String testStatfromOutput = matcher.group();
            testStatObj = new JSONObject(testStatfromOutput.replace("Test Result : ","")+"\n}");
        }
        return testStatObj;
    }

    private Response getResponseFromJenkins(String credentials, String url) throws IOException {
        OkHttpClient client = new OkHttpClient().newBuilder()
                .build();
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", credentials)
                .build();
        Response response = client.newCall(request).execute();
        return response;
    }

    private void getConsoleOutput(List<SubJobDetails> subJobDetails, String credentials) throws IOException {

        for(SubJobDetails subjob: subJobDetails){
            Response response = getResponseFromJenkins(credentials, subjob.getJobURL()+"/consoleText");
            String res = response.body().string();
            JSONObject testStat = getTestStatsfromConsoleOutput(res);
            if(testStat!=null){
                subjob.setTotal((Integer) testStat.get("Total"));
                subjob.setPass((Integer) testStat.get("Pass"));
                subjob.setTotalFailures((Integer) testStat.get("TotalFailures"));
                subjob.setNewFailures((Integer) testStat.get("NewFailure"));
                subjob.setKnownFailure((Integer) testStat.get("KnownFailure"));
                subjob.setNotRun((Integer) testStat.get("NotRun"));
            }

            subjob.setFailedTags(getFailedTags(subjob, res));
        }
    }

    private String getFailedTags(SubJobDetails subJobDetails,String consoleResponse) throws IOException {
        Pattern pattern = null;
        Boolean isChorus = checkChorusOrTestNG(subJobDetails);
        pattern = Pattern.compile((isChorus)
                ? FailedTagsPattern.valueOf("CHORUS").getPattern()
                : FailedTagsPattern.valueOf("TESTNG").getPattern());
        if(subJobDetails.getNewFailures()!=0){
            Matcher matcher = pattern.matcher(consoleResponse);
            if(matcher.find()){
                pattern = Pattern.compile("\\[.*]");
                matcher = pattern.matcher(matcher.group());
                if(matcher.find()){
                    String failedTags = "\""+matcher.group()+"\"";
                    System.out.println(failedTags);
                    return failedTags;
                }
            }
        }
        return "-";
    }

    private Boolean checkChorusOrTestNG(SubJobDetails subJobDetails) throws IOException {
        Response response = getResponseFromJenkins(credentials, subJobDetails.getJobURL()+"/api/json?pretty=true");
        String res = response.body().string();
        JSONObject jenkinsResponse = new JSONObject(res);
        JSONArray actionsArray = (JSONArray)jenkinsResponse.get("actions");
        JSONArray parameterArray = null;
        for(Object insideAction:actionsArray){
            if(((JSONObject) insideAction).keySet().contains("parameters")){
                parameterArray = (JSONArray) ((JSONObject) insideAction).get("parameters");
            }
        }
        for(Object action:parameterArray){
            if(((JSONObject) action).get("name").toString().equalsIgnoreCase("TestTag"))
                if(!((String) ((JSONObject) action).get("value")).equalsIgnoreCase("none"))
                    return true;
        }
        return false;
    }

    private JOBDetails parseJSON(JSONObject jenkinsResponse) {
        JOBDetails jobDetails = new JOBDetails();
        JSONArray parameterArray = null;
        jobDetails.setJobName((String) jenkinsResponse.get("fullDisplayName"));
        jobDetails.setJobURL((String) jenkinsResponse.get("url"));

        JSONArray actionsArray = (JSONArray)jenkinsResponse.get("actions");

        for(Object insideAction:actionsArray){
            if(((JSONObject) insideAction).keySet().contains("parameters")){
                parameterArray = (JSONArray) ((JSONObject) insideAction).get("parameters");
            }
        }

        for(Object action:parameterArray){
            if(((JSONObject) action).get("name").toString().equalsIgnoreCase("Branch"))
                jobDetails.setBranchName((String) ((JSONObject) action).get("value"));
            else if(((JSONObject) action).get("name").toString().equalsIgnoreCase("TestAgentBranch"))
                jobDetails.setTestAgentBranch((String) ((JSONObject) action).get("value"));
        }

        List<SubJobDetails> subJobDetails = new ArrayList<>();

        for(Object subBuild: new JSONArray(jenkinsResponse.get("subBuilds").toString())){
            SubJobDetails job = new SubJobDetails();
            job.setJobAlias((String) ((JSONObject) subBuild).get("jobAlias"));
            job.setJobURL("https://jenkins.striim.com:8080/"+((JSONObject) subBuild).get("url"));
            job.setResult((String) ((JSONObject) subBuild).get("result"));
            job.setParentJobName((String) ((JSONObject) subBuild).get("parentJobName"));
            job.setDuration((String) ((JSONObject) subBuild).get("duration"));
            subJobDetails.add(job);
        }
        jobDetails.setSubJobDetails(subJobDetails);
        return jobDetails;
    }
    private static void writeIntoFile(String fileName, JOBDetails content) throws IOException {
        File outputfile = new File(fileName);
        if(!outputfile.exists()) outputfile.createNewFile();
        FileWriter writer = new FileWriter(outputfile);
        writer.write(content.toString());
        writer.flush();
        System.out.println("Content Written to File! " + outputfile.getAbsolutePath());
    }
}
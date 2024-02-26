package com.example.JenkinsAnalyserV2;

import com.example.JenkinsAnalyserV2.models.JOBDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;

@Controller
public class Controllers {

    @RequestMapping("/")
    String home() {
        return "index";
    }
    @PostMapping("/jobdetails")
    String getJobDetails(@RequestParam("joburl") String jobURL, Model model) throws IOException {
        JobDetailsExtractor jobDetailsExtractor = new JobDetailsExtractor(jobURL);
        JOBDetails jobDetails = jobDetailsExtractor.extractor();
        model.addAttribute("jobDetails", jobDetails);
        return "jobDetailsRender";
    }
}

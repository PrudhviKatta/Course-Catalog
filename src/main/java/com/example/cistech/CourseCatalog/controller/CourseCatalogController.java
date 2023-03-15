package com.example.cistech.CourseCatalog.controller;

import com.example.cistech.CourseCatalog.dto.CourseNames;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;


@RestController
public class CourseCatalogController {
    @Autowired
    EurekaClient eurekaClient;

    @Value("${service.welcome.message}")
    String welcomeMessage;

    private static final String COURSE_CATALOG="COURSE_CATALOG";

    @GetMapping("/course-catalog")
    @CircuitBreaker(name="COURSE_CATALOG", fallbackMethod="fallBackMethod")
    public List<CourseNames> getNames(){
        //String courseAppURL ="http://localhost:8001/course";
        InstanceInfo nextServerFromEureka=eurekaClient.getNextServerFromEureka("course-app",false);
                String homePageUrl=nextServerFromEureka.getHomePageUrl();
        RestTemplate restTemplate=new RestTemplate();
        String courseAppURL=homePageUrl+"course";
        ResponseEntity<List<CourseNames>> rateResponse=restTemplate.exchange(
                courseAppURL, HttpMethod.GET,null,new ParameterizedTypeReference<List<CourseNames>>()
                {});
        List<CourseNames> courseNames=rateResponse.getBody();
        return courseNames;

    }

    public List<CourseNames> fallBackMethod(Exception e)
    {
        List<CourseNames> courses=new ArrayList<>();
        CourseNames cn=new CourseNames();
        cn.setId(1);
        cn.setName("No courses found at this point. Please try later!!");
        cn.setInstructor("No Instructors found at this point!!");
        courses.add(cn);
        return courses;
    }


    @GetMapping("/course-catalog/{id}")
    public CourseNames getSpecificCourse(@PathVariable String id)
    {
        //String courseAppURL="http://localhost:8001/course/"+id;
        InstanceInfo nextServerFromEureka=eurekaClient.getNextServerFromEureka("course-app",false);
        String homePageUrl= nextServerFromEureka.getHomePageUrl();
        String courseAppURL=homePageUrl+"course/"+id;
        RestTemplate restTemplate=new RestTemplate();
        CourseNames courseNames= restTemplate.getForObject(
                courseAppURL, CourseNames.class);
        return courseNames;

    }

    @GetMapping("/")
    public String hello()
    {
        return "Hello Message";
    }
    @GetMapping("/welcome")
    public String welcomeMethod()
    {
        return welcomeMessage;
    }
}

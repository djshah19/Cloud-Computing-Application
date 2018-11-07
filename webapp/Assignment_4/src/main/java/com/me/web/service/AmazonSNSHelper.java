package com.me.web.service;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.GetTopicAttributesRequest;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class AmazonSNSHelper {
    AmazonSNSClient snsClient = (AmazonSNSClient) AmazonSNSClientBuilder.standard().withRegion("us-east-1").build();
    @Value("${snsARN}")
    String snsArn;

    public void publish(String username){
        //publish to an SNS topic
        String msg = username;
        PublishRequest publishRequest = new PublishRequest(snsArn, msg);
        PublishResult publishResult = snsClient.publish(publishRequest);
//print MessageId of message published to SNS topic
        System.out.println("MessageId - " + publishResult.getMessageId());
    }

}

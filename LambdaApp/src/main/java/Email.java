import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;

import java.util.UUID;

public class Email implements RequestHandler<Object,String> {
    public String handleRequest(Object o, Context context) {
        context.getLogger().log("Testing CloudWatch logging");
        try {
            AmazonDynamoDB clientDynamoDB = AmazonDynamoDBClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("AKIAJ57D5A4O35BGNYDA","QDFmXU5EQ5FUZAr17YvGcs5edbT7MhyK4WFXtwMW"))).build();
            DynamoDB dynamoDB = new DynamoDB(clientDynamoDB);

            Table table = dynamoDB.getTable("password_reset");

            Item item = table.getItem("id", "346575-565656");

            AmazonSimpleEmailService client =
                    AmazonSimpleEmailServiceClientBuilder.standard().withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("AKIAIORQRBGBBU5CI2QA", "Db/I7J5s8vNaeO6i4s6mhx2gylPvuyDq9OIXObUu"))).build();

            SendEmailRequest request = new SendEmailRequest()
                    .withDestination(
                            new Destination().withToAddresses(item.getString("username")))
                    .withMessage(new Message()
                            .withBody(new Body()
                                    //.withHtml(new Content()
                                      //      .withCharset("UTF-8").withData(HTMLBODY))
                                    .withText(new Content()
                                            .withCharset("UTF-8").withData("Password reset Link:" + item.getString("link"))))
                            .withSubject(new Content()
                                    .withCharset("UTF-8").withData(item.getString("Subject"))))
                    .withSource("no-reply@test.csye6225-fall2018-fernandoi.me");

            client.sendEmail(request);
            context.getLogger().log("Email sent!");
            //System.out.println("Email sent!");
        } catch (Exception ex) {
            context.getLogger().log("The email was not sent. Error message: "
                    + ex.getMessage());
        }
        return null;
    }
}

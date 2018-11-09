import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.QuerySpec;
import com.amazonaws.services.dynamodbv2.document.utils.ValueMap;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.SNSEvent;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailService;
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder;
import com.amazonaws.services.simpleemail.model.*;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.UUID;

public class Email implements RequestHandler<SNSEvent,String> {
    public String handleRequest(SNSEvent snsEvent, Context context) {
        context.getLogger().log("Testing CloudWatch logging");
        try {

            AmazonDynamoDB clientDynamoDB = AmazonDynamoDBClientBuilder.standard().withRegion("us-east-1").build();
            DynamoDB dynamoDB = new DynamoDB(clientDynamoDB);
            String uuid = UUID.randomUUID().toString();
            Table table = dynamoDB.getTable("password_reset");
            //Item item = table.getItem("id", "346575-565656");
            for(SNSEvent.SNSRecord record: snsEvent.getRecords()) {
                context.getLogger().log("Testing CloudWatch logging 1");
                SNSEvent.SNS sns = record.getSNS();
                String email = sns.getMessage();
                //QuerySpec spec = new QuerySpec().withKeyConditionExpression("username = :username")
                  //      .withValueMap(new ValueMap()
                    //            .withString(":username", email));;
                //spec.withAttributesToGet("username").
                //
                //Calendar.getInstance().add(Calendar.MINUTE, 8);
                Date todayCal = Calendar.getInstance().getTime();
                SimpleDateFormat crunchifyFor = new SimpleDateFormat("MMM dd yyyy HH:mm:ss.SSS zzz");
                String curTime = crunchifyFor.format(todayCal);
                Date curDate = crunchifyFor.parse(curTime);
                Long epoch = curDate.getTime();
                //GetItemSpec getItemSpec = new GetItemSpec();
                QuerySpec querySpec = new QuerySpec().withKeyConditionExpression("username = :v_username and ttl_timestamp > :v_timestamp")
                        .withValueMap(new ValueMap().withString("v_username",email).withString(":v_timestamp",epoch.toString())).withConsistentRead(true);
                //Item checkItem = table.getItem("username",email);
                ItemCollection<QueryOutcome> items = table.query(querySpec);
                Iterator<Item> iterator = items.iterator();
                if(!iterator.hasNext()) {
                        context.getLogger().log("Testing CloudWatch logging 2");
                        Calendar.getInstance().add(Calendar.MINUTE, 8);
                        Date today = Calendar.getInstance().getTime();
                        SimpleDateFormat crunchifyFormat = new SimpleDateFormat("MMM dd yyyy HH:mm:ss.SSS zzz");
                        String currentTime = crunchifyFormat.format(today);
                        String link = "https://csye6225-fall2018-fernandoi.me/" + uuid;
                        Date date = crunchifyFormat.parse(currentTime);
                        long epochTime = date.getTime();
                        Item item = new Item();
                        //item.withPrimaryKey("id", uuid);
                        item.withPrimaryKey("username", email);
                        item.with("ttl_timestamp", epochTime);
                        item.with("Subject", "Password Reset Link");
                        item.with("link", link);
                        context.getLogger().log("Testing");
                        PutItemOutcome outcome = table.putItem(item);
                        //context.getLogger().log(outcome.toString());
                        context.getLogger().log(email);
                        AmazonSimpleEmailService client = AmazonSimpleEmailServiceClientBuilder.standard().withRegion("us-east-1").build();
                        SendEmailRequest request = new SendEmailRequest().withDestination(new Destination().withToAddresses(email)).withMessage(new Message()
                                .withBody(new Body()
                                        .withText(new Content()
                                                .withCharset("UTF-8").withData("Password reset Link:" + link)))
                                .withSubject(new Content()
                                        .withCharset("UTF-8").withData("Password Reset Link")))
                                .withSource("no-reply@test.csye6225-fall2018-fernandoi.me");
                        client.sendEmail(request);
                        context.getLogger().log("Email sent!");
                }
            }
        } catch (Exception ex) {
            context.getLogger().log("The email was not sent. Error message: " + ex.getMessage()+"stack: "+ex.getStackTrace()[ex.getStackTrace().length -1].getLineNumber());
            context.getLogger().log(ex.getStackTrace()[ex.getStackTrace().length -1].getFileName());

        }
        return null;
    }
}

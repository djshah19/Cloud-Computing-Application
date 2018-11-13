#!/bin/sh

stack_name=$1



myS3Bucket=$2

myLambdaIAMRole=$3

mySNSName=$4
#CICDstack_name=$4

bucketname="${myS3Bucket}"

lambdaIAMRole="${myLambdaIAMRole}"
snsname="${mySNSName}"

lambdaIAMRoleArn=$(aws iam get-role --role-name lambdaIAMRole --query Role.Arn --output text)



aws cloudformation create-stack --stack-name $stack_name --template-body file://csye6225-cf-serverless.json --parameters ParameterKey=myBucketName,ParameterValue=$bucketname ParameterKey=lambdaIAMRole,ParameterValue=$lambdaIAMRoleArn ParameterKey=SNSNAME,ParameterValue=$snsname --capabilities CAPABILITY_IAM CAPABILITY_NAMED_IAM
#ParameterKey=ListS3BucketsRole,ParameterValue=$EC2_role


if [ $? -eq 0 ]; then
  echo "Creating progress"
  aws cloudformation wait stack-create-complete --stack-name $stack_name
  echo "Stack created successfully"
  
else
  echo "Failure while creating stack"
fi

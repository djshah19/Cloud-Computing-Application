#!/bin/sh
#shell script to delete AWS stack
stack_name=$1

bucket_name=$2

bucketName="code-deploy.$bucket_name.csye6225.com"


echo deleting the stack


aws s3 rb s3://$bucketName --force


aws iam remove-role-from-instance-profile --instance-profile-name ayyodevre --role-name CodeDeployEC2ServiceRole

aws iam delete-role-policy --role-name CodeDeployEC2ServiceRole --policy-name CodeDeploy-EC2-S3


aws iam delete-role --role-name CodeDeployEC2ServiceRole

aws iam detach-role-policy --role-name CodeDeployServiceRole --policy-arn arn:aws:iam::aws:policy/service-role/AWSCodeDeployRole

aws iam delete-role --role-name CodeDeployServiceRole



if [ $? -eq 0 ]; then
  echo "Creating progress"
  aws cloudformation wait stack-delete-complete --stack-name $stack_name
  echo "Stack deleted successfully"
else
  echo "Failure while deleting stack"
fi

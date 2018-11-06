#!/bin/sh
#shell script to delete AWS stack
stack_name=$1

echo deleting the stack



if [ $? -eq 0 ]; then
  echo "Creating progress"
  aws cloudformation wait stack-delete-complete --stack-name $stack_name
  echo "Stack deleted successfully"
else
  echo "Failure while deleting stack"
fi

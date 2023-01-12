#!/bin/bash
# -- > Create S3 Bucket
echo 'Starting.....................'
echo $(awslocal s3api create-bucket --bucket portal-user-profile-images)
# --> List S3 Buckets
echo $(awslocal s3api list-buckets)
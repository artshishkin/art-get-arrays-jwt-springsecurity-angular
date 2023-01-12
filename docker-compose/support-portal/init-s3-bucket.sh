#!/bin/bash
# -- > Create S3 Bucket
echo $(awslocal s3 mb s3://portal-user-profile-images)
# --> List S3 Buckets
echo $(awslocal s3 ls)
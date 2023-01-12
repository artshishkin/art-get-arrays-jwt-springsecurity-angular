## Working with LocalStack

1. Start docker-compose
2. Working with AWS CLI
    - create bucket
        - `aws s3 mb s3://portal-user-profile-images --endpoint-url http://localhost:4566`
        - **or**
        - `aws s3api create-bucket --bucket portal-user-profile-images --endpoint-url http://localhost:4566 --create-bucket-configuration '{\"LocationConstraint\":\"eu-north-1\"}'`
            - will respond
            - ```
              {
                "Location": "http://portal-user-profile-images.s3.localhost.localstack.cloud:4566/"
              }
            ```
    - list buckets
        - `aws s3 ls --endpoint-url http://localhost:4566`
        - `aws s3api list-buckets --endpoint-url http://localhost:4566`
    - list objects in a bucket
        - `aws s3 ls s3://portal-user-profile-images --endpoint-url http://localhost:4566` - only one level (like dir)
        - `aws s3api list-objects --bucket portal-user-profile-images --endpoint-url http://localhost:4566` - view all object info
3. Working with `awslocal` cli inside running docker container
    - run `bash` inside container
        - `docker ps` - view container id `c60d2c36c5f9`
        - `docker exec -it c60 bash`
    - create bucket
        - `awslocal s3 mb s3://portal-user-profile-images`
        - `awslocal s3api create-bucket --bucket portal-user-profile-images --create-bucket-configuration '{"LocationConstraint":"eu-north-1"}'`
    - list buckets
        - `awslocal s3 ls`
        - `awslocal s3api list-buckets`
   - list objects in a bucket
        - `awslocal s3 ls s3://portal-user-profile-images` - only one level (like dir)
        - `awslocal s3api list-objects --bucket portal-user-profile-images` - view all object info
5. Working with `awslocal` cli installed locally
    - install localstack cli
        - `pip install localstack`
        - `localstack --version`

    



      
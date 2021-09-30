# art-get-arrays-jwt-springsecurity-angular
 JSON Web Token (JWT) with Spring Security And Angular - Tutorial from Get Arrays (Udemy)

### Notes

####  Section 19: HTML Template

#####  134. Configuring routes

-  Generate AppRoutingModule
    -  `ng generate module app-routing --flat --module=app`
-  Modify `app-routing.module.ts`

####  Section 29: Security Management - Front End

#####  199. Unsubscribe using Subsink library

1.  Install [SubSink](https://www.npmjs.com/package/subsink)
    -  `npm install subsink --save`
2.  Use it

####  Section 30: Deployment

#####  202. Creating EC2 instance

Create EC2 instance with custom security rules
-  open port 80, 5000, 443(ssh) for everyone 

##### 203. Configure EC2 instance

1.  Install apache web server
    -  `sudo service httpd start` -> service not found
    -  `sudo yum -y install httpd` -> OK
    -  `sudo service httpd status` -> running
    -  Go to EC2 public URL -> Apache Server Default Page
2.  Test work  
    -  Navigate to server content
        -  `cd /var/www/html`
    -  create sample html file
        -  `sudo nano index.html`
        -  `<h1>Welcome to AWS Apache server</h1>`
        -  Ctrl+O
        -  Ctrl+X
    -  Go to EC2 public URL -> Our web page
3.  Install Java
    -  `sudo amazon-linux-extras install java-openjdk11`
4.  Install MySQL
    -  `sudo wget https://dev.mysql.com/get/mysql57-community-release-el7-11.noarch.rpm`
    -  `sudo wget https://dev.mysql.com/get/mysql57-community-release-el7-11.noarch.rpm`
    -  `sudo yum localinstall mysql57-community-release-el7-11.noarch.rpm` 
    -  `sudo yum install mysql-community-server`
5.  Start MySQL    
    -  `sudo systemctl start mysqld.service`    
    -  `sudo systemctl status mysqld.service`    
    -  Check for temporarily root password
        -  `sudo grep 'temporary password' /var/log/mysqld.log`    
        -  `A temporary password is generated for root@localhost: idy7c#>jEetF`
    -  `sudo mysql_secure_installation`
        -  new password: `Supp0rtP0rta!`
        -  No for other questions
6.  Configure mysql
    -  `sudo mysql -u root -p` -> enter password
    -  `show databases;`
    -  `create database support_portal;`
    -  `create user 'support_portal_user'@'localhost' identified by 'Supp0rt_Porta!_P@ssword';`
    -  `grant all privileges on support_portal.* to 'support_portal_user'@'localhost'; `       

#####  206. Running in AWS

1.  Copy JAR file to EC2 home folder (secured copy)
    -  `scp -i "certified-dev-assoc-course.pem" "C:\Users\Admin\IdeaProjects\Study\GetArrays\art-get-arrays-jwt-springsecurity-angular\support-portal-backend\target\support-portal.jar"  ec2-user@ec2-13-51-129-89.eu-north-1.compute.amazonaws.com:~/`
2.  Set Environment Variables
    -  `export PORTAL_MAIL_USERNAME="{{your_gmail_username}}"`
    -  `export PORTAL_MAIL_PASSWORD="{{your_gmail_password}}"`
    -  `export SPRING_PROFILES_ACTIVE="aws-local"`
3.  Start java app
    -  `java -jar support-portal.jar`
4.  Run as executable
    -  `sudo chmod 755 support-portal.jar`   
    -  `ls -lh support-portal.jar` -> view permissions
    -  `./support-portal.jar`
         
#####  207. Deploying Angular Application

1.  Build Angular Application
    -  `ng build --prod` 
2.  Upload dist/support-portal-frontend folder to EC2
    -  `scp -r -i "certified-dev-assoc-course.pem" "C:\Users\Admin\IdeaProjects\Study\GetArrays\art-get-arrays-jwt-springsecurity-angular\support-portal-frontend\dist\*"  ec2-user@ec2-13-51-129-89.eu-north-1.compute.amazonaws.com:~/`
3.  Move files to httpd directory
    -  `sudo cp ~/support-portal-frontend/* /var/www/html`        

#####  208. Creating Unix Service       

1. Create dedicated user to run this app as a service
    -  without ability to login
    -  `sudo adduser --home /var/lib/supporthome --shell /sbin/nologin supportuser`
    -  `sudo cat /etc/passwrd`
2.  Add access for the system processes to access home folder's content
    -  `cd /var/lib`
    -  `ls -lh` -> only supportuser has access
    -  `sudo chmod 755 /var/lib/supporthome` 
        -  owner has full access 7 (rwx) - read write execute
        -  others - 5 (r-x) - read and execute   
3.  Copy jar into supportuser home
    -  `cd ~`
    -  `sudo cp support-portal.jar /var/lib/supporthome`    
4.  Change ownership of jar file
    -  `ls -lh` -> owner is root
    -  `sudo chown supportuser:supportuser support-portal.jar`    
    -  `ls -lh` -> owner is supportuser
5.  Change permission to read and execute only for supportuser
    -  `sudo chmod 500 support-portal.jar`    
6.  Protect the file from accident deletion
    -  `sudo chattr +i support-portal.jar` - change attribute `+i` (add immutable) 
    -  `rm support-portal.jar` ->
        -  ` cannot remove ‘support-portal.jar’: Operation not permitted`
    -  `sudo rm -f support-portal.jar` ->
        -  ` cannot remove ‘support-portal.jar’: Operation not permitted`
    -  (for deletion we need first remove immutability - `sudo chattr -i support-portal.jar`)
7.  Create symbolic link
    -  `sudo ln -s /var/lib/supporthome/support-portal.jar /etc/init.d/supportapi`
        -  `ln` - link
        -  `-s` - symbolic
        -  `/etc/init.d` - init directory
        -  `supportapi` - name of service
    -  `cd /etc/init.d`
    -  `ls` -> we have supportapi
8.  Start service
    -  `sudo service supportapi start`
    -  `sudo service supportapi status`            
9.  View logs
    -  `cd /var/log` -> `ls`
    -  `cat /var/log/supportapi.log`
    -  **or**
    -  `sudo vim supportapi.log`  -> :qa for quit
    -  **or**
    -  `sudo tail -f /var/log/supportapi.log`
10.  Setting Permanent Global Environment Variables for All Users
    -  `sudo nano /etc/environment`
    -  `SPRING_PROFILES_ACTIVE=aws-local`

#####  208.2 Creating Unix Service - Correct Way 

-  cd /etc/systemd/system
-  Create a file named your-service.service and include the following:
```shell script
[Unit]
Description=Support Portal API

[Service]
User=supportuser
WorkingDirectory=/var/lib/supporthome
ExecStart=/var/lib/supporthome/support-portal.jar
Restart=always
Environment="SPRING_PROFILES_ACTIVE=aws-local"

[Install]
WantedBy=multi-user.target
```
-  Reload the service files to include the new service.
    -  `sudo systemctl daemon-reload`
-  Start your service
    -  `sudo systemctl start supportapi.service`
-  To check the status of your service
    -  `sudo systemctl status supportapi.service`
-  To enable your service on every reboot
    -  `sudo systemctl enable supportapi.service`
          
#####  209. Testing in Production 

-  Test with: 
    -  Username: `art.shyshkin`
    -  Password: `17aH!?o>CJ`
    -  and another    
    -  Username: `shyshkin.art`
    -  Password: `5C<"0dVx=>`

#####  33 deploy frontend into s3 bucket

1.  S3 console
    -  Create bucket: `portal.shyshkin.net`
2.  Build Angular app
    -  create `environment.test.ts`
    -  modify `angular.json`
    -  `ng build -c test`
3.  Copy files from `dist/support-portal-frontend` to the bucket
4.  Static website hosting
    -  Enable
    -  index.html
5.  Allow public access
    -  `Block public access (bucket settings)` - All OFF
6.  Edit bucket policy
    -  `{`
    -  `    "Version": "2012-10-17",`
    -  `    "Statement": [`
    -  `        {`
    -  `            "Sid": "PublicRead",`
    -  `            "Effect": "Allow",`
    -  `            "Principal": "*",`
    -  `            "Action": [`
    -  `                "s3:GetObject",`
    -  `                "s3:GetObjectVersion"`
    -  `            ],`
    -  `            "Resource": "arn:aws:s3:::portal.shyshkin.net/*"`
    -  `        }`
    -  `    ]`
    -  `}`
7.  Visit `http://portal.shyshkin.net.s3-website.eu-north-1.amazonaws.com`    
8.  Make an Alias to Website
    -  Route 53 console
    -  Hosted zone: shyshkin.net
    -  Add record
        -  Name: portal
        -  Record Type: A
        -  Routing policy: Simple routing
        -  Alias: true
        -  Alias to S3 website endpoint
        -  Stockholm
        -  s3-website.eu-north-1.amazonaws.com
9.  Tune CORS for backend
    -  add `http://portal.shyshkin.net`

#####  35.1 deploy MySQL into AWS RDS - with public access

1.  RDS Console
    -  MySQL 8.0.23
    -  Dev/Test
    -  DB instance identifier: `portal-db`
    -  Master username: portal_user
    -  Master password: Supp0rt_Porta!_PAssword
    -  DB Instance: db.t3.micro
    -  Public access: yes
2.  Additional configuration
    - Initial database name:  support_portal  
3.  Create DB
4.  Create Security Group MySQLFromEverywheere
    -  allow port 3306 from everywhere (for testing purposes)
5.  Attach SG MySQLFromEverywheere to DB instance    

#####  35.2 deploy MySQL into AWS RDS - WITHOUT public access

1.  Detach SG MySQLFromEverywheere
2.  To access DB from EC2 `support-portal`
    -  attach `default` SG to EC2 (DB has `default` SG too)
3.  Delete SG MySQLFromEverywheere
4.  Run backend app with new profile (for testing)
    -  `java -jar -Dspring.profiles.active=aws-rds`
5.  Change environment variable (for service) 
    -  `sudo systemctl edit supportapi`
    -  Content:
        -  `[Service]`
        -  `Environment="SPRING_PROFILES_ACTIVE=aws-rds"`       

##### 34.1 Create EC2 instance for Docker

-  Create EC2 instance
-  User Data:
```shell script
#!/bin/bash
yum update -y
amazon-linux-extras install -y docker
service docker start
usermod -a -G docker ec2-user
chkconfig docker on
mkdir -p /etc/systemd/system/docker.service.d
echo "[Service]
        ExecStart=
        ExecStart=/usr/bin/dockerd -H unix:// -H tcp://0.0.0.0:2375" > /etc/systemd/system/docker.service.d/options.conf
systemctl daemon-reload
systemctl restart docker
``` 
-  Security group: `docker-security-group`
    -  Allow 8080 (tomcat), 2375 (from my PC), 22 (SSH)
-  Allocate Elastic IP: `docker-elastic-ip`
-  Associate `docker-elastic-ip` with `docker-ec2`
-  In Route 53 create record `dockerapp` with `docker-ec2` public IP 
 
####  34.2 Allow Docker EC2 to Access RDS

-  Create `mysql-vpc-security-group`
    -  Allow port 3306 from `docker-security-group`
-  Attach SG to database in RDS `portal-db` 

#####  34.3 Build and Run Docker image in Docker EC2

-  `mvn clean package docker:build docker:start`

#####  34.4 Configure frontend to call new backend

-  `ng build`
-  upload `dist/support-portal-frontend` to S3

#####  34.5 Logging remote docker

-  `mvn docker:logs`            
-  `mvn docker:logs -Ddocker.follow`            

#####  34.6 Persisting images to EC2 filesystem

1.  Initial state
    -  `docker container exec -it b36 bash`
    -  `pwd` -> /application
    -  `cd ~` -> `pwd` -> /root
    -  `ls` -> supportportal
    -  `ls supportportal/user` -> folders of users like `{UUID}`
2.  State after rebooting EC2 instance 
    -  same docker container
    -  `docker container exec -it b36 bash`
    -  `ls /root/supportportal/user` -> all left the same
3.  State after recreating container (or new image)
    -  `mvn docker:stop docker:start`
    -  other container
    -  `docker container exec -it bc7 bash`
    -  **or**
    -  `docker container exec -it angular-support-portal-backend bash`
    -  `ls /root/supportportal/user` -> **No such file or directory**
4.  Adding volume to store images between rebuilds
    -  add `<volume>/root/supportportal</volume>` - not successful
    -  add `<volume>~/supportportal:/root/supportportal</volume>` - Error
        -  `'~/supportportal' cannot be relativized, cannot resolve arbitrary user home paths.`
    -  add `<volume>/home/ec2-user/supportportal:/root/supportportal</volume>` - success

#####  36.1 Deploy  Spring Boot JAR file on AWS Elastic Beanstalk

1.  Info about deployment Spring Boot app on AWS
    -  AWS EBS expects for your apps to listen on port 5000
    -  Update your Spring Boot application.properties to use: server.port=5000
    -  Select Web App > Platform Java
    -  Upload the JAR file
2.  Modify RDS security
    -  create SG `mysql-marker-sg` with no inbound riles
    -  modify SG `mysql-vpc-security-group` to allow 3306 from `mysql-marker-sg` 
3.  Deploy Spring Boot App to AWS
    -  Log into to AWS
    -  Navigate to Elastic Beanstalk
    -  Create a new application
    -  Select app type: Web Application
    -  Give it the name: `support-portal-backend`
    -  Create a new environment
    -  For platform, select: Java
    -  Select option to Upload your JAR file.
        -  Note: the screen says only WAR and ZIP files, but it does in fact accept JAR files
    -  Upload your JAR file: target/support-portal.jar (directly or though S3)
        -  I choose `https://art-sources.s3.eu-north-1.amazonaws.com/support-portal.jar`
    -  Configure More Options
        -  Single instance
        -  Instances
            -  EC2 Security Groups: `mysql-marker-sg`
        -  Software -> Environment properties
        -  SPRING_PROFILES_ACTIVE: aws-rds
        -  SERVER_PORT: 5000 (not necessary because we set it in application.yml)
    -  Create the application
4.  View logs
    -  Supportportalbackend-env -> Logs -> Last 100 Lines    
5.  Once the app is created, then visit the app URL.
    -  Go to environment
    -  `http://supportportalbackend-env.eba-wfr5wya3.eu-north-1.elasticbeanstalk.com/`
    -  `{"timestamp":"2021-09-27T06:49:46.181755","httpStatusCode":403,"httpStatus":"FORBIDDEN","reason":"FORBIDDEN","message":"You need to log in to access this page"}`
    -  OK - it is working
6.  Test work with frontend
    -  modify `environment.ts`
    -  `ng serve`
    -  `localhost:4200` -> OK    
7.  Create subdomain for beanstalk environment
    -  Route 53 console
    -  Create new Record for hosted zone `shyshkin.net`
    -  Record name: `portal-bean`
    -  Record type A
    -  Route traffic to `Alias to Elastic Beanstalk`
    -  `Supportportalbackend-env.eba-wfr5wya3.eu-north-1.elasticbeanstalk.com`
    -  Visit `http://portal-bean.shyshkin.net` -> have a response from spring boot app -> OK
    -  Test with Angular App: `localhost:4200`      

#####  36.2 Providing HTTPS access to the backend through Elastic LoadBalancer (ELB) and Amazon Certificate Manager (ACM)

1.  Provision certificate
    -  ACM console
    -  Provision certificates
    -  Request a public certificate
    -  Domain name: `portal-back-secure.shyshkin.net`
    -  DNS validation
    -  Create record in Route53
    -  About 30 minutes -> Pending validation (it took about a minute for me)
    -  Certificate issued
2.  Create environment
    -  Beanstalk console
    -  Application -> `support-portal-backend`
    -  Create environment -> Web server environment -> Supportportalbackend-secured
    -  Domain -> Leave blank
    -  Java
    -  Application code -> Existing version 
    -  Configure more options
        -  High Availability (with Load Balancer)
        -  Software -> Environment properties
            -  SPRING_PROFILES_ACTIVE: aws-rds
        -  Instances
            -  EC2 Security Groups: `mysql-marker-sg`            
        -  Load Balancer -> Edit
        -  Listeners -> Add Listener
            -  Port: 443
            -  Protocol: HTTPS
            -  Certificate: `portal-back-secure.shyshkin.net - e0c...`
            -  SSL policy:  ELBSecurityPolicy-TLS-1-2-Ext-2018-06 (how strong security policy will be)
            -  Add
        -  We may disable HTTP (80), but just keep it for now
        -  Save
        -  This is `custom configuration`
        -  Create environment
3.  Create CNAME for load balancer URL
    -  Route53 console
    -  Hosted zone -> shyshkin.net
    -  Add Record
        -  Simple routing
        -  Record name: `portal-back-secure`.shyshkin.net
        -  Record type: CNAME
        -  Value: `supportportalbackend-secured.eba-wfr5wya3.eu-north-1.elasticbeanstalk.com`
4.  Wait some time
5.  Visit HTTP and HTTPS
    -  `http://portal-back-secure.shyshkin.net/` -> OK                  
    -  `https://portal-back-secure.shyshkin.net/` -> OK                  
6.  View certificate info
    -  Chrome -> Lock sign
    -  Certificate
    -  Publisher
        -  CN = Amazon
        -  OU = Server CA 1B
        -  O = Amazon
        -  C = US
7.  Verify Certificate in use
    -  ACM console - certificate for `portal-back-secure.shyshkin.net`
    -  In Use -> Yes
    -  Associated resources:
        -  `arn:aws:elasticloadbalancing:eu-north-1:392971033516:loadbalancer/app/awseb-AWSEB-1OGG0G42RZOR2/f2dec1e6adf1a4de`
8.  Disable HTTP
    -  Beanstalk
    -  Environment: Supportportalbackend-secured
    -  Configuration
    -  Load Balancer -> Edit
    -  Listeners -> HTTP -> Disable -> Apply
    -  Test it:
        -  `http://portal-back-secure.shyshkin.net` -> Timeout
        -  `https://portal-back-secure.shyshkin.net` -> OK
9.  Apply redirection HTTP -> HTTPS
    -  Enable HTTP back (like in step 8)
    -  Follow [How can I redirect HTTP requests to HTTPS using an Application Load Balancer?](https://aws.amazon.com/premiumsupport/knowledge-center/elb-redirect-http-to-https-using-alb/)
    -  EC2 console -> Load Balancer
    -  Find our ALB
    -  Listeners -> HTTP -> View/Edit Rules
    -  Edit -> DEFAULT -> Then -> Edit to 
    -  `Redirect to` -> HTTPS -> 443
    -  Update
    -  Test it
        -  `https://portal-back-secure.shyshkin.net/` -> OK         
        -  `http://portal-back-secure.shyshkin.net/` -> redirect to `https://portal-back-secure.shyshkin.net/` -> OK          
10.  But now visiting direct LoadBalancer gave an error
    -  `https://supportportalbackend-secured.eba-wfr5wya3.eu-north-1.elasticbeanstalk.com`
    -  `NET::ERR_CERT_COMMON_NAME_INVALID`                              
11.  Test with Frontend
    -  Using **HTTP** `http://portal-back-secure.shyshkin.net` -> ERROR
    -  `Access to XMLHttpRequest at 'http://portal-back-secure.shyshkin.net/user?size=2147483647' from origin 'http://localhost:4200' has been blocked by CORS policy: Response to preflight request doesn't pass access control check: Redirect is not allowed for a preflight request.`
    -  Using **HTTPS** `https://portal-back-secure.shyshkin.net` -> OK

####  40. Secure Communication with HTTPS - Spring Boot Backend - SelfSigned certificate

#####  40.1. Generate SelfSigned certificate

-  Follow the steps in [instruction](www.luv2code.com/keytool-steps)
-  Generate Key and Self-Signed Certificate
    -  `keytool -genkeypair -alias securedPortal -keystore src/main/resources/securedPortal-keystore.p12 -keypass secret -storeType PKCS12 -storepass secret -keyalg RSA -keysize 2048 -validity 365 -dname "C=US, ST=Pennsylvania, L=Philadelphia, O=securedPortal, OU=Training Backend, CN=dockerapp.shyshkin.net" -ext "SAN=dns:dockerapp.shyshkin.net"`
-  Verify Results
    -  `keytool -list -v -alias securedPortal -keystore src/main/resources/securedPortal-keystore.p12 -storepass secret`    

#####  40.3. Modify Security Group for EC2 instance

-  Allow 443 port from anywhere
-  Attach `https-443` SG to `docker-ec2` instance

#####  40.4 Deploy Application to `docker-ec2`

-  `mvn docker:stop`
-  `mvn clean package docker:build docker:start -DskipTests`
-  Visit `https://dockerapp.shyshkin.net` -> allow unsecure

#####  40.5 Testing frontend

-  `ng build -c production`
-  upload to S3
-  visit `http://portal.shyshkin.net`

####  38 Save Profile Images to S3

#####  38.2 Working with S3 (tutorial) 

1.  Follow Tutorial
    -  [How to Upload Files to Amazon S3 in Spring Boot](https://www.section.io/engineering-education/spring-boot-amazon-s3/)
2.  Create S3 Bucket
    -  `portal-user-profile-images`
3.  Access and secret keys
    -  My Security Credentials
        -  will redirect to `https://console.aws.amazon.com/iam/home?region=eu-north-1#/security_credentials`
    -  Create Access Key
        -  Access key ID: `AKIA...2GBJ`
        -  Secret access key: `LUS...H+yuAW`
4.  Adding Amazon SDK dependency
    -  `<dependency>`
    -  `    <groupId>com.amazonaws</groupId>`
    -  `    <artifactId>aws-java-sdk</artifactId>`
    -  `    <version>1.12.75</version>`
    -  `</dependency>`
5.  Create configuration
6.  Create ProfileImageService implementation

#####  38.4 Using Secrets Manager to store access keys

1.  Use this tutorial
    -  [Using AWS Secrets Manager to manage secrets in Spring Boot Applications](https://raymondhlee.wordpress.com/2019/10/11/using-aws-secrets-manager-to-manage-secrets-in-spring-boot-applications/)
2.  Create secrets for API calls
    -  Secrets Manager console
    -  Create new secret    
    -  Other type of secrets
        -  AMAZON_S3_ACCESS_KEY: {provide value}
        -  AMAZON_S3_SECRET_KEY: {provide value}
    -  Select the encryption key
        -  DefaultEncryptionKey
    -  Secret Name: `/image-s3/portal-api`
    -  Disable automatic rotation
    -  Next -> view Sample code
    -  Store    
3.  Create ApplicationListener<ApplicationPreparedEvent>    
4.  Add the new application listener to the `spring.factories` file in the folder `src/main/resources/META-INF`    
5.  Test locally -> works    
 
#####  38.5 Refactoring Configuration to run in EC2 and PC with configured AWS credentials

1.  Get rid of `Access key ID` and `Secret access key`
    -  on PC we have configured credentials for AWS-CLI
    -  ~/.aws/credentials -> 
        -  `[default]`
        -  `aws_access_key_id = AK...CY`
        -  `aws_secret_access_key = Fk...30`
2.  For EC2 attach IAM Role that allow access to S3 and Security Manager
    -  EC2 -> Instances -> `docker-ec2` -> Actions -> Security -> Modify IAM Role
    -  Create New IAM Role
        -  Use Case:
            -  EC2
            -  Allows EC2 instances to call AWS services on your behalf.
        -  Attach permissions policies
            -  SecretsManagerReadWrite
            -  AmazonS3FullAccess
        -  Name:  `ec2-services-role`                 
    -  Save
3.  Deploy
4.  Test -> OK

#####  43 Adjust ec2 iam role to have less permissions

-  Create policy to access to S3 bucket `portal-user-profile-images` 
    -  SupportPortalS3AccessPolicy
```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "VisualEditor0",
            "Effect": "Allow",
            "Action": [
                "s3:PutObject",
                "s3:GetObject",
                "s3:ListBucket",
                "s3:DeleteObject"
            ],
            "Resource": [
                "arn:aws:s3:::portal-user-profile-images/*",
                "arn:aws:s3:::portal-user-profile-images"
            ]
        }
    ]
}
```
-  Detach `AmazonS3FullAccess` and attach `SupportPortalS3AccessPolicy` to the role `ec2-service-role`
-  Create policy to access to Secrets Manager secret `/support-portal` 
    -  SupportPortalSecretsAccessPolicy
```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Sid": "VisualEditor0",
            "Effect": "Allow",
            "Action": [
                "secretsmanager:GetSecretValue",
                "secretsmanager:DescribeSecret"
            ],
            "Resource": "arn:aws:secretsmanager:eu-north-1:392971033516:secret:/support-portal*"
        }
    ]
}
```    
-  Detach `SecretsManagerReadWrite` and attach `SupportPortalSecretsAccessPolicy` to the role `ec2-service-role`
-  Test it -> OK
-  Create Role `support-portal-backend-role`
    -  Attach `SupportPortalS3AccessPolicy`
    -  Attach `SupportPortalSecretsAccessPolicy`
-  Change `docker-ec2` IAM role from `ec2-service-role` from to `support-portal-backend-role`

####  44 Encrypt passwords using jasypt

#####  44.4  Improve security by using more secure algorithm 

-  Jasypt Command Line 
    -  Download cli from official site
    -  `.\encrypt.bat input="sup...word" password="<insert your password>" algorithm=PBEWITHHMACSHA512ANDAES_256 ivGeneratorClassName=org.jasypt.iv.RandomIvGenerator`






    
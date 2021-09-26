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
         
####  207. Deploying Angular Application

1.  Build Angular Application
    -  `ng build --prod` 
2.  Upload dist/support-portal-frontend folder to EC2
    -  `scp -r -i "certified-dev-assoc-course.pem" "C:\Users\Admin\IdeaProjects\Study\GetArrays\art-get-arrays-jwt-springsecurity-angular\support-portal-frontend\dist\*"  ec2-user@ec2-13-51-129-89.eu-north-1.compute.amazonaws.com:~/`
3.  Move files to httpd directory
    -  `sudo cp ~/support-portal-frontend/* /var/www/html`        

####  208. Creating Unix Service       

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

####  208.2 Creating Unix Service - Correct Way 

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
          
####  209. Testing in Production 

-  Test with: 
    -  Username: `art.shyshkin`
    -  Password: `17aH!?o>CJ`
    -  and another    
    -  Username: `shyshkin.art`
    -  Password: `5C<"0dVx=>`

####  33 deploy frontend into s3 bucket

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

#### 34.1 Create EC2 instance for Docker

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

####  34.3 Build and Run Docker image in Docker EC2

-  `mvn clean package docker:build docker:start`

            
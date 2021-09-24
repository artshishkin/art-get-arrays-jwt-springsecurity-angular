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
    -  `cat supportapi.log`
    -  **or**
    -  `sudo vim supportapi.log`  -> :qa for quit
    -  **or**
    -  `sudo tail -f supportapi.log`  
    
          

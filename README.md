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

        



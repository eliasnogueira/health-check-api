## Proposal
The proposal is enable any team member to execute a checkpoint/health check test in an API endpoint (SOAP or REST).

Based on a JSON file this project will connect on a URL provided to verify the status code.
If the API is running all the test will pass.

## How to use
We recommend to clone the project and navigate to the project folder.

You''l also need Java JDK 6+ and Maven installed and configured on your path.

### health_check.json
Open the _health_check.json_ file and add any pair of _name_ and _url_ where:
 * name: is the name of the test
 * url: is the endpoint that will be tested

Examples:
```json
{
  "tests":
  [
    {
      "name": "Postman - GET",
      "url": "https://postman-echo.com/get?test=123"
    },
    {
      "name": "Postman - 401",
      "url": "https://postman-echo.com/digest-auth"
    },
    {
      "name": "Correios - SOAP",
      "url": "http://ws.correios.com.br/calculador/CalcPrecoPrazo.asmx"
    }
  ]
}
```  

### execution
We recommend you to execute the following command on the project folder:

```bash
mvn clean test 
```

### reports
There are two types of report that will be always generated:
 * xUnit: report will be generated on _target/surefire-reports_ folder.
 * HTML: report will be generated on _target/report/_. The filename will be the same of json filename.

If you want to aggregate the HTML report on the build pipeline you need:
 1. Install the [HTML Publish Plugin](https://jenkins.io/blog/2016/07/01/html-publisher-plugin/) on Jenkins
 2. Add the following code on Jenkins -> Manage Jenkins -> Script Console to allow Javascript and CSS to be load
 `System.setProperty("hudson.model.DirectoryBrowserSupport.CSP", "")`
 3. Add the _publishReport_ DSL on your build pipeline
 ```
 publishHTML (target: [
    allowMissing: false,
    alwaysLinkToLastBuild: true,
    keepAll: true,
    reportDir: 'target/report',
    reportFiles: 'health_check*.html',
    reportName: "Health Check HTML Report"
 ])
 ```
 
### multiple health check files
If you need to create many health check files to run against dev, test and other environments 
you need to use `-Dfile=filename.json` where:
 * _-D_ is an indication of a property to the code
 * _file_ is the name of the property (don't change it)
 * _filename.json_ is the file that you need to run

Example:

```bash
mvn clean test -Dfile=health_check_dev.json
```

As this project only execute one file per time, you'll need multiple executions.
 
 
### timeout
The default timeout is 10 seconds.

If you want to add a custom timeout used `-Dtimeout` property.

Eg: custom timeout with 5 seconds
```bash
mvn clean test -Dtimeout=5
```

### Test pipeline
We recommend you execute via command line or integrate the project on a build pipeline.
Here's an example of a pipeline with only a health check file (_health_check.json_):

```
node {
   def mvnHome
   stage('Preparation') { // for display purposes
      // Get some code from a GitHub repository
      git 'https://github.com/eliasnogueira/health-check-api.git'
      // Get the Maven tool.
      // ** NOTE: This 'M3' Maven tool must be configured
      // **       in the global configuration.           
      mvnHome = tool 'M3'
   }
   stage('Build') {
      if (isUnix()) {
         sh "'${mvnHome}/bin/mvn' -Dmaven.test.failure.ignore clean"
      } else {
         bat(/"${mvnHome}\bin\mvn" -Dmaven.test.failure.ignore clean/)
      }
   }
   stage('Health Check') {
      if (isUnix()) {
         sh "'${mvnHome}/bin/mvn' clean test"
      } else {
         bat(/"${mvnHome}\bin\mvn" clean test/)
      }
   }
   stage('Results') {
      junit '**/target/surefire-reports/TEST-*.xml'
   }
}
```

Here's an example of multiples health check files (note that the property -file=health_check_test.json 
was added after the word _test_):

```
node {
   def mvnHome
   stage('Preparation') { // for display purposes
      // Get some code from a GitHub repository
      git 'https://github.com/eliasnogueira/health-check-api.git'
      // Get the Maven tool.
      // ** NOTE: This 'M3' Maven tool must be configured
      // **       in the global configuration.           
      mvnHome = tool 'M3'
   }

   stage('Health Check - Dev') {
      if (isUnix()) {
         sh "'${mvnHome}/bin/mvn' clean test"
      } else {
         bat(/"${mvnHome}\bin\mvn" clean test/)
      }
   }
   
   stage('Health Check - Test') {
      if (isUnix()) {
         sh "'${mvnHome}/bin/mvn' clean test -file=health_check_test.json"
      } else {
         bat(/"${mvnHome}\bin\mvn" clean test -file=health_check_test.json/)
      }
   }
   
   stage('Results') {
      junit '**/target/surefire-reports/TEST-*.xml'
   }
}
```
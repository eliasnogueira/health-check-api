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
mvn test surefire-report:report-only site -DgenerateReports=false
```

This command will:
 1. Execute the test _TestExecutor.java_ that is a Data Driven Test and will read the _health_check.json_ file and verify if the endpoint is alive
 2. Generate the xUnit report (XML file)
 3. Generate a HTML report (user friendly report) based on xUnit XML
 
The xUnit report will be generated on _target/surefire-reports_ folder.

The HTML report will be generated on _target/site/surefire-report.html_.

### multiple health check files
If you need to create many health check files to run against dev, test and other environments 
you need to use `-Dfile=filename.json` where:
 * _-D_ is an indication of a property to the code
 * _file_ is the name of the property (don't change it)
 * _filename.json_ is the file that you need to run

Example:

```bash
mvn test -Dfile=health_check_dev.json surefire-report:report-only site -DgenerateReports=false
```

As this project only execute one file per time, you'll need multiple executions.
 

### Test pipelime
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
         sh "'${mvnHome}/bin/mvn' test surefire-report:report-only site -DgenerateReports=false"
      } else {
         bat(/"${mvnHome}\bin\mvn" test surefire-report:report-only site -DgenerateReports=false/)
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
         sh "'${mvnHome}/bin/mvn' test surefire-report:report-only site -DgenerateReports=false"
      } else {
         bat(/"${mvnHome}\bin\mvn" test surefire-report:report-only site -DgenerateReports=false/)
      }
   }
   
   stage('Health Check - Test') {
      if (isUnix()) {
         sh "'${mvnHome}/bin/mvn' test -file=health_check_test.json surefire-report:report-only site -DgenerateReports=false"
      } else {
         bat(/"${mvnHome}\bin\mvn" test -file=health_check_test.json surefire-report:report-only site -DgenerateReports=false/)
      }
   }
   
   stage('Results') {
      junit '**/target/surefire-reports/TEST-*.xml'
   }
}
```
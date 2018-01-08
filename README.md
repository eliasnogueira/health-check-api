##Proposal
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
```
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

`$ mvn test surefire-report:report-only site -DgenerateReports=false`

This command will:
 1. Execute the test _TestExecutor.java_ that is a Data Driven Test and will read the _health_check.json_ file and verify if the endpoint is alive
 2. Generate the xUnit report (XML file)
 3. Generate a HTML report (user friendly report) based on xUnit XML
 
The xUnit report will be generated on _target/surefire-reports_ folder.

The HTML report will be generated on _target/site/surefire-report.html_.

### Test pipelime
We recommend you execute via command line or integrate the project on a build pipeline.
Here an example:
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
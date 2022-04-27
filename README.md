# PrintMgr

## About

A friend of mine needed something like this for one of his projects and so I wrote it in my free-time.  It is meant to solve a specific use case:

- Allowing web applications (probably AWS-hosted systems) to print headlessly/without-human-intervention to local printers

It is composed of two components:

1) An SQS queue to be used specifically for this system to post request messages

2) A Java-based service running on a local machine that can be allowed to connect to remote entities, in this case the SQS queue.

The idea is that one's system produces as a PDF some document (i.e. an invoice) that needs to be printed in their facility.  And then that PDF is stored somewhere that can be accessed with a URL.

Their web system connects to the dedicated SQS queue and adds a message to it with data relating to the document they want printed & the printer to send it to.

In their facility, the Java app .. which is polling the queue .. pulls the message and can download the PDF and print it according to instructions.

## Setup

### Cloud (AWS)

First, create a queue to be used exclusively by this system.  

Second, create a user and give them (and only them) access to this queue.  

Finally, for the queue you will want the following information:

    aws.region=
    aws.access-key=
    aws.secret-key=
    aws.queue.name=

### Local

First, Java needs to be installed on the system (I've tested it with Java 8 and Java 17).

Then one needs to setup the properties file for the application to use and put it in whatever directory you typically put such files in.  I have named it printmgr.properties, though it can be name anything.

In that file, there should be the following values specified (matching the values above):

    # Required
    aws.region=
    aws.access-key=
    aws.secret-key=
    aws.queue.name=

    # Optional
    #logging.level.io.softwarestrategies.printmgr=DEBUG
    default.printer.name=

## Usage

### Start the Java application

Run the following command, with the "-Dspring.config.import" directive's value matching the config file name and its location.
```
java -jar -Dspring.config.import=file:/Users/personx/temp/printmgr.properties printmgr-0.0.1-SNAPSHOT.jar
```

If it succeeds, in the initial output of the application should be a list of the printers on the network that are available to the application.

And if it fails, make sure that:

1. The path for the config file is correct
2. And the AWS and queue values are correct

NOTE: Some of the properties in the config file can also be overriden by arguments passed in via the commandline as follows:

```
--aws.access-key=AKIA... --aws.secret-key=sq8T... --aws.region=us-west-2 --aws.queue.name=the_queue_name
```

### Add a Message to the Queue

Assuming that there's one or more PDF documents to be printed, create a message like below:

``` 
{
    "printJobs": [
        {
            "description": "Job #1",
            "fileUrl": "https://softwarestrategies-somebucket.s3.us-west-2.amazonaws.com/2021-09-21+Westshore+Wellness+receipt.PDF",
            "printerName": "EPSON WF-3730 Series",
            "numOfCopies": 1
        },
        {
            "description": "Job #2",
            "fileUrl": "https://softwarestrategies-somebucket.s3.us-west-2.amazonaws.com/2021-09-21+Westshore+Wellness+receipt.PDF",
            "printerName": "EPSON WF-3730 Series",
            "numOfCopies": 2
        }
    ]
}
```

Log into AWS, go to SQS and open the queue designated to be used.  Then you can add the message from above.

Now go and look at the output of the application.  And soon, you should show some output matching the message and soon also see output to your printer.





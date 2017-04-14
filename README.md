# twicsy-downloader



## Batch downloader for http://twicsy.com/ 

Twicsy is an archive of people's twitter pictures and tweets

It even has pictures of disabled accounts

## Usage

Run with the search as first parameter. 
Can either be a @username or a tag

e.g. 

    sbt "run @realDonaldTrump"

or 

    sbt "run cats"

It will create a new directory and put the images in there, as
well as some html containing the original tweet

----------

If you don't have sbt or Scala you can always just run the standalone fat jar as long as you have java8

[/releases](https://github.com/fancellu/twicsy-downloader/releases/latest)
twicsy-downloader.jar


e.g.

    java -jar twicsy-downloader.jar @RealDonaldTrump

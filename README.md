## Summary
1. Used Flink with Scala 2.11

2. For distance between query point and airports, haversine is used 

3. For spatial indexing BallTree[1] was used to partition the (lat,lng) pairs in airports file. This implementation performed far superior ( O(logn) avg case) as compared to the naive implementation that iterates over all the airports and finds minimum distance(O(n) time complexity)

4. The airports file is sent to all flink workers on application startup, wherein each worker builds a local spatial index (which takes roughly 1-1.2 seconds for each worker)

5. Each user item is streamed from the users file, wherein each worker reads a partition of the users file and a user object is enriched with the closest airport via a findNearestNeighbour search made to the local spatial index on each worker
(REFER TO design IMAGE)

6. The final results are outputted in a file. You would need to pass the ```outputPath``` to the program. The ```StreamingFileSink``` will write the results into this directory 


## Tests
I've tested the spatial tree implementation against the naive implementation for the first 1000 entries in the user file. Check ```test/scala/io/github/ankushs92/index/SpatialIndexSpec``` and run it if you wish.

## Setup
The users and airport file are already put under ```src/main/resources``` . The program expects an ```outputPath```, which should be a directory under which the output file will be written to.


You can run the application in two ways. In both approaches, 3 program arguments are expected : ```/path/to/user-geo-sample.csv.gz```, ```/path/to/optd-airports-sample.csv.gz```, ```/path/to/outputDir```. 
Here, ```path/to/outputDir``` is the directory where will the output of the program will be written.

The two approaches are 

1. Import the project directly in your IDE
Go to ```src/main/scala/io/github/ankushs92/TravelAudienceStreamingJob```. Provide the 3 arguments described above. Run the main method. That's it.

2. Build the project and use the jar
-> Go to the root of the project and do a ```mvn clean package```. This would run the test as well. 
-> Provide the 3 arguments described above
-> Go to ```/target```, and look for ```travel_audience-1.0-SNAPSHOT.jar```
-> Execute ```java -jar /path/to/jar /path/to/user-geo-sample.csv.gz /path/to/optd-airports-sample.csv.gz /path/to/outputDir```
   On my machine it looks something like this : 
  ```java -jar /Users/ankushsharma/travel_audience/target/travel_audience-1.0-SNAPSHOT.jar /Users/ankushsharma/travel_audience/src/main/resources/user-geo-sample.csv.gz /Users/ankushsharma/travel_audience/src/main/resources/optd-airports-sample.csv.gz  /Users/ankushsharma/Documents/coracle/travel_audience```

PS : I have turned off Flink-logging, so you won't be able to see Flink logs. If you wish to see flink logs, go to ```src/main/resources/log4j.properrties```, and put ```log4j.category.org.apache.flink=INFO``` . 

## Spatial Indexing
Initially, my approach was to lay out the base code structure with the naive implementation of iterating over all data points just to see how it would perform. In this approach, the program never terminated.
The problem is essentially to find the nearest neighbour to the lat and lng of a user. This quickly hinted to a tree like structure wherein it would be possible to prune most of the search space.
My initial approach was to use KD-Trees, but this would not have been correct because a KD-Tree partitions the search space into rectangles, and is therefore more suited for those applications where the distance between points
is computed using euclidean distance. In our case, however, since we are using haversine distance, which computes distance of two points on a sphere, KD-Trees were not applicable, and therefore I had to look for a different spatial indexing strategy.
A not-so-quick google search hinted towards BallTrees[1], which is a data structure similar to KD-Trees, with the exception that it can work with any distance metric that satisfies the triangle inequality(i.e also haversine).

The implementation of BallTree is based on the open source JSAT library(which took some time to find!). It has different anchoring and pivoting strategies. I benchmarked and used the quickest one(Check the class SpatialIndex.scala).


## The curious case of airports with same (lat,lng) but different iata code
There were some airports (like BSL and MLH) in the file with the same lat and lng. For such cases, the results returned by the naive implementation did not match that returned from the spatial index, but that's okay and correct, since we are only
interested in finding one nearest airport. 

### References
[1]https://en.wikipedia.org/wiki/Ball_tree
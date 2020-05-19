## Summary
1. Used Flink with Scala 2.11

2. For distance between query point and airports, haversine is used 

3. For spatial indexing BallTree[1] was used to partition the (lat,lng) pairs in airports file. This implementation performed far superior ( O(logn) avg case) as compared to the naive implementation that iterates over all the airports and finds minimum distance(O(n) time complexity)

4. The airports file is sent to all flink workers on application startup, wherein each worker builds a local spatial index (which takes roughly 1-1.2 seconds for each worker)

5. Each user item is streamed from the users file, wherein each worker reads a partition of the users file and a user object is enriched with the closest airport via a findNearestNeighbour search made to the local spatial index on each worker

6. The final results are outputted to a file. You would need to pass the ```outputPath``` to the program. The ```StreamingFileSink``` will write the results into this directory 


## Tests
I've tested the spatial tree implementation against the naive implementation for the first 1000 entries in the user file. Check ```test/scala/io/github/ankushs92/index/SpatialIndexSpec``` and run it if you wish.

## Setup
The program expects an ```outputPath```, which should be a directory under which the output file will be written to.

You can run the application in two ways. In both approaches, 3 program arguments are expected : ```/path/to/user-geo-sample.csv.gz```, ```/path/to/optd-airports-sample.csv.gz```, ```/path/to/outputDir```. 
Here, ```path/to/outputDir``` is the directory where will the output of the program will be written.

The two approaches are 

1. Import the project directly in your IDE
Go to ```src/main/scala/io/github/ankushs92/TravelAudienceStreamingJob```. Provide the 3 arguments described above. Run the main method. That's it.

2. Build the project and use the jar

* Go to the root of the project and do a ```mvn clean package```. This would execute the test as well. 
* Provide the 3 arguments described above
* Go to ```/target```, and look for ```travel_audience-1.0-SNAPSHOT.jar```
* Execute ```java -jar /path/to/jar /path/to/user-geo-sample.csv.gz /path/to/optd-airports-sample.csv.gz /path/to/outputDir```
  
  On my machine it looks something like this : 
  ```java -jar /Users/ankushsharma/travel_audience/target/travel_audience-1.0-SNAPSHOT.jar /Users/ankushsharma/travel_audience/src/main/resources/user-geo-sample.csv.gz /Users/ankushsharma/travel_audience/src/main/resources/optd-airports-sample.csv.gz  /Users/ankushsharma/Documents/coracle/travel_audience```

PS : I have turned off Flink-logging, so you won't be able to see Flink logs. If you wish to see flink logs, go to ```src/main/resources/log4j.properrties```, and put ```log4j.category.org.apache.flink=INFO``` . 

## Spatial Indexing
Initially, my approach was to lay out the base code structure with the naive implementation of iterating over all data points just to see how it would perform. In this approach, the program never terminated.
The problem is essentially to find the nearest neighbour to the lat and lng of a user. This quickly hinted to a tree like structure wherein it should be possible to prune most of the search space.
My initial approach was to use KD-Trees, but this would not have been correct because a KD-Tree partitions the search space into hyperrectangles(or rectangles in 2d space), and is therefore more suited for those applications where the distance between points
is computed using euclidean distance and the (x,y) axis are parallel to the rectangle. In our case, however, since we are using haversine distance, which computes distance of two points on surface of a sphere, KD-Trees were not applicable, and therefore I had to look for a different spatial indexing strategy.
A not-so-quick google search hinted towards BallTrees[1], which is a data structure similar to KD-Trees, with the exception that it can work with any distance metric that satisfies the triangle inequality(i.e also haversine).

The implementation of BallTree is based on the open source JSAT library(which took some time to find!). It has different anchoring and pivoting strategies. I benchmarked and used the quickest one(check the class SpatialIndex.scala).

## But wait, just how the hell does it work?
Flink is based on parallelism of operators. For instance, if you define parallelism of an operator to be 2, flink would ship the operator to 2 cores and run the operator there(a very simplified explanation)
The default parallelism for the job as well as for individual operators is the number of cores available on your system. 

What I did is the following : When flink starts, I register the airports file with Flink's DistributedCache. This file will be copied by flink to downstream operators.
The ```ClosestAirportMapper.scala``` class, which is implementation of map operator, will be executed on as many cores as you have in your system. As the workers are starting up, in the map transformation I open 
the airportsFile made available via DistributedCache and build a local spatial index. This takes rougly 700ms - 1300 ms for each worker.
  
Meanwhile, the usersFile is parsed as a DataStream. Flink will interally partition the file, and each DataSource operator will work as a single task with downstream map operators(in our case, with ClosestAirportMapper).
To put it simply, there would be as many partitions of the file as there are cores in your system, and each partition will pipeline the user object downstream to our ClosestAirportMapper, which would perform the nearestNeighbourSearch in avg O(log(n)) time.

The results are pipelined to a FileSink operator, which outputs the results to a file.

The entire process from start to finish takes about 130-150 seconds depending if you execute it in your IDE or via jar file.

## The curious case of airports with same (lat,lng) but different iata code
There were some airports (like BSL and MLH) in the file with the same lat and lng. For such cases, the results returned by the naive implementation did not match that returned from the spatial index, but that's okay and correct, since we are only
interested in finding one nearest airport. 

### References
[1]https://en.wikipedia.org/wiki/Ball_tree
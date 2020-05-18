## Summary
1. Used Flink with Scala 2.11

2. For distance between query point and airports, haversine is used 

3. For spatial indexing BallTree[1] was used to partition the (lat,lng) pairs in airports file. This implementation performed far superior ( O(logn) avg case) as compared to the naive implementation that iterates over all the airports and finds minimum distance(O(n) time complexity)

4. The airports file is sent to all flink workers on application startup, wherein each worker builds a local spatial index (which takes roughly 1-1.2 seconds for each worker)

5. Each user item is streamed from the users file, wherein each worker reads a partition of the users file and a user object is enriched with the closest airport via a findNearestNeighbour search made to the local spatial index on each worker
(REFER TO design IMAGE)


## Testing


## Setup


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
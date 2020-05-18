## Summary
1. Used Flink with Scala 2.11

2. For distance between query point and airports, haversine is used 

3. For spatial indexing BallTree[1] was used to partition the (lat,lng) pairs in airports file. This implementation performed far superior ( O(logn) avg case) as compared to the naive implementation that iterates over all the airports and finds minimum distance(O(n) time complexity)

4. The airports file is sent to all workers on application startup, wherein each worker builds a local spatial index (which takes roughly 1-1.2 seconds for each worker)

5. Each user item is streamed from the users file, wherein each worker reads a partition of the users file and a user object is enriched with the closest airport via a findNearestNeighbour search made to the local spatial index on each worker


#References
[1]https://en.wikipedia.org/wiki/Ball_tree
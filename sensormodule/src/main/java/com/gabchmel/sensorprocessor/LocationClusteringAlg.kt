package com.gabchmel.sensorprocessor

import kotlin.math.*

// Taken from: https://gist.github.com/mgadzhi/b2e8626556ae3b91f9bca1e37268e0ee

class LocationClusteringAlg {
    // First, we need to define, what a 'distance' is
    interface Distance<T> {
        fun T.distance(to: T): Double
    }

    // Now let's define few concrete data classes that will be used to store the data
    data class Location(val lat: Double, val lon: Double)
    data class Point(val x: Double, val y: Double)

    // It is possible to define different distance functions for different data classes
    // It is also possible to define different distance functions for the same data class
    // For example, we could define full blown 'geodesic' distance for locations
    // I tried to reproduce something like 'Typeclass' pattern which I would use in Scala/Haskell
    val euclideanDistance = object : Distance<Point> {
        override fun Point.distance(to: Point): Double {
            return sqrt((x - to.x).pow(2) + (y - to.y).pow(2))
        }
    }

    val haversineDistance = object : Distance<Location> {
        override fun Location.distance(to: Location): Double {
            val R = 6371000.0
            val lat1 = Math.toRadians(lat)
            val lat2 = Math.toRadians(to.lat)
            val lon1 = Math.toRadians(lon)
            val lon2 = Math.toRadians(to.lon)

            return 2 * R * asin(
                sqrt(
                    ((lat1 - lat2) / 2).pow(2) + cos(lat1) * cos(lat2) * sin((lon1 - lon2) / 2).pow(
                        2
                    )
                )
            )
        }
    }

    // We will only require a 'cluster' to have an id for now
    // And let's say we use integers as identifiers
    //data class Cluster(val id: Int)
    sealed class Cluster
    object Unknown : Cluster()
    object Outsider : Cluster()
    data class Identified(val id: Int) : Cluster()

    // Now we can try to define what types can be 'clustered'
    // 'We know how to compute a distance between 2 objects of a type' => 'We can cluster a list of such objects'
    interface ClusteringAlgorithm<T, D> {
        fun <D : Distance<T>> fit_transform(xs: List<T>, dist: D): List<Cluster>
    }

    // I first implemented a minimalistic version of a Matrix to hold pairwise distances,
    // but I didn't really use everything from here after all.
    // On a bright side, it's easy to add a method to ClusteringAlgorithm that would get
    // an arbitrary pairwise distance matrix and skip its own distance calculations
    // as it's done in sklearn
    interface DummyMatrix<T> {
        fun get(i: Int, j: Int): T
        fun bitMask(f: (item: T) -> Boolean): DummyMatrix<Boolean>
    }

    open class DummySquareMatrix<T>(val entries: List<T>, val dim: Int) : DummyMatrix<T> {
        override fun get(i: Int, j: Int): T {
            return entries[i * dim + j]
        }

        override fun bitMask(f: (item: T) -> Boolean): DummyMatrix<Boolean> {
            return DummySquareMatrix(entries.map(f), dim)
        }
    }

    class PairwiseDistanceMatrix private constructor(entries: List<Double>, dim: Int) :
        DummySquareMatrix<Double>(entries, dim) {
        companion object {
            operator fun <T, D : Distance<T>> invoke(
                locs: List<T>,
                dist: D
            ): PairwiseDistanceMatrix {
                val n = locs.size
                val distances = mutableListOf<Double>()
                for (i in 0 until n) {
                    for (j in 0 until n) {
                        distances.add(dist.run { locs[i].distance(locs[j]) })
                    }
                }
                return PairwiseDistanceMatrix(distances, n)
            }
        }
    }

    // Simplified implementation of DBSCAN
    // It doesn't take into account edge points
    class DBSCAN(val eps: Double, val minPts: Int) :
        ClusteringAlgorithm<Location, Distance<Location>> {
        override fun <D : Distance<Location>> fit_transform(
            xs: List<Location>,
            dist: D
        ): List<Cluster> {
            val result = MutableList<Cluster>(xs.size) { Unknown }
            var c = 0
            val distances = PairwiseDistanceMatrix(xs, dist)
            var neighbors: MutableList<Int>
            for (i in xs.indices) {
                if (result[i] !is Unknown) {
                    continue
                }
                neighbors = mutableListOf()
                for (j in xs.indices) {
                    if (distances.get(i, j) < eps) {
                        neighbors.add(j)
                    }
                }
                if (neighbors.size < minPts) {
                    result[i] = Outsider
                    continue
                }
                c += 1
                for (j in neighbors) {
                    if (result[j] is Outsider) {
                        result[j] = Identified(c)
                    }
                    if (result[j] !is Unknown) {
                        continue
                    }
                    result[j] = Identified(c)
                }
            }
            return result
        }
    }


}